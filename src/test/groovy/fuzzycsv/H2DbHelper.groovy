package fuzzycsv

import fuzzycsv.rdbms.DDLUtils
import groovy.sql.Sql
import org.h2.jdbcx.JdbcConnectionPool

import javax.sql.DataSource
import java.sql.Connection

class H2DbHelper {

    static Sql getConnection() {
        Sql.newInstance('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false')
    }

    static Sql getMySqlConnection() {

        def dbURL = 'jdbc:mysql://127.0.0.1:3306/playground'
        def dbUserName = 'root'
        def dbPassword = 'pass'
        def dbDriver = 'com.mysql.cj.jdbc.Driver'
        return Sql.newInstance(dbURL, dbUserName, dbPassword, dbDriver)
    }

    static JdbcConnectionPool getDataSource() {
        //use h2 datasource
        return JdbcConnectionPool.create('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', 'sa', '')
    }

    static void dropAllAndDispose(JdbcConnectionPool ds) {
         ds.connection.withCloseable { conn ->
             dropAllTables(conn)
         }
        ds.dispose()
    }
    static void dropAllTables(Connection connection) {
        Sql gsql = new Sql(connection)
        DDLUtils.allTables(connection, null)
                .filter { it.TABLE_TYPE == 'TABLE' }
                .each {
                    println("Dropping******** $it.TABLE_NAME")
                    gsql.execute("drop table $it.TABLE_NAME" as String)
                }

    }
}