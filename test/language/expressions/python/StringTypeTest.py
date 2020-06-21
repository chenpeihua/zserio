import unittest

from testutils import getZserioApi

class StringTypeTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "expressions.zs").string_type

    def testAppend(self):
        stringTypeExpression = self.api.StringTypeExpression.fromFields(self.VALUE)
        self.assertEqual(self.VALUE, stringTypeExpression.funcReturnValue())
        self.assertEqual("appendix", stringTypeExpression.funcAppendix())
        self.assertEqual(self.api.STRING_CONSTANT + "_appendix", stringTypeExpression.funcAppendToConst())

    VALUE = "value"
