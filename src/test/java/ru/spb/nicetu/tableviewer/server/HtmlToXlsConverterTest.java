package ru.spb.nicetu.tableviewer.server;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Тестирование конвертации html в xls
 * @author rlapin
 */
public class HtmlToXlsConverterTest {

    @Test
    public void testConvert() throws Exception {
        HtmlToXlsConverter htmlToXlsConverter = new HtmlToXlsConverter("<!DOCTYPE html><html><head> <meta charset=\"UTF-8\"><title>Выходной файл</title></head><body><table><tr><td>5 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование1 </td></tr><tr><td>Наименование2 </td></tr><tr><td>Наименование1 </td></tr></table></body></html>");
        Workbook workbook = htmlToXlsConverter.convert();
        assertEquals(workbook.getSheetAt(0).getRow(0).getCell(0).getNumericCellValue(), 5,0.0001);
        assertEquals(workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue(), "Наименование1");
    }
}