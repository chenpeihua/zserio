package zserio.emit.printer;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import zserio.ast.Root;
import zserio.emit.common.ZserioEmitException;
import zserio.tools.Extension;
import zserio.tools.Parameters;

public class PrinterExtension implements Extension
{
    @Override
    public String getName()
    {
        return "AST Nodes Printer";
    }

    @Override
    public String getVersion()
    {
        return "1.2.0";
    }

    @Override
    public void registerOptions(Options options)
    {
        final Option option = new Option(OptionPrinter, false, "print AST nodes on standard output");
        option.setRequired(false);
        options.addOption(option);
    }

    @Override
    public boolean isEnabled(Parameters parameters)
    {
        return parameters.argumentExists(OptionPrinter);
    }

    @Override
    public void generate(Parameters parameters, Root rootNode) throws ZserioEmitException
    {
        final PrinterEmitter printerEmitter = new PrinterEmitter();
        rootNode.emit(printerEmitter);
    }

    private final static String OptionPrinter = "print";
}
