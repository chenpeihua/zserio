package union_types;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import union_types.union_with_parameterized_field.TestUnion;
import union_types.union_with_parameterized_field.ArrayHolder;

import zserio.runtime.ZserioError;
import zserio.runtime.io.ByteArrayBitStreamReader;
import zserio.runtime.io.ByteArrayBitStreamWriter;
import zserio.runtime.array.UnsignedIntArray;

public class UnionWithParameterizedFieldTest
{
    @Test
    public void emptyConstructor() throws ZserioError, IOException
    {
        final TestUnion testUnion = new TestUnion();
        testUnion.setArrayHolder(new ArrayHolder((short)10));
        assertEquals((short)10, testUnion.getArrayHolder().getSize());
    }

    public void readConstructor() throws ZserioError, IOException
    {
        final TestUnion testUnion = new TestUnion();
        final ArrayHolder arrayHolder = new ArrayHolder((short)10);
        arrayHolder.setArray(new UnsignedIntArray(10));
        testUnion.setArrayHolder(arrayHolder);
        final ByteArrayBitStreamWriter writer = new ByteArrayBitStreamWriter();
        testUnion.write(writer);

        final byte[] buffer = writer.toByteArray();
        final ByteArrayBitStreamReader reader = new ByteArrayBitStreamReader(buffer);
        final TestUnion readTestUnion = new TestUnion(reader);
        assertEquals((short)10, readTestUnion.getArrayHolder().getSize());
    }
}
