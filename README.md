# FuzzyCSV is a groovy library to help you merge/append/query/ or manipulate CSV files.

**Table of Contents**

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Maven Dependency](#maven-dependency)
- [Features](#features)
  - [Illustrations:](#illustrations)
    - [Merging with a fuzzy match](#merging-with-a-fuzzy-match)
    - [Joins](#joins)
    - [Inner join](#inner-join)
    - [Left join](#left-join)
    - [Right join](#right-join)
    - [Full join](#full-join)
    - [Join with custom functions](#join-with-custom-functions)
  - [Other Utilities](#other-utilities)
    - [Record functions:](#record-functions)
  - [More Examples](#more-examples)
    - [Iterating over records/tables](#iterating-over-recordstables)
    - [Get Cell Value](#get-cell-value)
    - [Delete Column](#delete-column)
    - [CSV To MapList](#csv-to-maplist)
    - [Sql To CSV](#sql-to-csv)
    - [Add Column](#add-column)
    - [Filter Records](#filter-records)
    - [Transposing](#transposing)
    - [Simplistic Aggregations](#simplistic-aggregations)
- [Note:](#note)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Maven Dependency

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


## Features
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

### Illustrations:

Using the following as examples:

#### Merging with a fuzzy match
1. Set the accuracy threshold to 75%
2. Merge using code below
```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def csv1 = [
        ['name', 'sex'],
        ['alex', 'male'],
        ['sara', 'female']
]

def csv2 = [
        ['name', 'age', 'sex'],
        ['alex', '21', 'male'],
        ['peter', '21', 'male']
]

FuzzyCSV.ACCURACY_THRESHOLD.set(75) //set accuracy threshold
def csv = tbl(csv1).mergeByColumn(csv2)
println csv
```
This will output (Notice how it merged ***[first name]*** and ***[ferts nama]***)
```
[first name, sex, age]
[alex, male, ]
[sara, female, ]
[alex, male, 21]
```

#### Joins

For now joins do not use fuzzy matching simply because in my use case it was not necessary

```groovy
package fuzzycsv

import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.RecordFx.fn

def csv1 = [
        ['name', 'sex'],
        ['alex', 'male'],
        ['sara', 'female']
]

def csv2 = [
        ['name', 'age','hobby'],
        ['alex', '21','biking'],
        ['peter', '21','swimming']
]
```
#### Inner join
```groovy
def csv = tbl(csv1).join(csv2, 'name')
println csv
/*output
[name, sex, age, hobby]
[alex, male, 21, biking]*/
```

#### Left join
```groovy
csv = tbl(csv1).leftJoin(csv2, 'name')
println csv
/*output
[name, sex, age, hobby]
[alex, male, 21, biking]
[sara, female, null, null]*/
```

#### Right join
```groovy
csv = tbl(csv1).rightJoin(csv2, 'name')
println csv
/*output
[name, sex, age, hobby]
[alex, male, 21, biking]
[peter, null, 21, swimming]*/
```

#### Full join
```groovy
csv = tbl(csv1).fullJoin(csv2, 'name')
println csv
/*output
[name, sex, age, hobby]
[alex, male, 21, biking]
[sara, female, null, null]
[peter, null, 21, swimming]*/
```

#### Join with custom functions
```groovy
csv = tbl(csv1).fullJoin(csv2,fn{it.left('name') == it.right('name')})
println csv
/*output
[name, sex, name, age, hobby]
[alex, male, alex, 21, biking]
[sara, female, sara, null, null]
[peter, null, peter, 21, swimming]*/
```


### Other Utilities

#### Record functions:

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

### More Examples

Consider we have the following csv
```groovy
def csv2 = [
        ['name', 'age','hobby'],
        ['alex', '21','biking'],
        ['peter', '21','swimming']
]
```

#### Iterating over records/tables
```groovy
tbl(csv).each{println(r.name)}
/*output
alex
peter*/
```

#### Get Cell Value
```groovy
println tbl(csv2)['name'][2]
//output
peter
```
#### Delete Column
```groovy
println tbl(csv2).delete('name','age')
//output
//[hobby]
//[biking]
//[swimming]
```

#### CSV To MapList
```groovy
println tbl(csv2).toMapList()
/*output
[[name:alex, age:21, hobby:biking], [name:peter, age:21, hobby:swimming]]
*/
```

#### Sql To CSV
```groovy
tbl(FuzzyCSV.toCSV(sql, 'select * from PERSON'))
```

#### Add Column
```groovy
println tbl(csv2).addColumn(fn('Double Age') {it.age * 2})
//output
//[name, age, hobby, Double Age]
//[alex, 21, biking, 42]
//[peter, 21, swimming, 42]
```
#### Filter Records
```groovy
println tbl(csv2).filter(fn {it.name == 'alex'})
//output
//[name, age, hobby]
//[alex, 21, biking]
```

#### Transposing
```groovy
println tbl(csv2).transpose()
/*output
[name, alex, peter]
[age, 21, 21]
[hobby, biking, swimming]
*/
```

Example 2:
```groovy

def csv2 = [
        ['name', 'age','hobby','id'],
        ['alex', '21','biking',1],
        ['peter', '21','swimming',2]
]

//name = Column To Become Header
//age = Column Needed in Cells
//id and hobby = Columns that uniquely identify a record/row
println tbl(csv2).transpose('name','age','id','hobby')
/*output
[id, hobby, alex, peter]
[1, biking, 21, null]
[2, swimming, null, 21]
*/
```

#### Simplistic Aggregations

In the example below we find the average age in each hobby by making use of sum count and group by functions

```groovy
def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming'],
        ['davie', '15', 'swimming'],
        ['sam', '16', 'biking'],
]


println tbl(csv2)
        .aggregate(
        [
            'Hobby',

            sum('age').az('TT.Age'),

            count('name').az('TT.Count'),

            fn('Avg') { it['TT.Age'] / it['TT.Count'] }
        ],

        fn { it.hobby }

        ).toStringFormatted()
/*output
hobby    TT.Age  TT.Count  Avg
________  ______  ________  ____
biking    37      2         18.5
swimming  36      2         18
___________________
2 records
*/
```

## Note:
This library has not been tested with very large CSV files. So performance might be a concern

More example can be seen here

https://github.com/kayr/fuzzy-csv/blob/master/src/test/groovy/fuzzycsv/FuzzyCSVTest.groovy

and

https://github.com/kayr/fuzzy-csv/blob/master/src/test/groovy/fuzzycsv/FuzzyCSVTableTest.groovy
