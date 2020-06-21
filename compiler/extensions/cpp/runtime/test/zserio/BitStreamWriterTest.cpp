#include <cstring>

#include "zserio/BitStreamWriter.h"
#include "zserio/CppRuntimeException.h"

#include "gtest/gtest.h"

namespace zserio
{

class BitStreamWriterTest : public ::testing::Test
{
public:
    BitStreamWriterTest() :
        m_writer(),
        m_externalBufferWriter(m_externalBuffer, externalBufferSize),
        m_dummyBufferWriter(NULL, 0)
    {
        memset(m_externalBuffer, 0, sizeof(m_externalBuffer) / sizeof(m_externalBuffer[0]));
    }

protected:
    BitStreamWriter m_writer;
    BitStreamWriter m_externalBufferWriter;
    BitStreamWriter m_dummyBufferWriter;

    static const size_t externalBufferSize = 512;

    uint8_t m_externalBuffer[externalBufferSize];
};

TEST_F(BitStreamWriterTest, bitBufferConstructor)
{
    BitBuffer bitBuffer(11);
    BitStreamWriter writer(bitBuffer);
    size_t writeBufferByteSize = 0;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    ASSERT_EQ(bitBuffer.getBuffer(), writeBuffer);
    ASSERT_EQ(bitBuffer.getByteSize(), writeBufferByteSize);

    writer.writeBits(0x1F, 5);
    writer.writeBits(0x07, 3);
    ASSERT_EQ(0xFF, writeBuffer[0]);
    ASSERT_THROW(writer.writeBits(0x0F, 4), CppRuntimeException);
    writer.writeBits(0x07, 3);
    ASSERT_EQ(0xE0, writeBuffer[1]);
}

TEST_F(BitStreamWriterTest, writeUnalignedData)
{
    // number expected to be written at offset
    const uint8_t testValue = 123;

    for (int offset = 0; offset <= 64; ++offset)
    {
        BitBuffer bitBuffer(8 + offset);
        // fill the buffer with 1s to check proper masking
        std::memset(bitBuffer.getBuffer(), 0xFF, bitBuffer.getByteSize());

        BitStreamWriter writer(bitBuffer);

        writer.writeBits64(0, offset);
        writer.writeBits(testValue, 8);

        // check eof
        ASSERT_THROW(writer.writeBits64(0, 1), CppRuntimeException);

        // check written value
        uint8_t writtenTestValue = bitBuffer.getBuffer()[offset / 8] << (offset % 8);
        if (offset % 8 != 0)
            writtenTestValue |= bitBuffer.getBuffer()[offset / 8 + 1] >> (8 - (offset % 8));
        ASSERT_EQ(testValue, writtenTestValue) << "Offset: " << offset;
    }
}

TEST_F(BitStreamWriterTest, writeBits)
{
    // check invalid bitlength acceptance
    const uint8_t numBits[] = { 255, 0, 33 };
    for (size_t i = 0; i < sizeof(numBits) / sizeof(numBits[0]); ++i)
    {
        ASSERT_THROW(m_writer.writeBits(1, numBits[i]), CppRuntimeException);
    }

    // check value out of range
    for (int i = 1; i < 32; ++i)
    {
        const int maxUnsigned = (1L << i) - 1;
        m_writer.writeBits(maxUnsigned, static_cast<uint8_t>(i));

        const int maxUnsignedViolation = maxUnsigned + 1;
        ASSERT_THROW(m_writer.writeBits(maxUnsignedViolation, static_cast<uint8_t>(i)), CppRuntimeException);
    }
}

TEST_F(BitStreamWriterTest, writeBits64)
{
    // check invalid bitlength acceptance
    const uint8_t numBits[] = { 255, 0, 65 };
    for (size_t i = 0; i < sizeof(numBits) / sizeof(numBits[0]); ++i)
    {
        ASSERT_THROW(m_writer.writeBits64(1, numBits[i]), CppRuntimeException);
    }

    // check value out of range
    for (int i = 1; i < 64; ++i)
    {
        const int64_t maxUnsigned = (((int64_t) 1) << i) - 1;
        m_writer.writeBits64(maxUnsigned, static_cast<uint8_t>(i));

        const int64_t maxUnsignedViolation = maxUnsigned + 1;
        ASSERT_THROW(m_writer.writeBits64(maxUnsignedViolation, static_cast<uint8_t>(i)), CppRuntimeException);
    }
}

TEST_F(BitStreamWriterTest, writeSignedBits)
{
    // check invalid bitlength acceptance
    const uint8_t numBits[] = { 255, 0, 33 };
    for (size_t i = 0; i < sizeof(numBits) / sizeof(numBits[0]); ++i)
    {
        ASSERT_THROW(m_writer.writeSignedBits(1, numBits[i]), CppRuntimeException);
    }

    // check value out of range
    for (int i = 1; i < 32; ++i)
    {
        const int32_t minSigned = -( 1L << ( i - 1 ) );
        const int32_t maxSigned =  ( 1L << ( i - 1 ) ) - 1;
        m_writer.writeSignedBits(minSigned, static_cast<uint8_t>(i));
        m_writer.writeSignedBits(maxSigned, static_cast<uint8_t>(i));

        const int32_t minSignedViolation = minSigned - 1;
        const int32_t maxSignedViolation = maxSigned + 1;
        ASSERT_THROW(m_writer.writeSignedBits(minSignedViolation, static_cast<uint8_t>(i)),
                CppRuntimeException);
        ASSERT_THROW(m_writer.writeSignedBits(maxSignedViolation, static_cast<uint8_t>(i)),
                CppRuntimeException);
    }
}

TEST_F(BitStreamWriterTest, writeSignedBits64)
{
    // check invalid bitlength acceptance
    const uint8_t numBits[] = { 255, 0, 65 };
    for (size_t i = 0; i < sizeof(numBits) / sizeof(numBits[0]); ++i)
    {
        ASSERT_THROW(m_writer.writeSignedBits64(1, numBits[i]), CppRuntimeException);
    }

    // check value out of range
    for (int i = 1; i < 64; ++i)
    {
        const int64_t minSigned = -( ((int64_t) 1) << ( i - 1 ) );
        const int64_t maxSigned =  ( ((int64_t) 1) << ( i - 1 ) ) - 1;
        m_writer.writeSignedBits64(minSigned, static_cast<uint8_t>(i));
        m_writer.writeSignedBits64(maxSigned, static_cast<uint8_t>(i));

        const int64_t minSignedViolation = minSigned - 1;
        const int64_t maxSignedViolation = maxSigned + 1;
        ASSERT_THROW(m_writer.writeSignedBits64(minSignedViolation, static_cast<uint8_t>(i)),
                CppRuntimeException);
        ASSERT_THROW(m_writer.writeSignedBits64(maxSignedViolation, static_cast<uint8_t>(i)),
                CppRuntimeException);
    }
}

TEST_F(BitStreamWriterTest, writeVarInt64)
{
    // check value out of range
    const int64_t outOfRangeValue = ((int64_t) 1) << ( 6 + 7 + 7 + 7 + 7 + 7 + 7 + 8);
    ASSERT_THROW(m_writer.writeVarInt64(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeVarInt32)
{
    // check value out of range
    const int32_t outOfRangeValue = ((int32_t) 1) << (6 + 7 + 7 + 8);
    ASSERT_THROW(m_writer.writeVarInt32(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeVarInt16)
{
    // check value out of range
    const int16_t outOfRangeValue = ((int16_t) 1) << (6 + 8);
    ASSERT_THROW(m_writer.writeVarInt16(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeVarUInt64)
{
    // check value out of range
    const uint64_t outOfRangeValue = ((uint64_t) 1) << (7 + 7 + 7 + 7 + 7 + 7 + 7 + 8);
    ASSERT_THROW(m_writer.writeVarUInt64(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeVarUInt32)
{
    // check value out of range
    const uint32_t outOfRangeValue = ((uint32_t) 1) << (7 + 7 + 7 + 8);
    ASSERT_THROW(m_writer.writeVarUInt32(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeVarUInt16)
{
    // check value out of range
    const uint16_t outOfRangeValue = ((uint16_t) 1) << (7 + 8);
    ASSERT_THROW(m_writer.writeVarUInt16(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeVarInt)
{
    ASSERT_NO_THROW(m_writer.writeVarInt(INT64_MIN));
    ASSERT_NO_THROW(m_writer.writeVarInt(INT64_MAX));
}

TEST_F(BitStreamWriterTest, writeVarUInt)
{
    ASSERT_NO_THROW(m_writer.writeVarUInt(0));
    ASSERT_NO_THROW(m_writer.writeVarUInt(UINT64_MAX));
}

TEST_F(BitStreamWriterTest, writeVarSize)
{
    // check value out of range
    const uint32_t outOfRangeValue = ((uint32_t) 1) << (2 + 7 + 7 + 7 + 8);
    ASSERT_THROW(m_writer.writeVarSize(outOfRangeValue), CppRuntimeException);
}

TEST_F(BitStreamWriterTest, writeBitBuffer)
{
    static const size_t bitBufferBitSize = 24;
    BitBuffer bitBuffer(std::vector<uint8_t>{0xAB, 0xAB, 0xAB}, bitBufferBitSize);

    {
        ASSERT_NO_THROW(m_writer.writeBitBuffer(bitBuffer));
        size_t bufferSize = 0;
        const uint8_t* buffer = m_writer.getWriteBuffer(bufferSize);
        ASSERT_EQ(bitBufferBitSize / 8, bufferSize - 1); // first byte is bit buffer size
        BitBuffer readBitBuffer{buffer + 1, bitBufferBitSize};
        ASSERT_EQ(bitBuffer, readBitBuffer);
    }

    {
        ASSERT_NO_THROW(m_externalBufferWriter.writeBitBuffer(bitBuffer));
        size_t bufferSize = 0;
        const uint8_t* buffer = m_externalBufferWriter.getWriteBuffer(bufferSize);
        BitBuffer readBitBuffer{buffer + 1, bitBufferBitSize}; // first byte is bit buffer size
        ASSERT_EQ(bitBuffer, readBitBuffer);
    }

    ASSERT_NO_THROW(m_dummyBufferWriter.writeBitBuffer(bitBuffer));
    ASSERT_EQ(bitBufferBitSize + 8, m_dummyBufferWriter.getBitPosition()); // first byte is bit buffer size
}

TEST_F(BitStreamWriterTest, hasWriteBuffer)
{
    ASSERT_TRUE(m_writer.hasWriteBuffer());
    ASSERT_TRUE(m_externalBufferWriter.hasWriteBuffer());
    ASSERT_FALSE(m_dummyBufferWriter.hasWriteBuffer());
}

TEST_F(BitStreamWriterTest, getWriteBuffer)
{
    size_t writeBufferByteSize;
    ASSERT_EQ(NULL, m_writer.getWriteBuffer(writeBufferByteSize));
    ASSERT_EQ(0, writeBufferByteSize);

    ASSERT_EQ(m_externalBuffer, m_externalBufferWriter.getWriteBuffer(writeBufferByteSize));
    const size_t expectedWriteBufferByteSize = externalBufferSize;
    ASSERT_EQ(expectedWriteBufferByteSize, writeBufferByteSize);

    ASSERT_EQ(NULL, m_dummyBufferWriter.getWriteBuffer(writeBufferByteSize));
    ASSERT_EQ(0, writeBufferByteSize);
}

TEST_F(BitStreamWriterTest, externalBufferTest)
{
    m_externalBufferWriter.writeBits(1, 1);
    m_externalBufferWriter.alignTo(4);
    m_externalBufferWriter.writeBits(1, 1);
    m_externalBufferWriter.alignTo(4);
    m_externalBufferWriter.writeBits(37, 11);
    m_externalBufferWriter.alignTo(8);
    m_externalBufferWriter.writeBits(1, 1);
    ASSERT_EQ(25, m_externalBufferWriter.getBitPosition());
}

TEST_F(BitStreamWriterTest, dummyBufferTest)
{
    m_dummyBufferWriter.writeBits(1, 1);
    m_dummyBufferWriter.alignTo(4);
    m_dummyBufferWriter.writeBits(1, 1);
    m_dummyBufferWriter.alignTo(4);
    m_dummyBufferWriter.writeBits(37, 11);
    m_dummyBufferWriter.alignTo(8);
    m_dummyBufferWriter.writeBits(1, 1);
    ASSERT_EQ(25, m_dummyBufferWriter.getBitPosition());
}

} // namespace zserio
