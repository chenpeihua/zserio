import unittest
import zserio

from testutils import getZserioApi

class UnionWithParameterizedFieldTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "union_types.zs").union_with_parameterized_field

    def testEmptyConstructor(self):
        testUnion = self.api.TestUnion()
        testUnion.setArrayHolder(self.api.ArrayHolder(10))
        self.assertEqual(10, testUnion.getArrayHolder().getSize())

    def testFromReader(self):
        testUnion = self.api.TestUnion()
        testUnion.setArrayHolder(self.api.ArrayHolder.fromFields(10, list(range(10))))
        writer = zserio.BitStreamWriter()
        testUnion.write(writer)

        reader = zserio.BitStreamReader(writer.getByteArray())
        readTestUnion = self.api.TestUnion.fromReader(reader)
        self.assertEqual(10, readTestUnion.getArrayHolder().getSize())
