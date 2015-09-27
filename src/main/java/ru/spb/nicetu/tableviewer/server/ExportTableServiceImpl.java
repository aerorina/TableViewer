package ru.spb.nicetu.tableviewer.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import ru.spb.nicetu.tableviewer.client.ExportTableService;

/**
 * Сервис, отвечающий за конвертацию html в xls и создание файла под электронную таблицу<br>
 * Имя выходного файла описывается в SHEET_NAME
 * @author rlapin
 */
public class ExportTableServiceImpl extends RemoteServiceServlet implements ExportTableService {

    @Override public void exportTable(String tableString) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream("output.xls");
            HtmlToXlsConverter htmlToXlsConverter = new HtmlToXlsConverter(tableString);
            htmlToXlsConverter.setAutoSize(true);
            Workbook workbook = htmlToXlsConverter.convert();
            workbook.write(fileOut);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}