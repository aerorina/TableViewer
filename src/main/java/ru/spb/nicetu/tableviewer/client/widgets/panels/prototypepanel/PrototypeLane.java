package ru.spb.nicetu.tableviewer.client.widgets.panels.prototypepanel;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import ru.spb.nicetu.tableviewer.client.resources.Resources;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.ChangeListener;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.prototypepanel.DefaultLaneChangeListener;
import ru.spb.nicetu.tableviewer.client.widgets.listeners.prototypepanel.LaneChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Строка в панели макетирования, отвечающая за задание соответствия между столбцом макета и столбцом электронной таблицы
 *
 * @author rlapin
 */
public class PrototypeLane extends Composite {
    /**
     * Количество знаков в английском алфавите
     */
    public static final int ENG_ALPH_SIZE = 25;
    /**
     * Максимальное количество элементов для выбора колонок
     */
    public static final int MAX_LIST_BOX_COUNT = 4;
    /**
     * Модель компонента
     */
    private final PrototypeLaneModel model;
    /**
     * Слушатель изменения значений в компоненте
     */
    private LaneChangeListener listener = new DefaultLaneChangeListener();
    /**
     * Список элементов для выбора колонок
     */
    private List<ListBox> listBoxes = new ArrayList<ListBox>();
    /**
     * Панель на которой лежат элементы для выбора колонок
     */
    private HorizontalPanel listBoxPanel = new HorizontalPanel();
    /**
     * Кнопка для удаления listbox
     */
    private PushButton delBtn;
    private PushButton addBtn;
    private RadioButton radioButton;

    public PrototypeLane(PrototypeLaneModel model, ClickHandler tooltipModeHandler) {
        this.model = model;

        initComponents(tooltipModeHandler);
        initListeners();
        model.setCurrentIndexToLast();
    }

    /**
     * Иницилизация слушателей
     */
    private void initListeners() {
        model.setColumnChangeListener(new ChangeListener() {
            @Override
            public void onChanged() {
                for (int i = 0; i < listBoxes.size(); i++) {
                    ListBox listBox = listBoxes.get(i);
                    if (i == model.getCurrentIndex()) {
                        listBox.addStyleName("activeitem");
                    } else {
                        listBox.removeStyleName("activeitem");
                    }

                }
            }
        });
    }

    public void setListener(LaneChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Иницилизация компонентов
     */
    private void initComponents(ClickHandler tooltipModeHandler) {
        FocusPanel focusPanel = new FocusPanel();
        HorizontalPanel lane = new HorizontalPanel();
        HorizontalPanel columnLane = new HorizontalPanel();
        columnLane.setStyleName("columnLane");
        focusPanel.setStyleName("focusPanel");
        radioButton = new RadioButton("colsel", model.getColumnName());
        radioButton.setValue(model.isColumnChecked());

        radioButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                selectLane();
            }
        });
        final ListBox listBox = createListBox(tooltipModeHandler);

        listBoxPanel.add(listBox);
        focusPanel.add(radioButton);

        focusPanel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                selectLane();
            }
        });
        columnLane.add(focusPanel);
        focusPanel.setWidth("600px");
        columnLane.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        columnLane.add(listBoxPanel);
        lane.add(columnLane);
        addBtn = createAddBtn(tooltipModeHandler);
        lane.add(addBtn);
        delBtn = createDelBtn(tooltipModeHandler);
        lane.add(delBtn);
        initWidget(lane);
    }

    /**
     * Создает элемент управления для выбора колонки из входной таблицы и добавляет его в список этих элементов
     *
     * @return список выбора колонок
     */
    private ListBox createListBox(ClickHandler tooltipModeHandler) {
        final ListBox listBox = new ListBox();
        final int listBoxIndex = listBoxes.size();
        listBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                selectColumn(listBox.getSelectedIndex(),listBoxIndex);
            }
        });
        listBox.addClickHandler(tooltipModeHandler);

        fillListBox(listBox, model.getInputColumnCount());
        listBoxes.add(listBox);
        return listBox;
    }



    /**
     * Создать кнопку для добавления новой строки в макет таблицы
     *
     * @return кнопку добавления
     */
    private PushButton createAddBtn(final ClickHandler tooltipModeHandler) {
        Image imgAdd = new Image(Resources.INSTANCE.imgAdd());
        imgAdd.setPixelSize(24, 24);
        final PushButton button = new PushButton(imgAdd);
        Image imgAddUp = new Image(Resources.INSTANCE.imgAddUp());
        imgAddUp.setPixelSize(24, 24);
        button.setTitle("Добавить элемент для выбора колонки");
        button.getDownFace().setImage(imgAddUp);
        setOpacity(button, 0.3);
        button.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                setOpacity(button, 1);
            }
        });
        button.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                setOpacity(button, 0.3);
            }
        });
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {

                listBoxPanel.add(createListBox(tooltipModeHandler));
                delBtn.setEnabled(true);
                setOpacity(delBtn, 0.3);
                model.getIndices().add(0);
                radioButton.setValue(true);
                model.setColumnChecked(true);
                model.setCurrentIndexToLast();
                selectLane();
                if (listBoxes.size() == MAX_LIST_BOX_COUNT - 1) {
                    button.setEnabled(false);
                    setOpacity(button, 0.1);
                }
            }
        });
        return button;
    }

    /**
     * Задать прозрачность элемента
     *
     * @param widget  виджет, к которому применяется прозрачность
     * @param opacity прозрачность 0<=value<=1
     */
    private void setOpacity(Widget widget, double opacity) {
        widget.getElement().getStyle().setOpacity(opacity);
    }

    /**
     * Создать кнопку для удаления строки из макета таблицы
     *
     * @return кнопка удаления
     */
    private PushButton createDelBtn(ClickHandler tooltipModeHandler) {
        Image imgDel = new Image(Resources.INSTANCE.imgDel());
        imgDel.setPixelSize(24, 24);
        final PushButton button = new PushButton(imgDel);
        button.setTitle("Удалить элемент для выбора колонки");
        Image imgDelUp = new Image(Resources.INSTANCE.imgDelUp());
        imgDelUp.setPixelSize(24, 24);
        button.getDownFace().setImage(imgDelUp);
        button.setEnabled(false);
        setOpacity(button, 0.1);
        button.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                setOpacity(button, 1);
            }
        });
        button.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                setOpacity(button, 0.3);
            }
        });
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                int size = listBoxes.size() - 1;
                listBoxes.get(size).removeFromParent();
                listBoxes.remove(size);
                model.getIndices().remove(model.getIndices().size() - 1);
                model.incCurrentIndex();
                addBtn.setEnabled(true);
                selectLane();
                setOpacity(addBtn, 0.3);
                if (size == 1) {
                    button.setEnabled(false);
                    setOpacity(button, 0.1);
                }
            }
        });
        button.addClickHandler(tooltipModeHandler);
        return button;
    }

    /**
     * Активировать данную строку
     */
    private void selectLane() {
        radioButton.setValue(true);
        model.setColumnChecked(true);
        listener.laneSelected();
    }

    /**
     * Заполняет listBox со значениями от A до A+#количество столбцов в таблице , также добавляя пустое значение
     *
     * @param box  компонент , в который добавляются значения
     * @param size количество столбцов в таблице
     */
    //TODO сделать чтобы заполнение происходило один раз и просто передавать массив строк
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
     * Выбрать колонку в выходной таблице
     * В случае , если колонка уже выбрана других листбоксом , то выдается сообщение об ошибке и
     * @param index индекс выбранной колонки в входной таблице
     */
    public void selectColumn(int index) {
        if(!model.isColumnChecked()){
            selectLane();
        }
        int columnIndex = model.getCurrentIndex();
        if(model.getIndices().contains(index)){
            if(model.getIndices().get(model.getCurrentIndex()) != index) {
                AlertMessageBox messageBox = new AlertMessageBox("Ошибка", "Выбранная колонка, уже используется");
                messageBox.show();
            }
            listBoxes.get(columnIndex).setSelectedIndex(0);
            model.getIndices().set(columnIndex,0);
        }else {
            model.incCurrentIndex();
            listBoxes.get(columnIndex).setSelectedIndex(index);
            model.getIndices().set(columnIndex, index);
        }
        listener.laneSelected();
    }
    /**
     * Выбор колонки для листбокса с индексом
     * @param selectedIndex номер значения
     * @param listBoxIndex индекс листбокса
     */
    private void selectColumn(int selectedIndex, int listBoxIndex) {
        //Сохраняем текущий индекс так как не хотим его менять
        int curIndex = model.getCurrentIndex();
        model.setCurrentIndex(listBoxIndex);
        selectColumn(selectedIndex);
        //Возвращаем обратно
        model.setCurrentIndex(curIndex);
    }
    /**
     * @return модель компонента
     */
    public PrototypeLaneModel getModel() {
        return model;
    }
}
