package ru.spb.nicetu.tableviewer.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author rlapin
 */
public interface ExportTableServiceAsync {
    void exportTable(String tableString, AsyncCallback<Void> async);
}
