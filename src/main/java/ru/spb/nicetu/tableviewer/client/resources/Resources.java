package ru.spb.nicetu.tableviewer.client.resources;

/**
 * Импортирует ресурсы
 * @author root
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface Resources extends ClientBundle {
    Resources INSTANCE = GWT.create(Resources.class);

    @Source({"images/add.png"})
    com.google.gwt.resources.client.ImageResource imgAdd();

    @Source({"images/del.png"})
    com.google.gwt.resources.client.ImageResource imgDel();

    @Source({"images/upload.png"})
    com.google.gwt.resources.client.ImageResource imgUpload();

    @Source({"images/addup.png"})
    com.google.gwt.resources.client.ImageResource imgAddUp();

    @Source({"images/delup.png"})
    com.google.gwt.resources.client.ImageResource imgDelUp();
}