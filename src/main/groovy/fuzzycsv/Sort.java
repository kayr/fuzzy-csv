package fuzzycsv;

import fuzzycsv.javaly.Fx1;
import fuzzycsv.javaly.Fx2;
import lombok.AllArgsConstructor;
import lombok.With;
import org.codehaus.groovy.runtime.NumberAwareComparator;

import java.util.Arrays;
import java.util.Comparator;


@SuppressWarnings({"unchecked", "rawtypes"})
@AllArgsConstructor
public class Sort {


    private static final NumberAwareComparator numberAwareComparator = new NumberAwareComparator();
    private Comparator<Record> comparator;
    @With
    private boolean ascending;


    public static Sort[] byColumns(String... names) {
        return Arrays.stream(names).map(Sort::byColumn).toArray(Sort[]::new);
    }

    public static Sort[] byColumns(int... colIdx) {
        return Arrays.stream(colIdx).mapToObj(Sort::byColumn).toArray(Sort[]::new);

    }

    public static Sort byColumn(String name) {
        return byFx(r -> r.get(name));
    }


    public static Sort byColumn(int colIdx) {
        return byFx(r -> r.get(colIdx));
    }

    public static Sort byFx(Fx1<Record, Object> fx) {
        Comparator<Record> comparator1 = (Record a, Record b) -> {
            try {
                Object v1 = fx.call(a);
                Object v2 = fx.call(b);
                return numberAwareComparator.compare(v1, v2);
            } catch (Exception e) {
                throw FuzzyCsvException.wrap(e);
            }
        };
        return new Sort(comparator1, true);
    }

    public static Sort byComparing(Fx2<Record, Record, Integer> comparator) {
        Comparator<Record> cmp = (Record a, Record b) -> {
            try {
                return comparator.call(a, b);
            } catch (Exception e) {
                throw FuzzyCsvException.wrap(e);
            }
        };
        return new Sort(cmp, true);
    }


    public Sort asc() {
        return withAscending(true);
    }

    public Sort desc() {
        return withAscending(false);
    }

    Comparator<Record> toComparator() {
        return ascending ? comparator : comparator.reversed();
    }


}
