package offsets;

import static org.junit.Assert.*;

import indexed_offsets.auto_indexed_offset_array.AutoIndexedOffsetArray;

import java.io.IOException;
import java.io.File;

import org.junit.Test;

import zserio.runtime.ZserioError;
import zserio.runtime.array.UnsignedByteArray;
import zserio.runtime.array.UnsignedIntArray;
import zserio.runtime.io.BitStreamReader;
import zserio.runtime.io.BitStreamWriter;
import zserio.runtime.io.ByteArrayBitStreamWriter;
import zserio.runtime.io.FileBitStreamReader;
import zserio.runtime.io.FileBitStreamWriter;

public class AutoIndexedOffsetArrayTest
{
    @Test
    public void read() throws IOException, ZserioError
    {
        final boolean writeWrongOffsets = false;
        final File file = new File("test.bin");
        writeAutoIndexedOffsetArrayToFile(file, writeWrongOffsets);
        final BitStreamReader stream = new FileBitStreamReader(file);
        final AutoIndexedOffsetArray autoIndexedOffsetArray = new AutoIndexedOffsetArray(stream);
        stream.close();
        checkAutoIndexedOffsetArray(autoIndexedOffsetArray);
    }

    @Test(expected=ZserioError.class)
    public void readWrongOffsets() throws IOException, ZserioError
    {
        final boolean writeWrongOffsets = true;
        final File file = new File("test.bin");
        writeAutoIndexedOffsetArrayToFile(file, writeWrongOffsets);
        final BitStreamReader stream = new FileBitStreamReader(file);
        final AutoIndexedOffsetArray autoIndexedOffsetArray = new AutoIndexedOffsetArray(stream);
        stream.close();
        checkAutoIndexedOffsetArray(autoIndexedOffsetArray);
    }

    @Test
    public void bitSizeOf()
    {
        final boolean createWrongOffsets = false;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        assertEquals(AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE, autoIndexedOffsetArray.bitSizeOf());
    }

    @Test
    public void bitSizeOfWithPosition()
    {
        final boolean createWrongOffsets = false;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        final int bitPosition = 1;
        assertEquals(AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE - bitPosition,
                autoIndexedOffsetArray.bitSizeOf(bitPosition));
    }

    @Test
    public void initializeOffsets()
    {
        final boolean createWrongOffsets = true;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        final int bitPosition = 0;
        assertEquals(AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE, autoIndexedOffsetArray.initializeOffsets(bitPosition));
        checkAutoIndexedOffsetArray(autoIndexedOffsetArray);
    }

    @Test
    public void initializeOffsetsWithPosition()
    {
        final boolean createWrongOffsets = true;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        final int bitPosition = 9;
        assertEquals(AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE + bitPosition - 1,
                autoIndexedOffsetArray.initializeOffsets(bitPosition));

        final short offsetShift = 1;
        checkOffsets(autoIndexedOffsetArray, offsetShift);
    }

    @Test
    public void write() throws IOException, ZserioError
    {
        final boolean createWrongOffsets = true;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        final File file = new File("test.bin");
        final BitStreamWriter writer = new FileBitStreamWriter(file);
        autoIndexedOffsetArray.write(writer);
        writer.close();
        checkAutoIndexedOffsetArray(autoIndexedOffsetArray);
        final AutoIndexedOffsetArray readAutoIndexedOffsetArray = new AutoIndexedOffsetArray(file);
        checkAutoIndexedOffsetArray(readAutoIndexedOffsetArray);
        assertTrue(autoIndexedOffsetArray.equals(readAutoIndexedOffsetArray));
    }

    @Test
    public void writeWithPosition() throws IOException, ZserioError
    {
        final boolean createWrongOffsets = true;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        final File file = new File("test.bin");
        final BitStreamWriter writer = new FileBitStreamWriter(file);
        final int bitPosition = 8;
        writer.writeBits(0, bitPosition);
        autoIndexedOffsetArray.write(writer);
        writer.close();

        final short offsetShift = 1;
        checkOffsets(autoIndexedOffsetArray, offsetShift);
    }

    @Test(expected=ZserioError.class)
    public void writeWrongOffsets() throws ZserioError, IOException
    {
        final boolean createWrongOffsets = true;
        final AutoIndexedOffsetArray autoIndexedOffsetArray = createAutoIndexedOffsetArray(createWrongOffsets);
        final BitStreamWriter writer = new ByteArrayBitStreamWriter();
        autoIndexedOffsetArray.write(writer, false);
        writer.close();
    }

    private void writeAutoIndexedOffsetArrayToFile(File file, boolean writeWrongOffsets) throws IOException
    {
        final FileBitStreamWriter writer = new FileBitStreamWriter(file);

        writer.writeVarSize(NUM_ELEMENTS);
        long currentOffset = ELEMENT0_OFFSET;
        for (short i = 0; i < NUM_ELEMENTS; ++i)
        {
            if ((i + 1) == NUM_ELEMENTS && writeWrongOffsets)
                writer.writeUnsignedInt(WRONG_OFFSET);
            else
                writer.writeUnsignedInt(currentOffset);
            currentOffset += ALIGNED_ELEMENT_BYTE_SIZE;
        }

        writer.writeBits(SPACER_VALUE, 1);

        writer.writeVarSize(NUM_ELEMENTS);
        writer.writeBits(0, 7);
        for (short i = 0; i < NUM_ELEMENTS; ++i)
        {
            writer.writeBits(i % 64, ELEMENT_SIZE);
            if ((i + 1) != NUM_ELEMENTS)
                writer.writeBits(0, ALIGNED_ELEMENT_SIZE - ELEMENT_SIZE);
        }

        writer.close();
    }

    private void checkOffsets(AutoIndexedOffsetArray autoIndexedOffsetArray, short offsetShift)
    {
        final UnsignedIntArray offsets = autoIndexedOffsetArray.getOffsets();
        assertEquals(NUM_ELEMENTS, offsets.length());
        long expectedOffset = ELEMENT0_OFFSET + offsetShift;
        for (long offset : offsets)
        {
            assertEquals(expectedOffset, offset);
            expectedOffset += ALIGNED_ELEMENT_BYTE_SIZE;
        }
    }

    private void checkAutoIndexedOffsetArray(AutoIndexedOffsetArray autoIndexedOffsetArray)
    {
        final short offsetShift = 0;
        checkOffsets(autoIndexedOffsetArray, offsetShift);

        assertEquals(SPACER_VALUE, autoIndexedOffsetArray.getSpacer());

        final UnsignedByteArray data = autoIndexedOffsetArray.getData();
        assertEquals(NUM_ELEMENTS, data.length());
        for (short i = 0; i < NUM_ELEMENTS; ++i)
            assertEquals(i % 64, data.elementAt(i));
    }

    private AutoIndexedOffsetArray createAutoIndexedOffsetArray(boolean createWrongOffsets)
    {
        final AutoIndexedOffsetArray autoIndexedOffsetArray = new AutoIndexedOffsetArray();

        final UnsignedIntArray offsets = new UnsignedIntArray(NUM_ELEMENTS);
        long currentOffset = ELEMENT0_OFFSET;
        for (short i = 0; i < NUM_ELEMENTS; ++i)
        {
            if ((i + 1) == NUM_ELEMENTS && createWrongOffsets)
                offsets.setElementAt(WRONG_OFFSET, i);
            else
                offsets.setElementAt(currentOffset, i);
            currentOffset += ALIGNED_ELEMENT_BYTE_SIZE;
        }
        autoIndexedOffsetArray.setOffsets(offsets);
        autoIndexedOffsetArray.setSpacer(SPACER_VALUE);

        final UnsignedByteArray data = new UnsignedByteArray(NUM_ELEMENTS);
        for (short i = 0; i < NUM_ELEMENTS; ++i)
            data.setElementAt((short)(i % 64), i);
        autoIndexedOffsetArray.setData(data);

        return autoIndexedOffsetArray;
    }

    private static final short  NUM_ELEMENTS = (short)5;

    private static final long   WRONG_OFFSET = (long)0;

    private static final int    AUTO_ARRAY_LENGTH_BYTE_SIZE = 1;
    private static final long   ELEMENT0_OFFSET = AUTO_ARRAY_LENGTH_BYTE_SIZE +
            (long)(NUM_ELEMENTS * Integer.SIZE + Byte.SIZE) / Byte.SIZE + AUTO_ARRAY_LENGTH_BYTE_SIZE;
    private static final int    ELEMENT_SIZE = 5;
    private static final int    ALIGNED_ELEMENT_SIZE = Byte.SIZE;
    private static final int    ALIGNED_ELEMENT_BYTE_SIZE = ALIGNED_ELEMENT_SIZE / Byte.SIZE;

    private static final byte   SPACER_VALUE = 1;

    private static final int    AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE = (int)ELEMENT0_OFFSET * Byte.SIZE +
            (NUM_ELEMENTS - 1) * ALIGNED_ELEMENT_SIZE + ELEMENT_SIZE;
}
