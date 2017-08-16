package fuzzycsv

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@Log4j
//@CompileStatic
class Excel2Csv {

    static Map<String, FuzzyCSVTable> toCsv(File file) {
        Workbook wb = new XSSFWorkbook(file);
        return allSheetsToCsv(wb)

    }

    static Map<String, FuzzyCSVTable> allSheetsToCsv(Workbook wb) {
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        Map<String, FuzzyCSVTable> entries = wb.collectEntries { Sheet sheet -> [sheet.sheetName, sheetToCsvImpl(sheet, fe)] }
        return entries


    }

    static FuzzyCSVTable toCsv(Workbook wb, int sheetNo = 0) {
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        def sheet = wb.getSheetAt(sheetNo)
        return sheetToCsvImpl(sheet, fe)

    }

    static FuzzyCSVTable sheetToCsv(Sheet sheet, int startRow = 0, int endRow = Integer.MAX_VALUE) {
        def evaluator = sheet.workbook.creationHelper.createFormulaEvaluator()
        return sheetToCsvImpl(sheet, evaluator, startRow, endRow)

    }

    private static FuzzyCSVTable sheetToCsvImpl(Sheet sheet, FormulaEvaluator fe, int startRow = 0, int endRow = Integer.MAX_VALUE) {
        List<List> result = []
        for (Row row in sheet) {
            if (row == null) { continue }
            def csvRow = row.collect { Cell cell -> getCellValue(fe, cell) }
            result.add(csvRow)
        }
        return FuzzyCSVTable.tbl(result)
    }

    static def getCellValue(FormulaEvaluator fe, Cell cell) {

        if (cell == null) return null

        if (cell.getCellTypeEnum() == CellType.FORMULA) {
            cell = fe.evaluateInCell(cell);
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
                return "!!ERROR!!"

        }
        log.error("Unable to read data from ")
        return "!!UNKNOWN DATA TYPE!!"
    }

}
