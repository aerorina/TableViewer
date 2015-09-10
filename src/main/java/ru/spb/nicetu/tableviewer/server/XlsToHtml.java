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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER_SELECTION;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_FILL;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_JUSTIFY;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT;
import static org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASHED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DOTTED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_DOUBLE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_HAIR;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASHED;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_NONE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_SLANTED_DASH_DOT;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THICK;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER;
import static org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP;

/**
 * This example shows how to display a spreadsheet in HTML using the classes for
 * spreadsheet display.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class XlsToHtml {
    private final Workbook wb;
    private final Appendable output;
    private boolean completeHTML;
    private Formatter out;
    private boolean gotBounds;
    private int firstColumn;
    private int endColumn;
    private HtmlHelper helper;
    private String rootPath = "";

    private static final String DEFAULTS_CLASS = "excelDefaults";
    private static final String COL_HEAD_CLASS = "colHeader";
    private static final String ROW_HEAD_CLASS = "rowHeader";

    private static final Map<Short, String> ALIGN = mapFor(ALIGN_LEFT, "left",
            ALIGN_CENTER, "center", ALIGN_RIGHT, "right", ALIGN_FILL, "left",
            ALIGN_JUSTIFY, "left", ALIGN_CENTER_SELECTION, "center");

    private static final Map<Short, String> VERTICAL_ALIGN = mapFor(
            VERTICAL_BOTTOM, "bottom", VERTICAL_CENTER, "middle", VERTICAL_TOP,
            "top");

    private static final Map<Short, String> BORDER = mapFor(BORDER_DASH_DOT,
            "dashed 1pt", BORDER_DASH_DOT_DOT, "dashed 1pt", BORDER_DASHED,
            "dashed 1pt", BORDER_DOTTED, "dotted 1pt", BORDER_DOUBLE,
            "double 3pt", BORDER_HAIR, "solid 1px", BORDER_MEDIUM, "solid 2pt",
            BORDER_MEDIUM_DASH_DOT, "dashed 2pt", BORDER_MEDIUM_DASH_DOT_DOT,
            "dashed 2pt", BORDER_MEDIUM_DASHED, "dashed 2pt", BORDER_NONE,
            "none", BORDER_SLANTED_DASH_DOT, "dashed 2pt", BORDER_THICK,
            "solid 3pt", BORDER_THIN, "solid 1pt");

    @SuppressWarnings({"unchecked"})
    private static <K, V> Map<K, V> mapFor(Object... mapping) {
        Map<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < mapping.length; i += 2) {
            map.put((K) mapping[i], (V) mapping[i + 1]);
        }
        return map;
    }

    /**
     * Creates a new converter to HTML for the given workbook.
     *
     * @param wb     The workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static XlsToHtml create(Workbook wb, Appendable output) {
        return new XlsToHtml(wb, output);
    }

    /**
     * Creates a new converter to HTML for the given workbook.  If the path ends
     * with "<tt>.xlsx</tt>" an {@link XSSFWorkbook} will be used; otherwise
     * this will use an {@link HSSFWorkbook}.
     *
     * @param path   The file that has the workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static XlsToHtml create(String path, Appendable output)
            throws IOException {
        return create(new FileInputStream(path), output);
    }

    /**
     * Creates a new converter to HTML for the given workbook.  This attempts to
     * detect whether the input is XML (so it should create an {@link
     * XSSFWorkbook} or not (so it should create an {@link HSSFWorkbook}).
     *
     * @param in     The input stream that has the workbook.
     * @param output Where the HTML output will be written.
     *
     * @return An object for converting the workbook to HTML.
     */
    public static XlsToHtml create(InputStream in, Appendable output)
            throws IOException {
        try {
            Workbook wb = WorkbookFactory.create(in);
            return create(wb, output);
        } catch (InvalidFormatException e){
            throw new IllegalArgumentException("Cannot create workbook from stream", e);
        }
    }

    private XlsToHtml(Workbook wb, Appendable output) {
        if (wb == null)
            throw new NullPointerException("wb");
        if (output == null)
            throw new NullPointerException("output");
        this.wb = wb;
        this.output = output;
        setupColorMap();
    }

    private void setupColorMap() {
        if (wb instanceof HSSFWorkbook)
            helper = new HSSFHtmlHelper((HSSFWorkbook) wb);
        else if (wb instanceof XSSFWorkbook)
            helper = new XSSFHtmlHelper((XSSFWorkbook) wb);
        else
            throw new IllegalArgumentException(
                    "unknown workbook type: " + wb.getClass().getSimpleName());
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
            System.err.println("usage: XlsToHtml inputWorkbook outputHtmlFile");
            return;
        }

        XlsToHtml xlsToHtml = create(args[0], new PrintWriter(new FileWriter(args[1])));
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

        // now add css for each used style
        Set<CellStyle> seen = new HashSet<CellStyle>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {
                Row row = rows.next();
                for (Cell cell : row) {
                    CellStyle style = cell.getCellStyle();
                    if (!seen.contains(style)) {
                        printStyle(style, out, false, false);
                        seen.add(style);
                    }
                }
            }
        }
    }

    private void printStyle(CellStyle style, Formatter out, boolean isBuiltIn, boolean isNumeric) {
        if (!isBuiltIn)
            out.format(".%s .%s {%n", DEFAULTS_CLASS, styleName(style, out));
        styleContents(style, out, isBuiltIn, isNumeric);
        if (!isBuiltIn)
            out.format("}%n");
    }

    private void styleContents(CellStyle style, Formatter out, boolean isBuiltIn, boolean isNumeric) {
        if (isNumeric && style.getAlignment() == 0)
            styleOut("text-align", "right", out, isBuiltIn);
        else
            styleOut("text-align", style.getAlignment(), ALIGN, out, isBuiltIn);
        styleOut("vertical-align", style.getVerticalAlignment(), VERTICAL_ALIGN, out, isBuiltIn);
        fontStyle(style, out, isBuiltIn);
        borderStyles(style, out, isBuiltIn);
        helper.colorStyles(style, out, isBuiltIn);
    }

    private void borderStyles(CellStyle style, Formatter out, boolean isBuiltIn) {
        styleOut("border-left", style.getBorderLeft(), BORDER, out, isBuiltIn);
        styleOut("border-right", style.getBorderRight(), BORDER, out, isBuiltIn);
        styleOut("border-top", style.getBorderTop(), BORDER, out, isBuiltIn);
        styleOut("border-bottom", style.getBorderBottom(), BORDER, out, isBuiltIn);
    }

    private void fontStyle(CellStyle style, Formatter out, boolean isBuiltIn) {
        Font font = wb.getFontAt(style.getFontIndex());

        if (font.getBoldweight() >= HSSFFont.BOLDWEIGHT_BOLD)
            styleOut("font-weight", "bold", out, isBuiltIn);
        if (font.getItalic())
            styleOut("font-style", "italic", out, isBuiltIn);
        if (font.getFontName() != null && !"".equals(font.getFontName()))
            styleOut("font-family", font.getFontName(), out, isBuiltIn);

        int fontheight = font.getFontHeightInPoints();
        if (fontheight == 9) {
            //fix for stupid ol Windows
            fontheight = 10;
        }
        styleOut("font-size", "" + fontheight + "pt", out, isBuiltIn);

        // Font color is handled with the other colors
    }

    private String styleName(CellStyle style, Formatter out) {
        if (style == null)
            style = wb.getCellStyleAt((short) 0);
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        try {
            fmt.format("style_%02x", style.getIndex());
            return fmt.toString();
        } finally {
            fmt.close();
        }
    }

    private String styleSimpleContents(CellStyle style, boolean isNumeric) {
        StringWriter styleWriter = new StringWriter();
        Formatter styleFormatter = new Formatter(styleWriter);
        if (style == null)
            style = wb.getCellStyleAt((short) 0);

        if (style == null) {
            styleWriter.append("");
        } else {
            printStyle(style, styleFormatter, true, isNumeric);
        }

        return styleWriter.toString();
    }

    private <K> void styleOut(String attr, K key, Map<K, String> mapping, Formatter out, boolean isBuiltIn) {
        String value = mapping.get(key);
        styleOut(attr, value, out, isBuiltIn);
    }

    private void styleOut(String attr, String value, Formatter out, boolean isBuiltIn) {
        if (value != null) {
            if (!isBuiltIn)
                out.format("  ");
            out.format("%s: %s;", attr, value);
            if (!isBuiltIn)
                out.format("%n");
        }
    }

    private static int ultimateCellType(Cell c) {
        int type = c.getCellType();
        if (type == Cell.CELL_TYPE_FORMULA)
            type = c.getCachedFormulaResultType();
        return type;
    }

    private void printSheets() {
        ensureOut();
        for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
            Sheet sheet = wb.getSheetAt(numSheet);
            printSheet(sheet, numSheet);
        }
    }

    public void printSheet(Sheet sheet, int numSheet) {
        ensureOut();
        gotBounds = false;
        String sheetName = sheet.getSheetName();
        if (sheetName == null)
            sheetName = "";
        out.format((numSheet > 0 ? "<br/>%n" : "") + "<div width=\"100%%\" style=\"background-color: #dddddd; border: medium solid #dd7777; margin-bottom: 3px;\"><b>Лист № %s :  '%s'</b></div>%n", "" + (numSheet + 1), sheetName.replace("<", "&lt;"));
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

        Iterator<Row> iter = sheet.rowIterator();
        firstColumn = (iter.hasNext() ? Integer.MAX_VALUE : 0);
        endColumn = 0;
        while (iter.hasNext()) {
            Row row = iter.next();
            short firstCell = row.getFirstCellNum();
            if (firstCell >= 0) {
                firstColumn = Math.min(firstColumn, firstCell);
                endColumn = Math.max(endColumn, row.getLastCellNum());
            }
        }
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
        Iterator<Row> rows = sheet.rowIterator();
        int lastNum = -1;
        while (rows.hasNext()) {
            Row row = rows.next();
            int curNum = row.getRowNum();
            if (curNum - lastNum > 1) {
                for (int i = lastNum + 2; i <= curNum; i++) {
                    out.format("  <tr>%n");
                    out.format("    <td class=%s>%d</td>%n", ROW_HEAD_CLASS, i);
                    out.format("    <td colspan=%d style=\"%s\">&nbsp;</td>%n", (endColumn - firstColumn + 1), styleSimpleContents(null, false));
                    out.format("  </tr>%n");
                }
            }
            lastNum = curNum;

            out.format("  <tr>%n");
            out.format("    <td class=%s>%d</td>%n", ROW_HEAD_CLASS,
                    row.getRowNum() + 1);
            for (int i = firstColumn; i < endColumn; i++) {
                String content = "&nbsp;";
                String attrs = "";
                CellStyle style = null;
                boolean isNumeric = false;
                if (i >= row.getFirstCellNum() && i < row.getLastCellNum()) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        style = cell.getCellStyle();
                        attrs = tagStyle(cell, style);

                        CellFormat cf = CellFormat.getInstance(style.getDataFormatString());
                        CellFormatResult result = cf.apply(cell);
                        content = result.text;
                        if (content != null && !content.equals("") && (cell.getCellType() == Cell.CELL_TYPE_NUMERIC ||
                                cell.getCellType() == Cell.CELL_TYPE_FORMULA && cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC)) {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                // Date
                                if ("mmm-yy".equals(style.getDataFormatString())) {
                                    SimpleDateFormat sdfRus = new SimpleDateFormat("MMM.yy");
                                    content = sdfRus.format(cell.getDateCellValue());
                                } else if ("h:mm".equals(style.getDataFormatString())) {
                                    SimpleDateFormat sdfRus = new SimpleDateFormat("HH:mm");
                                    content = sdfRus.format(cell.getDateCellValue());
                                } else if (style.getDataFormatString() != null && style.getDataFormatString().contains("mm")) {
                                    SimpleDateFormat sdfRus = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                                    content = sdfRus.format(cell.getDateCellValue());
                                } else {
                                    SimpleDateFormat sdfRus = new SimpleDateFormat("dd.MM.yyyy");
                                    content = sdfRus.format(cell.getDateCellValue());
                                }
                            } else {
                                // Number
                                if ("- 0".equals(content.trim()))
                                    content = "&nbsp;";
                                else
                                    content = "<nobr>" + content.replace(",", " ").replace(".", ",") + "</nobr>";
                                isNumeric = true;
                            }
                        }

                        if (content == null || content.equals(""))
                            content = "&nbsp;";
                    }
                }

                boolean isInRangeNotFirst = false;
                for (int j = 0; j < sheet.getNumMergedRegions(); j++) {
                    CellRangeAddress rangeAddress = sheet.getMergedRegion(j);
                    if (row.getRowNum() == rangeAddress.getFirstRow() && i == rangeAddress.getFirstColumn()) {
                        if (rangeAddress.getLastRow() - row.getRowNum() > 0)
                            attrs += " rowspan=" + (rangeAddress.getLastRow() - row.getRowNum() + 1);
                        if (rangeAddress.getLastColumn() - i > 0)
                            attrs += " colspan=" + (rangeAddress.getLastColumn() - i + 1);
                        break;
                    } else if (row.getRowNum() >= rangeAddress.getFirstRow() && row.getRowNum() <= rangeAddress.getLastRow() &&
                            i >= rangeAddress.getFirstColumn() && i <= rangeAddress.getLastColumn()) {
                        isInRangeNotFirst = true;
                        break;
                    }
                }

                if (!isInRangeNotFirst) {
                    out.format("    <td style=\"%s\" %s>%s</td>%n", styleSimpleContents(style, isNumeric), attrs, content);
                }
            } // columns
            out.format("  </tr>%n");
        } // rows

        out.format("</tbody>%n");
    }

    private String tagStyle(Cell cell, CellStyle style) {
        if (style.getAlignment() == ALIGN_GENERAL) {
            switch (ultimateCellType(cell)) {
            case HSSFCell.CELL_TYPE_STRING:
                return "style=\"text-align: left;\"";
            case HSSFCell.CELL_TYPE_BOOLEAN:
            case HSSFCell.CELL_TYPE_ERROR:
                return "style=\"text-align: center;\"";
            case HSSFCell.CELL_TYPE_NUMERIC:
            default:
                // "right" is the default
                break;
            }
        }
        return "";
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}