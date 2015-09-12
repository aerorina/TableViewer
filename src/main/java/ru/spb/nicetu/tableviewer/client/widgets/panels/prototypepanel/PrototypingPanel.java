package ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel;

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
import ru.spb.nicetu.tableviewer.client.widgets.GTextRange;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.ChangeListener;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.prototypepanel.LaneChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.gwt.query.client.GQuery.$;

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
     *
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

//            lane.add(createDelBtn());
            final PrototypeLaneModel prototypeLaneModel = new PrototypeLaneModel(this.model.getColumnName(i), i == 0, inputTableColumnCount);
            PrototypeLane prototypeLane = new PrototypeLane(prototypeLaneModel);
            final int index = i;
            prototypeLane.setListener(new LaneChangeListener() {
                @Override
                public void laneSelected() {
                    changeItemHandler(index, prototypeLaneModel.getIndices().get(0));
                }

                @Override
                public void inputColumnSet() {
                    changeItemHandler(index, prototypeLaneModel.getIndices().get(0));
                }

                @Override
                public void inputColumnAdded() {

                }

                @Override
                public void inputColumnRemoved() {

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
            }
        });
        rangePanel.add(rangeLabel);
        rangePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rangePanel.add(textRange);
        return rangePanel;
    }


    /**
     * Создать кнопку для обработки таблицы согласно макету
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
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<table>");
            final Map<Integer, Integer> links = model.getLinks();
            stringBuilder.append("<tr>");
            final int []num = new int[inputTableColumnCount];
            for (Integer key : links.keySet()) {
                stringBuilder.append("<th>").append(model.getColumnName(key)).append("</th>");

                num[links.get(key)]++;
            }
            stringBuilder.append("</tr>");
            $(TABLE_CSS_CLASS + " tr").each(new Function() {
                @Override
                public void f() {
                    if ($(this).index() >= startRow - 1 && $(this).index() < endRow) {
                        stringBuilder.append("<tr>");
                        int curColumnNum = 0;
                        for (Integer key : links.keySet()) {
                            Integer index = links.get(key);
                            if (index != 0) {
                                int columnNum = num[index];
                                String tableValue = getTableValueByIndex($(this), index);
                                if (columnNum > 1) {
                                    tableValue = tableValue.split(" ")[curColumnNum++];
                                }
                                stringBuilder.append("<td>").append(tableValue).append("</td>");
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
     * Получить значение из столбца таблицы
     * @param row строка таблица в терминах GwtQuery
     * @param index номер столбца таблицы
     * @return текстовое значение ячейки таблицы
     */
    private String getTableValueByIndex(GQuery row, final int index) {
        
        final String []result = new String[1];
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
                rowSelected(parent.index());
                return true;
            }
        });
    }

    /**
     * Выбрана строка таблицы
     *
     * @param index номер строки
     */
    private void rowSelected(int index) {

        if (isSelectStartRange) {
            int oldValue = textRange.getStartValue();
            if (oldValue != index + 1) {
//                $(TABLE_CSS_CLASS + " tr").eq(oldValue).removeClass("selrow");
                textRange.setStartValue(index + 1);
//                $(TABLE_CSS_CLASS + " tr").eq(index + 1).addClass("selrow");
            }

        } else {
            int oldValue = textRange.getEndValue();

            if (oldValue != index + 1) {
//                $(TABLE_CSS_CLASS + " tr").eq(oldValue).removeClass("selrow");
                textRange.setEndValue(index + 1);
//                $(TABLE_CSS_CLASS + " tr").eq(index + 1).addClass("selrow");
            }
        }
        isSelectStartRange = !isSelectStartRange;
    }

    /**
     * Иницилизация слушателей для столбцов таблицы
     */
    private void initColumnHeaderListeners() {
        $(TABLE_CSS_CLASS + " th").click(new Function() {
            @Override
            public boolean f(Event e) {
                int index = $(this).index();
                selectColumn(index);
                prototypeLaneList.get(outputColumn).selectColumn(index);


                return true;
            }
        });
    }

    /**
     * Действие по выбору элемента в панели
     *
     * @param index
     * @param selectedIndex выбранная колонка во входной таблице , соответствующая данной выходной колонке
     */
    private void changeItemHandler(int index, int selectedIndex) {
        selectColumn(selectedIndex);
        outputColumn = index;
    }

    /**
     * Выбор колонки с индексом
     *
     * @param index индекс колонки
     */
    private void selectColumn(final int index) {

        $(TABLE_CSS_CLASS + " tr td").removeClass("selcolumn");
        if (index != 0) {
            model.putLinkValue(outputColumn, index);
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
        }else{
            model.removeLinkValue(outputColumn);
        }
    }




}