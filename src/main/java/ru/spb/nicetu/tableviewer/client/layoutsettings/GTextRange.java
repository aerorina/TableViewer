package ru.spb.nicetu.tableviewer.client.layoutsettings;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;

/**
 * Компонент для задания диапазона значений
 * @author root
 */
public class GTextRange extends Composite {
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

    private void setupWidget(IntegerBox box) {
        box.getElement().setAttribute("type", "number");
        box.setWidth("60px");
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
    }

    /**
     * Задать правую границу диапазона
     * @param value значение правой границы диапазона
     */
    public void setEndValue(int value){
        endBox.setValue(value);
    }

}