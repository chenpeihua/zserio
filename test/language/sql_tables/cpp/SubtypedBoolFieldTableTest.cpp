#include <cstdio>
#include <string>
#include <fstream>

#include "gtest/gtest.h"

#include "sql_tables/TestDb.h"

namespace sql_tables
{
namespace subtyped_bool_field_table
{

class SubtypedBoolFieldTableTest : public ::testing::Test
{
public:
    SubtypedBoolFieldTableTest()
    {
        std::remove(DB_FILE_NAME);

        m_database = new sql_tables::TestDb(DB_FILE_NAME);
        m_database->createSchema();
    }

    ~SubtypedBoolFieldTableTest()
    {
        delete m_database;
    }

protected:
    static void fillRow(SubtypedBoolFieldTable::Row& row, size_t i)
    {
        row.setId(static_cast<int32_t>(i));
        row.setBoolField(i % 2 == 0 ? true : false);
    }

    static void fillRows(std::vector<SubtypedBoolFieldTable::Row>& rows)
    {
        rows.clear();
        for (size_t i = 0; i < NUM_ROWS; ++i)
        {
            SubtypedBoolFieldTable::Row row;
            fillRow(row, i);
            rows.push_back(row);
        }
    }

    static void checkRow(const SubtypedBoolFieldTable::Row& row1, const SubtypedBoolFieldTable::Row& row2)
    {
        ASSERT_EQ(row1.getId(), row2.getId());
        ASSERT_EQ(row1.getBoolField(), row2.getBoolField());
    }

    static void checkRows(const std::vector<SubtypedBoolFieldTable::Row>& rows1,
            const std::vector<SubtypedBoolFieldTable::Row>& rows2)
    {
        ASSERT_EQ(rows1.size(), rows2.size());
        for (size_t i = 0; i < rows1.size(); ++i)
            checkRow(rows1[i], rows2[i]);
    }

    static const char DB_FILE_NAME[];
    static const size_t NUM_ROWS;

    sql_tables::TestDb* m_database;
};

const char SubtypedBoolFieldTableTest::DB_FILE_NAME[] = "subtyped_bool_field_table_test.sqlite";
const size_t SubtypedBoolFieldTableTest::NUM_ROWS = 5;

TEST_F(SubtypedBoolFieldTableTest, readWithoutCondition)
{
    SubtypedBoolFieldTable& table = m_database->getSubtypedBoolFieldTable();

    std::vector<SubtypedBoolFieldTable::Row> rows;
    fillRows(rows);
    table.write(rows);

    std::vector<SubtypedBoolFieldTable::Row> readRows;
    auto reader = table.createReader();
    while (reader.hasNext())
        readRows.push_back(reader.next());

    checkRows(rows, readRows);
}

} // namespace subtyped_bool_field_table
} // namespace sql_tables
