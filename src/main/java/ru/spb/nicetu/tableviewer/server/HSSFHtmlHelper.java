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

import java.util.Formatter;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;


/**
 * Implementation of {@link HtmlHelper} for HSSF file.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class HSSFHtmlHelper implements HtmlHelper {
    private final HSSFWorkbook wb;
    private final HSSFPalette colors;

    private static final HSSFColor HSSF_AUTO = new HSSFColor.AUTOMATIC();

    public HSSFHtmlHelper(HSSFWorkbook wb) {
        this.wb = wb;
        // If there is no custom palette, then this creates a new one that is
        // a copy of the default
        colors = wb.getCustomPalette();
    }

    public void colorStyles(CellStyle style, Formatter out, boolean isBuiltIn) {
        HSSFCellStyle cs = (HSSFCellStyle) style;
        if (!isBuiltIn)
            out.format("  ");
        out.format("/* fill pattern = %d */", cs.getFillPattern());
        if (!isBuiltIn)
            out.format("%n");
        styleColor(out, "background-color", cs.getFillForegroundColor(), isBuiltIn);
        styleColor(out, "color", cs.getFont(wb).getColor(), isBuiltIn);
        styleColor(out, "border-left-color", cs.getLeftBorderColor(), isBuiltIn);
        styleColor(out, "border-right-color", cs.getRightBorderColor(), isBuiltIn);
        styleColor(out, "border-top-color", cs.getTopBorderColor(), isBuiltIn);
        styleColor(out, "border-bottom-color", cs.getBottomBorderColor(), isBuiltIn);
    }

    private void styleColor(Formatter out, String attr, short index, boolean isBuiltIn) {
        HSSFColor color = colors.getColor(index);
        if (index == HSSF_AUTO.getIndex() || color == null) {
            if (!isBuiltIn)
                out.format("  ");
            out.format("/* %s: index = %d */", attr, index);
            if (!isBuiltIn)
                out.format("%n");
        } else {
            short[] rgb = color.getTriplet();
            if (!isBuiltIn)
                out.format("  ");
            out.format("%s: #%02x%02x%02x; /* index = %d */", attr, rgb[0],
                    rgb[1], rgb[2], index);
            if (!isBuiltIn)
                out.format("%n");
        }
    }
}