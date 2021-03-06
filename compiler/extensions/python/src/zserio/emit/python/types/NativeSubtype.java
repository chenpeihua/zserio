package zserio.emit.python.types;

import zserio.ast.PackageName;

public class NativeSubtype extends NativeUserType
{
    public NativeSubtype(PackageName packageName, String name, PythonNativeType nativeTargetBaseType)
    {
        super(packageName, name);
        this.nativeTargetBaseType = nativeTargetBaseType;
    }

    public PythonNativeType getNativeTargetBaseType()
    {
        return nativeTargetBaseType;
    }

    private final PythonNativeType nativeTargetBaseType;
}
