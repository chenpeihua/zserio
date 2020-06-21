package zserio.emit.java;

import zserio.ast.BooleanType;
import zserio.ast.DynamicBitFieldInstantiation;
import zserio.ast.ExternType;
import zserio.ast.FixedBitFieldType;
import zserio.ast.IntegerType;
import zserio.ast.TypeInstantiation;
import zserio.ast.ZserioAstDefaultVisitor;
import zserio.ast.FloatType;
import zserio.ast.StdIntegerType;
import zserio.ast.StringType;
import zserio.ast.VarIntegerType;
import zserio.emit.common.ExpressionFormatter;
import zserio.emit.common.ZserioEmitException;

public class JavaRuntimeFunctionDataCreator
{
    public static RuntimeFunctionTemplateData createData(TypeInstantiation typeInstantiation,
            ExpressionFormatter javaExpressionFormatter, JavaNativeMapper javaNativeMapper)
                    throws ZserioEmitException
    {
        if (typeInstantiation instanceof DynamicBitFieldInstantiation)
        {
            return mapDynamicBitField(
                    (DynamicBitFieldInstantiation)typeInstantiation, javaExpressionFormatter, javaNativeMapper);
        }
        else
        {
            final Visitor visitor = new Visitor(javaNativeMapper);
            typeInstantiation.getBaseType().accept(visitor);

            final ZserioEmitException thrownException = visitor.getThrownException();
            if (thrownException != null)
                throw thrownException;

            // template data can be null, this need to be handled specially in template
            return visitor.getTemplateData();
        }
    }

    private static RuntimeFunctionTemplateData mapDynamicBitField(DynamicBitFieldInstantiation instantiation,
            ExpressionFormatter javaExpressionFormatter, JavaNativeMapper javaNativeMapper)
                    throws ZserioEmitException
    {
        final String suffix = (instantiation.getBaseType().isSigned()) ? "SignedBits" :
            (instantiation.getMaxBitSize() > 63) ? "BigInteger" : "Bits";
        // this int cast is necessary because length can be bigger than integer (uint64, uint32)
        final String arg = "(int)" + javaExpressionFormatter.formatGetter(instantiation.getLengthExpression());
        return new RuntimeFunctionTemplateData(
                suffix, arg, javaNativeMapper.getJavaType(instantiation).getFullName());
    }

    private static class Visitor extends ZserioAstDefaultVisitor
    {
        public Visitor(JavaNativeMapper javaNativeMapper)
        {
            this.javaNativeMapper = javaNativeMapper;
        }

        public RuntimeFunctionTemplateData getTemplateData()
        {
            return templateData;
        }

        public ZserioEmitException getThrownException()
        {
            return thrownException;
        }

        @Override
        public void visitBooleanType(BooleanType type)
        {
            templateData = new RuntimeFunctionTemplateData("Bool");
        }

        @Override
        public void visitFloatType(FloatType type)
        {
            templateData = new RuntimeFunctionTemplateData("Float" + type.getBitSize());
        }

        @Override
        public void visitExternType(ExternType type)
        {
            templateData = new RuntimeFunctionTemplateData("BitBuffer");
        }

        @Override
        public void visitFixedBitFieldType(FixedBitFieldType type)
        {
            handleFixedIntegerType(type, type.getBitSize());
        }

        @Override
        public void visitStdIntegerType(StdIntegerType type)
        {
            handleFixedIntegerType(type, type.getBitSize());
        }

        @Override
        public void visitStringType(StringType type)
        {
            templateData = new RuntimeFunctionTemplateData("String");
        }

        @Override
        public void visitVarIntegerType(VarIntegerType type)
        {
            final StringBuilder suffix = new StringBuilder();
            final int maxBitSize = type.getMaxBitSize();
            suffix.append("Var");
            if (maxBitSize == 40) // VarSize
            {
                suffix.append("Size");
            }
            else
            {
                if (!type.isSigned())
                    suffix.append("U");
                suffix.append("Int");
                if (maxBitSize != 72) // Var(U)Int takes up to 9 bytes
                    suffix.append(maxBitSize);
            }

            templateData = new RuntimeFunctionTemplateData(suffix.toString());
        }

        private void handleFixedIntegerType(IntegerType type, int bitSize)
        {
            if (type.isSigned())
            {
                switch (bitSize)
                {
                case 8:
                    templateData = new RuntimeFunctionTemplateData("Byte");
                    break;

                case 16:
                    templateData = new RuntimeFunctionTemplateData("Short");
                    break;

                case 32:
                    templateData = new RuntimeFunctionTemplateData("Int");
                    break;

                case 64:
                    templateData = new RuntimeFunctionTemplateData("Long");
                    break;

                default:
                    try
                    {
                        final String suffix = "SignedBits";
                        final String arg = JavaLiteralFormatter.formatDecimalLiteral(bitSize);
                        templateData = new RuntimeFunctionTemplateData(
                                suffix, arg, javaNativeMapper.getJavaType(type).getFullName());
                    }
                    catch (ZserioEmitException exception)
                    {
                        thrownException = exception;
                    }
                    break;
                }
            }
            else
            {
                switch (bitSize)
                {
                case 8:
                    templateData = new RuntimeFunctionTemplateData("UnsignedByte");
                    break;

                case 16:
                    templateData = new RuntimeFunctionTemplateData("UnsignedShort");
                    break;

                case 32:
                    templateData = new RuntimeFunctionTemplateData("UnsignedInt");
                    break;

                case 64:
                    try
                    {
                        templateData = new RuntimeFunctionTemplateData("BigInteger",
                                JavaLiteralFormatter.formatDecimalLiteral(bitSize));
                    }
                    catch (ZserioEmitException exception)
                    {
                        thrownException = exception;
                    }
                    break;

                default:
                    try
                    {
                        final String suffix = "Bits";
                        final String arg = JavaLiteralFormatter.formatDecimalLiteral(bitSize);
                        templateData = new RuntimeFunctionTemplateData(
                                suffix, arg, javaNativeMapper.getJavaType(type).getFullName());
                    }
                    catch (ZserioEmitException exception)
                    {
                        thrownException = exception;
                    }
                    break;
                }
            }
        }

        private final JavaNativeMapper javaNativeMapper;

        private RuntimeFunctionTemplateData templateData = null;
        private ZserioEmitException thrownException = null;
    }
}
