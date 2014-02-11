package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kayr
 * Date: 10/20/13
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
interface Aggregator<T> {

    void setData(List<List> data)

    T getValue()

    String getColumnName()

    void setColumnName(String name)
}
