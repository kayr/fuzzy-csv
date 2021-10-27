@Grab(group = 'io.github.kayr', module = 'fuzzy-csv', version = '1.7.1')
import fuzzycsv.FuzzyCSVTable

def file = 'index.adoc' as File

def pattern = $///\[out:[a-zA-Z-]+\]/$


def finalText = file.text.replaceAll(pattern) {
    return 'Output\n' +
            '[source]\n' +
            '----\n' +
            runForCode(it) +
            '----\n'
}


new File('index.out.adoc').text = finalText

exec('asciidoctorj', 'index.out.adoc', '-o', 'index.html')
exec('firefox', 'index.html')

void exec(String... commands) {
    println("  > $commands")
    def execute = commands.execute()
    execute.consumeProcessOutput(System.out, System.err)
    execute.waitFor()
}

String runForCode(String pattern) {
    def fileName = pattern.replace('//[out:', '')
            .replace(']', '') + '.groovy'

    def absPath = 'samples/' + fileName
    println("Executing $absPath ....")
    def theGroovyFile = (absPath as File).absoluteFile

    def result = evalFileInNewWorkingDir(theGroovyFile)
    if (result instanceof FuzzyCSVTable) return result.toStringFormatted()
    return result
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
