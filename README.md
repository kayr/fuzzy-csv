FuzzyCSV is a groovy library to help you merge/append/query/ or manipulate CSV files.

### Features
 * Merging using Fuzzy Matching with the help of the SecondString project
 * Inner Join
 * Right Join
 * Left join
 * Full Join
 * Record functions

##### Illustrations:

Using the following as examples:

```def csv2 = [
        ['first name', 'sex'],
        ['alex', 'male']
]

def csv3 = [
        ['ferts nama ', 'age', 'sex'],
        ['alex', '21', 'male']
]```

##### Merging with a fuzzy match
1. Set the accuracy threshold to 75%
2. Merge using code below
    ```import static fuzzycsv.FuzzyCSVTable.tbl

    FuzzyCSV.ACCURACY_THRESHOLD.set(75) //set accuracy threshold

     tbl(csv2).mergeByColumn(tbl(csv3))
     ```
This will output(Notice how it merged *[first name]* and *[ferts nama]*)
     ```[first name, sex, age]
      [alex, male, ]
      [alex, male, 21]```


