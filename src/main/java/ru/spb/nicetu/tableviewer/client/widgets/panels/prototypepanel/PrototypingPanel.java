package ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel;

import static com.google.gwt.query.client.GQuery.$;






import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import java.util.ArrayList;
import java.util.List;
import ru.spb.nicetu.tableviewer.client.widgets.GTextRange;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.ChangeListener;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.prototypepanel.LaneChangeListener;

/**
 * Компонент для настройки макета таблицы
 *
 * @author rlapin on 16.08.15.
 */
public class PrototypingPanel extends Composite {

    /**
     * CSS - класс таблицы
     */
    public static final String TABLE_CSS_CLASS = ".excelDefaults";
    public static final String PANEL_CSS_CLASS = "gwt-PrototypingPanel";
    /**
     * Компонент задающий диапазон строк , которые будут входить в выходную таблицу
     */
    private GTextRange textRange;
    /**
     * Количество столбцов в входной таблице
     */
    private int inputTableColumnCount;
    /**
     * Выходная колонка
     */
    private int outputColumn;
    /**
     * Выбираем начальный диапазон или конечный?
     */
    private boolean isSelectStartRange = true;
    final private PrototypingModel model;
    private List<PrototypeLane> prototypeLaneList;
    private TabPanel tabPanel;
    /**
     * Заголовок панели
     */
    private String title;


    /**
     * @param model модель компонента {@link PrototypingModel}
     */
    public PrototypingPanel(PrototypingModel model, TabPanel tabPanel, String title) {
        this.model = model;
        this.tabPanel = tabPanel;
        this.title = title;
        initComponents();
        initTableListeners();
    }


    private void initComponents() {
        VerticalPanel settingsPanel = new VerticalPanel();
        settingsPanel.setStyleName(PANEL_CSS_CLASS);
        Label label = new Label(title);
        label.setStyleName("labeldesc");
        settingsPanel.add(label);
        inputTableColumnCount = $(TABLE_CSS_CLASS + " th").length() - 1;
        prototypeLaneList = new ArrayList<PrototypeLane>();
        for (int i = 0; i < model.getColumnsCount(); i++) {
            final PrototypeLaneModel prototypeLaneModel = new PrototypeLaneModel(this.model.getColumnName(i), i == 0, inputTableColumnCount);
            PrototypeLane prototypeLane = new PrototypeLane(prototypeLaneModel);
            final int index = i;
            prototypeLane.setListener(new LaneChangeListener() {
                @Override
                public void laneSelected() {
                    changeItemHandler(index);
                }


            });
            settingsPanel.add(prototypeLane);
            prototypeLaneList.add(prototypeLane);
        }
        settingsPanel.add(createRangePanel());
        settingsPanel.add(createPerformButton());

        initWidget(settingsPanel);
    }

    /**
     * Создание панели для задания диапазона строк в выходной таблице<br>
     *
     * @return панель задания диапазона строк. Для изменения стиля использовать класс <b>.gwt-rangeTextLane</b><br>
     */
    private HorizontalPanel createRangePanel() {
        HorizontalPanel rangePanel = new HorizontalPanel();
        rangePanel.setStyleName("gwt-rangeTextLane");
        Label rangeLabel = new Label("Диапазон строк для обработки: ");
        textRange = new GTextRange();
        textRange.setChangeListener(new ChangeListener() {
            @Override
            public void onChanged() {
                model.setStartRow(textRange.getStartValue());
                model.setEndRow(textRange.getEndValue());
                highlightRows();
            }
        });
        rangePanel.add(rangeLabel);

        rangePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rangePanel.add(textRange);
        textRange.getStartTextBox().addStyleName("activeitem");
        return rangePanel;
    }


    /**
     * Создать кнопку для обработки таблицы согласно макету
     *
     * @return gwt-кнопка
     */
    private Button createPerformButton() {
        Button btnPerform = new Button("Обработать загруженный файл");
        btnPerform.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                processInputFile();
            }
        });
        return btnPerform;
    }

    /**
     * Обработать входной файл
     */
    private void processInputFile() {

        final int startRow = model.getStartRow();
        final int endRow = model.getEndRow();
        if (startRow < endRow) {
            HTML html = new HTML();
            final List<Integer> duplicateList = createDuplicateColumnList();
            findDuplicates(duplicateList);
            final StringBuilder stringBuilder = new StringBuilder("<table>");
            writeTableColumns(stringBuilder);
            $(TABLE_CSS_CLASS + " tr").each(new Function() {
                @Override
                public void f() {
                    if ($(this).index() >= startRow - 1 && $(this).index() < endRow) {
                        stringBuilder.append("<tr>");
                        // для дублирующихся колонок
                        int curColumnNum = 0;
                        // Есть ли ссылки на колонки входной таблицы в данной строке

                        for (PrototypeLane prototypeLane : prototypeLaneList) {
                            List<Integer> indices = prototypeLane.getModel().getIndices();
                            boolean containLinks = false;
                            for (int i : indices) {
                                if (i != 0) {
                                    String tableValue = getTableValueByIndex($(this), i);
                                    // Несколько столбцов ссылаются на одну колонку входной таблицы
                                    if (duplicateList.get(i) > 1) {
                                        tableValue = tableValue.split(" ")[curColumnNum++];
                                    }
                                    if(!containLinks) {
                                        stringBuilder.append("<td>");
                                        containLinks = true;
                                    }
                                    stringBuilder.append(tableValue).append(" ");
                                }
                            }
                            if (containLinks) {
                                stringBuilder.append("</td>");
                            }
                        }
                        stringBuilder.append("</tr>");
                    }
                }


            });

            stringBuilder.append("</table>");
            html.setHTML(stringBuilder.toString());
            if (tabPanel.getWidgetCount() == 2) {
                tabPanel.remove(1);
            }
            if (tabPanel.getWidgetCount() == 1) {
                tabPanel.add(html, "Выборка");
            }
            tabPanel.selectTab(1);

        } else {
            AlertMessageBox messageBox = new AlertMessageBox("Ошибка", "Диапазон строк для обработки задан некорректно");
            messageBox.show();

        }
    }

    /**
     * Вывод колонок таблицы
     *
     * @param stringBuilder буфер куда выводится таблица
     */
    private void writeTableColumns(StringBuilder stringBuilder) {
        stringBuilder.append("<tr>");
        for (PrototypeLane prototypeLane : prototypeLaneList) {
            PrototypeLaneModel prototypeModel = prototypeLane.getModel();
            List<Integer> indices = prototypeModel.getIndices();
            if (containsOnlyZeros(indices)) {
                continue;
            }
            stringBuilder.append("<th>").append(prototypeModel.getColumnName()).append("</th>");
        }
        stringBuilder.append("</tr>");
    }

    /**
     * Проверка на то , что список состоит только из 0
     *
     * @param indices список значений
     * @return true, если список состоит только из 0
     */
    private boolean containsOnlyZeros(List<Integer> indices) {
        for (Integer i : indices) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Создать список с количество повторяющийхся колонок
     *
     * @return список с количеством колонок
     */
    private List<Integer> createDuplicateColumnList() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < model.getColumnsCount(); i++) {
            list.add(0);
        }
        return list;
    }

    /**
     * Поиск одинаковых колонок, на которые ссылаются выходные колонки и заполнение списка количеством дубликатов
     *
     * @param list список с количеством встречающихся колонок в различных выходных колонках
     */
    private void findDuplicates(List<Integer> list) {
        for (PrototypeLane prototypeLane : prototypeLaneList) {
            List<Integer> indices = prototypeLane.getModel().getIndices();
            for (int i = 0; i < model.getColumnsCount(); i++) {
                if (indices.contains(i)) {
                    list.set(i, list.get(i) + 1);
                }
            }
        }
    }

    /**
     * Получить значение из столбца таблицы
     *
     * @param row   строка таблица в терминах GwtQuery
     * @param index номер столбца таблицы
     * @return текстовое значение ячейки таблицы
     */
    private String getTableValueByIndex(GQuery row, final int index) {

        final String[] result = new String[1];
        row.children("td").each(new Function() {
            int val = 0;

            @Override
            public void f() {
                GQuery col = $(this);
                if (!col.attr("colspan").isEmpty()) {
                    int span = Integer.parseInt(col.attr("colspan"));
                    val += span;
                } else {
                    val++;
                    if (val == index + 1) {

                        result[0] = col.html();
                    }
                }

            }
        });
        return result[0];
    }

    /**
     * Иницилизация слушателей для строк и столбцов таблицы
     */
    private void initTableListeners() {
        initColumnHeaderListeners();
        initRowHeaderListener();
    }

    /**
     * Иницилизация слушателей для заголовков строк(1 столбец таблицы)
     */
    private void initRowHeaderListener() {

        $(TABLE_CSS_CLASS + " tr td:first-child").click(new Function() {
            @Override
            public boolean f(Event e) {
                GQuery parent = $(this).parent();
                if (isSelectStartRange) {
                    textRange.setStartValue(parent.index() + 1);
                } else {
                    textRange.setEndValue(parent.index() + 1);
                }
                highlightRows();
                isSelectStartRange = !isSelectStartRange;
                if (isSelectStartRange) {
                    textRange.getEndTextBox().removeStyleName("activeitem");
                    textRange.getStartTextBox().addStyleName("activeitem");
                } else {
                    textRange.getStartTextBox().removeStyleName("activeitem");
                    textRange.getEndTextBox().addStyleName("activeitem");
                }
                return true;
            }
        });

    }

    /**
     * Выделить строки таблицы
     */
    private void highlightRows() {

        final int startRow = model.getStartRow();
        final int endRow = model.getEndRow();
        $(TABLE_CSS_CLASS + " tr").removeClass("selrow");
        if (startRow < endRow) {
            for (int i = startRow; i <= endRow; i++) {
                $(TABLE_CSS_CLASS + " tr").eq(i).addClass("selrow");
            }
        }


    }

    /**
     * Иницилизация слушателей для столбцов таблицы
     */
    private void initColumnHeaderListeners() {
        $(TABLE_CSS_CLASS + " th").click(new Function() {
            @Override
            public boolean f(Event e) {
                int index = $(this).index();
                prototypeLaneList.get(outputColumn).selectColumn(index);


                return true;
            }
        });
    }

    /**
     * Действие по выбору элемента в панели
     *
     * @param index
     */
    private void changeItemHandler(int index) {
        outputColumn = index;
        highlightColumns();
    }

    /**
     * Подсветка колонок входной таблицы для выбранного выходного столбца
     */
    private void highlightColumns() {

        PrototypeLaneModel model = prototypeLaneList.get(outputColumn).getModel();
        $(TABLE_CSS_CLASS + " tr td").removeClass("selcolumn");
        for (final int index : model.getIndices()) {
            if (index != 0) {
                $(TABLE_CSS_CLASS + " tr").each(new Function() {
                    int val;

                    @Override
                    public void f() {
                        val = 0;
                        $(this).children("td").each(new Function() {
                            @Override
                            public void f() {
                                GQuery col = $(this);

                                if (!col.attr("colspan").isEmpty()) {
                                    int span = Integer.parseInt(col.attr("colspan"));
                                    val += span;
                                } else {
                                    val++;
                                    if (val == index + 1) {
                                        $(this).addClass("selcolumn");
                                    }
                                }


                            }
                        });

                    }
                });
            }
        }
    }


}