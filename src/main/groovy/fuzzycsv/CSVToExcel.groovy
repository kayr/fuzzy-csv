package fuzzycsv

import groovy.transform.CompileStatic
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class CSVToExcel {

    private static Logger log = LoggerFactory.getLogger(CSVToExcel)

    static void exportToExcelFile(Map<String, FuzzyCSVTable> csvMap, String filePath) {
        def file = new File(filePath)
        def excel = exportToExcel(csvMap)
        file.withOutputStream { OutputStream o -> excel.write(o) }

    }

    static Workbook exportToExcel(Map<String, FuzzyCSVTable> csvMap) {

        def workbook = new SXSSFWorkbook()

        def ec = new XlsExportContext(workbook)

        csvMap.forEach { String name, FuzzyCSVTable csv ->
            Sheet sheet = workbook.createSheet(name)
            writeExcel(csv, sheet, ec)
        }

        return workbook
    }

    private static void writeExcel(FuzzyCSVTable csv, Sheet sheet, XlsExportContext ec) {
        csv.csv.eachWithIndex { r, i ->
            Row row = sheet.createRow(i)
            def size = r.size()
            for (int j = 0; j < size; j++) {
                def value = r.get(j)
                def cell = row.createCell(j)
                setCellData(value, cell, ec)
            }

        }
    }


    private static class XlsExportContext {
        SXSSFWorkbook wb
        CellStyle dateStyle

        XlsExportContext(SXSSFWorkbook wb) {
            this.wb = wb
            init()
        }

        private def init() {
            def createHelper = wb.getCreationHelper()
            dateStyle = wb.createCellStyle()
            dateStyle.setDataFormat(
                    //createHelper.createDataFormat().getFormat("MMMM dd, yyyy"))
                    createHelper.createDataFormat().getFormat("yy-mmm-d h:mm"))
            return this
        }

    }

    private static void setCellData(Object dataValue, Cell cell, XlsExportContext ec) {
        try {

            if (dataValue == null) {
                cell.setCellType(Cell.CELL_TYPE_BLANK)
                return
            }

            switch (dataValue) {
                case Number:
                    def data = ((Number) dataValue).toDouble()
                    cell.setCellValue(data)
                    break
                case Calendar:
                case Date:
                    cell.setCellStyle(ec.dateStyle)
                    cell.setCellValue((Date) dataValue)
                    break
                case String:
                    cell.setCellValue((String) dataValue)
                    break
                case Boolean:
                    cell.setCellValue((boolean) dataValue)
                    break
                case byte[]:
                    cell.setCellValue('[BINARY_DATA]')
                    break
                default:
                    cell.setCellValue(dataValue?.toString())
            }

        } catch (Exception ex) {
            log.error("Could not set cell data [$ex.message]")
            cell.setCellType(Cell.CELL_TYPE_ERROR)
        }
    }


}
