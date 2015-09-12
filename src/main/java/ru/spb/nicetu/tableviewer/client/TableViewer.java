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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel.PrototypingModel;
import ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel.PrototypingPanel;
import ru.spb.nicetu.tableviewer.client.resources.Resources;

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
    private String filePath;
    private FileUpload fileUpload;
    final HTML tableHTML = new HTML();
    TabPanel tabPanel = new TabPanel();

    final TextBox fileNameBox = new TextBox();
    public static final String[] COLUMN_NAMES = new String[]{"Наименование","Дата начала","Дата окончания","Время начала","Время окончания",
    "Продолжительность (час)","Отвественное подразделение (наименование)","Ответственное лицо (ФИО)","Примечание"};
    private HorizontalPanel mainPanel;
    private PrototypingPanel prototypingPanel;

    public void onModuleLoad() {
        mainPanel = new HorizontalPanel();
        VerticalPanel leftPanel = new VerticalPanel();
        final HorizontalPanel hp = new HorizontalPanel();
        tabPanel.add(tableHTML, "Файл");
        tabPanel.setVisible(false);
        final ScrollPanel scrollPanel = new ScrollPanel(tabPanel);
//        final ScrollPanel scrollPanel = new ScrollPanel(tableHTML);
        scrollPanel.setHeight("600px");

        final Label label = new Label("Выберите файл (xls/xlsx/ods): ");
        hp.add(label);

        HorizontalPanel hpFile = new HorizontalPanel();
        fileNameBox.setWidth("200px");
        fileNameBox.setEnabled(false);
        hpFile.add(fileNameBox);
        final FormPanel formPanel = createUploadForm();


        Image image = new Image(Resources.INSTANCE.imgUpload());
        image.setSize("24px","24px");
        image.addClickHandler(new ClickHandler() {
            @Override public void onClick(ClickEvent event) {
                fileUpload.click();
            }
        });
        hp.add(hpFile);
        hp.add(image);
        formPanel.setVisible(false);
        leftPanel.add(formPanel);
        leftPanel.add(hp);
        leftPanel.add(actionLabel);
        leftPanel.add(scrollPanel);

        mainPanel.add(leftPanel);

        RootPanel.get().add(mainPanel);
    }
    private FormPanel createUploadForm() {
        final FormPanel formPanel = new FormPanel();
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.setAction("/tableviewer/upload");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        fileUpload = new FileUpload();
        fileUpload.setTitle("Выберите файл");

        fileUpload.setName("file-upload");
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                formPanel.submit();
            }
        });


        // Add an event handler to the form.
        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit(FormPanel.SubmitEvent event) {
                // This event is fired just before the form is submitted. We can take
                // this opportunity to perform validation.
                String filePathStr = fileUpload.getFilename();
                if (!filePathStr.toLowerCase().endsWith("xls") && !filePathStr.toLowerCase().endsWith("xlsx") &&
                        !filePathStr.toLowerCase().endsWith("ods")) {
                    event.cancel();
                    actionLabel.setStyleName("errorLabel");
                    actionLabel.setHTML("Ошибка загрузки файла.");
                }
            }
        });
        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                // When the form submission is successfully completed, this event is
                // fired. Assuming the service returned a response of type text/html,
                // we can get the result text here (see the FormPanel documentation for
                // further explanation).
                tableHTML.setHTML("");
                tabPanel.setVisible(false);

                actionLabel.setStyleName("processLabel");
                if (prototypingPanel != null) {
                    mainPanel.remove(prototypingPanel);
                }
                filePath = event.getResults();
                actionLabel.setHTML("Начинаем загрузку файла");
                String filename = fileUpload.getFilename();
                filename = filename.substring(filename.lastIndexOf("\\")+1);
                fileNameBox.setText(filename);
                createHTMLTable();

            }
        });
        formPanel.add(fileUpload);
        return formPanel;
    }
    private void createHTMLTable() {


        actionLabel.setStyleName("processLabel");
        actionLabel.setHTML("Идет построение формы...");
        XlsViewerService.App.getInstance().buildHtml(filePath, false, new AsyncCallback<String>() {
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
                    tabPanel.selectTab(0);
                    tabPanel.setVisible(true);
                    tabPanel.setWidth("600px");
                    prototypingPanel = new PrototypingPanel(new PrototypingModel(COLUMN_NAMES), tabPanel,"Укажите столбцы с данными для загрузки");
                    mainPanel.add(prototypingPanel);
                }
            }
        });
    }




}
