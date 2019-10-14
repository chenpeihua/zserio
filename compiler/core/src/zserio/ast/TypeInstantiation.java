package zserio.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypeInstantiation extends AstNodeBase
{
    public TypeInstantiation(AstLocation location, TypeReference typeReference, List<Expression> typeArguments)
    {
        super(location);

        this.typeReference = typeReference;
        this.typeArguments = typeArguments;
    }

    @Override
    public void accept(ZserioAstVisitor visitor)
    {
        visitor.visitTypeInstantiation(this);
    }

    @Override
    public void visitChildren(ZserioAstVisitor visitor)
    {
        typeReference.accept(visitor);
        for (Expression typeArgument : typeArguments)
            typeArgument.accept(visitor);
    }

    public TypeReference getTypeReference()
    {
        return typeReference;
    }

    /**
     * Gets a list of parameters used in this type instantiation.
     *
     * This returns a list of parameters of the base type together with the expressions used in the
     * instantiation.
     *
     * The order of the items in list is the order of the parameters.
     *
     * @return The list of instantiated parameters.
     */
    public List<InstantiatedParameter> getInstantiatedParameters()
    {
        return Collections.unmodifiableList(instantiatedParameters);
    }

    /**
     * Class describing one parameter in a type instantiation.
     */
    public static class InstantiatedParameter
    {
        /**
         * Constructor from argument expression and parameter.
         *
         * @param argumentExpression Argument expression used for parameter instantiation.
         * @param parameter          The parameter as used in the definition of the parameterized compound type.
         */
        public InstantiatedParameter(Expression argumentExpression, Parameter parameter)
        {
            this.argumentExpression = argumentExpression;
            this.parameter = parameter;
        }

        /**
         * Gets the argument expression.
         *
         * @return The expression used for parameter instantiation.
         */
        public Expression getArgumentExpression()
        {
            return argumentExpression;
        }

        /**
         * Gets the parameter as used in the definition of the parameterized compound type.
         *
         * @return The parameter that is instantiated.
         */
        public Parameter getParameter()
        {
            return parameter;
        }


        private final Expression argumentExpression;
        private final Parameter parameter;
    }

    TypeInstantiation instantiate(List<TemplateParameter> templateParameters,
            List<TypeReference> templateArguments)
    {
        final TypeReference instantiatedTypeReference =
                typeReference.instantiate(templateParameters, templateArguments);

        final List<Expression> instantiatedTypeArguments = new ArrayList<Expression>();
        for (Expression expression : typeArguments)
            instantiatedTypeArguments.add(expression.instantiate(templateParameters, templateArguments));

        return new TypeInstantiation(getLocation(), instantiatedTypeReference, instantiatedTypeArguments);
    }

    void evaluate()
    {
        if (!isEvaluated)
        {
            // check if referenced type is a compound type
            final ZserioType baseType = typeReference.getBaseType();
            final boolean isParameterized =
                    baseType instanceof CompoundType && !((CompoundType)baseType).getTypeParameters().isEmpty();
            if (isParameterized)
            {
                final CompoundType baseCompoundType = (CompoundType)baseType;
                // check if referenced type is a parameterized type
                final List<Parameter> typeParameters = baseCompoundType.getTypeParameters();
                if (typeArguments.size() == 0)
                {
                    throw new ParserException(typeReference, "Referenced type '" +
                            ZserioTypeUtil.getReferencedFullName(typeReference) +
                            "' is defined as parameterized type!");
                }
                if (typeArguments.size() != typeParameters.size())
                {
                    throw new ParserException(typeReference, "Parameterized type instantiation '" +
                            ZserioTypeUtil.getReferencedFullName(typeReference) +
                            "' has wrong number of arguments! " +
                            "Expecting " + typeArguments.size() + ", got " + typeParameters.size() + "!");
                }

                // fill instantiated parameter list
                for (int i = 0; i < typeParameters.size(); i++)
                {
                    instantiatedParameters.add(new InstantiatedParameter(
                            typeArguments.get(i), typeParameters.get(i)));
                }
            }
            else if (!typeArguments.isEmpty())
            {
                throw new ParserException(typeReference, "Referenced type '" +
                        ZserioTypeUtil.getReferencedFullName(typeReference) + "' is not a parameterized type!");
            }
            isEvaluated = true;
        }
    }

    void check()
    {
        // check all argument types in instantiated parameter list
        for (InstantiatedParameter instantiatedParameter : instantiatedParameters)
        {
            final Expression argumentExpression = instantiatedParameter.getArgumentExpression();
            if (!argumentExpression.isExplicitVariable())
            {
                ExpressionUtil.checkExpressionType(argumentExpression,
                        instantiatedParameter.getParameter().getTypeReference().getBaseType());
            }
        }
    }

    private final TypeReference typeReference;
    private final List<Expression> typeArguments;

    private boolean isEvaluated = false;
    private final List<InstantiatedParameter> instantiatedParameters = new ArrayList<InstantiatedParameter>();
}
