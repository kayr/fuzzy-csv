package fuzzycsv.rdbms.stmt

import groovy.transform.CompileStatic;

@CompileStatic
class DumbH2Renderer extends DefaultSqlRenderer {

    private static DumbH2Renderer instance;

    static DumbH2Renderer getInstance() {
        if (instance == null)// don't care about synchronization
            instance = new DumbH2Renderer();
        return instance;
    }

    @Override
    String quoteName(String name) {
        return "\"" + name + "\"";
    }
}
