package fuzzycsv

import com.github.kayr.phrasematcher.PhraseMatcher
import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class Fuzzy {
    private static Logger log = LoggerFactory.getLogger(Fuzzy)

    static int findBestPosition(def phrases, String header, double minScore) {

        phrases = DefaultGroovyMethods.asType(phrases, List) // groovy 4 no longer supports phrases as List
        def csvColIdx = findPosition(phrases, header)
        if (csvColIdx == -1 && minScore < 1.0) {
            csvColIdx = findClosestPosition(phrases, header, minScore)
        }
        csvColIdx
    }

    static int findClosestPosition(def phrases, String phrase, double minScore) {
        phrases = DefaultGroovyMethods.asType(phrases, List)
        def ph = PhraseMatcher.train(phrases as List)
        def newName = ph.bestHit(phrase, minScore)

        if (newName.isInvalid()) {
            if (log.isDebugEnabled())
                log.debug "getColumnPositionUsingHeuristic(): warning: no column match found:  [$phrase] = [$newName]"
            return -1
        }
        if (log.isDebugEnabled())
            log.debug "getColumnPositionUsingHeuristic(): heuristic: [$phrase] = [$newName]"
        return findPosition(phrases, newName.phrase)
    }

    static int findPosition(def phrases, String name) {
        phrases.findIndexOf { value -> value.toString().toLowerCase().trim().equalsIgnoreCase(name.trim().toLowerCase()) }
    }
}
