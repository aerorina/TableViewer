package ru.spb.nicetu.tableviewer.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.core.client.GWT;

/**
 * @author rlapin
 */
@RemoteServiceRelativePath("ExportTableService")
public interface ExportTableService extends RemoteService {
    void exportTable(String tableString);

    /**
     * Utility/Convenience class.
     * Use ExportTableService.App.getInstance() to access static instance of DownloadFileServiceAsync
     */
    public static class App {
        private static final ExportTableServiceAsync ourInstance = (ExportTableServiceAsync) GWT.create(ExportTableService.class);

        public static ExportTableServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
