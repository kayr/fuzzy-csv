package fuzzycsv

import groovy.sql.Sql
import org.h2.jdbcx.JdbcConnectionPool

import javax.sql.DataSource

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

    static DataSource getDataSource() {
        //use h2 datasource
        return JdbcConnectionPool.create('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false', 'sa', '')
    }
}