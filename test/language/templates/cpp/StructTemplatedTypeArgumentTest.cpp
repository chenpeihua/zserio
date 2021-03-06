#include "gtest/gtest.h"

#include "templates/struct_templated_type_argument/StructTemplatedTypeArgument.h"

namespace templates
{
namespace struct_templated_type_argument
{

TEST(StructTemplatedTypeArgumentTest, readWrite)
{
    StructTemplatedTypeArgument structTemplatedTypeArgument;
    structTemplatedTypeArgument.setParamHolder(ParamHolder_uint32{42});
    structTemplatedTypeArgument.setParameterized(Parameterized_uint32{std::string{"description"}, 13});

    zserio::BitStreamWriter writer;
    structTemplatedTypeArgument.write(writer);
    size_t bufferSize = 0;
    const uint8_t* buffer = writer.getWriteBuffer(bufferSize);

    zserio::BitStreamReader reader(buffer, bufferSize);
    StructTemplatedTypeArgument readStructTemplatedTypeArgument(reader);

    ASSERT_TRUE(structTemplatedTypeArgument == readStructTemplatedTypeArgument);
}

} // namespace struct_templated_type_argument
} // namespace templates
