package fuzzycsv

import com.github.kayr.phrasehelper.PhraseHelper2
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class Fuzzy {
    private static Logger log = LoggerFactory.getLogger(Fuzzy)

    static int findBestPosition(def phrases, String header, double minScore) {
        phrases = phrases as List
        def csvColIdx = findPosition(phrases, header)
        if (csvColIdx == -1 && FuzzyCSV.ACCURACY_THRESHOLD.get() <= 1.0) {
            csvColIdx = findClosestPosition(phrases, header, minScore)
        }
        csvColIdx
    }

    static int findClosestPosition(def phrases, String phrase, double minScore) {
        phrases = phrases as List
        def ph = PhraseHelper2.train(phrases as List)
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
