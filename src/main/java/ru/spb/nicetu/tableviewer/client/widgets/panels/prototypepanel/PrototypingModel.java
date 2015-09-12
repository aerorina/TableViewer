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
     * Определяет связь между выходной таблицей и обрабатываемым файлом
     * Ключом является номер столбца выходной таблицы
     * Значением является номер столбцы входной таблицы
     */
    private Map<Integer, Integer> links = new HashMap<Integer, Integer>();
    /**
     * Список с именами колонок выходной таблицы
     */
    private final List<String> columnsNames;
    /**
     * Индекс строки, с которой начинается выводимый диапазон
     */
    private int startRow;
    /**
     * Индекс строки , которой оканчивается выводимый диапазон
     */
    private int endRow;

    /**
     * @param columns массив имен колонок
     */
    public PrototypingModel(String[] columns) {
        columnsNames = new ArrayList<String>();
        for (String column : columns) {
            columnsNames.add(column);
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
     * @return Количество колонок
     */
    public int getColumnsCount(){
        return columnsNames.size();
    }

    /**
     * Добавить значение связи выходной таблицы со входной
     * @param outputIndex колонка выходной таблицы
     * @param inputIndex колонка входной таблицы
     */
    public void putLinkValue(int outputIndex, int inputIndex){
        links.put(outputIndex, inputIndex);
    }

    /**
     * Получить список связей
     * @return список связей входной и выходной таблиц
     */
    public Map<Integer,Integer> getLinks() {
        return links;
    }

    /**
     * Удалить значение связи
     * @param outputColumn колонка выходной таблицы
     */
    public void removeLinkValue(int outputColumn) {
        links.remove(outputColumn);
    }
}
