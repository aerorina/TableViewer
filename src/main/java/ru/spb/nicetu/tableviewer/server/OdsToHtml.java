/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package ru.spb.nicetu.tableviewer.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;
import java.util.List;

import org.jdom.Attribute;
import org.jopendocument.dom.MyStyledNode;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.CellStyle;
import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.MyCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.jopendocument.dom.style.SideStyleProperties;

/**
 * This example shows how to display a spreadsheet in HTML using the classes for
 * spreadsheet display.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class OdsToHtml {

    private final SpreadSheet wb;
    private final Appendable output;
    private boolean completeHTML;
    private Formatter out;
    private boolean gotBounds;
    private int firstColumn;
    private int endColumn;
    private int endRow;
    private String rootPath = "";

    private static final String DEFAULTS_CLASS = "excelDefaults";
    private static final String COL_HEAD_CLASS = "colHeader";
    private static final String ROW_HEAD_CLASS = "rowHeader";

    private static final int MAX_EMPTY_ROWS = 100;
    private static final int MAX_ROW_NUMBER = 10000;
    private static final int MAX_COLUMN_NUMBER = 100;

    /**
     * Creates a new converter to HTML for the given workbook.
     *
     * @param wb     The workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static OdsToHtml create(SpreadSheet wb, Appendable output) {
        return new OdsToHtml(wb, output);
    }

    /**
     * Creates a new converter to HTML for the given workbook.  If the path ends
     * with "<tt>.xlsx</tt>" an {@link org.apache.poi.xssf.usermodel.XSSFWorkbook} will be used; otherwise
     * this will use an {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}.
     *
     * @param path   The file that has the workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static OdsToHtml create(String path, Appendable output)
            throws IOException {
        return create(new File(path), output);
    }

    /**
     * Creates a new converter to HTML for the given workbook.  This attempts to
     * detect whether the input is XML (so it should create an {@link
     * org.apache.poi.xssf.usermodel.XSSFWorkbook} or not (so it should create an {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}).
     *
     * @param in     The input stream that has the workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static OdsToHtml create(File in, Appendable output)
            throws IOException {
        try {
            SpreadSheet wb = SpreadSheet.createFromFile(in);
            return create(wb, output);
        } catch (Exception e){
            throw new IllegalArgumentException("Cannot create workbook from stream", e);
        }
    }

    private OdsToHtml(SpreadSheet wb, Appendable output) {
        if (wb == null)
            throw new NullPointerException("wb");
        if (output == null)
            throw new NullPointerException("output");
        this.wb = wb;
        this.output = output;
    }

    /**
     * Run this class as a program
     *
     * @param args The command line arguments.
     *
     * @throws Exception Exception we don't recover from.
     */
    public static void main(String[] args) throws Exception {
        if(args.length < 2){
            System.err.println("usage: OdsToHtml inputWorkbook outputHtmlFile");
            return;
        }

        OdsToHtml xlsToHtml = create(args[0], new PrintWriter(new FileWriter(args[1])));
        xlsToHtml.setCompleteHTML(true);
        xlsToHtml.printPage();
    }

    public void setCompleteHTML(boolean completeHTML) {
        this.completeHTML = completeHTML;
    }

    public void printPage() throws IOException {
        try {
            ensureOut();
            if (completeHTML) {
                out.format(
                        "<?xml version=\"1.0\" encoding=\"utf-8\" ?>%n");
                out.format("<html>%n");
                out.format("<head>%n");
                out.format("  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">%n");
                out.format("</head>%n");
                out.format("<body>%n");
            }

            print();

            if (completeHTML) {
                out.format("</body>%n");
                out.format("</html>%n");
            }
        } finally {
            if (out != null)
                out.close();
            if (output instanceof Closeable) {
                Closeable closeable = (Closeable) output;
                closeable.close();
            }
        }
    }

    public void print() {
        if (completeHTML)
            printInlineStyle();
        printSheets();
    }

    private void printInlineStyle() {
        //out.format("<link href=\"excelStyle.css\" rel=\"stylesheet\" type=\"text/css\">%n");
        out.format("<style type=\"text/css\">%n");
        printStyles();
        out.format("</style>%n");
    }

    private void ensureOut() {
        if (out == null)
            out = new Formatter(output);
    }

    public void printStyles() {
        ensureOut();

        // First, copy the base css
        BufferedReader in = null;
        try {
            //in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("excelStyle.css")));
            in = new BufferedReader(new InputStreamReader(new FileInputStream(rootPath + "/excelStyle.css")));
            String line;
            while ((line = in.readLine()) != null) {
                out.format("%s%n", line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Reading standard css", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new IllegalStateException("Reading standard css", e);
                }
            }
        }
    }

    private void printSheets() {
        ensureOut();
        for (int numSheet = 0; numSheet < wb.getSheetCount(); numSheet++) {
            Sheet sheet = wb.getSheet(numSheet);
            printSheet(sheet, numSheet);
        }
    }

    public void printSheet(Sheet sheet, int numSheet) {
        ensureOut();
        gotBounds = false;
        String sheetName = sheet.getName();
        if (sheetName == null)
            sheetName = "";
        out.format((numSheet > 0 ? "<br/>%n" : "") +
                "<div width=\"100%%\" style=\"background-color: #dddddd; border: medium solid #dd7777; margin-bottom: 3px;\"><b>Лист № %s :  '%s'</b></div>%n", "" +
                (numSheet + 1), sheetName.replace("<", "&lt;"));
        out.format("<table class=%s>%n", DEFAULTS_CLASS);
        printCols(sheet);
        printSheetContent(sheet);
        out.format("</table>%n");
    }

    private void printCols(Sheet sheet) {
        out.format("<col/>%n");
        ensureColumnBounds(sheet);
        for (int i = firstColumn; i < endColumn; i++) {
            out.format("<col/>%n");
        }
    }

    private void ensureColumnBounds(Sheet sheet) {
        if (gotBounds)
            return;

        firstColumn = 0;
        endColumn = sheet.getColumnCount();
        if (endColumn > MAX_COLUMN_NUMBER)
            endColumn = MAX_COLUMN_NUMBER;
        endRow = sheet.getRowCount();
        if (endRow > MAX_ROW_NUMBER)
            endRow = MAX_ROW_NUMBER;

        int emptyRows = 0;
        int lastNotEmptyColumn = -1;
        int lastNotEmptyRow = -1;
        for (int curRow = 0; curRow < endRow; curRow++) {
            boolean isRowEmpty = true;
            for (int i = firstColumn; i < endColumn; i++) {
                MutableCell<SpreadSheet> cell = null;
                try {
                    cell = sheet.getCellAt(i, curRow);
                } catch (Exception e) {

                }
                if (cell != null) {
                    String content = cell.getTextValue();

                    if (content == null || content.equals("")) {

                    } else {
                        isRowEmpty = false;
                        if (i > lastNotEmptyColumn)
                            lastNotEmptyColumn = i;
                    }
                }
            } // columns
            if (isRowEmpty) {
                emptyRows++;
            } else {
                emptyRows = 0;
                if (curRow > lastNotEmptyRow)
                    lastNotEmptyRow = curRow;
            }

            if (emptyRows > MAX_EMPTY_ROWS)
                break;
        } // rows

        endColumn = lastNotEmptyColumn + 1;
        endRow = lastNotEmptyRow + 1;

        gotBounds = true;
    }

    private void printColumnHeads() {
        out.format("<thead>%n");
        out.format("  <tr class=%s>%n", COL_HEAD_CLASS);
        out.format("    <th class=%s>&#x25CA;</th>%n", COL_HEAD_CLASS);
        //noinspection UnusedDeclaration
        StringBuilder colName = new StringBuilder();
        for (int i = firstColumn; i < endColumn; i++) {
            colName.setLength(0);
            int cnum = i;
            do {
                colName.insert(0, (char) ('A' + cnum % 26));
                cnum /= 26;
            } while (cnum > 0);
            out.format("    <th class=%s>%s</th>%n", COL_HEAD_CLASS, colName);
        }
        out.format("  </tr>%n");
        out.format("</thead>%n");
    }

    private void printSheetContent(Sheet sheet) {
        printColumnHeads();
        out.format("<tbody>%n");
        int emptyRows = 0;
        for (int curRow = 0; (curRow < endRow); curRow++) {
            out.format("  <tr>%n");
            out.format("    <td class=%s>%d</td>%n", ROW_HEAD_CLASS, curRow + 1);
            boolean isRowEmpty = true;
            for (int i = firstColumn; i < endColumn; i++) {
                String content = "&nbsp;";
                String attrs = "";
                CellStyle style = null;
                boolean isInRangeNotFirst = false;
                if (i >= 0 && i < sheet.getColumnCount()) {
                    Cell<SpreadSheet> cell = null;
                    try {
                        cell = sheet.getImmutableCellAt(i, curRow);
                    } catch (Exception e) {

                    }
                    if (cell != null) {
                        if (cell.getColumnsSpanned() > 1)
                            attrs += " colspan=" + cell.getColumnsSpanned();
                        if (cell.getRowsSpanned() > 1)
                            attrs += " rowspan=" + cell.getRowsSpanned();
                        try {
                            style = cell.getStyle();
                        } catch (Exception e) {
                            isInRangeNotFirst = true;
                        }

                        if (style != null) {
                            String cellStyle = "";
                            if (style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.LEFT) != null)
                                cellStyle += "border-left: " + style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.LEFT).replace("thin", "1pt") + "; ";
                            else
                                cellStyle += "border-left: none; ";
                            if (style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.RIGHT) != null)
                                cellStyle += "border-right: " + style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.RIGHT).replace("thin", "1pt") + "; ";
                            else
                                cellStyle += "border-right: none; ";
                            if (style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.TOP) != null)
                                cellStyle += "border-top: " + style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.TOP).replace("thin", "1pt") + "; ";
                            else
                                cellStyle += "border-top: none; ";
                            if (style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.BOTTOM) != null)
                                cellStyle += "border-bottom: " + style.getTableCellProperties(cell).getBorder(SideStyleProperties.Side.BOTTOM).replace("thin", "1pt") + "; ";
                            else
                                cellStyle += "border-bottom: none; ";

                            if (style.getTableCellProperties(cell).getBackgroundColor() != null && style.getTableCellProperties(cell).getBackgroundColor().getAlpha() > 0)
                                cellStyle += "background-color: " + formatColor(
                                        style.getTableCellProperties(cell).getBackgroundColor().getRed(),
                                        style.getTableCellProperties(cell).getBackgroundColor().getGreen(),
                                        style.getTableCellProperties(cell).getBackgroundColor().getBlue()) + "; ";
                            if (style.getTextProperties().getColor() != null && style.getTextProperties().getColor().getAlpha() > 0)
                                cellStyle += "color: " + formatColor(
                                        style.getTextProperties().getColor().getRed(),
                                        style.getTextProperties().getColor().getGreen(),
                                        style.getTextProperties().getColor().getBlue()) + "; ";

                            if (style.getTextProperties().getFontName() != null)
                                cellStyle += " font-family: " + style.getTextProperties().getFontName() + "; ";
                            if ("bold".equalsIgnoreCase(style.getTextProperties().getWeight()))
                                cellStyle += " font-weight: bold; ";

                            String fontItalic = getAttribute(style.getTextProperties().getElement().getAttributes(), "font-style");
                            String fontUnderline = getAttribute(style.getTextProperties().getElement().getAttributes(), "text-underline-style");
                            if ("italic".equalsIgnoreCase(fontItalic))
                                cellStyle += " font-style: italic; ";
                            if ("solid".equalsIgnoreCase(fontUnderline))
                                cellStyle += " text-decoration: underline; ";

                            if ("center".equalsIgnoreCase(style.getParagraphProperties().getAlignment()))
                                cellStyle += " text-align: center; ";
                            else if ("end".equalsIgnoreCase(style.getParagraphProperties().getAlignment()))
                                cellStyle += " text-align: right; ";
                            else if (style.getParagraphProperties().getAlignment() == null && "float".equals(MyCell.getType(cell)))
                                cellStyle += " text-align: right; ";

                            String fontVerticalAlign = getAttribute(style.getTableCellProperties(cell).getElement().getAttributes(), "vertical-align");
                            if ("middle".equalsIgnoreCase(fontVerticalAlign))
                                cellStyle += " vertical-align: middle; ";
                            if ("bottom".equalsIgnoreCase(fontVerticalAlign))
                                cellStyle += " vertical-align: bottom; ";

                            String fontSize = getAttribute(style.getTextProperties().getElement().getAttributes(), "font-size");
                            if (fontSize != null)
                                cellStyle += " font-size: " + fontSize + "; ";
                            else if (style.getParentStyle() != null && style.getParentStyle().getName() != null) {
                                fontSize = getAttribute(((CellStyle) MyStyledNode.getStyle(cell, style.getParentStyle().getName())).getTextProperties().getElement().getAttributes(), "font-size");
                                cellStyle += " font-size: " + fontSize + "; ";
                            }


                            attrs += " style=\"" + cellStyle + "\"";

                            content = cell.getTextValue(); // todo
                        }

                        if (content == null || content.equals(""))
                            content = "&nbsp;";
                        else
                            isRowEmpty = false;
                    } else {
                        attrs += "style=\"border: none;\"";
                    }
                }

                if (!isInRangeNotFirst) {
                    out.format("    <td %s>%s</td>%n", attrs, content);
                }
            } // columns

            out.format("  </tr>%n");

            if (isRowEmpty)
                emptyRows++;
            else
                emptyRows = 0;

            if (emptyRows > MAX_EMPTY_ROWS)
                break;
        } // rows

        out.format("</tbody>%n");
    }

    private String formatColor(int red, int green, int blue) {
        StringWriter styleWriter = new StringWriter();
        Formatter styleFormatter = new Formatter(styleWriter);
        return styleFormatter.format("#%02x%02x%02x;", red, green, blue).toString();
    }

    private String getAttribute (List<Attribute> attributes, String name) {
        if (attributes == null)
            return null;
        for (Attribute attr: attributes) {
            if (name.equals(attr.getName()))
                return attr.getValue();
        }
        return null;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}