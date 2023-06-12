package fuzzycsv;

import fuzzycsv.javaly.Fx2;
import lombok.AccessLevel;
import lombok.SneakyThrows;

import java.util.Objects;

@lombok.With
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Getter
public class SpreadConfig {

    private Object col;
    private Fx2<Object, Object, String> nameGenFn;

    public SpreadConfig() {
        nameGenFn = (Object col, Object value) -> RecordFx.resolveName(col) + "_" + value;
    }

    @SneakyThrows
    public String createName(Object key) {
        Object call = nameGenFn.call(col, key);
        return Objects.toString(call);
    }
}
