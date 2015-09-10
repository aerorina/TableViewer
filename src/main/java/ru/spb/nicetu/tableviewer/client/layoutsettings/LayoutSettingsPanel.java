package ru.spb.nicetu.tableviewer.client.layoutsettings;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import ru.spb.nicetu.tableviewer.client.resources.Resources;

import java.util.Map;

import static com.google.gwt.query.client.GQuery.$;

/**
 * Компонент для настройки макета таблицы
 *
 * @author rlapin on 16.08.15.
 */
public class LayoutSettingsPanel extends Composite {
    /**
     * Количество знаков в английском алфавите
     */
    public static final int ENG_ALPH_SIZE = 25;
    /**
     * CSS - класс таблицы
     */
    public static final String TABLE_CLASS = ".excelDefaults";
    /**
     * Компонент задающий диапазон строк , которые будут входить в выходную таблицу
     */
    private final GTextRange textRange;
    /**
     * Количество столбцов в входной таблице
     */
    private final int inputTableColumnCount;
    /**
     * Выходная колонка
     */
    private int outputColumn;
    /**
     * Выбираем начальный диапазон или конечный?
     */
    private boolean isSelectStartRange = true;
    final private LayoutSettingsModel model;
    private TabPanel tabPanel;


    /**
     * Заполняет listBox со значениями от A до A+#количество столбцов в таблице , также добавляя пустое значение
     *
     * @param box  компонент , в который добавляются значения
     * @param size количество столбцов в таблице
     */
    private void fillListBox(ListBox box, int size) {
        box.addItem(" ");
        int ch = 0;
        for (int i = 0; i < size; i++) {
            int tempCh = ch;
            String str = "";
            while (tempCh > ENG_ALPH_SIZE) {
                str = (char) ((tempCh % (ENG_ALPH_SIZE + 1)) + 'A') + str;
                tempCh = tempCh / (ENG_ALPH_SIZE + 1) - 1;

            }
            str = (char) (tempCh + 'A') + str;
            box.addItem(str);
            ch++;
        }
    }

    /**
     * @param model модель компонента {@link LayoutSettingsModel}
     * @param text  заголовок панели
     */
    public LayoutSettingsPanel(LayoutSettingsModel model, TabPanel tabPanel, String text) {
        this.model = model;
        this.tabPanel = tabPanel;
        VerticalPanel settingsPanel = new VerticalPanel();
        settingsPanel.setStyleName("gwt-LayoutSettingsPanel");
        Label label = new Label(text);
        label.setStyleName("labeldesc");
        boolean isFirst = true;
        settingsPanel.add(label);
        this.inputTableColumnCount = $(TABLE_CLASS + " th").length() - 1;
        for (int i = 0; i < model.getColumnsCount(); i++) {
            FocusPanel focusPanel = new FocusPanel();
            HorizontalPanel lane = new HorizontalPanel();
            HorizontalPanel columnLane = new HorizontalPanel();
            columnLane.setStyleName("columnLane");
            final RadioButton radioButton = new RadioButton("colsel", model.getColumnName(i));
            if (isFirst) {
                radioButton.setValue(true);
                isFirst = false;
            }
            final int index = i;
            final ListBox listBox = new ListBox();
            radioButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    changeItemHandler(index, listBox.getSelectedIndex());
                }
            });

            listBox.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    changeItemHandler(index, listBox.getSelectedIndex());
                }
            });
            fillListBox(listBox, inputTableColumnCount);
            columnLane.add(radioButton);
            columnLane.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            columnLane.add(listBox);
            focusPanel.add(columnLane);
            focusPanel.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    radioButton.setValue(true);
                    changeItemHandler(index, listBox.getSelectedIndex());
                }
            });
            lane.add(focusPanel);
            lane.add(createAddBtn());
//            lane.add(createDelBtn());
            settingsPanel.add(lane);
        }
        HorizontalPanel rangePanel = new HorizontalPanel();
        rangePanel.setStyleName("gwt-rangeTextLane");
        Label rangeLabel = new Label("Диапазон строк для обработки: ");
        textRange = new GTextRange();
        rangePanel.add(rangeLabel);
        rangePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rangePanel.add(textRange);
        settingsPanel.add(rangePanel);
        Button btnPerform = new Button("Обработать загруженный файл");
        btnPerform.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                processInputFile();
            }
        });
        settingsPanel.add(btnPerform);
        initWidget(settingsPanel);
        initTableListeners();
    }

    /**
     * Обработать входной файл
     */
    private void processInputFile() {

        final int startRow = textRange.getStartValue();
        final int endRow = textRange.getEndValue();
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
            $(TABLE_CLASS + " tr").each(new Function() {
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

            tabPanel.remove(1);
            if (tabPanel.getWidgetCount() == 1) {
                tabPanel.add(html, "Выборка");
            }
            tabPanel.selectTab(1);

//            RootPanel.get().add(html);
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

        $(TABLE_CLASS + " tr td:first-child").click(new Function() {
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
//                $(TABLE_CLASS + " tr").eq(oldValue).removeClass("selrow");
                textRange.setStartValue(index + 1);
//                $(TABLE_CLASS + " tr").eq(index + 1).addClass("selrow");
            }

        } else {
            int oldValue = textRange.getEndValue();

            if (oldValue != index + 1) {
//                $(TABLE_CLASS + " tr").eq(oldValue).removeClass("selrow");
                textRange.setEndValue(index + 1);
//                $(TABLE_CLASS + " tr").eq(index + 1).addClass("selrow");
            }
        }
        isSelectStartRange = !isSelectStartRange;
    }

    /**
     * Иницилизация слушателей для столбцов таблицы
     */
    private void initColumnHeaderListeners() {
        $(TABLE_CLASS + " th").click(new Function() {
            @Override
            public boolean f(Event e) {
                int index = $(this).index();
                selectColumn(index);
                $(".gwt-LayoutSettingsPanel .gwt-ListBox").eq(outputColumn).prop("selectedIndex", "" + index);
                model.putLinkValue(outputColumn, index);
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
        $(TABLE_CLASS + " tr td").removeClass("selcolumn");
        if (index != 0) {

            $(TABLE_CLASS + " tr").each(new Function() {
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

    /**
     * Создать кнопку для добавления новой строки в макет таблицы
     *
     * @return кнопку добавления
     */
    private Widget createAddBtn() {
        Image imgAdd = new Image(Resources.INSTANCE.imgAdd());
        imgAdd.setPixelSize(24, 24);
        final PushButton button = new PushButton(imgAdd);
        Image imgAddUp = new Image(Resources.INSTANCE.imgAddUp());
        imgAddUp.setPixelSize(24, 24);
        button.getDownFace().setImage(imgAddUp);
        button.getElement().getStyle().setOpacity(0);
        button.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                button.getElement().getStyle().setOpacity(1);
            }
        });
        button.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                button.getElement().getStyle().setOpacity(0);
            }
        });
        return button;
    }

    /**
     * Создать кнопку для удаления строки из макета таблицы
     *
     * @return кнопка удаления
     */
    private Widget createDelBtn() {
        Image imgDel = new Image(Resources.INSTANCE.imgDel());
        imgDel.setPixelSize(24, 24);
        final PushButton button = new PushButton(imgDel);
        Image imgDelUp = new Image(Resources.INSTANCE.imgDelUp());
        imgDelUp.setPixelSize(24, 24);
        button.getDownFace().setImage(imgDelUp);
        button.getElement().getStyle().setOpacity(0);
        button.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                button.getElement().getStyle().setOpacity(1);
            }
        });
        button.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                button.getElement().getStyle().setOpacity(0);
            }
        });
        return button;
    }
}