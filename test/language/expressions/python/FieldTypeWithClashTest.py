import unittest

from testutils import getZserioApi

class FieldTypeWithClashTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "expressions.zs").field_type_with_clash

    def testBitSizeOfWithOptional(self):
        containedType = self.api.ContainedType.fromFields(True)
        fieldTypeExpression = self.api.FieldTypeExpression.fromFields(containedType, self.EXTRA_VALUE)
        self.assertEqual(self.COMPOUND_TYPE_EXPRESSION_BIT_SIZE_WITH_OPTIONAL, fieldTypeExpression.bitSizeOf())

    def testBitSizeOfWithoutOptional(self):
        containedType = self.api.ContainedType.fromFields(False)
        fieldTypeExpression = self.api.FieldTypeExpression()
        fieldTypeExpression.setContainedType(containedType)
        self.assertEqual(self.COMPOUND_TYPE_EXPRESSION_BIT_SIZE_WITHOUT_OPTIONAL,
                         fieldTypeExpression.bitSizeOf())

    COMPOUND_TYPE_EXPRESSION_BIT_SIZE_WITH_OPTIONAL = 4
    COMPOUND_TYPE_EXPRESSION_BIT_SIZE_WITHOUT_OPTIONAL = 1

    EXTRA_VALUE = 0x02
