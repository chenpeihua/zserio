#include "gtest/gtest.h"

#include "zserio/BitStreamWriter.h"
#include "zserio/BitStreamReader.h"
#include "zserio/CppRuntimeException.h"

#include "indexed_offsets/auto_indexed_offset_array/AutoIndexedOffsetArray.h"

namespace indexed_offsets
{
namespace auto_indexed_offset_array
{

class AutoIndexedOffsetArrayTest : public ::testing::Test
{
protected:
    void writeAutoIndexedOffsetArrayToByteArray(zserio::BitStreamWriter& writer, bool writeWrongOffsets)
    {
        writer.writeVarSize(NUM_ELEMENTS);
        const uint32_t wrongOffset = WRONG_OFFSET;
        uint32_t currentOffset = ELEMENT0_OFFSET;
        for (uint8_t i = 0; i < NUM_ELEMENTS; ++i)
        {
            if ((i + 1) == NUM_ELEMENTS && writeWrongOffsets)
                writer.writeBits(wrongOffset, 32);
            else
                writer.writeBits(currentOffset, 32);
            currentOffset += ALIGNED_ELEMENT_BYTE_SIZE;
        }

        writer.writeBits(SPACER_VALUE, 1);

        writer.writeVarSize(NUM_ELEMENTS);
        writer.writeBits(0, 7);
        for (uint8_t i = 0; i < NUM_ELEMENTS; ++i)
        {
            writer.writeBits(i % 64, ELEMENT_SIZE);
            if ((i + 1) != NUM_ELEMENTS)
                writer.writeBits(0, ALIGNED_ELEMENT_SIZE - ELEMENT_SIZE);
        }
    }

    void checkOffsets(const AutoIndexedOffsetArray& autoIndexedOffsetArray, uint16_t offsetShift)
    {
        const std::vector<uint32_t>& offsets = autoIndexedOffsetArray.getOffsets();
        const size_t expectedNumElements = NUM_ELEMENTS;
        ASSERT_EQ(expectedNumElements, offsets.size());
        uint32_t expectedOffset = ELEMENT0_OFFSET + offsetShift;
        for (std::vector<uint32_t>::const_iterator it = offsets.begin(); it != offsets.end(); ++it)
        {
            ASSERT_EQ(expectedOffset, *it);
            expectedOffset += ALIGNED_ELEMENT_BYTE_SIZE;
        }
    }

    void checkAutoIndexedOffsetArray(const AutoIndexedOffsetArray& autoIndexedOffsetArray)
    {
        const uint16_t offsetShift = 0;
        checkOffsets(autoIndexedOffsetArray, offsetShift);

        const uint8_t expectedSpacer = SPACER_VALUE;
        ASSERT_EQ(expectedSpacer, autoIndexedOffsetArray.getSpacer());

        const std::vector<uint8_t>& data = autoIndexedOffsetArray.getData();
        const size_t expectedNumElements = NUM_ELEMENTS;
        ASSERT_EQ(expectedNumElements, data.size());
        for (uint8_t i = 0; i < NUM_ELEMENTS; ++i)
            ASSERT_EQ(i % 64, data[i]);
    }

    void fillAutoIndexedOffsetArray(AutoIndexedOffsetArray& autoIndexedOffsetArray, bool createWrongOffsets)
    {
        std::vector<uint32_t>& offsets = autoIndexedOffsetArray.getOffsets();
        offsets.reserve(NUM_ELEMENTS);
        const uint32_t wrongOffset = WRONG_OFFSET;
        uint32_t currentOffset = ELEMENT0_OFFSET;
        for (uint8_t i = 0; i < NUM_ELEMENTS; ++i)
        {
            if ((i + 1) == NUM_ELEMENTS && createWrongOffsets)
                offsets.push_back(wrongOffset);
            else
                offsets.push_back(currentOffset);
            currentOffset += ALIGNED_ELEMENT_BYTE_SIZE;
        }
        autoIndexedOffsetArray.setSpacer(SPACER_VALUE);

        std::vector<uint8_t>& data = autoIndexedOffsetArray.getData();
        data.reserve(NUM_ELEMENTS);
        for (uint8_t i = 0; i < NUM_ELEMENTS; ++i)
            data.push_back(i % 64);
    }

    static const uint8_t    NUM_ELEMENTS = 5;

    static const uint32_t   WRONG_OFFSET = 0;

    static const size_t     AUTO_ARRAY_LENGTH_BYTE_SIZE = 1;
    static const uint32_t   ELEMENT0_OFFSET = AUTO_ARRAY_LENGTH_BYTE_SIZE + NUM_ELEMENTS * sizeof(uint32_t) +
            sizeof(uint8_t) + AUTO_ARRAY_LENGTH_BYTE_SIZE;
    static const uint8_t    ELEMENT_SIZE = 5;
    static const uint8_t    ALIGNED_ELEMENT_SIZE = 8;
    static const uint8_t    ALIGNED_ELEMENT_BYTE_SIZE = ALIGNED_ELEMENT_SIZE / 8;

    static const uint8_t    SPACER_VALUE = 1;

    static const size_t     AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE = ELEMENT0_OFFSET * 8 +
            (NUM_ELEMENTS - 1) * ALIGNED_ELEMENT_SIZE + ELEMENT_SIZE;
};

TEST_F(AutoIndexedOffsetArrayTest, read)
{
    const bool writeWrongOffsets = false;
    zserio::BitStreamWriter writer;
    writeAutoIndexedOffsetArrayToByteArray(writer, writeWrongOffsets);
    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);

    AutoIndexedOffsetArray autoIndexedOffsetArray(reader);
    checkAutoIndexedOffsetArray(autoIndexedOffsetArray);
}

TEST_F(AutoIndexedOffsetArrayTest, readWrongOffsets)
{
    const bool writeWrongOffsets = true;
    zserio::BitStreamWriter writer;
    writeAutoIndexedOffsetArrayToByteArray(writer, writeWrongOffsets);
    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);

    EXPECT_THROW(AutoIndexedOffsetArray autoIndexedOffsetArray(reader), zserio::CppRuntimeException);
}

TEST_F(AutoIndexedOffsetArrayTest, bitSizeOf)
{
    const bool createWrongOffsets = false;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    const size_t expectedBitSize = AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE;
    ASSERT_EQ(expectedBitSize, autoIndexedOffsetArray.bitSizeOf());
}

TEST_F(AutoIndexedOffsetArrayTest, bitSizeOfWithPosition)
{
    const bool createWrongOffsets = false;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    const size_t bitPosition = 1;
    const size_t expectedBitSize = AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE - bitPosition;
    ASSERT_EQ(expectedBitSize, autoIndexedOffsetArray.bitSizeOf(bitPosition));
}

TEST_F(AutoIndexedOffsetArrayTest, initializeOffsets)
{
    const bool createWrongOffsets = true;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    const size_t bitPosition = 0;
    const size_t expectedBitSize = AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE;
    ASSERT_EQ(expectedBitSize, autoIndexedOffsetArray.initializeOffsets(bitPosition));
    checkAutoIndexedOffsetArray(autoIndexedOffsetArray);
}

TEST_F(AutoIndexedOffsetArrayTest, initializeOffsetsWithPosition)
{
    const bool createWrongOffsets = true;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    const size_t bitPosition = 9;
    const size_t expectedBitSize = AUTO_INDEXED_OFFSET_ARRAY_BIT_SIZE + bitPosition - 1;
    ASSERT_EQ(expectedBitSize, autoIndexedOffsetArray.initializeOffsets(bitPosition));

    const uint16_t offsetShift = 1;
    checkOffsets(autoIndexedOffsetArray, offsetShift);
}

TEST_F(AutoIndexedOffsetArrayTest, write)
{
    const bool createWrongOffsets = true;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    zserio::BitStreamWriter writer;
    autoIndexedOffsetArray.write(writer);
    checkAutoIndexedOffsetArray(autoIndexedOffsetArray);

    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);
    AutoIndexedOffsetArray readAutoIndexedOffsetArray(reader);
    checkAutoIndexedOffsetArray(readAutoIndexedOffsetArray);
    ASSERT_TRUE(autoIndexedOffsetArray == readAutoIndexedOffsetArray);
}

TEST_F(AutoIndexedOffsetArrayTest, writeWithPosition)
{
    const bool createWrongOffsets = true;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    zserio::BitStreamWriter writer;
    const size_t bitPosition = 8;
    writer.writeBits(0, bitPosition);
    autoIndexedOffsetArray.write(writer);

    const uint16_t offsetShift = 1;
    checkOffsets(autoIndexedOffsetArray, offsetShift);
}

TEST_F(AutoIndexedOffsetArrayTest, writeWrongOffsets)
{
    const bool createWrongOffsets = true;
    AutoIndexedOffsetArray autoIndexedOffsetArray;
    fillAutoIndexedOffsetArray(autoIndexedOffsetArray, createWrongOffsets);

    zserio::BitStreamWriter writer;
    ASSERT_THROW(autoIndexedOffsetArray.write(writer, zserio::NO_PRE_WRITE_ACTION),
            zserio::CppRuntimeException);
}

} // namespace auto_indexed_offset_array
} // namespace indexed_offsets
