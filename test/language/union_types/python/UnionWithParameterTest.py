import unittest
import zserio

from testutils import getZserioApi

class UnionWithParameterTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "union_types.zs").union_with_parameter

    def testParamConstructor(self):
        testUnion = self.api.TestUnion(True)
        self.assertTrue(testUnion.getCase1Allowed())

        testUnion.setCase1Field(11)
        writer = zserio.BitStreamWriter()
        testUnion.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readTestUnion = self.api.TestUnion.fromReader(reader, True)
        self.assertEqual(testUnion.getCase1Allowed(), readTestUnion.getCase1Allowed())
        self.assertEqual(testUnion.getCase1Field(), readTestUnion.getCase1Field())

    def testParamConstructorCase1Forbidden(self):
        testUnion = self.api.TestUnion(False)
        self.assertFalse(testUnion.getCase1Allowed())

        testUnion.setCase1Field(11)
        writer = zserio.BitStreamWriter()
        with self.assertRaises(zserio.PythonRuntimeException):
            testUnion.write(writer) # raises exception

    def testFromReader(self):
        testUnion = self.api.TestUnion(True)
        case3FieldValue = -1
        testUnion.setCase3Field(case3FieldValue)
        writer = zserio.BitStreamWriter()
        testUnion.write(writer)

        reader = zserio.BitStreamReader(writer.getByteArray())
        readTestUnion = self.api.TestUnion.fromReader(reader, True)
        self.assertEqual(testUnion.choiceTag(), readTestUnion.choiceTag())
        self.assertEqual(testUnion.getCase3Field(), readTestUnion.getCase3Field())
        self.assertEqual(case3FieldValue, readTestUnion.getCase3Field())
