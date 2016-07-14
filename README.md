FuzzyCSV is a groovy library to help you merge/append/query/ or manipulate CSV files.

#### Maven

```xml
 <dependency>
     <groupId>fuzzy-csv</groupId>
     <artifactId>fuzzycsv</artifactId>
     <version>${version}</version>
</dependency>
```
Repositories
```xml
<repositories>
    <repository>
        <id>kayr.repo.snapshots</id>
        <url>https://dl.dropboxusercontent.com/u/3038882/p/m2/snapshots</url>
    </repository>
    <repository>
        <id>kayr.repo.releases</id>
        <url>https://dl.dropboxusercontent.com/u/3038882/p/m2/releases</url>
    </repository>
</repositories>
```


### Features
 * Merging using Fuzzy Matching with the help of the SecondString project(useful when you have misspelled column names in the different CSV files)
 * Inner Join
 * Right Join
 * Left join
 * Full Join
 * Record functions
 * Transposing
 * Grouping
 * Sum and Count Aggregate functions
 * Lenient arithmetic operations i.e strings are coerced to numbers

##### Illustrations:

Using the following as examples:

##### Merging with a fuzzy match
1. Set the accuracy threshold to 75%
2. Merge using code below
```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def csv1 = [
        ['first name', 'sex'],
        ['alex', 'male'],
        ['sara', 'male']
]

def csv2 = [
        ['ferts nama ', 'age', 'sex'],
        ['alex', '21', 'male']
]

FuzzyCSV.ACCURACY_THRESHOLD.set(75) //set accuracy threshold
def csv = tbl(csv1).mergeByColumn(csv2)
println csv
```
This will output (Notice how it merged ***[first name]*** and ***[ferts nama]***)
```
[first name, sex, age]
[alex, male, ]
[sara, male, ]
[alex, male, 21]
```

##### Joins

For now joins do not use fuzzy matching simply because in my use case it was not necessary

```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def csv1 = [
        ['name', 'sex'],
        ['alex', 'male'],
        ['sara', 'male']
]

def csv2 = [
        ['name', 'age', 'sex'],
        ['alex', '21', 'male']
]

//inner join
def csv = tbl(csv1).join(csv2, 'name')
println csv
//output
//[name, sex, age, sex]
//[alex, male, 21, male]

//left join
csv = tbl(csv1).leftJoin(csv2, 'name')
println csv
//output
//[name, sex, age, sex]
//[alex, male, 21, male]
//[sara, male, null, male]

//right join
csv = tbl(csv1).rightJoin(csv2, 'name')
println csv
//output
//[name, sex, age, sex]
//[alex, male, 21, male]
//[peter, male, 21, male]

//full join
csv = tbl(csv1).fullJoin(csv2, 'name')
println csv
//output
//[alex, male, 21, male]
//[sara, male, null, male]
//[peter, male, 21, male]
```

##### Record functions:

These Help you write expression or functions for a record. E.g A function multiplying price by quantity
```groovy
import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.RecordFx.fn

def csv2 = [
        ['price', 'quantity'],
        ['2', '40'],
        ['3', '20']
]

def csv = tbl(csv2).select('price', 'quantity', fn('total') { it.price * it.quantity })
println csv
//output
//[price, quantity, total]
//[2, 40, 80]
//[3, 20, 60]
```

#### Note:
This library has not been tested with very large CSV files. So performance is not known

More example can be seen here
https://github.com/kayr/fuzzy-csv/blob/master/src/test/groovy/fuzzycsv/FuzzyCSVTest.groovy
and
https://github.com/kayr/fuzzy-csv/blob/master/src/test/groovy/fuzzycsv/FuzzyCSVTableTest.groovy












