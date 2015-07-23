package fuzzycsv


interface Aggregator<T> {

    void setData(List<List> data)

    T getValue()

    String getColumnName()

    void setColumnName(String name)

    Aggregator az(String name)

}
