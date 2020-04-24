package liquibase.ext;

import liquibase.change.DatabaseChange;
import liquibase.change.core.CreateTableChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import org.apache.commons.lang3.ArrayUtils;

@DatabaseChange(
        name = "inheritTable",
        description = "Inherit Table",
        priority = 1
)
public class InheritTable extends CreateTableChange {
    private String inheritsFrom;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {new InheritTableStatement(getCatalogName(), getSchemaName(), getTableName(), getInheritsFrom()) };
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    private static class InheritTableStatement extends CreateTableStatement {
        private final String parent;

        public InheritTableStatement(String catalogName, String schemaName, String tableName, String parent) {
            super(catalogName, schemaName, tableName);
            this.parent = parent;
        }

        public String getParent() {
            return parent;
        }
    }

    public static class InheritTableGenerator implements SqlGenerator<InheritTableStatement> {
        private final CreateTableGenerator createTable = new CreateTableGenerator();

        public InheritTableGenerator() {
            super();
        }

        @Override
        public int getPriority() {
            return createTable.getPriority();
        }

        @Override
        public boolean supports(InheritTableStatement inheritTableStatement, Database database) {
            return createTable.supports(inheritTableStatement, database);
        }

        @Override
        public boolean generateStatementsIsVolatile(Database database) {
            return createTable.generateStatementsIsVolatile(database);
        }

        @Override
        public boolean generateRollbackStatementsIsVolatile(Database database) {
            return createTable.generateRollbackStatementsIsVolatile(database);
        }

        @Override
        public ValidationErrors validate(InheritTableStatement inheritTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
            ValidationErrors validationErrors = createTable.validate(inheritTableStatement, database, sqlGeneratorChain);
            validationErrors.checkRequiredField("inheritsFrom", inheritTableStatement.getParent());
            return validationErrors;
        }

        @Override
        public Warnings warn(InheritTableStatement inheritTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
            return createTable.warn(inheritTableStatement, database, sqlGeneratorChain);
        }

        @Override
        public Sql[] generateSql(InheritTableStatement inheritTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
            return ArrayUtils.add(
                    createTable.generateSql(inheritTableStatement, database, sqlGeneratorChain),
                    null
            );
        }
    }
}
