package ru.spb.nicetu.tableviewer.server;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

/**
 * Конвертирует таблицу, представленную в виде html тэгов, в xls таблицу
 *
 * @author rlapin
 */
public class HtmlToXlsConverter {
    private static final Logger LOGGER = getLogger(HtmlToXlsConverter.class.getName());
    public static final String TR_TAG = "<tr>";
    private static final String TR_END_TAG = "</tr>";
    private static final String TD_TAG = "<td>";
    private static final String TD_END_TAG = "</td>";
    private static final String TH_TAG = "<th>";
    private static final String TH_END_TAG = "</th>";
    private static final String NO_BR_TAG = "<nobr>";
    private static final String NO_BR_END_TAG = "</nobr>";
    public static final String SHEET_NAME = "Лист 1";
    private String htmlTable;
    private Workbook workbook;
    /**
     * Отвечает за то , нужно ли расширять колонки по содержимому
     */
    private boolean isAutoSize;

    public HtmlToXlsConverter(String htmlTable) {
        this.htmlTable = htmlTable;
        workbook = new HSSFWorkbook();
    }

    /**
     * Конвертировать html таблицу в xls таблицу
     *
     * @return xls таблицу
     */
    public Workbook convert() {
        htmlTable = htmlTable.trim();
        Sheet sheet = workbook.createSheet(SHEET_NAME);
        int nRow = 0;
        while (htmlTable.contains(TR_TAG)) {
            int beginIndex = htmlTable.indexOf(TR_TAG) + TR_TAG.length();
            int endIndex = htmlTable.indexOf(TR_END_TAG);
            String row = htmlTable.substring(beginIndex, endIndex);
            handleRow(sheet, row, nRow++);
            htmlTable = htmlTable.substring(endIndex + TR_END_TAG.length());
        }

        if (isAutoSize) {
            if (nRow > 0) {
                Row row = workbook.getSheetAt(0).getRow(0);
                for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
                    sheet.autoSizeColumn(colNum);
                }
            }
        }
        return workbook;
    }

    /**
     * Конвертировать html строку в xls строку и добавить на лист
     *
     * @param sheet   xls лист
     * @param htmlRow html строка
     * @param nRow    номер строки на листе
     */
    private void handleRow(Sheet sheet, String htmlRow, int nRow) {
        Row row = sheet.createRow(nRow);
        int nCell = 0;
        while (htmlRow.contains(TD_TAG) || htmlRow.contains(TH_TAG)) {
            String tag = getNextCellTag(htmlRow);
            String endTag = getNextCellEndTag(htmlRow);
            int beginIndex = htmlRow.indexOf(tag) + tag.length();
            int endIndex = htmlRow.indexOf(endTag);
            String htmlCell = htmlRow.substring(beginIndex, endIndex);
            handleCell(row, htmlCell, nCell++);
            htmlRow = htmlRow.substring(endIndex + endTag.length());
        }
    }

    /**
     * @param htmlRow html строка
     * @return значение следующего открывающего тэга ячейки (tr или th)
     */
    private String getNextCellTag(String htmlRow) {
        int index1 = htmlRow.indexOf(TD_TAG);
        int index2 = htmlRow.indexOf(TH_TAG);
        return (index1 == -1 ? TH_TAG : index2 == -1 ? TD_TAG : index1 < index2 ? TD_TAG : TH_TAG);
    }

    /**
     * @param htmlRow html строка
     * @return значение следующего закрывающего тэга ячейки (/tr или /th)
     */
    private String getNextCellEndTag(String htmlRow) {
        int index1 = htmlRow.indexOf(TD_END_TAG);
        int index2 = htmlRow.indexOf(TH_END_TAG);
        return (index1 == -1 ? TH_END_TAG : index2 == -1 ? TD_END_TAG : index1 < index2 ? TD_END_TAG : TH_END_TAG);
    }

    /**
     * Конвертировать html ячейку(td или th) в xls ячейку и добавить в строку
     *
     * @param row      xls строка
     * @param htmlCell html ячейка
     * @param nCell    номер ячейки на листе
     */
    private void handleCell(Row row, String htmlCell, int nCell) {
        htmlCell = htmlCell.trim();
        if (htmlCell.contains(NO_BR_TAG)) {
            int startIndex = htmlCell.indexOf(NO_BR_TAG) + NO_BR_TAG.length();
            int endIndex = htmlCell.indexOf(NO_BR_END_TAG);
            htmlCell = htmlCell.substring(startIndex, endIndex);
        }
        Cell cell = row.createCell(nCell);
        CellStyle cellStyle = cell.getCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        try {
            double value = Double.parseDouble(htmlCell);
            cell.setCellValue(value);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.info("Значение:" + htmlCell + " не может быть преобразовано в числовое значение");
            cell.setCellValue(htmlCell);
        }

    }

    public void setAutoSize(boolean isAutoSize) {
        this.isAutoSize = isAutoSize;
    }
}
