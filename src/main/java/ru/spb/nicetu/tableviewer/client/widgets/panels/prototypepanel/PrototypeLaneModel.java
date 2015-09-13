package ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel;

import ru.spb.nicetu.tableviewer.client.widgets.listeners.ChangeListener;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.DefaultChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Модель компонента строка панели макетирования
 *
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
    /**
     * Значение текущей входной колонки
     */
    private int currentIndex;
    /**
     * Слушатель изменения текущей колонки входной таблицы
     */
    private ChangeListener columnChangeListener;


    public PrototypeLaneModel(String columnName, boolean columnChecked, int inputColumnCount) {
        this.columnName = columnName;
        this.columnChecked = columnChecked;
        this.inputColumnCount = inputColumnCount;
        columnChangeListener = new DefaultChangeListener();
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

    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Увеличить значение текущей колонки на 1
     */
    public void incCurrentIndex() {
        if (++currentIndex >= indices.size()) {
            currentIndex = 0;
        }
        columnChangeListener.onChanged();
    }

    /**
     * Задать значение текущей колонки , как последнее в списке колонок
     */
    public void setCurrentIndexToLast() {
        currentIndex = getIndices().size() - 1;
        columnChangeListener.onChanged();

    }

    /**
     * Получить значение текущей колонки входной таблицы и увеличить её на 1
     *
     * @return значение текущей колонки входной таблицы
     */
    public int getAndIncCurrentIndex() {
        int returnIndex = currentIndex;
        incCurrentIndex();
        return returnIndex;
    }

    public void setColumnChangeListener(ChangeListener listener) {
        columnChangeListener = listener;
    }

    public void setColumnChecked(boolean columnChecked) {
        this.columnChecked = columnChecked;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
}
