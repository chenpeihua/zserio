import unittest
import os

from testutils import getZserioApi, getApiDir

class BlobFieldWithChildrenInitializationTableTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "sql_tables.zs")
        cls._fileName = os.path.join(getApiDir(os.path.dirname(__file__)),
                                     "blob_field_with_children_initialization_table_test.sqlite")

    def setUp(self):
        if os.path.exists(self._fileName):
            os.remove(self._fileName)
        self._database = self.api.TestDb.fromFile(self._fileName)
        self._database.createSchema()

    def tearDown(self):
        self._database.close()

    def testReadWithoutCondition(self):
        testTable = self._database.getBlobFieldWithChildrenInitializationTable()

        writtenRows = self._createRows()
        testTable.write(writtenRows)

        readRows = testTable.read()
        numReadRows = 0
        for readRow in readRows:
            self.assertEqual(writtenRows[numReadRows], readRow)
            numReadRows += 1
        self.assertTrue(len(writtenRows), numReadRows)

    def _createRows(self):
        rows = []
        for i in range(self.NUM_ROWS):
            rows.append(self._createRow(i))

        return rows

    def _createRow(self, i):
        testApi = self.api.blob_field_with_children_initialization_table
        parameterizedArray = testApi.ParameterizedArray.fromFields(i, list(range(i)))
        return (i, testApi.BlobWithChildrenInitialization.fromFields(i, parameterizedArray))

    NUM_ROWS = 5
