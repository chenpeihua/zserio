package zserio.emit.cpp;

import java.util.ArrayList;
import java.util.List;

import zserio.ast.ChoiceCase;
import zserio.ast.ChoiceCaseExpression;
import zserio.ast.ChoiceDefault;
import zserio.ast.ChoiceType;
import zserio.ast.Expression;
import zserio.ast.Field;
import zserio.emit.common.ExpressionFormatter;
import zserio.emit.common.ZserioEmitException;

public class ChoiceEmitterTemplateData extends CompoundTypeTemplateData
{
    public ChoiceEmitterTemplateData(TemplateDataContext context, ChoiceType choiceType)
            throws ZserioEmitException
    {
        super(context, choiceType);

        final CppNativeMapper cppNativeMapper = context.getCppNativeMapper();
        final ExpressionFormatter cppExpressionFormatter = context.getExpressionFormatter(this);

        final Expression expression = choiceType.getSelectorExpression();
        selectorExpression = cppExpressionFormatter.formatGetter(expression);
        // TODO[Mi-L@]: Consider using switch on bitmask also (using valueof).
        canUseNativeSwitch = expression.getExprType() != Expression.ExpressionType.BOOLEAN &&
                expression.getExprType() != Expression.ExpressionType.BITMASK;

        caseMemberList = new ArrayList<CaseMember>();
        final boolean withWriterCode = context.getWithWriterCode();
        final boolean withRangeCheckCode = context.getWithRangeCheckCode();
        final ExpressionFormatter cppIndirectExpressionFormatter =
                context.getOwnerIndirectExpressionFormatter(this);
        final Iterable<ChoiceCase> choiceCaseTypes = choiceType.getChoiceCases();
        for (ChoiceCase choiceCaseType : choiceCaseTypes)
        {
            caseMemberList.add(new CaseMember(cppNativeMapper, choiceType, choiceCaseType,
                    cppExpressionFormatter, cppIndirectExpressionFormatter, this,
                    withWriterCode, withRangeCheckCode));
        }

        final ChoiceDefault choiceDefaultType = choiceType.getChoiceDefault();
        if (choiceDefaultType != null)
        {
            defaultMember = new DefaultMember(cppNativeMapper, choiceType, choiceDefaultType,
                    cppExpressionFormatter, cppIndirectExpressionFormatter, this,
                    withWriterCode, withRangeCheckCode);
        }
        else
        {
            defaultMember = null;
        }

        isDefaultUnreachable = choiceType.isChoiceDefaultUnreachable();
    }

    public String getSelectorExpression()
    {
        return selectorExpression;
    }

    public boolean getCanUseNativeSwitch()
    {
        return canUseNativeSwitch;
    }

    public Iterable<CaseMember> getCaseMemberList()
    {
        return caseMemberList;
    }

    public DefaultMember getDefaultMember()
    {
        return defaultMember;
    }

    public boolean getIsDefaultUnreachable()
    {
        return isDefaultUnreachable;
    }

    public static class CaseMember
    {
        public CaseMember(CppNativeMapper cppNativeMapper, ChoiceType choiceType,
                ChoiceCase choiceCaseType, ExpressionFormatter cppExpressionFormatter,
                ExpressionFormatter cppIndirectExpressionFormatter, IncludeCollector includeCollector,
                boolean withWriterCode, boolean withRangeCheckCode) throws ZserioEmitException
        {
            expressionList = new ArrayList<String>();
            final Iterable<ChoiceCaseExpression> caseExpressions = choiceCaseType.getExpressions();
            for (ChoiceCaseExpression caseExpression : caseExpressions)
                expressionList.add(cppExpressionFormatter.formatGetter(caseExpression.getExpression()));

            final Field fieldType = choiceCaseType.getField();
            compoundField = (fieldType != null) ? new CompoundFieldTemplateData(cppNativeMapper,
                    choiceType, fieldType, cppExpressionFormatter,
                    cppIndirectExpressionFormatter, includeCollector,
                    withWriterCode, withRangeCheckCode) : null;
        }

        public List<String> getExpressionList()
        {
            return expressionList;
        }

        public CompoundFieldTemplateData getCompoundField()
        {
            return compoundField;
        }

        private final List<String> expressionList;
        private final CompoundFieldTemplateData compoundField;
    }

    public static class DefaultMember
    {
        public DefaultMember(CppNativeMapper cppNativeMapper,
                ChoiceType choiceType, ChoiceDefault choiceDefaultType,
                ExpressionFormatter cppExpressionFormatter, ExpressionFormatter cppIndirectExpressionFormatter,
                IncludeCollector includeCollector, boolean withWriterCode,
                boolean withRangeCheckCode) throws ZserioEmitException
        {
            final Field fieldType = choiceDefaultType.getField();
            compoundField = (fieldType != null) ? new CompoundFieldTemplateData(
                    cppNativeMapper, choiceType, fieldType, cppExpressionFormatter,
                    cppIndirectExpressionFormatter, includeCollector,
                    withWriterCode, withRangeCheckCode) : null;
        }

        public CompoundFieldTemplateData getCompoundField()
        {
            return compoundField;
        }

        private final CompoundFieldTemplateData compoundField;
    }

    private final String selectorExpression;
    private final boolean canUseNativeSwitch;
    private final List<CaseMember> caseMemberList;
    private final DefaultMember defaultMember;
    private final boolean isDefaultUnreachable;
}
