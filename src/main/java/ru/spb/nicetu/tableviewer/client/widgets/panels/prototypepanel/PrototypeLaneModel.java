package ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Модель компонента строка панели макетирования
 * @author rlapin
 */
public class PrototypeLaneModel {
    /**
     * Название колонки выходной таблицы
     */
    private String columnName;
    /**
     * Количество столбцов во входной таблице
     */
    private int inputColumnCount;

    /**
     * Выбрана ли данная колонка
     */
    private boolean columnChecked;
    /**
     * Список индексов колонок , которые будут использоваться в формировании данных таблицы
     */
    private List<Integer> indices = new ArrayList<Integer>();


    public PrototypeLaneModel(String columnName, boolean columnChecked, int inputColumnCount) {
        this.columnName = columnName;
        this.columnChecked = columnChecked;
        this.inputColumnCount = inputColumnCount;
        // Добавляем нулевой индекс т.к по умолчанию у нас не выбрана колонка входной таблицы
        indices.add(0);
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isColumnChecked() {
        return columnChecked;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public int getInputColumnCount() {
        return inputColumnCount;
    }
}
