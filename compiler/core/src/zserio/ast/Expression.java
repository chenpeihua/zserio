package zserio.ast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import zserio.antlr.ZserioParser;

/**
 * AST node for expressions defined in the language.
 */
public class Expression extends AstNodeBase
{
    /**
     * Defines expression flag for constructors.
     */
    public enum ExpressionFlag
    {
        NONE,                       /** no flag */
        IS_EXPLICIT,                /** the explicit keyword was before expression in the source */
        IS_TOP_LEVEL_DOT,           /** the expression is top level dot operator */
        IS_DOT_RIGHT_OPERAND_ID,    /** the expression is identifier which is dot right operand */
        IS_DOT_LEFT_OPERAND_ID      /** the expression is identifier which is dot left operand */
    };

    /**
     * Constructor.
     *
     * @param location       AST node location.
     * @param pkg            Package to which the expression belongs.
     * @param expressionType Expression grammar token type.
     * @param expressionText Expression grammar token text.
     * @param expressionFlag Flag for the expression.
     */
    public Expression(AstLocation location, Package pkg, int expressionType, String expressionText,
            ExpressionFlag expressionFlag)
    {
        this(location, pkg, expressionType, expressionText, expressionFlag, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param location       AST node location.
     * @param pkg            Package to which the expression belongs.
     * @param expressionType Expression grammar token type.
     * @param expressionText Expression grammar token text.
     * @param expressionFlag Flag for the expression.
     * @param operand1       Operand of the expression.
     */
    public Expression(AstLocation location, Package pkg, int expressionType, String expressionText,
            ExpressionFlag expressionFlag, Expression operand1)
    {
        this(location, pkg, expressionType, expressionText, expressionFlag, operand1, null, null);
    }

    /**
     * Constructor.
     *
     * @param location       AST node location.
     * @param pkg            Package to which the expression belongs.
     * @param expressionType Expression grammar token type.
     * @param expressionText Expression grammar token text.
     * @param expressionFlag Flag for the expression.
     * @param operand1       Left operand of the expression.
     * @param operand2       Right operand of the expression.
     */
    public Expression(AstLocation location, Package pkg, int expressionType, String expressionText,
            ExpressionFlag expressionFlag, Expression operand1, Expression operand2)
    {
        this(location, pkg, expressionType, expressionText, expressionFlag, operand1, operand2, null);
    }

    /**
     * Constructor.
     *
     * @param location       AST node location.
     * @param pkg            Package to which the expression belongs.
     * @param expressionType Expression grammar token type.
     * @param expressionText Expression grammar token text.
     * @param expressionFlag Flag for the expression.
     * @param operand1       Left operand of the expression.
     * @param operand2       Middle operand of the expression.
     * @param operand3       Right operand of the expression.
     */
    public Expression(AstLocation location, Package pkg, int expressionType, String expressionText,
            ExpressionFlag expressionFlag, Expression operand1, Expression operand2, Expression operand3)
    {
        super(location);

        this.pkg = pkg;
        type = expressionType;
        text = stripExpressionText(expressionType, expressionText);
        this.expressionFlag = expressionFlag;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;

        initialize();
    }

    @Override
    public void accept(ZserioAstVisitor visitor)
    {
        visitor.visitExpression(this);
    }

    @Override
    public void visitChildren(ZserioAstVisitor visitor)
    {
        if (operand1 != null)
        {
            operand1.accept(visitor);
            if (operand2 != null)
            {
                operand2.accept(visitor);
                if (operand3 != null)
                    operand3.accept(visitor);
            }
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder stringBuilder = new StringBuilder(text);
        if (operand1 != null)
        {
            stringBuilder.append(operand1.toString());
            if (operand2 != null)
            {
                stringBuilder.append(operand2.toString());
                if (operand3 != null)
                    stringBuilder.append(operand3.toString());
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Gets the expression type given by the parser.
     *
     * This method should not be public but it is used by expression formatters at the moment.
     *
     * @return Expression type given by grammar.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Gets the expression text given by the parser.
     *
     * This method should not be public but it is used by expression formatters at the moment.
     *
     * @return Expression text given by grammar.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Gets the first operand for the expression.
     *
     * This method should not be public but it is used by expression formatters at the moment.
     *
     * @return Returns the first operand.
     */
    public Expression op1()
    {
        return operand1;
    }

    /**
     * Gets the second operand for the expression.
     *
     * This method should not be public but it is used by expression formatters at the moment.
     *
     * @return Returns the second operand.
     */
    public Expression op2()
    {
        return operand2;
    }

    /**
     * Gets the third operand for the expression.
     *
     * This method should not be public but it is used by expression formatters at the moment.
     *
     * @return Returns the third operand.
     */
    public Expression op3()
    {
        return operand3;
    }

    /**
     * Checks if the expression contains explicit variable.
     *
     * @return Returns true if expression is explicit otherwise false.
     */
    public boolean isExplicitVariable()
    {
        return expressionFlag == ExpressionFlag.IS_EXPLICIT;
    }

    /**
     * Checks if the expression is most left identifier.
     *
     * @return Returns true if expression is not explicit and is dot left operand or single identifier.
     */
    public boolean isMostLeftId()
    {
        return (!isExplicitVariable() && type == ZserioParser.ID &&
                (expressionFlag == ExpressionFlag.IS_DOT_LEFT_OPERAND_ID ||
                expressionFlag != ExpressionFlag.IS_DOT_RIGHT_OPERAND_ID));
    }

    /**
     * Defines evaluated type of the expression.
     */
    public enum ExpressionType
    {
        /** Unknown expression. Used during evaluation only. Method getExprType() never returns this value. */
        UNKNOWN,

        /**
         * Integer expression. Result of expression can be read using getIntegerValue(). Actually, integer
         * result is needed to evaluate const types, length of bit field types and enumeration item values.
         */
        INTEGER,

        /** Float expression. Result of expression is not available. */
        FLOAT,

        /** String expression. */
        STRING,

        /** Boolean expression. */
        BOOLEAN,

        /** Expression which result is enumeration type. */
        ENUM,

        /** Expression which result is bitmask type. */
        BITMASK,

        /** Expression which result is compound type. */
        COMPOUND
    };

    /**
     * Gets the evaluated type of the expression.
     *
     * @return Returns the type of the expression.
     */
    public ExpressionType getExprType()
    {
        return expressionType;
    }

    /**
     * Gets the evaluated Zserio type for the expression.
     *
     * @return Returns the Zserio type for the expression.
     */
    public ZserioType getExprZserioType()
    {
        return zserioType;
    }

    /**
     * Gets the evaluated identifier symbol object for the expression.
     *
     * @return Returns the identifier symbol object for the expression.
     */
    public AstNode getExprSymbolObject()
    {
        return symbolObject;
    }

    /**
     * Gets value for integer expression.
     *
     * @return Returns value for integer expression or null if expression is not integer or if value of integer
     *         expression is not possible to evaluate during compile time.
     */
    public BigInteger getIntegerValue()
    {
        return expressionIntegerValue.getValue();
    }

    /**
     * Gets value for string expression.
     *
     * @return Returns value for string expression or null if expression is not string or if value of string
     *         expression is not possible to evaluate during compile time.
     */
    public String getStringValue()
    {
        return expressionStringValue;
    }

    /**
     * Gets upper bound for integer expression.
     *
     * @return Returns upper bound for integer expression or null if expression is not integer or if upper bound
     *         of integer expression is not possible to evaluate during compile time.
     */
    public BigInteger getIntegerUpperBound()
    {
        return expressionIntegerValue.getUpperBound();
    }

    /**
     * Gets needs BigInteger flag.
     *
     * @return Returns true if the expression contains value which needs BigInteger type.
     */
    public boolean needsBigInteger()
    {
        return expressionIntegerValue.needsBigInteger();
    }

    /**
     * Gets needs BigInteger casting to native flag.
     *
     * @return Returns true if the expression contains value which needs BigInteger type but it is assigned
     *                 to the native type.
     */
    public boolean needsBigIntegerCastingToNative()
    {
        return needsBigIntegerCastingToNative;
    }

    /**
     * Gets all objects of given class referenced from the expression.
     *
     * @param clazz Class of which objects should be found.
     *
     * @return Set of objects of given class referenced from the expression.
     */
    public <T extends AstNode> Set<T> getReferencedSymbolObjects(Class<? extends T> clazz)
    {
        final Set<T> referencedSymbolObjects = new HashSet<T>();
        addReferencedSymbolObject(referencedSymbolObjects, clazz);

        return referencedSymbolObjects;
    }

    /**
     * Returns true if the expression requires the context of its owner.
     *
     * This is true if the expression references the compound type that contains the expression (i.e. a field,
     * a parameter) or if the expression contains compound function.
     *
     * @return Returns true if the expression needs reference to its owner.
     */
    public boolean requiresOwnerContext()
    {
        // check if expression contains a field
        if (!(getReferencedSymbolObjects(Field.class).isEmpty()))
            return true;

        // check if expression contains a parameter
        if (!(getReferencedSymbolObjects(Parameter.class).isEmpty()))
            return true;

        // check if expression contains a function type
        if (!(getReferencedSymbolObjects(Function.class).isEmpty()))
            return true;

        return false;
    }

    /**
     * Checks if expression contains token "index".
     *
     * @return Returns true if this expression contains token "index".
     */
    public boolean containsIndex()
    {
        return containsOperand(ZserioParser.INDEX);
    }

    /**
     * Checks if expression contains ternary operator 'a ? b : c'.
     *
     * @return Returns true if this expression contains ternary operator.
     */
    public boolean containsTernaryOperator()
    {
        return containsOperand(ZserioParser.QUESTIONMARK);
    }

    /**
     * Checks if expression contains function call.
     *
     * @return Returns true if this expression contains function call.
     */
    public boolean containsFunctionCall()
    {
        return containsOperand(ZserioParser.RPAREN);
    }

    /**
     * Sets lexical scope for the expression evaluation.
     *
     * This method is called by ZserioAstScopeSetter.
     *
     * @param evaluationScope Lexical scope for evaluation to set.
     */
    void setEvaluationScope(Scope evaluationScope)
    {
        this.evaluationScope = evaluationScope;
    }

    /**
     * Adds additional lexical scope to the expression evaluation scope.
     *
     * @param additionalEvaluationScope Additional scope for evaluation to add.
     */
    void addEvaluationScope(Scope additionalEvalutionScope)
    {
        evaluationScope.add(additionalEvalutionScope);
    }

    /**
     * Evaluates the expression.
     */
    void evaluate()
    {
        evaluate(evaluationScope);
    }

    /**
     * This method evaluates one expression.
     *
     * It is supposed that the previous expression properties have been set to initialization values
     * (set by constructor).
     *
     * If given forced evaluation scope is different to expression evaluation scope, the method forces
     * evaluation even if the expression has been already evaluated (this is used for function called within
     * owner structure).
     *
     * @param forcedEvaluationScope Forced scope for evaluation.
     */
    void evaluate(Scope forcedEvaluationScope)
    {
        if (evaluationState == EvaluationState.IN_EVALUATION)
            throw new ParserException(this, "Cyclic dependency detected in expression evaluation!");

        // force evaluation if different scope is specified
        if (forcedEvaluationScope != evaluationScope && evaluationState != EvaluationState.NOT_EVALUATED)
            initialize();

        if (evaluationState == EvaluationState.NOT_EVALUATED)
        {
            evaluationState = EvaluationState.IN_EVALUATION;

            switch (type)
            {
                case ZserioParser.LPAREN:              // parenthesizedExpression
                    evaluateParenthesizedExpression();
                    break;

                case ZserioParser.RPAREN:              // functionCallExpression
                    evaluateFunctionCallExpression(forcedEvaluationScope);
                    break;

                case ZserioParser.LBRACKET:            // arrayExpression
                    evaluateArrayElement();
                    break;

                case ZserioParser.DOT:                 // dotExpression
                    evaluateDotExpression();
                    break;

                case ZserioParser.LENGTHOF:
                    evaluateLengthOfOperator();         // lengthofExpression
                    break;

                case ZserioParser.VALUEOF:
                    evaluateValueOfOperator();          // valueofExpression
                    break;

                case ZserioParser.NUMBITS:
                    evaluateNumBitsOperator();          // numbitsExpression
                    break;

                case ZserioParser.PLUS:                // unaryExpression or additiveExpression
                    if (operand2 == null)
                        evaluateUnaryPlusMinus(false);
                    else
                        evaluateArithmeticExpression();
                    break;

                case ZserioParser.MINUS:               // unaryExpression or additiveExpression
                    if (operand2 == null)
                        evaluateUnaryPlusMinus(true);
                    else
                        evaluateArithmeticExpression();
                    break;

                case ZserioParser.BANG:                // unaryExpression
                    evaluateNegationOperator();
                    break;

                case ZserioParser.TILDE:               // unaryExpression
                    evaluateBitNotExpression();
                    break;

                case ZserioParser.MULTIPLY:            // multiplicativeExpression
                case ZserioParser.DIVIDE:
                case ZserioParser.MODULO:
                    evaluateArithmeticExpression();
                    break;

                case ZserioParser.LSHIFT:              // shiftExpression
                case ZserioParser.RSHIFT:
                case ZserioParser.AND:                 // bitwiseAndExpression
                case ZserioParser.XOR:                 // bitwiseXorExpression
                case ZserioParser.OR:                  // bitwiseOrExpression
                    evaluateBitExpression();
                    break;

                case ZserioParser.LT:                  // relationalExpression
                case ZserioParser.LE:
                case ZserioParser.GT:
                case ZserioParser.GE:
                case ZserioParser.EQ:                  // equalityExpression
                case ZserioParser.NE:
                    evaluateRelationalExpression();
                    break;

                case ZserioParser.LOGICAL_AND:         // logicalAndExpression
                case ZserioParser.LOGICAL_OR:          // logicalOrExpression
                    evaluateLogicalExpression();
                    break;

                case ZserioParser.QUESTIONMARK:        // ternaryExpression
                    evaluateConditionalExpression();
                    break;

                case ZserioParser.BINARY_LITERAL:      // literalExpression
                    expressionType = ExpressionType.INTEGER;
                    final String binText = stripBinaryLiteral(getText());
                    expressionIntegerValue = new ExpressionIntegerValue(new BigInteger(binText, 2));
                    break;

                case ZserioParser.OCTAL_LITERAL:       // literalExpression
                    expressionType = ExpressionType.INTEGER;
                    expressionIntegerValue = new ExpressionIntegerValue(new BigInteger(getText(), 8));
                    break;

                case ZserioParser.DECIMAL_LITERAL:     // literalExpression
                    expressionType = ExpressionType.INTEGER;
                    expressionIntegerValue = new ExpressionIntegerValue(new BigInteger(getText()));
                    break;

                case ZserioParser.HEXADECIMAL_LITERAL: // literalExpression
                    expressionType = ExpressionType.INTEGER;
                    final String hexText = stripHexadecimalLiteral(getText());
                    expressionIntegerValue = new ExpressionIntegerValue(new BigInteger(hexText, 16));
                    break;

                case ZserioParser.BOOL_LITERAL:        // literalExpression
                    expressionType = ExpressionType.BOOLEAN;
                    break;

                case ZserioParser.STRING_LITERAL:      // literalExpression
                    expressionType = ExpressionType.STRING;
                    expressionStringValue = stripStringLiteral(getText());
                    break;

                case ZserioParser.FLOAT_LITERAL:       // literalExpression
                case ZserioParser.DOUBLE_LITERAL:
                    expressionType = ExpressionType.FLOAT;
                    break;

                case ZserioParser.INDEX:               // indexExpression
                    evaluateIndexExpression();
                    break;

                case ZserioParser.ID:                  // identifierExpression
                    evaluateIdentifier(forcedEvaluationScope);
                    break;

                default:
                    throw new ParserException(this, "Illegal expression type '" + type + "'!");
            }

            evaluationState = EvaluationState.EVALUATED;
        }
    }

    /**
     * This method propagates 'needs BigInteger' flag into already evaluated parts of expression.
     *
     * Method is necessary because Java expression formatter needs to have BigInteger flag set correctly for all
     * parts of expression.
     *
     * Example 1: 4 * 3 + uint64Value. Expression '4 * 3' will be evaluated before 'uint64Value'. Thus, literal
     * expressions '4' and '3' will not have set BigInteger flag. Such BigInteger flags can be set only
     * afterwards during evaluation of expression 'uint64Value'.
     *
     * Example 2: 4 * 3. Expression '4 * 3' will never have BigInteger flag set. Such BigInteger flags can be
     * set only after whole expression evaluation during checking of expression type in assignment. Therefore,
     * this method must be public.
     */
    void propagateNeedsBigInteger()
    {
        if (expressionType == ExpressionType.INTEGER && !expressionIntegerValue.needsBigInteger())
        {
            expressionIntegerValue = new ExpressionIntegerValue(expressionIntegerValue.getValue(),
                    expressionIntegerValue.getLowerBound(), expressionIntegerValue.getUpperBound(), true);

            if (operand1 != null)
            {
                operand1.propagateNeedsBigInteger();
                if (operand2 != null)
                {
                    operand2.propagateNeedsBigInteger();
                    if (operand3 != null)
                        operand3.propagateNeedsBigInteger();
                }
            }
        }
    }

    /**
     * Sets needs BigInteger casting flag.
     *
     * Method is necessary because Java expression formatter needs to know if expression which uses BigInteger
     * is assigned to the native type. In this case, casting to long native type is necessary.
     *
     * Example:
     *
     * CastUInt64ToUInt8Expression
     * {
     *     uint64  uint64Value;
     *
     *     function uint8 uint8Value()
     *     {
     *         return uint64Value;
     *     }
     * };
     */
    void setNeedsBigIntegerCastingNative()
    {
        needsBigIntegerCastingToNative = true;
    }

    /**
     * Instantiate the expression.
     *
     * @param templateParameters Template parameters.
     * @param templateArguments Template arguments.
     *
     * @return New expression instantiated from this using the given template arguments.
     */
    Expression instantiate(List<TemplateParameter> templateParameters,
            List<TemplateArgument> templateArguments)
    {
        if (operand1 == null)
        {
            String instantiatedText = text;
            if (isMostLeftId())
            {
                // expression is single ID or is left dot operand
                final int index = TemplateParameter.indexOf(templateParameters, text);
                if (index != -1)
                {
                    // template parameter has been found
                    final TypeReference templateArgumentReference =
                            templateArguments.get(index).getTypeReference();
                    final PackageName templateArgumentPackage =
                            templateArgumentReference.getReferencedPackageName();
                    if (!templateArgumentPackage.isEmpty())
                    {
                        // found template argument is type reference with specified package
                        return createInstantiationTree(templateArgumentPackage,
                                templateArgumentReference.getReferencedTypeName());
                    }
                    instantiatedText = templateArgumentReference.getReferencedTypeName();
                }
            }

            return new Expression(getLocation(), pkg, type, instantiatedText, expressionFlag, null, null, null);
        }
        else
        {
            return new Expression(getLocation(), pkg, type, text, expressionFlag,
                    operand1.instantiate(templateParameters, templateArguments),
                    operand2 == null ? null : operand2.instantiate(templateParameters, templateArguments),
                    operand3 == null ? null : operand3.instantiate(templateParameters, templateArguments));
        }
    }

    private Expression createInstantiationTree(PackageName templateArgumentPackage, String templateArgumentName)
    {
        final List<String> templateArgumentPackageIds = templateArgumentPackage.getIdList();
        Expression operand1 = new Expression(getLocation(), pkg, ZserioParser.ID,
                templateArgumentPackageIds.get(0), ExpressionFlag.IS_DOT_LEFT_OPERAND_ID, null, null, null);
        final List<String> expressionIds = new ArrayList<String>(templateArgumentPackageIds);
        expressionIds.add(templateArgumentName);
        for (int i = 1; i < expressionIds.size(); i++)
        {
            final Expression operand2 = new Expression(getLocation(), pkg, ZserioParser.ID,
                    expressionIds.get(i), ExpressionFlag.IS_DOT_RIGHT_OPERAND_ID, null, null, null);
            final Expression dotOperand = new Expression(getLocation(), pkg, ZserioParser.DOT, ".",
                    (i + 1 == expressionIds.size()) ? ExpressionFlag.IS_TOP_LEVEL_DOT : ExpressionFlag.NONE,
                    operand1, operand2, null);
            operand1 = dotOperand;
        }

        return operand1;
    }

    @SuppressWarnings("unchecked")
    private <T extends AstNode> void addReferencedSymbolObject(Set<T> referencedObjectList,
            Class<? extends AstNode> elementClass)
    {
        if (symbolObject != null && elementClass.isInstance(symbolObject))
            referencedObjectList.add((T)symbolObject);

        if (operand1 != null)
        {
            operand1.addReferencedSymbolObject(referencedObjectList, elementClass);
            if (operand2 != null)
            {
                operand2.addReferencedSymbolObject(referencedObjectList, elementClass);
                if (operand3 != null)
                    operand3.addReferencedSymbolObject(referencedObjectList, elementClass);
            }
        }
    }

    private boolean containsOperand(int operandTokenType)
    {
        if (type == operandTokenType)
            return true;

        if (operand1 != null)
        {
            if (operand1.containsOperand(operandTokenType))
                return true;

            if (operand2 != null)
            {
                if (operand2.containsOperand(operandTokenType))
                    return true;

                if (operand3 != null)
                    if (operand3.containsOperand(operandTokenType))
                        return true;
            }
        }

        return false;
    }

    private void evaluateParenthesizedExpression()
    {
        expressionType = operand1.expressionType;
        expressionIntegerValue = operand1.expressionIntegerValue;
        expressionStringValue = operand1.expressionStringValue;
        zserioType = operand1.zserioType;
        symbolObject = operand1.symbolObject;
        symbolInstantiation = operand1.symbolInstantiation;
        unresolvedIdentifiers = operand1.unresolvedIdentifiers;
    }

    private void evaluateFunctionCallExpression(Scope forcedEvaluationScope)
    {
        if (!(operand1.symbolObject instanceof Function))
            throw new ParserException(operand1, "'" + operand1.text + "' is not a function!");

        final Function function = (Function)operand1.symbolObject;
        final Expression functionResultExpression = function.getResultExpression();

        // function expression should know only symbols available on a place of the function call:
        // - if it's called within its owner object, it can see only symbols defined before the call
        try
        {
            ZserioAstEvaluator evaluator;
            if (forcedEvaluationScope.getOwner() == functionResultExpression.evaluationScope.getOwner())
                evaluator = new ZserioAstEvaluator(forcedEvaluationScope); // called within the owner
            else
                evaluator = new ZserioAstEvaluator(); // called externally from different compound type
            functionResultExpression.accept(evaluator);
        }
        catch (ParserException e)
        {
            final AstLocation location = getLocation();

            final ParserStackedException stackedException = new ParserStackedException(e);
            stackedException.pushMessage(location, "In function '" + function.getName() + "' " +
                    "called from here");
            throw stackedException;
        }

        evaluateExpressionType(function.getReturnTypeReference());
        expressionIntegerValue = functionResultExpression.expressionIntegerValue;
        expressionStringValue = functionResultExpression.expressionStringValue;
    }

    private void evaluateArrayElement()
    {
        if (!(operand1.symbolInstantiation instanceof ArrayInstantiation))
            throw new ParserException(operand1, "'" + operand1.text + "' is not an array!");

        if (operand2.expressionType != ExpressionType.INTEGER)
            throw new ParserException(operand2, "Integer expression expected!");

        final ArrayInstantiation arrayInstantiation = (ArrayInstantiation)operand1.symbolInstantiation;
        evaluateExpressionType(arrayInstantiation.getElementTypeInstantiation());
    }

    private void evaluateDotExpression()
    {
        if (operand1.zserioType == null)
        {
            // left operand is unknown => it still can be a part of package
            evaluatePackageDotExpression();
        }
        else if (operand1.zserioType instanceof EnumType)
        {
            // left operand is enumeration type
            evaluateEnumDotExpression();
        }
        else if (operand1.zserioType instanceof BitmaskType)
        {
            // left operand is bitmask type
            evaluateBitmaskDotExpression();
        }
        else if (operand1.zserioType instanceof CompoundType)
        {
            // left operand is compound type
            evaluateCompoundDotExpression();
        }
        else
        {
            throw new ParserException(operand1, "Unexpected dot expression '" + operand1.text + "'!");
        }
    }

    private void evaluatePackageDotExpression()
    {
        // try to resolve operand1 as package name and operand2 as type
        final PackageName.Builder op1UnresolvedPackageNameBuilder = new PackageName.Builder();
        for (Expression unresolvedIdentifier : operand1.unresolvedIdentifiers)
            op1UnresolvedPackageNameBuilder.addId(unresolvedIdentifier.text);

        Package identifierPackage = null;
        final AstNode identifierSymbol = pkg.getVisibleSymbol(this, op1UnresolvedPackageNameBuilder.get(),
                operand2.text);
        if (identifierSymbol != null)
        {
            symbolObject = identifierSymbol;
            operand2.symbolObject = identifierSymbol;
            if (identifierSymbol instanceof Constant)
            {
                Constant constant = (Constant)identifierSymbol;
                evaluateConstant(constant);
                identifierPackage = constant.getPackage();
            }
            else
            {
                throw new ParserException(this, "Symbol '" + operand2.text + "' (" +
                        identifierSymbol.getClass() + ") is not allowed here!");
            }
        }
        else
        {
            final ZserioType identifierType = pkg.getVisibleType(this, op1UnresolvedPackageNameBuilder.get(),
                    operand2.text);
            if (identifierType == null)
            {
                // identifier still not found
                if (expressionFlag == ExpressionFlag.IS_TOP_LEVEL_DOT)
                {
                    // and we are top level dot
                    throw new ParserException(this, "Unresolved symbol '" +
                            op1UnresolvedPackageNameBuilder.get().toString() + "' within expression scope!");
                }

                // this can happened for long package name, we must wait for dot
                unresolvedIdentifiers.addAll(operand1.unresolvedIdentifiers);
                unresolvedIdentifiers.add(operand2);
            }
            else
            {
                // this is used by formatters (operand2 was not evaluated)
                operand2.symbolObject = identifierType;
                evaluateIdentifierType(identifierType);
                identifierPackage = identifierType.getPackage();
            }
        }

        if (identifierPackage != null)
        {
            // set symbolObject to all unresolved identifier expressions (needed for formatters)
            for (Expression unresolvedIdentifier : operand1.unresolvedIdentifiers)
                unresolvedIdentifier.symbolObject = identifierPackage;
        }
    }

    private void evaluateEnumDotExpression()
    {
        final EnumType enumType = (EnumType)(operand1.zserioType);
        final Scope enumScope = enumType.getScope();
        final String dotOperand = operand2.text;
        final AstNode enumSymbol = enumScope.getSymbol(dotOperand);
        if (!(enumSymbol instanceof EnumItem))
        {
            throw new ParserException(this, "'" + dotOperand + "' undefined in enumeration '" +
                    enumType.getName() + "'!");
        }

        operand2.symbolObject = enumSymbol; // this is used by formatters (operand2 was not evaluated)
        symbolObject = enumSymbol;
        evaluateExpressionType(enumType);
    }

    private void evaluateBitmaskDotExpression()
    {
        final BitmaskType bitmaskType = (BitmaskType)(operand1.zserioType);
        final Scope bitmaskScope = bitmaskType.getScope();
        final String dotOperand = operand2.text;
        final AstNode bitmaskSymbol = bitmaskScope.getSymbol(dotOperand);
        if (!(bitmaskSymbol instanceof BitmaskValue))
        {
            throw new ParserException(this, "'" + dotOperand + "' undefined in bitmask '" +
                    bitmaskType.getName() + "'!");
        }

        operand2.symbolObject = bitmaskSymbol; // this is used by formatters (operand2 was not evaluated)
        symbolObject = bitmaskSymbol;
        evaluateExpressionType(bitmaskType);
    }

    private void evaluateCompoundDotExpression()
    {
        final CompoundType compoundType = (CompoundType)(operand1.zserioType);
        final Scope compoundScope = compoundType.getScope();
        final String dotOperand = operand2.text;
        final AstNode compoundSymbol = compoundScope.getSymbol(dotOperand);
        if (compoundSymbol == null)
            throw new ParserException(this, "'" + dotOperand + "' undefined in compound '" +
                    compoundType.getName() + "'!");

        operand2.symbolObject = compoundSymbol; // this is used by formatter (operand2 was not evaluated)
        symbolObject = compoundSymbol;
        if (compoundSymbol instanceof Field)
        {
            evaluateExpressionType(((Field)compoundSymbol).getTypeInstantiation());
        }
        else if (compoundSymbol instanceof Parameter)
        {
            evaluateExpressionType(((Parameter)compoundSymbol).getTypeReference());
        }
        else if (compoundSymbol instanceof Function)
        {
            // function type, we must wait for "()"
        }
        else
        {
            throw new ParserException(this, "'" + dotOperand + "' undefined in compound '" +
                    compoundType.getName() + "'!");
        }
    }

    private void evaluateLengthOfOperator()
    {
        if (!(operand1.zserioType instanceof ArrayType))
            throw new ParserException(operand1, "'" + operand1.text + "' is not an array!");

        expressionType = ExpressionType.INTEGER;
        // length of result has default expressionIntegerValue
    }

    private void evaluateValueOfOperator()
    {
        if (operand1.expressionType != ExpressionType.ENUM && operand1.expressionType != ExpressionType.BITMASK)
            throw new ParserException(operand1, "'" + operand1.text + "' is not an enumeration or bitmask!");

        expressionType = ExpressionType.INTEGER;
        expressionIntegerValue = operand1.expressionIntegerValue;
    }

    private void evaluateNumBitsOperator()
    {
        if (operand1.expressionType != ExpressionType.INTEGER)
            throw new ParserException(operand1, "Integer expression expected!");

        expressionType = ExpressionType.INTEGER;
        expressionIntegerValue = operand1.expressionIntegerValue.numbits();
    }

    private void evaluateUnaryPlusMinus(boolean isNegate)
    {
        if (operand1.expressionType != ExpressionType.INTEGER &&
                operand1.expressionType != ExpressionType.FLOAT)
            throw new ParserException(this, "Integer or float expressions expected!");

        if (operand1.expressionType == ExpressionType.FLOAT)
        {
            expressionType = ExpressionType.FLOAT;
        }
        else
        {
            expressionType = ExpressionType.INTEGER;
            expressionIntegerValue = (isNegate) ? operand1.expressionIntegerValue.negate() :
                operand1.expressionIntegerValue;
        }
    }

    private void evaluateNegationOperator()
    {
        final Expression op1 = op1();
        if (op1.expressionType != ExpressionType.BOOLEAN)
            throw new ParserException(this, "Boolean expression expected!");

        expressionType = ExpressionType.BOOLEAN;
    }

    private void evaluateBitNotExpression()
    {
        final Expression op1 = op1();
        if (op1.expressionType != ExpressionType.INTEGER && op1.expressionType != ExpressionType.BITMASK)
            throw new ParserException(this, "Integer or bitmask expression expected!");

        expressionType = op1.expressionType;
        expressionIntegerValue = op1.expressionIntegerValue.not();
        zserioType = op1.zserioType;
    }

    private void evaluateArithmeticExpression()
    {
        if (type == ZserioParser.PLUS && operand1.expressionType == ExpressionType.STRING &&
                operand2.expressionType == ExpressionType.STRING)
        {
            expressionType = ExpressionType.STRING;

            if (operand1.expressionStringValue == null || operand2.expressionStringValue == null)
                throw new ParserException(this, "Constant string expressions expected!");

            expressionStringValue = operand1.expressionStringValue + operand2.expressionStringValue;
        }
        else
        {
            if ((operand1.expressionType != ExpressionType.INTEGER &&
                    operand1.expressionType != ExpressionType.FLOAT) ||
                (operand2.expressionType != ExpressionType.INTEGER &&
                    operand2.expressionType != ExpressionType.FLOAT))
            {
                throw new ParserException(this, "Integer or float expressions expected!");
            }

            if (operand1.expressionType == ExpressionType.FLOAT ||
                    operand2.expressionType == ExpressionType.FLOAT)
            {
                expressionType = ExpressionType.FLOAT;
            }
            else
            {
                expressionType = ExpressionType.INTEGER;
                switch (getType())
                {
                    case ZserioParser.PLUS:
                        expressionIntegerValue = operand1.expressionIntegerValue.add(
                                operand2.expressionIntegerValue);
                        break;

                    case ZserioParser.MINUS:
                        expressionIntegerValue =
                            operand1.expressionIntegerValue.subtract(operand2.expressionIntegerValue);
                        break;

                    case ZserioParser.MULTIPLY:
                        expressionIntegerValue =
                            operand1.expressionIntegerValue.multiply(operand2.expressionIntegerValue);
                        break;

                    case ZserioParser.DIVIDE:
                        expressionIntegerValue = operand1.expressionIntegerValue.divide(
                                operand2.expressionIntegerValue);
                        break;

                    case ZserioParser.MODULO:
                        expressionIntegerValue =
                            operand1.expressionIntegerValue.remainder(operand2.expressionIntegerValue);
                        break;

                    default:
                        throw new ParserException(this, "Illegal expression type " + type + "!");
                }

                if (expressionIntegerValue.needsBigInteger())
                {
                    operand1.propagateNeedsBigInteger();
                    operand2.propagateNeedsBigInteger();
                }
            }
        }
    }

    private void evaluateBitExpression()
    {
        expressionType = operand1.expressionType;

        if (operand1.expressionType != operand2.expressionType ||
                (expressionType != ExpressionType.INTEGER &&
                expressionType != ExpressionType.BITMASK))
            throw new ParserException(this, "Integer or bitmask expressions expected!");

        if ((type == ZserioParser.LSHIFT || type == ZserioParser.RSHIFT) &&
                expressionType != ExpressionType.INTEGER)
            throw new ParserException(this, "Integer expressions expected!");

        switch (type)
        {
            case ZserioParser.LSHIFT:
                expressionIntegerValue = operand1.expressionIntegerValue.shiftLeft(
                        operand2.expressionIntegerValue);
                break;

            case ZserioParser.RSHIFT:
                expressionIntegerValue = operand1.expressionIntegerValue.shiftRight(
                        operand2.expressionIntegerValue);
                break;

            case ZserioParser.AND:
                expressionIntegerValue = operand1.expressionIntegerValue.and(
                        operand2.expressionIntegerValue);
                break;

            case ZserioParser.OR:
                expressionIntegerValue = operand1.expressionIntegerValue.or(operand2.expressionIntegerValue);
                break;

            case ZserioParser.XOR:
                expressionIntegerValue = operand1.expressionIntegerValue.xor(operand2.expressionIntegerValue);
                break;

            default:
                throw new ParserException(this, "Illegal expression type '" + type + "'!");
        }

        if (type != ZserioParser.LSHIFT && type != ZserioParser.RSHIFT &&
                expressionIntegerValue.needsBigInteger())
        {
            operand1.propagateNeedsBigInteger();
            operand2.propagateNeedsBigInteger();
        }

        // needed for bitmask expressions
        zserioType = operand1.zserioType;
    }

    private void evaluateRelationalExpression()
    {
        if (operand1.expressionType == ExpressionType.UNKNOWN ||
                operand1.expressionType != operand2.expressionType)
            throw new ParserException(this, "Incompatible expression types (" + operand1.expressionType +
                    " != " + operand2.expressionType + ")!");

        if (operand1.expressionType == ExpressionType.FLOAT &&
                type != ZserioParser.LT && type != ZserioParser.GT)
            throw new ParserException(this, "Equality operator is not allowed for floats!");

        if (operand1.expressionType == ExpressionType.STRING &&
                type != ZserioParser.EQ && type != ZserioParser.NE)
            throw new ParserException(this,
                    "'Greater than' and 'less than' comparison is not allowed for strings!");

        if (operand1.expressionType == ExpressionType.STRING)
            throw new ParserException(this, "String comparison is not implemented!");

        expressionType = ExpressionType.BOOLEAN;
        if (operand1.expressionType == ExpressionType.INTEGER)
        {
            expressionIntegerValue = operand1.expressionIntegerValue.relationalOperator(
                    operand2.expressionIntegerValue);
            if (expressionIntegerValue.needsBigInteger())
            {
                operand1.propagateNeedsBigInteger();
                operand2.propagateNeedsBigInteger();
            }
        }
    }

    private void evaluateLogicalExpression()
    {
        if (operand1.expressionType != ExpressionType.BOOLEAN ||
                operand2.expressionType != ExpressionType.BOOLEAN)
            throw new ParserException(this, "Boolean expressions expected!");

        expressionType = ExpressionType.BOOLEAN;
    }

    private void evaluateConditionalExpression()
    {
        if (operand1.expressionType != ExpressionType.BOOLEAN)
            throw new ParserException(operand1, "Boolean expression expected!");

        if (operand2.expressionType == ExpressionType.UNKNOWN ||
                operand2.expressionType != operand3.expressionType)
            throw new ParserException(this, "Incompatible expression types (" + operand2.expressionType +
                    " != " + operand3.expressionType + ")!");

        expressionType = operand2.expressionType;
        zserioType = operand2.zserioType;
        if (expressionType == ExpressionType.INTEGER)
        {
            expressionIntegerValue = operand2.expressionIntegerValue.conditional(
                    operand3.expressionIntegerValue);
            if (expressionIntegerValue.needsBigInteger())
            {
                operand2.propagateNeedsBigInteger();
                operand3.propagateNeedsBigInteger();
            }
        }
    }

    private void evaluateIndexExpression()
    {
        expressionType = ExpressionType.INTEGER;
        // array index has default expressionIntegerValue
    }

    private void evaluateIdentifier(Scope forcedEvaluationScope)
    {
        // identifier on right side of dot operator cannot be evaluated because the identifier without left
        // side (package) can be found wrongly in local scope
        if (expressionFlag != ExpressionFlag.IS_DOT_RIGHT_OPERAND_ID)
        {
            // explicit identifier does not have to be evaluated
            if (expressionFlag != ExpressionFlag.IS_EXPLICIT)
            {
                AstNode identifierSymbol = forcedEvaluationScope.getSymbol(text);
                if (identifierSymbol == null) // try package "global" scope
                    identifierSymbol = pkg.getVisibleSymbol(this, PackageName.EMPTY, text);
                if (identifierSymbol == null)
                {
                    // it still can be a type
                    final ZserioType identifierType = pkg.getVisibleType(this, PackageName.EMPTY, text);
                    if (identifierType == null)
                    {
                        // identifier not found
                        if (expressionFlag != ExpressionFlag.IS_DOT_LEFT_OPERAND_ID)
                        {
                            // and expression is not in dot expression
                            throw new ParserException(this, "Unresolved symbol '" + text +
                                    "' within expression scope!");
                        }

                        // this can happened for a long package name, we must wait for dot
                        unresolvedIdentifiers.add(this);
                    }
                    else
                    {
                        evaluateIdentifierType(identifierType);
                    }
                }
                else
                {
                    evaluateIdentifierSymbol(identifierSymbol, forcedEvaluationScope, text);
                }
            }
        }
    }

    private void evaluateIdentifierType(ZserioType identifierType)
    {
        symbolObject = identifierType;

        final ZserioType baseType = getBaseType(identifierType);
        if (baseType instanceof EnumType || baseType instanceof BitmaskType)
        {
            // enumeration or bitmask type, we must wait for enumeration item or bitmask value and dot
            zserioType = baseType;
        }
        else
        {
            throw new ParserException(this, "Type '" + baseType.getName() + "' (" +
                    baseType.getClass() + ") is not allowed here!");
        }
    }

    private void evaluateIdentifierSymbol(AstNode identifierSymbol, Scope forcedEvaluationScope,
            String identifier)
    {
        symbolObject = identifierSymbol;
        if (identifierSymbol instanceof Field)
        {
            evaluateExpressionType(((Field)identifierSymbol).getTypeInstantiation());
        }
        else if (identifierSymbol instanceof Parameter)
        {
            evaluateExpressionType(((Parameter)identifierSymbol).getTypeReference());
        }
        else if (identifierSymbol instanceof Function)
        {
            // function type, we must wait for "()"
        }
        else if (identifierSymbol instanceof EnumItem)
        {
            // enumeration item (this can happen for enum choices where enum is visible or for enum itself)
            final ZserioType scopeOwner = forcedEvaluationScope.getOwner();
            if (scopeOwner instanceof ChoiceType)
            {
                // this enumeration item is in choice with enumeration type selector
                final ChoiceType enumChoice = (ChoiceType)scopeOwner;
                final Expression selectorExpression = enumChoice.getSelectorExpression();
                final ZserioType selectorExprZserioType = selectorExpression.getExprZserioType();
                if (selectorExprZserioType instanceof EnumType)
                {
                    final EnumType enumType = (EnumType)selectorExprZserioType;
                    zserioType = enumType;
                    evaluateExpressionType(enumType);
                }
            }
            // if this enumeration item is in own enum, leave it unresolved (we have problem with it because
            // such enumeration items cannot be evaluated yet)
        }
        else if (identifierSymbol instanceof BitmaskValue)
        {
            // bitmask value
            // (this can happen for bitmask choices where bitmask is visible or for bitmask itself)
            final ZserioType scopeOwner = forcedEvaluationScope.getOwner();
            if (scopeOwner instanceof ChoiceType)
            {
                // this bitmaks value is in choice with bitmask type selector
                final ChoiceType bitmaskChoice = (ChoiceType)scopeOwner;
                final Expression selectorExpression = bitmaskChoice.getSelectorExpression();
                final ZserioType selectorExprZserioType = selectorExpression.getExprZserioType();
                if (selectorExprZserioType instanceof BitmaskType)
                {
                    final BitmaskType bitmaskType = (BitmaskType)selectorExprZserioType;
                    zserioType = bitmaskType;
                    evaluateExpressionType(bitmaskType);
                }
            }
            // if this bitmask vlaue is in own bitmask, leave it unresolved (we have problem with it because
            // such bitmask values cannot be evaluated yet)
        }
        else if (identifierSymbol instanceof Constant)
        {
            evaluateConstant((Constant)identifierSymbol);
        }
        else
        {
            throw new ParserException(this, "Symbol '" + identifier + "' (" +
                    identifierSymbol.getClass() + ") is not allowed here!");
        }
    }

    private void evaluateConstant(Constant constant)
    {
        // constant type
        evaluateExpressionType(constant.getTypeInstantiation());

        // call evaluation explicitly because this const does not have to be evaluated yet
        final ZserioAstEvaluator evaluator = new ZserioAstEvaluator();
        constant.accept(evaluator);

        final Expression constValueExpression = constant.getValueExpression();
        expressionIntegerValue = constValueExpression.expressionIntegerValue;
        expressionStringValue = constValueExpression.expressionStringValue;
    }

    private void evaluateExpressionType(TypeInstantiation typeInstantiation)
    {
        // call evaluation explicitly because this type instantiation does not have to be evaluated yet
        final ZserioAstEvaluator evaluator = new ZserioAstEvaluator();
        typeInstantiation.accept(evaluator);
        symbolInstantiation = typeInstantiation;
        evaluateExpressionType(typeInstantiation.getType());
    }

    private void evaluateExpressionType(TypeReference typeReference)
    {
        evaluateExpressionType(typeReference.getType());
    }

    private void evaluateExpressionType(ZserioType type)
    {
        final ZserioType baseType = getBaseType(type);
        if (baseType instanceof EnumType)
        {
            expressionType = ExpressionType.ENUM;
            if (symbolObject instanceof EnumItem)
            {
                // call evaluation explicitly because this enumeration item does not have to be evaluated yet
                final EnumItem enumItem = (EnumItem)symbolObject;
                final ZserioAstEvaluator evaluator = new ZserioAstEvaluator();
                ((EnumType)baseType).accept(evaluator);

                // set integer value according to this enumeration item
                expressionIntegerValue = new ExpressionIntegerValue(enumItem.getValue());
            }
        }
        else if (baseType instanceof BitmaskType)
        {
            expressionType = ExpressionType.BITMASK;
            if (symbolObject instanceof BitmaskValue)
            {
                // call evaluation explicitly because this bitmask value does not have to be evaluated yet
                final BitmaskValue bitmaskValue = (BitmaskValue)symbolObject;
                final ZserioAstEvaluator evaluator = new ZserioAstEvaluator();
                ((BitmaskType)baseType).accept(evaluator);

                // set integer value according to this bitmask value
                expressionIntegerValue = new ExpressionIntegerValue(bitmaskValue.getValue());
            }
        }
        else if (baseType instanceof IntegerType)
        {
            expressionType = ExpressionType.INTEGER;
            final IntegerType integerType = (IntegerType)baseType;
            if (symbolInstantiation instanceof DynamicBitFieldInstantiation)
            {
                final DynamicBitFieldInstantiation dynamicBitFieldInstantiation =
                        (DynamicBitFieldInstantiation)symbolInstantiation;

                // call evaluation explicitly because this length does not have to be evaluated yet
                final ZserioAstEvaluator evaluator = new ZserioAstEvaluator();
                dynamicBitFieldInstantiation.accept(evaluator);
                final BigInteger lowerBound = dynamicBitFieldInstantiation.getLowerBound();
                final BigInteger upperBound = dynamicBitFieldInstantiation.getUpperBound();
                expressionIntegerValue = new ExpressionIntegerValue(lowerBound, upperBound);
            }
            else
            {
                // unknown instantiation, get the type's fixed limits
                final BigInteger lowerBound = integerType.getLowerBound();
                final BigInteger upperBound = integerType.getUpperBound();
                expressionIntegerValue = new ExpressionIntegerValue(lowerBound, upperBound);
            }
        }
        else if (baseType instanceof FloatType)
        {
            expressionType = ExpressionType.FLOAT;
        }
        else if (baseType instanceof StringType)
        {
            expressionType = ExpressionType.STRING;
        }
        else if (baseType instanceof BooleanType)
        {
            expressionType = ExpressionType.BOOLEAN;
        }
        else if (baseType instanceof CompoundType)
        {
            expressionType = ExpressionType.COMPOUND;
        }
        else
        {
            expressionType = ExpressionType.UNKNOWN;
        }

        zserioType = baseType;
    }

    private ZserioType getBaseType(ZserioType type)
    {
        if (type instanceof Subtype)
            return ((Subtype)type).getBaseTypeReference().getType();
        else if (type instanceof InstantiateType)
            return ((InstantiateType)type).getTypeReference().getType();
        else
            return type;
    }

    private void initialize()
    {
        evaluationState = EvaluationState.NOT_EVALUATED;
        expressionType = ExpressionType.UNKNOWN;
        expressionIntegerValue = new ExpressionIntegerValue();
        expressionStringValue = null;
        zserioType = null;
        symbolObject = null;
        symbolInstantiation = null;
        unresolvedIdentifiers = new ArrayList<Expression>();
        needsBigIntegerCastingToNative = false;
    }

    private enum EvaluationState
    {
        NOT_EVALUATED,
        IN_EVALUATION,
        EVALUATED
    };

    private static String stripExpressionText(int expressionType, String expressionText)
    {
        switch (expressionType)
        {
            case ZserioParser.BINARY_LITERAL:
                expressionText = stripBinaryLiteral(expressionText);
                break;

            case ZserioParser.OCTAL_LITERAL:
                expressionText = stripOctalLiteral(expressionText);
                break;

            case ZserioParser.HEXADECIMAL_LITERAL:
                expressionText = stripHexadecimalLiteral(expressionText);
                break;

            case ZserioParser.FLOAT_LITERAL:
                expressionText = stripFloatLiteral(expressionText);
                break;

            default:
                break;
        }

        return expressionText;
    }

    private static String stripBinaryLiteral(String binaryLiteral)
    {
        final int postfixPos = findChars(binaryLiteral, 'b', 'B');

        return (postfixPos == -1) ? binaryLiteral : binaryLiteral.substring(0, postfixPos);
    }

    private static String stripOctalLiteral(String octalLiteral)
    {
        final int prefixPos = octalLiteral.indexOf('0');

        return (prefixPos == -1) ? octalLiteral : octalLiteral.substring(prefixPos + 1);
    }

    private static String stripHexadecimalLiteral(String hexadecimalLiteral)
    {
        final int prefixPos = findChars(hexadecimalLiteral, 'x', 'X');

        return (prefixPos == -1) ? hexadecimalLiteral : hexadecimalLiteral.substring(prefixPos + 1);
    }

    private static String stripFloatLiteral(String floatLiteral)
    {
        final int postfixPos = findChars(floatLiteral, 'f', 'F');

        return (postfixPos == -1) ? floatLiteral : floatLiteral.substring(0, postfixPos);
    }

    private static String stripStringLiteral(String stringLiteral)
    {
        final int prefixPos = stringLiteral.indexOf('"');
        final String strippedStringLiteral = (prefixPos == -1) ? stringLiteral :
            stringLiteral.substring(prefixPos + 1);

        final int postfixPos = strippedStringLiteral.lastIndexOf('"');

        return (postfixPos == -1) ? strippedStringLiteral : strippedStringLiteral.substring(0, postfixPos);
    }

    private static int findChars(String text, char firstChar, char secondChar)
    {
        int pos = text.indexOf(firstChar);
        if (pos == -1)
            pos = text.indexOf(secondChar);

        return pos;
    }

    private final Package pkg;

    private final int type;
    private final String text;

    private final ExpressionFlag expressionFlag;

    private final Expression operand1;
    private final Expression operand2;
    private final Expression operand3;

    private Scope evaluationScope;

    private EvaluationState evaluationState;

    private ExpressionType expressionType;
    private ExpressionIntegerValue expressionIntegerValue;
    private String expressionStringValue;
    private ZserioType zserioType;
    private AstNode symbolObject;
    private TypeInstantiation symbolInstantiation;
    private List<Expression> unresolvedIdentifiers;

    private boolean needsBigIntegerCastingToNative;
}
