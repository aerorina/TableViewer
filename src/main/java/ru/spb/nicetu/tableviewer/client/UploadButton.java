package ru.spb.nicetu.tableviewer.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import ru.spb.nicetu.tableviewer.client.resources.Resources;


/**
 * Кнопка-виджет для использования в uploader-e файлов
 * <p/>
 * User: sromashkin
 * Date: 14.02.14
 * Time: 16:15
 */
public class UploadButton extends Composite implements HasClickHandlers {


    public UploadButton(int width, int height) {
        Image load = new Image(Resources.INSTANCE.imgUpload());
        load.setTitle("Загрузка");
        load.setAltText("Загрузка");
        load.setPixelSize(width, height);

        initWidget(load);
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }
}