package zserio.runtime.array;

import java.io.IOException;

import zserio.runtime.ZserioError;
import zserio.runtime.io.BitStreamReader;

/**
 * Interface used by {@link ObjectArray ObjectArray&lt;E&gt;} to construct elements from a stream.
 *
 * @param <E> Type of the elements.
 */
public interface ElementFactory<E>
{
   /**
     * Creates array elements from bit stream.
     *
     * @param reader  Bit stream to read from.
     * @param index Index of element to create.
     *
     * @return Created element.
     *
     * @throws IOException Failure during bit stream manipulation.
     * @throws ZserioError Failure during element creation.
     */
    E create(BitStreamReader reader, int index) throws IOException, ZserioError;
}
