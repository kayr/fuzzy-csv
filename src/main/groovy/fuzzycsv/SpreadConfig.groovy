package fuzzycsv

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy;

@Builder(builderStrategy = SimpleStrategy, prefix = 'with')
@CompileStatic
class SpreadConfig {
    Object col
    Closure nameGenFn = { Object col, Object value -> "${RecordFx.resolveName(col)}_${value}" }


    String createName(def key) {
        return nameGenFn.call(col, key)?.toString()
    }

}