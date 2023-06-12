package fuzzycsv

import fuzzycsv.javaly.Fx2
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = 'with')
@CompileStatic
class SpreadConfig<COL,KEY> {
    Object col
    Fx2<Object, Object, String> nameGenFn = { Object col, Object value -> "${RecordFx.resolveName(col)}_${value}" }


    String createName(KEY key) {
        def call = nameGenFn.call(col, key)
        return call?.toString()
    }

}