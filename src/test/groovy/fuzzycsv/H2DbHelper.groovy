package fuzzycsv

import groovy.sql.Sql

class H2DbHelper {

    static Sql getConnection() {
        Sql.newInstance('jdbc:h2:mem:test')
    }

    static Sql getMySqlConnection() {

        def dbURL = 'jdbc:mysql://127.0.0.1:3306/playground'
        def dbUserName = 'root'
        def dbPassword = 'pass'
        def dbDriver = 'com.mysql.cj.jdbc.Driver'
        return Sql.newInstance(dbURL,dbUserName,dbPassword,dbDriver)
    }
}
