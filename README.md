# FuzzyCSV is a simple light weight groovy data processing library to help you merge/append/query/ or manipulate CSV files or any tabular data.


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.kayr/fuzzy-csv/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.github.kayr/fuzzy-csv) 
![Release](https://jitpack.io/v/kayr/fuzzy-csv.svg)
![Java CI](https://github.com/kayr/fuzzy-csv/workflows/Java%20CI/badge.svg)

### Use Cases

FuzzyCSV is a lightweigt groovy data processing library that helps in shaping and cleaning your dataset before its consumed by another service. 


**Table of Contents**

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Dependency](#dependency)
    - [From Maven Central *](#from-maven-central-)
    - [Gradle](#gradle)
    - [Jitpack (For unpublished artifacts)](#jitpack-for-unpublished-artifacts)
- [Features](#features)
  - [Examples of Real World Applications](#examples-of-real-world-applications)
  - [Illustrations:](#illustrations)
    - [Loading data into fuzzyCSV](#loading-data-into-fuzzycsv)
    - [Visualize json data in a console grid table](#visualize-json-data-in-a-console-grid-table)
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
    - [Map Table To POJO](#map-table-to-pojo)
    - [Sql To CSV](#sql-to-csv)
    - [Add Column](#add-column)
    - [Filter Records](#filter-records)
    - [Delete rows](#delete-rows)
    - [Distinct by column](#distinct-by-column)
    - [Adding records](#adding-records)
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
    - [Spreading a column](#spreading-a-column)
    - [Move column](#move-column)
    - [Navigation](#navigation)
    - [Excel utility classes](#excel-utility-classes)
- [Note:](#note)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Dependency

#### From Maven Central [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.kayr/fuzzy-csv/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.github.kayr/fuzzy-csv)

```xml
<dependency>
     <groupId>io.github.kayr</groupId>
     <artifactId>fuzzy-csv</artifactId>
     <version>${version}</version>
</dependency>
```
#### Gradle
` compile 'io.github.kayr:fuzzy-csv:${version}'`

#### Jitpack (For unpublished artifacts)
If you want to get a version that is not yest published to maven central then you can use JITPACK ![Release](https://jitpack.io/v/kayr/fuzzy-csv.svg) (https://jitpack.io/#kayr/fuzzy-csv)
notice that the `io.github.kayr` is repleaced with `com.github.kayr`.
```
compile 'com.github.kayr:fuzzy-csv:${version}'`
```
```xml
<!-- Coordinate from JITPACK-->
<dependency>
     <groupId>com.github.kayr</groupId>
     <artifactId>fuzzy-csv</artifactId>
     <version>${version}</version>
</dependency>

<!-- Repository -->
<repositories>
    <repository>
      <id>jitpack.io</id>
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

### Examples of Real World Applications

Visit the following page to view examples of how to use fuzzyCSV for real world applications.

http://kayr.github.io/fuzzy-csv/fuzzy-csv-examples.html


### Illustrations:

Using the following as examples:

#### Loading data into fuzzyCSV
```groovy
FuzzyCSVTable.fromResultSet(sqlResultSet)
FuzzyCSVTable.fromSqlQuery(groovySql, "select * from table")
FuzzyCSVTable.fromListList(listMap)
FuzzyCSVTable.fromMapList(listOfLists)
FuzzyCSVTable.fromJsonText('''[["colum"],["value1"]]''')
//parse
FuzzyCSVTable.fromCsvString(csvString)
FuzzyCSVTable.fromCsvReader(reader)
//if you wish to customise the parsing you can provide more options
FuzzyCSVTable.fromCsvString(csvString, separator/* , */, quoteChar /* " */, escapeChar /* \ */)
```

#### Visualize json data in a console grid table

Given the following json:

```json
{
  "id": "0001",
  "type": "donut",
  "name": "Cake",
  "ppu": 0.55,
  "batters":
    {
      "batter":
        [
          { "id": "1001", "type": "Regular" },
          { "id": "1002", "type": "Chocolate","color": "Brown" }
        ]
    },
  "topping":
    [
      { "id": "5001", "type": "None" },
      { "id": "5002", "type": "Glazed" },
      { "id": "5005", "type": "Sugar" ,"color": "Brown"}
    ]
}
```
Convert the above to a grid like this `FuzzyCSVTable.fromJsonText(r).asListGrid().printTable()`

```text
╔═════════╤═══════════════════════════════════════════╗
║ key     │ value                                     ║
╠═════════╪═══════════════════════════════════════════╣
║ batters │ ╔════════╤══════════════════════════════╗ ║
║         │ ║ key    │ value                        ║ ║
║         │ ╠════════╪══════════════════════════════╣ ║
║         │ ║ batter │ ╔══════╤═══════════╤═══════╗ ║ ║
║         │ ║        │ ║ id   │ type      │ color ║ ║ ║
║         │ ║        │ ╠══════╪═══════════╪═══════╣ ║ ║
║         │ ║        │ ║ 1001 │ Regular   │ -     ║ ║ ║
║         │ ║        │ ╟──────┼───────────┼───────╢ ║ ║
║         │ ║        │ ║ 1002 │ Chocolate │ Brown ║ ║ ║
║         │ ║        │ ╚══════╧═══════════╧═══════╝ ║ ║
║         │ ╚════════╧══════════════════════════════╝ ║
╟─────────┼───────────────────────────────────────────╢
║ id      │ 0001                                      ║
╟─────────┼───────────────────────────────────────────╢
║ name    │ Cake                                      ║
╟─────────┼───────────────────────────────────────────╢
║ ppu     │ 0.55                                      ║
╟─────────┼───────────────────────────────────────────╢
║ topping │ ╔══════╤════════╤═══════╗                 ║
║         │ ║ id   │ type   │ color ║                 ║
║         │ ╠══════╪════════╪═══════╣                 ║
║         │ ║ 5001 │ None   │ -     ║                 ║
║         │ ╟──────┼────────┼───────╢                 ║
║         │ ║ 5002 │ Glazed │ -     ║                 ║
║         │ ╟──────┼────────┼───────╢                 ║
║         │ ║ 5005 │ Sugar  │ Brown ║                 ║
║         │ ╚══════╧════════╧═══════╝                 ║
╟─────────┼───────────────────────────────────────────╢
║ type    │ donut                                     ║
╚═════════╧═══════════════════════════════════════════╝
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
#### Map Table To POJO

```groovy
import static fuzzycsv.FuzzyCSVTable.tbl
class Person{
  Sting name
  Integer age
  String hobby
}
List<Person> people = tbl(csv2).toPojoList(Person.class)
```

#### Sql To CSV
```groovy
FuzzyCSVTable.toCSV(groovySql,"select * from PERSON")
//or
FuzzyCSVTable.toCSV(reCSVsultSet)
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

#### Delete rows

```groovy
import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming', 'S'],
        ['barbara', '23', 'swimming', 'S']
]

tbl(csv).delete {it.age == '21'}.printTable()

/*
╔═════════╤═════╤══════════╤══════════╗
║ name    │ age │ hobby    │ category ║
╠═════════╪═════╪══════════╪══════════╣
║ barbara │ 23  │ swimming │ S        ║
╚═════════╧═════╧══════════╧══════════╝
 */

```

#### Distinct by column
```groovy

import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming', 'S'],
        ['barbara', '23', 'swimming', 'S']
]

tbl(csv).distinctBy('age','category').printTable()

/*
╔═════════╤═════╤══════════╤══════════╗
║ name    │ age │ hobby    │ category ║
╠═════════╪═════╪══════════╪══════════╣
║ alex    │ 21  │ biking   │ A        ║
╟─────────┼─────┼──────────┼──────────╢
║ peter   │ 21  │ swimming │ S        ║
╟─────────┼─────┼──────────┼──────────╢
║ barbara │ 23  │ swimming │ S        ║
╚═════════╧═════╧══════════╧══════════╝
 */

```

#### Adding records

```groovy
def t = '''[["name","number"],["john",1.1]]'''

def c = FuzzyCSVTable.fromJsonText(t)

c.addRecordArr("JB", 455)
        .addRecord(["JLis", 767])
        .addRecordMap([name: "MName", number: 90])
        .addRecordArr()
        .addRecordMap([name: "MNameEmp"])
        .printTable()


/*
╔══════════╤════════╗
║ name     │ number ║
╠══════════╪════════╣
║ john     │ 1.1    ║
╟──────────┼────────╢
║ JB       │ 455    ║
╟──────────┼────────╢
║ JLis     │ 767    ║
╟──────────┼────────╢
║ MName    │ 90     ║
╟──────────┼────────╢
║ -        │ -      ║
╟──────────┼────────╢
║ MNameEmp │ -      ║
╚══════════╧════════╝
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

#### Spreading a column

Expand outwards a column which contains list items

```groovy

import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name',     'AgeList'  ],
        ['biking',   [21,16]    ],
        ['swimming', [21,15]    ]
]

tbl(csv).spread('AgeList')
        .printTable()

/*
╔══════════╤═══════════╤═══════════╗
║ name     │ AgeList_1 │ AgeList_2 ║
╠══════════╪═══════════╪═══════════╣
║ biking   │ 21        │ 16        ║
╟──────────┼───────────┼───────────╢
║ swimming │ 21        │ 15        ║
╚══════════╧═══════════╧═══════════╝
*/
```

Spread out a column with maps
```groovy


import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'Age'],
        ['biking', [age: 21, height: 16]],
        ['swimming', [age: 21, height: 15]]
]

tbl(csv).spread('Age')
        .printTable()

/*
╔══════════╤═════════╤════════════╗
║ name     │ Age_age │ Age_height ║
╠══════════╪═════════╪════════════╣
║ biking   │ 21      │ 16         ║
╟──────────┼─────────┼────────────╢
║ swimming │ 21      │ 15         ║
╚══════════╧═════════╧════════════╝
 */
```

Spread with custom column names

```groovy
import static fuzzycsv.FuzzyStaticApi.spreader
import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'Age'],
        ['biking', [age: 21, height: 16]],
        ['swimming', [age: 21, height: 15]]
]

tbl(csv).spread(spreader("Age") { col, key -> "MyColName: $key" })
        .printTable()

/*
╔══════════╤═════════════╤════════════════╗
║ name     │ MyTest: age │ MyTest: height ║
╠══════════╪═════════════╪════════════════╣
║ biking   │ 21          │ 16             ║
╟──────────┼─────────────┼────────────────╢
║ swimming │ 21          │ 15             ║
╚══════════╧═════════════╧════════════════╝
 */
```

#### Move column
```groovy


import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming', 'S'],
        ['barbara', '23', 'swimming', 'S']
]
tbl(csv).moveCol("age", "category")
        .printTable()

/*
╔═════════╤══════════╤══════════╤═════╗
║ name    │ hobby    │ category │ age ║
╠═════════╪══════════╪══════════╪═════╣
║ alex    │ biking   │ A        │ 21  ║
╟─────────┼──────────┼──────────┼─────╢
║ peter   │ swimming │ S        │ 21  ║
╟─────────┼──────────┼──────────┼─────╢
║ charles │ swimming │ S        │ 21  ║
╟─────────┼──────────┼──────────┼─────╢
║ barbara │ swimming │ S        │ 23  ║
╚═════════╧══════════╧══════════╧═════╝
 */
```

#### Navigation

Navigators help move through the table cells easily. You can look above,below, right or left of a cell.

```groovy

import fuzzycsv.nav.Navigator

import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming', 'S'],
        ['barbara', '23', 'swimming', 'S']
]

def navigator = new Navigator(0, 0, tbl(csv))


assert navigator.down().down().value() == 'peter'
assert navigator.right().value() == 'age'
assert navigator.right().left().value() == 'name'
assert navigator.down().up().value() == 'name'

// Move down
assert navigator.downIter().collect { it.value() } == ['name', 'alex', 'peter', 'charles', 'barbara']

// MoveRight
assert navigator.rightIter().collect { it.value() } == ['name', 'age', 'hobby', 'category']

//move through all
assert navigator.allIter().collect { it.value() } == ['name', 'age', 'hobby', 'category', 'alex', '21', 'biking', 'A', 'peter', '21', 'swimming',
                                                      'S', 'charles', '21', 'swimming', 'S', 'barbara', '23', 'swimming', 'S']
//move through all bounded
assert navigator.allBoundedIter(1, 2).collect { it.value() } == ['name', 'age', 'alex', '21', 'peter', '21']

//move up
assert new Navigator(0, 4, tbl(csv)).upIter().collect { it.value() } == ['barbara', 'charles', 'peter', 'alex', 'name']

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


