#include "gtest/gtest.h"

#include "array_types/variable_array_varuint/VariableArray.h"

#include "zserio/BitStreamWriter.h"
#include "zserio/BitStreamReader.h"
#include "zserio/StringConvertUtil.h"
#include "zserio/CppRuntimeException.h"

namespace array_types
{
namespace variable_array_varuint
{

class VariableArrayVarUIntTest : public ::testing::Test
{
protected:
    void writeVariableArrayToByteArray(zserio::BitStreamWriter& writer, size_t numElements)
    {
        writer.writeBits64(static_cast<uint64_t>(numElements), 8);
        for (size_t i = 0; i < numElements; ++i)
        {
            writer.writeBits(static_cast<uint32_t>(i), 32);
            writer.writeString(std::string("Name") + zserio::convertToString(i));
        }
    }
};

TEST_F(VariableArrayVarUIntTest, bitSizeOf)
{
    const size_t numElements = 33;
    std::vector<TestStructure> compoundArray;
    compoundArray.reserve(numElements);
    for (size_t i = 0; i < numElements; ++i)
    {
        TestStructure testStructure;
        testStructure.setId(static_cast<uint32_t>(i));
        testStructure.setName(std::string("Name") + zserio::convertToString(i));
        compoundArray.push_back(testStructure);
    }
    VariableArray variableArray;
    variableArray.setNumElements(static_cast<uint64_t>(numElements));
    variableArray.setCompoundArray(compoundArray);

    const size_t bitPosition = 2;
    const size_t numOneNumberIndexes = 10; // (numElements > 9) ? 10 : numElements
    const size_t expectedBitSize = (1 + numElements * (4 + 7) - numOneNumberIndexes) * 8;
    ASSERT_EQ(expectedBitSize, variableArray.bitSizeOf(bitPosition));
}

TEST_F(VariableArrayVarUIntTest, initializeOffsets)
{
    const size_t numElements = 33;
    std::vector<TestStructure> compoundArray;
    compoundArray.reserve(numElements);
    for (size_t i = 0; i < numElements; ++i)
    {
        TestStructure testStructure;
        testStructure.setId(static_cast<uint32_t>(i));
        testStructure.setName(std::string("Name") + zserio::convertToString(i));
        compoundArray.push_back(testStructure);
    }
    VariableArray variableArray;
    variableArray.setNumElements(static_cast<uint64_t>(numElements));
    variableArray.setCompoundArray(compoundArray);

    const size_t bitPosition = 2;
    const size_t numOneNumberIndexes = 10; // (numElements > 9) ? 10 : numElements
    const size_t expectedEndBitPosition = bitPosition + (1 + numElements * (4 + 7) - numOneNumberIndexes) * 8;
    ASSERT_EQ(expectedEndBitPosition, variableArray.initializeOffsets(bitPosition));
}

TEST_F(VariableArrayVarUIntTest, read)
{
    const size_t numElements = 59;
    zserio::BitStreamWriter writer;
    writeVariableArrayToByteArray(writer, numElements);
    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);
    VariableArray variableArray(reader);

    ASSERT_EQ(numElements, static_cast<size_t>(variableArray.getNumElements()));
    const std::vector<TestStructure>& compoundArray = variableArray.getCompoundArray();
    ASSERT_EQ(numElements, compoundArray.size());
    for (size_t i = 0; i < numElements; ++i)
    {
        ASSERT_EQ(i, compoundArray[i].getId());
        ASSERT_EQ(std::string("Name") + zserio::convertToString(i), compoundArray[i].getName());
    }
}

TEST_F(VariableArrayVarUIntTest, write)
{
    const size_t numElements = 33;
    std::vector<TestStructure> compoundArray;
    compoundArray.reserve(numElements);
    for (size_t i = 0; i < numElements; ++i)
    {
        TestStructure testStructure;
        testStructure.setId(static_cast<uint32_t>(i));
        testStructure.setName(std::string("Name") + zserio::convertToString(i));
        compoundArray.push_back(testStructure);
    }
    VariableArray variableArray;
    variableArray.setNumElements(static_cast<uint64_t>(numElements));
    variableArray.setCompoundArray(compoundArray);

    zserio::BitStreamWriter writer;
    variableArray.write(writer);

    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);
    VariableArray readVariableArray(reader);
    const std::vector<TestStructure>& readCompoundArray = readVariableArray.getCompoundArray();
    ASSERT_EQ(numElements, readCompoundArray.size());
    for (size_t i = 0; i < numElements; ++i)
    {
        ASSERT_EQ(i, readCompoundArray[i].getId());
        ASSERT_EQ(std::string("Name") + zserio::convertToString(i), readCompoundArray[i].getName());
    }
}

TEST_F(VariableArrayVarUIntTest, writeWrongArray)
{
    const size_t numElements = 33;
    std::vector<TestStructure> compoundArray;
    compoundArray.reserve(numElements);
    for (size_t i = 0; i < numElements; ++i)
    {
        TestStructure testStructure;
        testStructure.setId(static_cast<uint32_t>(i));
        testStructure.setName(std::string("Name") + zserio::convertToString(i));
        compoundArray.push_back(testStructure);
    }
    VariableArray variableArray;
    variableArray.setNumElements(static_cast<uint64_t>(numElements + 1));
    variableArray.setCompoundArray(compoundArray);

    zserio::BitStreamWriter writer;
    ASSERT_THROW(variableArray.write(writer), zserio::CppRuntimeException);
}

} // namespace variable_array_varuint
} // namespace array_types
