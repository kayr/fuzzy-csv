package fuzzycsv

import groovy.sql.Sql

class H2DbHelper {

    static Sql getConnection() {
        Sql.newInstance('jdbc:h2:mem:test')
    }
}
