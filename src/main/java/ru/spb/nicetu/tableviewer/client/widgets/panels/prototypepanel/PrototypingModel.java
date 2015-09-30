package ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Модель панели макетирования
 * @author rlapin
 */
public class PrototypingModel {


    /**
     * Список с именами колонок выходной таблицы
     */
    private final List<String> columnsNames;
    /**
     * Список с подсказками к колонкам выходной таблицы
     */
    private final List<String> columnsTooltips;
    /**
     * Индекс строки, с которой начинается выводимый диапазон
     */
    private int startRow;
    /**
     * Индекс строки , которой оканчивается выводимый диапазон
     */
    private int endRow;

    /**
     * @param columnsNames массив имен колонок
     * @param tooltips массив подсказок к колонкам
     */
    public PrototypingModel(String[] columnsNames, String[] tooltips) {
        this.columnsNames = new ArrayList<String>();
        for (String column : columnsNames) {
            this.columnsNames.add(column);
        }

        columnsTooltips = new ArrayList<String>();
        for (String column : tooltips) {
            columnsTooltips.add(column);
        }
    }

    /**
     *
     * @return индекс строки , с которой начинается выходной диапазон
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Задать индекс строки, с которой начинается выходной диапазон
     * @param startRow индекс строки
     */
    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
    /**
     *
     * @return индекс строки , которой заканчивается выходной диапазон
     */
    public int getEndRow() {
        return endRow;
    }
    /**
     * Задать индекс строки, которой заканчивается выходной диапазон
     * @param endRow индекс строки
     */
    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    /**
     * Возвращает наименование колонки по индексу
     * @param index индекс колонки
     * @return имя колонки
     */
    public String getColumnName(int index){
        return columnsNames.get(index);
    }

    /**
     * Возвращает подсказку к колонке по индексу
     * @param index индекс колонки
     * @return подсказка к колонке
     */
    public String getColumnTooltip(int index){
        return columnsTooltips.get(index);
    }

    /**
     * @return Количество колонок
     */
    public int getColumnsCount(){
        return columnsNames.size();
    }


}
