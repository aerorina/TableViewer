package ru.spb.nicetu.tableviewer.client.layoutsettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Описывает модель , используемую в компоненте макетирования
 * @author rlapin on 16.08.15.
 */
public class LayoutSettingsModel {

    /**
     * Определяет связь между выходной таблицей и обрабатываемым файлом
     * Ключом является номер столбца выходной таблицы
     * Значением является номер столбцы входной таблицы
     */
    private Map<Integer, Integer> links = new HashMap<Integer, Integer>();
    /**
     * Список с имена колонок
     */
    private final List<String> columnsNames;


    /**
     * @param columns массив имен колонок
     */
    public LayoutSettingsModel(String[] columns) {
        columnsNames = new ArrayList<String>();
        for (String column : columns) {
            columnsNames.add(column);
        }
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
}
