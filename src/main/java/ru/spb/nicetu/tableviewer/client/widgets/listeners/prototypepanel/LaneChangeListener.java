package ru.spb.nicetu.tableviewer.client.widgets.listeners.prototypepanel;

/**
 * Слушатель событий по изменению значений в компоненте строка панели макетирования
 * @author rlapin
 */
public interface LaneChangeListener {
    /**
     * Строка выбрана
     */
    void laneSelected();

    /**
     * Изменена колонка входной таблицы
     */
    void inputColumnSet();

    /**
     * Добавлена новая колонка
     */
    void inputColumnAdded();

    /**
     * Удалена колонка
     */
    void inputColumnRemoved();
}
