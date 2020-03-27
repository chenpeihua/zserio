/**
 * Automatically generated by Zserio C++ extension version 2.0.0-pre1.
 */

#ifndef PMR_POC_SAMPLE_STRUCT_H
#define PMR_POC_SAMPLE_STRUCT_H

#include <zserio/BitStreamReader.h>
#include <zserio/BitStreamWriter.h>
#include <zserio/PreWriteAction.h>
#include <zserio/pmr/String.h>
#include <zserio/Types.h>

#include <pmr_poc/ChildStruct.h>

namespace pmr_poc
{

template <typename ALLOC = std::allocator<void>>
class SampleStruct
{
public:
    explicit SampleStruct(::zserio::BitStreamReader& in, const ALLOC& allocator = ALLOC());

    ~SampleStruct() = default;

    SampleStruct(const SampleStruct&) = default;
    SampleStruct& operator=(const SampleStruct&) = default;

    SampleStruct(SampleStruct&&) = default;
    SampleStruct& operator=(SampleStruct&&) = default;

    uint8_t getUint8Field() const;

    const std::basic_string<char, std::char_traits<char>, ::zserio::RebindAlloc<ALLOC, char>>&
    getStringField() const;

    const ::pmr_poc::ChildStruct<ALLOC>& getChildField() const;

    size_t bitSizeOf(size_t bitPosition = 0) const;

    bool operator==(const SampleStruct& other) const;
    int hashCode() const;

private:
    uint8_t readUint8Field(::zserio::BitStreamReader& in);
    std::basic_string<char, std::char_traits<char>, ::zserio::RebindAlloc<ALLOC, char>> readStringField(
                ::zserio::BitStreamReader& in, const ALLOC& allocator);
    ::pmr_poc::ChildStruct<ALLOC> readChildField(::zserio::BitStreamReader& in, const ALLOC& allocator);

    uint8_t m_uint8Field_;
    std::basic_string<char, std::char_traits<char>, ::zserio::RebindAlloc<ALLOC, char>> m_stringField_;
    ::pmr_poc::ChildStruct<ALLOC> m_childField_;
};

} // namespace pmr_poc

#include "SampleStruct.hpp"

#endif // PMR_POC_SAMPLE_STRUCT_H
