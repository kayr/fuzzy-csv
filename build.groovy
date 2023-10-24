@Grab(group = 'io.github.kayr', module = 'fuzzy-csv', version = '1.9.1-groovy4')
import fuzzycsv.FuzzyCSVTable
import groovy.io.FileType

def file = 'index.adoc' as File

def pattern = $///\[(out|raw|pre|full):[a-zA-Z-]+\]/$


def finalText = file.text.replaceAll(pattern) {
    def phrase = it[0]
    def r = runForCode(phrase)

    def code = r.code

    if (phrase.contains('raw:')) {
        return '/*output\n' +
                code +
                '*/\n'
    } else if (phrase.contains('pre:')) {
        return '_Output_\n' +
                '....\n' +
                code +
                '\n....\n'
    } else if (phrase.contains('full:')) {
        return """
[source,groovy]
----
include::samples/${r.file.name}[tag=code]
----
_Output_
....
$code
....
"""
    } else {

        return '.#Output#\n' +
                '[%collapsible]\n' +
                '====\n' +
                '[source]\n' +
                '----\n' +
                code +
                '----\n' +
                '====\n'
    }
}


new File('index.out.adoc').text = finalText

exec('asciidoctorj', 'index.out.adoc', '-o', 'index.html')

def toBuild = []
def workingDir = new File('.')
workingDir.eachFileRecurse(FileType.FILES) {
    if (it.name.endsWith('.adoc') && it.name !in ['index.out.adoc', 'index.adoc'])
        toBuild << it.absolutePath
}

exec(*['asciidoctorj', *toBuild])

//exec('firefox', 'index.html')

void exec(String... commands) {
    println("  > ${commands.join(' ')}")
    def execute = commands.execute()
    execute.consumeProcessOutput(System.out, System.err)
    execute.waitFor()
}

Map runForCode(String pattern) {


    def fileName = pattern.replace('//[out:', '')
            .replace('//[raw:', '')
            .replace('//[pre:', '')
            .replace('//[full:', '')
            .replace(']', '') + '.groovy'

    def absPath = 'samples/' + fileName
    println("Executing $absPath ....")
    def theGroovyFile = (absPath as File).absoluteFile

    def result = evalFileInNewWorkingDir(theGroovyFile)
    if (result instanceof FuzzyCSVTable) return [code: result.toStringFormatted(), file: theGroovyFile]
    return [code: result.toString(), file: theGroovyFile]
}


def evalFileInNewWorkingDir(File file) {
    String oldDir = System.getProperty("user.dir")
    try {
        System.setProperty("user.dir", ("samples" as File).absoluteFile.absolutePath)
        return evaluate(file)
    } finally {
        System.setProperty("user.dir", oldDir)
    }
}
