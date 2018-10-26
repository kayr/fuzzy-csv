package fuzzycsv

import groovy.transform.CompileStatic
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class Excel2Csv {

    private static Logger log = LoggerFactory.getLogger(Excel2Csv)

    static void testClassPath() {
        try {
            Class.forName('org.apache.poi.ss.usermodel.Workbook')
        } catch (Throwable e) {
            log.error("Apache Poi No Found.")
            printRequiredDependencies()
            throw e
        }
    }

    static void printRequiredDependencies() {
        println("""Add to Gradle:
     compileOnly 'org.apache.poi:poi-ooxml:3.16', {
        exclude group: 'stax', module: 'stax-api'
    }
    compileOnly 'org.apache.poi:ooxml-schemas:1.3', {
        exclude group: 'stax', module: 'stax-api'
    }""")
    }

    static Map<String, FuzzyCSVTable> toCsv(File file, int startRow = 0, int endRow = Integer.MAX_VALUE) {

        Workbook wb = null;
        if (file.name.endsWith(".xls")) {
            file.withInputStream {
                wb = new HSSFWorkbook(it)
            }
        } else {
            wb = new XSSFWorkbook(file);
        }
        return allSheetsToCsv(wb, startRow, endRow)

    }

    static Map<String, FuzzyCSVTable> allSheetsToCsv(Workbook wb, int startRow = 0, int endRow = Integer.MAX_VALUE) {
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator()
        Map<String, FuzzyCSVTable> entries = wb.collectEntries { Sheet sheet -> [sheet.sheetName, sheetToCsvImpl(sheet, fe, startRow, endRow)] }
        return entries


    }

    static FuzzyCSVTable toCsv(Workbook wb, int sheetNo = 0) {
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator()
        def sheet = wb.getSheetAt(sheetNo)
        return sheetToCsvImpl(sheet, fe)

    }

    static FuzzyCSVTable sheetToCsv(Sheet sheet, int startRow = 0, int endRow = Integer.MAX_VALUE) {
        def evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()
        return sheetToCsvImpl(sheet, evaluator, startRow, endRow)

    }

    private
    static FuzzyCSVTable sheetToCsvImpl(Sheet sheet, FormulaEvaluator fe, int startRow = 0, int endRow = Integer.MAX_VALUE) {
        List<List> result = []

        int index = 0
        for (Row row in sheet) {
            if (row == null || index++ < startRow) {
                continue
            }
            def csvRow = row.collect { Cell cell -> getCellValue(fe, cell) }
            result.add(csvRow)

            if (index > endRow) break
        }
        return FuzzyCSVTable.tbl(result)
    }

    static def getCellValue(FormulaEvaluator fe, Cell cell) {

        if (cell == null) return null

        if (cell.getCellTypeEnum() == CellType.FORMULA) {
            cell = fe.evaluateInCell(cell)
        }
        switch (cell.cellTypeEnum) {
            case CellType.BOOLEAN:
                return cell.booleanCellValue
            case CellType.NUMERIC:
                return cell.numericCellValue
            case CellType.STRING:
                return cell.stringCellValue
            case CellType.BLANK:
                return null
            case CellType.ERROR:
                log.error("Unable to read data from cell[{},{}]", cell.rowIndex, cell.columnIndex)
                return "!!ERROR!!"

        }
        return "!!UNKNOWN DATA TYPE!!"
    }

}
