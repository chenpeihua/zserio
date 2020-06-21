package zserio.ast;

import java.math.BigInteger;
import java.util.List;

/**
 * AST node for dynamic bit field type instantiation.
 */
public class DynamicBitFieldInstantiation extends TypeInstantiation
{
    /**
     * Constructor.
     *
     * @param location             AST node location.
     * @param typeReference        Reference to the instantiated type definition.
     * @param elementTypeArguments Arguments for the type instantiation.
     * @param lengthExpression     Length expression associated with this dynamic bit field type.
     */
    public DynamicBitFieldInstantiation(AstLocation location, TypeReference typeReference,
            Expression lengthExpression)
    {
        super(location, typeReference);

        this.lengthExpression = lengthExpression;
    }

    @Override
    public void visitChildren(ZserioAstVisitor visitor)
    {
        super.visitChildren(visitor);
        lengthExpression.accept(visitor);
    }

    /**
     * Gets the expression which gives dynamic bit field length.
     *
     * Length expression must be provided according to the grammar, so this method cannot return null.
     *
     * @return Length expression.
     */
    public Expression getLengthExpression()
    {
        return lengthExpression;
    }

    /**
     * Gets the maximal number of bits used by this dynamic bit field.
     *
     * @return Maximal number of bits used by this dynamic bit field.
     */
    public int getMaxBitSize()
    {
        return maxBitSize;
    }

    /**
     * Gets upper bound for this integer type.
     *
     * @return Upper bound.
     */
    public BigInteger getUpperBound()
    {
        return upperBound;
    }

    /**
     * Gets lower bound for this integer type.
     *
     * @return Lower bound.
     */
    public BigInteger getLowerBound()
    {
        return lowerBound;
    }

    @Override
    public DynamicBitFieldType getBaseType()
    {
        return (DynamicBitFieldType)super.getBaseType();
    }

    @Override
    DynamicBitFieldInstantiation instantiateImpl(List<TemplateParameter> templateParameters,
            List<TemplateArgument> templateArguments, TypeReference instantiatedTypeReference)
    {
        final Expression instantiatedLengthExpression = getLengthExpression().instantiate(templateParameters,
                templateArguments);

        return new DynamicBitFieldInstantiation(getLocation(), instantiatedTypeReference,
                instantiatedLengthExpression);
    }

    @Override
    void resolve()
    {
        if (!(super.getBaseType() instanceof DynamicBitFieldType))
        {
            throw new ParserException(getTypeReference(), "Referenced type '" +
                    ZserioTypeUtil.getReferencedFullName(getTypeReference()) +
                    "' is not a dynamic bit field type!");
        }
    }

    @Override
    void evaluate()
    {
        if (!isEvaluated)
        {
            // check length expression
            if (lengthExpression.getExprType() != Expression.ExpressionType.INTEGER)
                throw new ParserException(lengthExpression, "Invalid length expression for bit field. " +
                        "Length must be integer!");

            final DynamicBitFieldType type = getBaseType();

            // expression needs BigInteger but Zserio type must be integer => cast expression to long
            if (lengthExpression.needsBigInteger())
                lengthExpression.setNeedsBigIntegerCastingNative();

            // evaluate bit sizes
            final BigInteger lengthValue = lengthExpression.getIntegerValue();
            if (lengthValue != null)
            {
                maxBitSize = lengthValue.intValue();
                if (maxBitSize < 1 || maxBitSize > type.getMaxBitSize())
                    throw new ParserException(lengthExpression, "Invalid length '" + maxBitSize +
                            "' for the dynamic bit field. Length must be within range [1," +
                            type.getMaxBitSize() + "]!");
            }
            else
            {
                final BigInteger upperBound = lengthExpression.getIntegerUpperBound();
                maxBitSize = (upperBound.compareTo(BigInteger.valueOf(type.getMaxBitSize())) > 0) ?
                        type.getMaxBitSize() : upperBound.intValue();
            }

            // evaluate upper and lower bounds
            if (type.isSigned())
            {
                // (1 << (bitSize - 1)) - 1
                upperBound = BigInteger.ONE.shiftLeft(maxBitSize - 1).subtract(BigInteger.ONE);
                // -(1 << (bitSize - 1))
                lowerBound = BigInteger.ONE.shiftLeft(maxBitSize - 1).negate();
            }
            else
            {
                // (1 << bitSize) - 1
                upperBound = BigInteger.ONE.shiftLeft(maxBitSize).subtract(BigInteger.ONE);
                lowerBound = BigInteger.ZERO;
            }

            isEvaluated = true;
        }
    }

    private final Expression lengthExpression;

    private int maxBitSize;
    private BigInteger upperBound;
    private BigInteger lowerBound;

    private boolean isEvaluated = false;
};
