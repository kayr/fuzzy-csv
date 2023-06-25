package fuzzycsv


import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Test

import java.text.DateFormat

class Excel2CsvTest {

    @Test
    void testConvertingToCSV() {
        def path = '/sample_file.xlsx'

        def workbook = new XSSFWorkbook(getClass().getResourceAsStream(path))


        def sheets = Excel2Csv.allSheetsToCsv(workbook)

        def table = sheets['client_transactions']

        def modify = table.modify {
            set {
//                it.date = (it.date as Date).format("dd-MM-yyyy")
                it.set("date", (it.date as Date).format("dd-MM-yyyy"))
            }
        }


        assert modify.csv == [['date', 'batch_number', 'client_number', 'agent_number', 'transaction_type', 'clearing_status', 'amount', 'transaction_id'],
                              ['07-09-2017', null, '701-721572311', null, 'SUBSCRIPTION', 'Cleared', 2.0E7, null],
                              ['07-09-2017', null, '701-995855822', null, 'SUBSCRIPTION', 'Pending', 2.0E8, null],
                              ['11-09-2017', null, '701-1916955102', null, 'SUBSCRIPTION', 'Transferred', 800000.0, null]]
    }

}
