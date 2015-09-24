package ru.spb.nicetu.tableviewer.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ru.spb.nicetu.tableviewer.client.ExportTableService;

/**
 * @author rlapin
 */
public class ExportTableServiceImpl extends RemoteServiceServlet implements ExportTableService {
    @Override public void exportTable(String tableString) {
        File file = new File("output.html");
        BufferedWriter bufferedWriter = null;
        try {

            bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.append("<!DOCTYPE html><html><head> <meta charset=\"UTF-8\"><title>Выходной файл</title></head><body>");
            bufferedWriter.write(tableString);
            bufferedWriter.append("</body></html>");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert bufferedWriter != null;
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}