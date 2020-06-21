#ifndef ZSERIO_BIT_BUFFER_H_INC
#define ZSERIO_BIT_BUFFER_H_INC

#include <vector>
#include <cstddef>

#include "zserio/Types.h"

namespace zserio
{

/**
 * Class which holds any bit sequence.
 *
 * Because bit buffer size does not have to be byte aligned (divisible by 8), it's possible that not all bits
 * of the last byte are used. In this case, only most significant bits of the corresponded size are used.
 */
class BitBuffer
{
public:
    /**
     * Empty constructor.
     */
    BitBuffer();

    /**
     * Constructor from bit size.
     *
     * \param bitSize Size in bits of created bit buffer.
     */
    explicit BitBuffer(size_t bitSize);

    /**
     * Constructor from STL vector.
     *
     * \param buffer STL vector of bytes from which the bit buffer should be created.
     */
    explicit BitBuffer(const std::vector<uint8_t>& buffer);

    /**
     * Constructor from STL vector and bit size.
     *
     * \param buffer STL vector of bytes from which the bit buffer should be created.
     * \param bitSize Number of bits stored in buffer to use.
     *
     * \throw CppRuntimeException If given bit size is out of range for given vector.
     */
    explicit BitBuffer(const std::vector<uint8_t>& buffer, size_t bitSize);

    /**
     * Constructor from moved STL vector.
     *
     * \param buffer STL vector of bytes from which the bit buffer should be created.
     */
    explicit BitBuffer(std::vector<uint8_t>&& buffer);

    /**
     * Constructor from moved STL vector and bit size.
     *
     * \param buffer STL vector of bytes from which the bit buffer should be created.
     * \param bitSize Number of bits stored in buffer to use.
     *
     * \throw CppRuntimeException If given bit size is out of range for given vector.
     */
    explicit BitBuffer(std::vector<uint8_t>&& buffer, size_t bitSize);

    /**
     * Constructor from raw pointer.
     *
     * \param buffer Raw pointer to all bytes from which the bit buffer should be created.
     * \param bitSize Number of bits stored in buffer to use.
     */
    explicit BitBuffer(const uint8_t* buffer, size_t bitSize);

    /**
     * Method generated by default.
     * \{
     */
    ~BitBuffer() = default;

    BitBuffer(const BitBuffer&) = default;
    BitBuffer& operator=(const BitBuffer&) = default;

    BitBuffer(BitBuffer&&) = default;
    BitBuffer& operator=(BitBuffer&&) = default;
    /**
     * \}
     */

    /**
     * Equal operator.
     *
     * \param other The another instance of bit buffer to which compare this bit buffer.
     */
    bool operator==(const BitBuffer& other) const;

    /**
     * Calculates hash code of the bit buffer.
     *
     * \return Calculated hash code.
     */
    int hashCode() const;

    /**
     * Gets the underlying buffer.
     *
     * \return Pointer to the constant underlying buffer.
     */
    const uint8_t* getBuffer() const;

    /**
     * Gets the underlying buffer.
     *
     * \return Pointer to the underlying buffer.
     */
    uint8_t* getBuffer();

    /**
     * Gets the number of bits stored in the bit buffer.
     *
     * \return Bit buffer size in bits.
     */
    size_t getBitSize() const;

    /**
     * Gets the number of bytes stored in the bit buffer.
     *
     * Not all bits of the last byte must be used. Unused bits of the last byte are set to zero.
     *
     * \return Bit buffer size in bytes.
     */
    size_t getByteSize() const;

private:
    uint8_t getMaskedLastByte() const;

    std::vector<uint8_t> m_buffer;
    size_t m_bitSize;
};

} // namespace zserio

#endif // ifndef ZSERIO_BIT_BUFFER_H_INC
