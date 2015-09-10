/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ru.spb.nicetu.tableviewer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import gwtupload.client.*;
import ru.spb.nicetu.tableviewer.client.layoutsettings.LayoutSettingsModel;
import ru.spb.nicetu.tableviewer.client.layoutsettings.LayoutSettingsPanel;

/**
 * Начальная точка приложения
 * TableViewer
 */
public class TableViewer implements EntryPoint {

    /**
     * Элемент , описывающий текущее состояние (Нет действий , загрузка файла и т.д)
     */
    final HTML actionLabel = new HTML("Нет действий...");
    /**
     * Элемент, куда создается таблица
     */
    final HTML tableHTML = new HTML();
    TabPanel tabPanel = new TabPanel();
    final TextBox filePath = new TextBox();
    final TextBox fileNameBox = new TextBox();
    public static final String[] COLUMN_NAMES = new String[]{"Наименование","Дата начала","Дата окончания","Время начала","Время окончания",
    "Продолжительность (час)","Отвественное подразделение (наименование)","Ответственное лицо (ФИО)","Примечание"};
    private HorizontalPanel mainPanel;
    private LayoutSettingsPanel layoutSettingsPanel;

    public void onModuleLoad() {
        mainPanel = new HorizontalPanel();
        VerticalPanel leftPanel = new VerticalPanel();
        final HorizontalPanel hp = new HorizontalPanel();
        tabPanel.add(tableHTML, "");
        tabPanel.add(new Label(""), "");
        tabPanel.selectTab(0);
        final ScrollPanel scrollPanel = new ScrollPanel(tabPanel);
//        final ScrollPanel scrollPanel = new ScrollPanel(tableHTML);
        scrollPanel.setHeight("600px");

        final Label label = new Label("Выберите файл (xls/xlsx/ods): ");
        hp.add(label);

        HorizontalPanel hpFile = new HorizontalPanel();
        fileNameBox.setWidth("200px");
        fileNameBox.setEnabled(false);
        hpFile.add(fileNameBox);

        final UploadButton uploadButton = new UploadButton(16,16);
        final SingleUploader uploader = new SingleUploader(IFileInput.FileInputType.CUSTOM.with(uploadButton), new ModalUploadStatus());
        uploader.setServletPath("tableviewer/xlsUpload");
        uploader.setAutoSubmit(true);
        uploader.addOnChangeUploadHandler(new IUploader.OnChangeUploaderHandler() {
            public void onChange(IUploader iUploader) {
                String filePathStr = iUploader.getFileInput().getFilename();
                if (!filePathStr.toLowerCase().endsWith("xls") && !filePathStr.toLowerCase().endsWith("xlsx") &&
                        !filePathStr.toLowerCase().endsWith("ods")) {
                    Window.alert("Загруженный файл не формата xls/xlsx/ods!");
                    uploader.cancel();
                    actionLabel.setStyleName("errorLabel");
                    actionLabel.setHTML("Ошибка загрузки файла.");
                }
            }
        });
        uploader.addOnStatusChangedHandler(new IUploader.OnStatusChangedHandler() {
            public void onStatusChanged(IUploader uploader) {
                tableHTML.setHTML("");
                tabPanel.getTabBar().setTabText(0, "");
                actionLabel.setStyleName("processLabel");
                if(layoutSettingsPanel!=null) {
                    mainPanel.remove(layoutSettingsPanel);
                }
                actionLabel.setHTML("Начинаем загрузку файла");
                if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                    IUploader.UploadedInfo info = uploader.getServerInfo();
                    fileNameBox.setText(info.name);
                    filePath.setText(info.message);
                    createHTMLTable();
                }
            }
        });
        hpFile.add(uploader);
        hp.add(hpFile);

        leftPanel.add(hp);
        leftPanel.add(actionLabel);
        leftPanel.add(scrollPanel);

        mainPanel.add(leftPanel);

        RootPanel.get().add(mainPanel);
    }

    private void createHTMLTable() {
        String filePathStr = filePath.getValue();

        actionLabel.setStyleName("processLabel");
        actionLabel.setHTML("Идет построение формы...");
        XlsViewerService.App.getInstance().buildHtml(filePathStr, false, new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                actionLabel.setStyleName("errorLabel");
                actionLabel.setHTML("Ошибка при построении формы: " + (caught != null ? caught.getMessage() : "смотрите журнал работы сервера"));
            }

            public void onSuccess(String result) {
                if (result == null) {
                    actionLabel.setStyleName("errorLabel");
                    actionLabel.setHTML("Ошибка при построении формы: смотрите журнал работы сервера.");
                }
                else {
                    actionLabel.setStyleName("successLabel");
                    actionLabel.setHTML("Форма построена");
                    tableHTML.setHTML(result);
                    tabPanel.getTabBar().setTabText(0, "Файл");

                    layoutSettingsPanel = new LayoutSettingsPanel(new LayoutSettingsModel(COLUMN_NAMES), tabPanel, "Укажите столбцы с данными для загрузки");
                    mainPanel.add(layoutSettingsPanel);
                }
            }
        });
    }




}
