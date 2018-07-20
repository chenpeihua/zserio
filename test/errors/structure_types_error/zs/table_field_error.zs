package table_field_error;

sql_table TestTable
{
    int32       schoolId;
    int32       classId;
    int32       studentId;

    sql "PRIMARY KEY (schoolId)";
};

struct TableFieldError
{
    // This must cause an error.
    TestTable   testTable;
};
