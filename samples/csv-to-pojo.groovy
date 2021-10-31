@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyCSVTable.tbl

def csv = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming']]

class Person {
    String name
    Integer age
    String hobby
}

List<Person> people = tbl(csv).toPojoList(Person.class)

assert people.size() == 3
assert people.first().name == 'alex'
assert people.first().age == 21
