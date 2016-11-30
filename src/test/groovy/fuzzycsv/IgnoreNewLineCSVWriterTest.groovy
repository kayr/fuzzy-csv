package fuzzycsv

import junit.framework.Assert
import org.junit.Test

/**
 * Created by kay on 11/30/2016.
 */
public class IgnoreNewLineCSVWriterTest {

    @Test
    public void writeTo() throws Exception {
        def csv = [
                ['asas', 'sasas'],
                ['kasj \n ', '''""asas"\\" ' \r ''']
        ]

        def w = new StringWriter()

        IgnoreNewLineCSVWriter.writeTo(w, csv)
        Assert.assertEquals w.toString(), '''"asas","sasas"
                                            |"kasj  ","""""asas""\\"" '  "
                                            |'''.stripMargin()

    }

}