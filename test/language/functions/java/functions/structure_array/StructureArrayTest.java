package functions.structure_array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import zserio.runtime.array.ObjectArray;
import zserio.runtime.io.ByteArrayBitStreamReader;
import zserio.runtime.io.ByteArrayBitStreamWriter;

public class StructureArrayTest
{
    @Test
    public void checkStructureArrayElement0() throws IOException
    {
        checkStructureArray(0);
    }

    @Test
    public void checkStructureArrayElement1() throws IOException
    {
        checkStructureArray(1);
    }

    @Test
    public void checkStructureArrayElement2() throws IOException
    {
        checkStructureArray(2);
    }

    private byte[] writeStructureArrayToByteArray(int pos) throws IOException
    {
        ByteArrayBitStreamWriter writer = new ByteArrayBitStreamWriter();
        writer.writeBits(NUM_ITEM_ELEMENTS, 16);

        for (Item item : ITEMS)
        {
            writer.writeBits(item.getA(), 8);
            writer.writeBits(item.getB(), 8);
        }

        writer.writeBits(pos, 16);

        writer.close();

        return writer.toByteArray();
    }

    private StructureArray createStructureArray(int pos)
    {
        final StructureArray structureArray = new StructureArray();

        structureArray.setNumElements(NUM_ITEM_ELEMENTS);
        structureArray.setValues(new ObjectArray<Item>(ITEMS));

        structureArray.setPos(pos);

        return structureArray;
    }

    private void checkStructureArray(int pos) throws IOException
    {
        final StructureArray structureArray = createStructureArray(pos);
        assertEquals(ITEMS.get(pos), structureArray.getElement());

        final ByteArrayBitStreamWriter writer = new ByteArrayBitStreamWriter();
        structureArray.write(writer);
        final byte[] writtenByteArray = writer.toByteArray();
        writer.close();

        final byte[] expectedByteArray = writeStructureArrayToByteArray(pos);
        assertTrue(Arrays.equals(expectedByteArray, writtenByteArray));

        final ByteArrayBitStreamReader reader = new ByteArrayBitStreamReader(writtenByteArray);
        final StructureArray readStructureArray = new StructureArray(reader);
        assertEquals(structureArray, readStructureArray);
    }

    private static final List<Item> ITEMS = Arrays.asList
    (
        new Item((short)1, (short)2),
        new Item((short)3, (short)4),
        new Item((short)5, (short)6)
    );
    private static final int NUM_ITEM_ELEMENTS = ITEMS.size();
}
