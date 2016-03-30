package fuzzycsv

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import secondstring.PhraseHelper

@Log4j
@CompileStatic
class Fuzzy {

    static int findBestPosition(def phrases, String header, float minScore) {
        phrases = phrases as List
        def csvColIdx = findPosition(phrases, header)
        if (csvColIdx == -1 && FuzzyCSV.ACCURACY_THRESHOLD.get() <= 100) {
            csvColIdx = findClosestPosition(phrases, header, minScore)
        }
        csvColIdx
    }

    static int findClosestPosition(def phrases, String phrase, float minScore) {
        phrases = phrases as List
        def ph = PhraseHelper.train(phrases as List)
        def newName = ph.bestInternalHit(phrase, minScore)

        if (newName == null) {
            if (log.isDebugEnabled())
                log.debug "getColumnPositionUsingHeuristic(): warning: no column match found:  [$phrase] = [$newName]"
            return -1
        }
        if (log.isDebugEnabled())
            log.debug "getColumnPositionUsingHeuristic(): ${ph.compare(newName, phrase)} heuristic: [$phrase] = [$newName]"
        return findPosition(phrases, newName)
    }

    static int findPosition(def phrases, String name) {
        phrases.findIndexOf { value -> value.toString().toLowerCase().trim().equalsIgnoreCase(name.trim().toLowerCase()) }
    }
}
