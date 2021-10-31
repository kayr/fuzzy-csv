@Grab('io.github.kayr:fuzzy-csv:1.7.2')
//tag::code[]
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
//end::code[]


