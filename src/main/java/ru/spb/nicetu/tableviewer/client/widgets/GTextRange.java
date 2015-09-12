package ru.spb.nicetu.tableviewer.client.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.ChangeListener;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.DefaultChangeListener;

/**
 * Компонент для задания диапазона значений<br>
 * Для задания стилей самого компонента используйте класс <b>.gwt-rangeTextLane</b><br>
 * Для задания стилей для полей ввода используйте класс <b>.gwt-rangeTextLane input[type=number]</b><br>
 * Для задания стилей для разделителя используйте класс <b>.gwt-rangeTextLane .gwt-Label</b><br>
 * @author rlapin
 */
public class GTextRange extends Composite {
    private ChangeListener changeListener = new DefaultChangeListener();
    private final IntegerBox startBox;
    private final IntegerBox endBox;

    public GTextRange() {
        HorizontalPanel textPanel = new HorizontalPanel();
        textPanel.setStyleName("gwt-rangeTextLane");
        endBox = new IntegerBox();
        startBox = new IntegerBox();
        setupWidget(endBox);
        setupWidget(startBox);
        textPanel.add(startBox);
        textPanel.add(new Label("-"));
        textPanel.add(endBox);


        initWidget(textPanel);

    }

    /**
     * Задать слушателя для события изменение значения
     * @param changeListener
     */
    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    private void setupWidget(IntegerBox box) {
        box.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                valueChanged();
            }
        });
        box.getElement().setAttribute("type", "number");
        box.setWidth("60px");
    }

    /**
     * Сообщает слушателю об изменении значений в компоненте
     */
    private void valueChanged() {
        changeListener.onChanged();
    }

    public int getStartValue() {
        try {

            return startBox.getValueOrThrow();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getEndValue() {
        try {

            return endBox.getValueOrThrow();
        } catch (Exception e) {
            return 0;
        }
    }
    /**
     * Задать левую границу диапазона
     * @param value значение левой границы диапазона
     */
    public void setStartValue(int value){
        startBox.setValue(value);
        valueChanged();
    }

    /**
     * Задать правую границу диапазона
     * @param value значение правой границы диапазона
     */
    public void setEndValue(int value){
        endBox.setValue(value);
        valueChanged();
    }

}