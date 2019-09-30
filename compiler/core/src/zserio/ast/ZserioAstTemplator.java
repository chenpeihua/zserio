package zserio.ast;

import zserio.antlr.util.ParserException;
import zserio.ast.Package;

/**
 * Implementation of ZserioAstVisitor which handles templates instantiation.
 */
public class ZserioAstTemplator extends ZserioAstWalker
{
    @Override
    public void visitPackage(Package currentPackage)
    {
        this.currentPackage = currentPackage;

        currentPackage.visitChildren(this);

        this.currentPackage = null;
    }

    @Override
    public void visitStructureType(StructureType structureType)
    {
        if (structureType.getTemplateParameters().isEmpty())
            structureType.visitChildren(this);
    }

    @Override
    public void visitChoiceType(ChoiceType choiceType)
    {
        if (choiceType.getTemplateParameters().isEmpty())
            choiceType.visitChildren(this);
    }

    @Override
    public void visitUnionType(UnionType unionType)
    {
        if (unionType.getTemplateParameters().isEmpty())
            unionType.visitChildren(this);
    }

    @Override
    public void visitSqlTableType(SqlTableType sqlTableType)
    {
        if (sqlTableType.getTemplateParameters().isEmpty())
            sqlTableType.visitChildren(this);
    }

    @Override
    public void visitTypeReference(TypeReference typeReference)
    {
        if (!typeReference.getTemplateArguments().isEmpty()) // if is a template instantiation
        {
            // instantiate instantiations in template arguments
            for (ZserioType templateArgument : typeReference.getTemplateArguments())
                templateArgument.accept(this);

            final ZserioType type = currentPackage.getVisibleType(
                    typeReference.getReferencedPackageName(), typeReference.getReferencedTypeName());
            if (type == null)
            {
                // TODO[Mi-L@]: The same error comes from TypeReference.resolve(). How to share it?
                throw new ParserException(typeReference, "Unresolved referenced type '" +
                        ZserioTypeUtil.getReferencedFullName(typeReference) + "'!");
            }

            if (!(type instanceof ZserioTemplatableType) ||
                    ((ZserioTemplatableType)type).getTemplateParameters().isEmpty())
            {
                throw new ParserException(typeReference,
                        "'" + ZserioTypeUtil.getReferencedFullName(typeReference) + "' is not a template!");
            }

            final ZserioTemplatableType template = (ZserioTemplatableType)type;
            final ZserioTemplatableType instantiation = template.instantiate(typeReference);
            instantiation.accept(this);
        }
    }

    private Package currentPackage = null;
}
