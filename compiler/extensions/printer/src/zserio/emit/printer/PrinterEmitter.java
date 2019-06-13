package zserio.emit.printer;

import zserio.ast.ChoiceType;
import zserio.ast.CompoundType;
import zserio.ast.ConstType;
import zserio.ast.EnumType;
import zserio.ast.Field;
import zserio.ast.Import;
import zserio.ast.Package;
import zserio.ast.ServiceType;
import zserio.ast.SqlDatabaseType;
import zserio.ast.SqlTableType;
import zserio.ast.StructureType;
import zserio.ast.Subtype;
import zserio.ast.UnionType;
import zserio.emit.common.DefaultEmitter;
import zserio.emit.common.ZserioEmitException;

public class PrinterEmitter extends DefaultEmitter
{
    @Override
    public void beginPackage(Package pkg) throws ZserioEmitException
    {
        System.out.println("  package " + pkg.getPackageName());
    }

    @Override
    public void beginImport(Import importNode) throws ZserioEmitException
    {
        System.out.println("    import " + importNode.getImportedPackageName());
    }

    @Override
    public void beginConst(ConstType constType) throws ZserioEmitException
    {
        System.out.println("    const " + constType.getName());
    }

    @Override
    public void beginSubtype(Subtype subType) throws ZserioEmitException
    {
        System.out.println("    subtype " + subType.getName());
    }

    @Override
    public void beginStructure(StructureType structureType) throws ZserioEmitException
    {
        System.out.println("    structure " + structureType.getName());
        printCompoundField(structureType);
    }

    @Override
    public void beginChoice(ChoiceType choiceType) throws ZserioEmitException
    {
        System.out.println("    choice " + choiceType.getName());
        printCompoundField(choiceType);
    }

    @Override
    public void beginUnion(UnionType unionType) throws ZserioEmitException
    {
        System.out.println("    union " + unionType.getName());
        printCompoundField(unionType);
    }

    @Override
    public void beginEnumeration(EnumType enumType) throws ZserioEmitException
    {
        System.out.println("    enum " + enumType.getName());
    }

    @Override
    public void beginSqlTable(SqlTableType sqlTableType) throws ZserioEmitException
    {
        System.out.println("    sql_table " + sqlTableType.getName());
        printCompoundField(sqlTableType);
    }

    @Override
    public void beginSqlDatabase(SqlDatabaseType sqlDatabaseType) throws ZserioEmitException
    {
        System.out.println("    sql_database " + sqlDatabaseType.getName());
        printCompoundField(sqlDatabaseType);
    }

    @Override
    public void beginService(ServiceType service) throws ZserioEmitException
    {
        System.out.println("    service " + service.getName());
    }

    private void printCompoundField(CompoundType compoundType)
    {
        for (Field field : compoundType.getFields())
            System.out.println("      " + field.getFieldType().getName() + " " + field.getName());
    }
}
