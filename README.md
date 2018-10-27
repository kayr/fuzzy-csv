# FuzzyCSV is a simple light weight groovy data processing library to help you merge/append/query/ or manipulate CSV files or any tabular data.

[![Release](https://jitpack.io/v/kayr/fuzzy-csv.svg)]
(https://jitpack.io/#kayr/fuzzy-csv)
### Use Cases

FuzzyCSV is a lightweigt groovy data processing library that helps in shaping and cleaning your dataset before its consumed by another service. 


**Table of Contents**

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Dependency](#dependency)
    - [Maven](#maven)
    - [Gradle](#gradle)
    - [Repositories](#repositories)
- [Features](#features)
  - [Illustrations:](#illustrations)
    - [Loading data into fuzzyCSV](#loading-data-into-fuzzycsv)
    - [Merging with a fuzzy match](#merging-with-a-fuzzy-match)
    - [Joins](#joins)
    - [Inner join](#inner-join)
    - [Left join](#left-join)
    - [Right join](#right-join)
    - [Full join](#full-join)
    - [Join with custom functions](#join-with-custom-functions)
    - [Record functions:](#record-functions)
    - [Doing a Select with a calculated field](#doing-a-select-with-a-calculated-field)
    - [Iterating over records/tables](#iterating-over-recordstables)
    - [Get Cell Value](#get-cell-value)
    - [Delete Column](#delete-column)
    - [CSV To MapList](#csv-to-maplist)
    - [Sql To CSV](#sql-to-csv)
    - [Add Column](#add-column)
    - [Filter Records](#filter-records)
    - [Sorting](#sorting)
    - [Ranges](#ranges)
    - [Up and Down Navigation e.g (for running sum)](#up-and-down-navigation-eg-for-running-sum)
    - [Update values with where clause](#update-values-with-where-clause)
    - [Transform each cell record](#transform-each-cell-record)
    - [Transposing](#transposing)
    - [Pivoting](#pivoting)
    - [Simplistic Aggregations](#simplistic-aggregations)
    - [Custom Aggregation](#custom-aggregation)
    - [Unwinding a column](#unwinding-a-column)
    - [Excel utility classes](#excel-utility-classes)
- [Note:](#note)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Dependency
[![Release](https://jitpack.io/v/kayr/fuzzy-csv.svg)]
(https://jitpack.io/#kayr/fuzzy-csv)
#### Maven
```xml
<dependency>
     <groupId>com.github.kayr</groupId>
     <artifactId>fuzzy-csv</artifactId>
     <version>${version}</version>
</dependency>
```
#### Gradle
` compile 'com.github.kayr:fuzzy-csv:${version}'`


#### Repositories
```xml
<repositories>
   <repository>
            <name>JitPack.io</name>
            <id>JitPack</id>
            <url>https://jitpack.io</url>
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
 * Pivoting
 * and some extra utilities

### Illustrations:

Using the following as examples:

#### Loading data into fuzzyCSV
```groovy
FuzzyCSVTable.toCSV(sqlResultSet)
FuzzyCSVTable.toCSV(groovySql,"select * from table")
FuzzyCSVTable.toCSV(List<Map>)
FuzzyCSVTable.tbl(List<List>)
FuzzyCSVTable.fromJsonText('''[["colum"],["value1"]]''' )
//parse 
FuzzyCSVTable.parseCsv(String csvString)
FuzzyCSVTable.parseCsv(Reader csvString)
//if you wish to customise the parsing you can provide more options
FuzzyCSVTable.parseCsv(String csvString, separator/* , */, quoteChar /* " */, escapeChar /* \ */)
```
#### Merging with a fuzzy match
1. Set the accuracy threshold to 75%
2. Merge using code below
```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def csv1 = [
        ['first name', 'sex'],
        ['alex', 'male'],
        ['sara', 'female']
]

def csv2 = [
        ['ferts nama', 'age', 'sex'],
        ['alex', '21', 'male'],
        ['peter', '21', 'male']
]

FuzzyCSV.ACCURACY_THRESHOLD.set(0.75) //set accuracy threshold
tbl(csv1).mergeByColumn(csv2).printTable()
```
This will output (Notice how it merged ***[first name]*** and ***[ferts nama]***)
```
  first name   sex      age  
  ----------   ---      ---  
  alex         male     -    
  sara         female   -    
  alex         male     21   
  peter        male     21   
_________
4 Rows
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
def csv = tbl(csv1).fullJoin(csv2){it.left('name') == it.right('name')}
println csv.toStringFormatted()
/*output
  name    sex      name    age   hobby
  ----    ---      ----    ---   -----
  alex    male     alex    21    biking
  sara    female   sara    -     -
  peter   -        peter   21    swimming
_________
3 Rows
*/
```


#### Record functions:

These Help you write expression or functions for a record. E.g A function multiplying price by quantity. The record function run in two modes:

 - One with type coercion which can be created using`RecordFx.fn{}`.This mode is lenient and does not throw most exceptions. This mode supports division of nulls(`null/2`), zero(`2/0`) division and type coercion(`"2"/2 or Object/null`) . This mode adds extra overhead and is much slower if your are dealing with lots of records.
 - Another mode is `RecordFx.fx{}` which uses the default groovy evaluator. This mode is much faster if you are working with lots of records. However this mode is not lenient and hence can throw `java.lang.ArithmeticException: Division by zero`. If you want to enable leniency but still want to use the faster `RecordFx.fx{}` you can wrap your code in the `fuzzycsv.FxExtensions` category(e.g `use(FxExtensions){ ...code here.. }`) So the category is registered only once as compared to the former where the category is reqistered on each and every evaluation.

#### Doing a Select with a calculated field

```groovy

import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['price', 'quantity'],
        ['2', '40'],
        ['3', '20']
]

tbl(csv2).select('price',
                 'quantity',
                 fn('total') { it.price * it.quantity })
         .printTable()

/* output
  price   quantity   total  
  -----   --------   -----  
  2       40         80     
  3       20         60     
_________
2 Rows

 */
```

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

import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age','hobby'],
        ['alex', '21','biking'],
        ['peter', '21','swimming']
]


tbl(csv2).delete('name','age').printTable()

/* output
  hobby     
  -----     
  biking    
  swimming  
_________
2 Rows
 */
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
FuzzyCSVTable.toCSV(groovySql,"select * from PERSON")
//or
FuzzyCSVTable.toCSV(resultSet)
```

#### Add Column
```groovy
import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming']
]


tbl(csv2).addColumn(fn('Double Age') { it.age * 2 }).printTable()

/*output
  name    age   hobby      Double Age  
  ----    ---   -----      ----------  
  alex    21    biking     42          
  peter   21    swimming   42          
_________
2 Rows

 */
```
#### Filter Records
```groovy
tbl(csv2).filter { it.name == 'alex' }.printTable()

/*output
  name   age   hobby   
  ----   ---   -----   
  alex   21    biking  
_________
1 Rows
 */
```

#### Sorting

```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def csv2 = [
        ['name', 'age','hobby'],
        ['alex', '21','biking'],
        ['martin', '40','swimming'],
        ['dan', '25','swimming'],
        ['peter', '21','swimming'],
]

tbl(csv2).sort('age','name').printTable()

//or sort using closure
tbl(csv2).sort{"$it.age $it.name"}.printTable()

/*Output for both
  name     age   hobby
  ----     ---   -----
  alex     21    biking
  peter    21    swimming
  dan      25    swimming
  martin   40    swimming
_________
4 Rows
 */
```

#### Ranges
Ranges help slice the csv record..e.g selecting last 2, top 2,  3rd to 2nd last record
```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def table = tbl([
        ['name', 'age','hobby'],
        ['alex', '21','biking'],
        ['martin', '40','swimming'],
        ['dan', '25','swimming'],
        ['peter', '21','swimming'],
])

//top 2
table[1..2].printTable()

/*Output
  name     age   hobby
  ----     ---   -----
  alex     21    biking
  martin   40    swimming
_________
2 Rows
 */

//last 2
table[-1..-2].printTable()
/*
  name    age   hobby
  ----    ---   -----
  peter   21    swimming
  dan     25    swimming
_________
2 Rows
 */

```
#### Up and Down Navigation e.g (for running sum)

Example showing running sum
```groovy

def csv = [["name", "age"],
           ["kay", 1],
           ["sa", 22],
           ["kay2", 1],
           ["ben", 10]]


           
//add running sum of age

tbl(csv).addColumn(fx("running_sum") { (it.up()?.running_sum ?: 0) + it.age }).printTable()
/*output
  name   age   running_sum  
  ----   ---   -----------  
  kay    1     1            
  sa     22    23           
  kay2   1     24           
  ben    10    34           
_________
4 Rows
*/
                   
                   
```

Or sum bottom value with current value

```groovy
tbl(csv).addColumn(fx("bottom_up") { (it.down().age ?: 0) + it.age }).printTable()

/*output
  name   age   bottom_up  
  ----   ---   ---------  
  kay    1     23         
  sa     22    23         
  kay2   1     11         
  ben    10    10         
_________
4 Rows
 */

```

#### Update values with where clause

```groovy

import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['martin', '40', 'swimming'],
        ['dan', '25', 'swimming'],
        ['peter', '21', 'swimming'],
]

tbl(csv2).modify {
    set {
        it.hobby = "running"
        it.age  = '900'
    }
    where {
        it.name in ['dan', 'alex']
    }
}.printTable()

/*Output for both
  name     age   hobby     
  ----     ---   -----     
  alex     900   running   
  martin   40    swimming  
  dan      900   running   
  peter    21    swimming  
_________
4 Rows
 */
```
#### Transform each cell record
```groovy
import static fuzzycsv.FuzzyCSVTable.tbl

def table = tbl([
        ['name', 'age','hobby'],
        ['alex', '21','biking'],
        ['martin', '40','swimming'],
        ['dan', '25','swimming'],
        ['peter', '21','swimming'],
])

table.transform {it.padRight(10,'-')}.printTable()

/*
  name         age          hobby
  ----         ---          -----
  alex------   21--------   biking----
  martin----   40--------   swimming--
  dan-------   25--------   swimming--
  peter-----   21--------   swimming--
_________
4 Rows
*/
```

#### Transposing
```groovy
tbl(csv2).transpose()
         .printTable()

/*output
  name    alex     peter     
  ----    ----     -----     
  age     21       21        
  hobby   biking   swimming  
_________
2 Rows
 */
```

#### Pivoting
```groovy

import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming','S'],
        ['barbara', '23', 'swimming', 'S']
]

//name = Column To Become Header
//age = Column Needed in Cells
//id and hobby = Columns that uniquely identify a record/row
tbl(csv2).pivot('name', 'age', 'category', 'hobby')
         .printTable()
/*output
  category   hobby      alex   peter   charles   barbara  
  --------   -----      ----   -----   -------   -------  
  A          biking     21     -       -         -        
  S          swimming   -      21      21        23       
_________
2 Rows
*/
```

#### Simplistic Aggregations

In the example below we find the average age in each hobby by making use of sum count and group by functions

```groovy

import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'Hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming'],
        ['davie', '15', 'swimming'],
        ['sam', '16', 'biking'],
]


tbl(csv2).summarize(

        'Hobby',

        sum('age').az('TT.Age'),

        count('name').az('TT.Count')
).printTable()
/*output
  Hobby      TT.Age   TT.Count
  -----      ------   --------
  biking     37       2
  swimming   36       2
_________
2 Rows
*/
```

#### Custom Aggregation
```groovy
tbl(csv2).summarize(
        'Hobby',
        reduce { group -> group['age'] }.az('AgeList')
).printTable()
/*output
  Hobby      AgeList
  -----      -------
  biking     [21, 16]
  swimming   [21, 15]
_________
2 Rows

*/
```

#### Unwinding a column
This is kind can be used to unwind a coluwn which has nested listes

```groovy
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name',     'AgeList'  ],
        ['biking',   [21,16]    ],
        ['swimming', [21,15]    ]
]


tbl(csv).unwind('AgeList')
        .printTable()
        
/*output
  name       AgeList  
  ----       -------  
  biking     21       
  biking     16       
  swimming   21       
  swimming   15       
_________
4 Rows
*/

```

#### Excel utility classes
To use the excel utilities you have to add the poi dependency to your classpath

If you are using gradle add this.
```groovy
     compile 'org.apache.poi:poi-ooxml:3.16', {
         exclude group: 'stax', module: 'stax-api'
     }
     compile 'org.apache.poi:ooxml-schemas:1.3', {
         exclude group: 'stax', module: 'stax-api'
     }
```

After this you can use the Excel utilities to convert excel sheets to and from FuzzyCSVTables.

There are mainly two classes that help with this which include `fuzzycsv.Excel2Csv` and `fuzzycsv.CSVToExcel` 

## Note:
This library has not been tested with very large(700,000 records plus) CSV files. So performance might be a concern.

More example can be seen here

https://github.com/kayr/fuzzy-csv/blob/master/src/test/groovy/fuzzycsv/FuzzyCSVTest.groovy

and

https://github.com/kayr/fuzzy-csv/blob/master/src/test/groovy/fuzzycsv/FuzzyCSVTableTest.groovy


