package offsets;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.File;

import offsets.auto_array_offset.AutoArrayHolder;

import org.junit.Test;

import zserio.runtime.ZserioError;
import zserio.runtime.array.ByteArray;
import zserio.runtime.io.BitStreamReader;
import zserio.runtime.io.BitStreamWriter;
import zserio.runtime.io.ByteArrayBitStreamWriter;
import zserio.runtime.io.FileBitStreamReader;
import zserio.runtime.io.FileBitStreamWriter;

public class AutoArrayOffsetTest
{
    @Test
    public void read() throws IOException, ZserioError
    {
        final boolean writeWrongOffset = false;
        final File file = new File("test.bin");
        writeAutoArrayHolderToFile(file, writeWrongOffset);
        final BitStreamReader stream = new FileBitStreamReader(file);
        final AutoArrayHolder autoArrayHolder = new AutoArrayHolder(stream);
        stream.close();
        checkAutoArrayHolder(autoArrayHolder);
    }

    @Test(expected=ZserioError.class)
    public void readWrongOffsets() throws IOException, ZserioError
    {
        final boolean writeWrongOffset = true;
        final File file = new File("test.bin");
        writeAutoArrayHolderToFile(file, writeWrongOffset);
        final BitStreamReader stream = new FileBitStreamReader(file);
        new AutoArrayHolder(stream);
        stream.close();
    }

    @Test
    public void bitSizeOf()
    {
        final boolean createWrongOffset = false;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        assertEquals(AUTO_ARRAY_HOLDER_BIT_SIZE, autoArrayHolder.bitSizeOf());
    }

    @Test
    public void bitSizeOfWithPosition()
    {
        final boolean createWrongOffset = false;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        final int bitPosition = 2;
        assertEquals(AUTO_ARRAY_HOLDER_BIT_SIZE - bitPosition, autoArrayHolder.bitSizeOf(bitPosition));
    }

    @Test
    public void initializeOffsets()
    {
        final boolean createWrongOffset = true;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        final int bitPosition = 0;
        assertEquals(AUTO_ARRAY_HOLDER_BIT_SIZE, autoArrayHolder.initializeOffsets(bitPosition));
        checkAutoArrayHolder(autoArrayHolder);
    }

    @Test
    public void initializeOffsetsWithPosition()
    {
        final boolean createWrongOffset = true;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        final int bitPosition = 2;
        assertEquals(AUTO_ARRAY_HOLDER_BIT_SIZE, autoArrayHolder.initializeOffsets(bitPosition));
        checkAutoArrayHolder(autoArrayHolder, bitPosition);
    }

    @Test
    public void write() throws IOException, ZserioError
    {
        final boolean createWrongOffset = true;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        final File file = new File("test.bin");
        final BitStreamWriter writer = new FileBitStreamWriter(file);
        autoArrayHolder.write(writer);
        writer.close();
        checkAutoArrayHolder(autoArrayHolder);
        final AutoArrayHolder readAutoArrayHolder = new AutoArrayHolder(file);
        checkAutoArrayHolder(readAutoArrayHolder);
        assertTrue(autoArrayHolder.equals(readAutoArrayHolder));
    }

    @Test
    public void writeWithPosition() throws IOException, ZserioError
    {
        final boolean createWrongOffset = true;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        final File file = new File("test.bin");
        final BitStreamWriter writer = new FileBitStreamWriter(file);
        final int bitPosition = 2;
        writer.writeBits(0, bitPosition);
        autoArrayHolder.write(writer);
        writer.close();
        checkAutoArrayHolder(autoArrayHolder, bitPosition);
    }

    @Test(expected=ZserioError.class)
    public void writeWrongOffset() throws ZserioError, IOException
    {
        final boolean createWrongOffset = true;
        final AutoArrayHolder autoArrayHolder = createAutoArrayHolder(createWrongOffset);
        final BitStreamWriter writer = new ByteArrayBitStreamWriter();
        autoArrayHolder.write(writer, false);
        writer.close();
    }

    private void writeAutoArrayHolderToFile(File file, boolean writeWrongOffset) throws IOException
    {
        final FileBitStreamWriter writer = new FileBitStreamWriter(file);
        writer.writeUnsignedInt((writeWrongOffset) ? WRONG_AUTO_ARRAY_OFFSET : AUTO_ARRAY_OFFSET);
        writer.writeBits(FORCED_ALIGNMENT_VALUE, 8);
        writer.writeVarSize(AUTO_ARRAY_LENGTH);
        for (int i = 0; i < AUTO_ARRAY_LENGTH; ++i)
            writer.writeBits(i, 7);
        writer.close();
    }

    private void checkAutoArrayHolder(AutoArrayHolder autoArrayHolder)
    {
        checkAutoArrayHolder(autoArrayHolder, 0);
    }

    private void checkAutoArrayHolder(AutoArrayHolder autoArrayHolder, int bitPosition)
    {
        final long expectedAutoArrayOffset = (bitPosition == 0) ? AUTO_ARRAY_OFFSET :
            AUTO_ARRAY_OFFSET + (bitPosition / 8);
        assertEquals(expectedAutoArrayOffset, autoArrayHolder.getAutoArrayOffset());

        assertEquals(FORCED_ALIGNMENT_VALUE, autoArrayHolder.getForceAlignment());

        final ByteArray autoArray = autoArrayHolder.getAutoArray();
        assertEquals(AUTO_ARRAY_LENGTH, autoArray.length());
        for (int i = 0; i < AUTO_ARRAY_LENGTH; ++i)
            assertEquals((byte)i, autoArray.elementAt(i));
    }

    private AutoArrayHolder createAutoArrayHolder(boolean createWrongOffset)
    {
        final long autoArrayOffset = (createWrongOffset) ? WRONG_AUTO_ARRAY_OFFSET : AUTO_ARRAY_OFFSET;
        final ByteArray autoArray = new ByteArray(AUTO_ARRAY_LENGTH);
        for (int i = 0; i < AUTO_ARRAY_LENGTH; ++i)
            autoArray.setElementAt((byte)i, i);

        return new AutoArrayHolder(autoArrayOffset, FORCED_ALIGNMENT_VALUE, autoArray);
    }

    private static final int    AUTO_ARRAY_LENGTH = 5;
    private static final byte   FORCED_ALIGNMENT_VALUE = 0;

    private static final long   WRONG_AUTO_ARRAY_OFFSET = 0;
    private static final long   AUTO_ARRAY_OFFSET = 5;

    private static final int    AUTO_ARRAY_HOLDER_BIT_SIZE = 32 + 1 + 7 + 8 + AUTO_ARRAY_LENGTH * 7;
}
