/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ru.spb.nicetu.tableviewer.server;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import ru.spb.nicetu.tableviewer.client.XlsViewerService;


/**
 * A simple servlet that responds to requests to log from the server, either
 * directly, or by calling the shared logging library code.
 */
public class XlsViewerServiceImpl extends RemoteServiceServlet implements XlsViewerService {

    private static Logger logger =
            Logger.getLogger(XlsViewerServiceImpl.class.getName());

    public static void main (String[] args) throws Exception {
        if ("xls".equals(args[0]))
            testXls();
        else if ("xlsx".equals(args[0]))
            testXlsx();
        else if ("ods".equals(args[0]))
            testOds();
        else
            System.out.println("Error: start with argument xls|xlsx|ods");
    }

    public static void testOds () throws Exception {
        Appendable out;
        out = new PrintWriter(new FileWriter("./war/test/testods.html"));
        OdsToHtml odsToHtml = OdsToHtml.create("./war/test/test.ods", out);
        odsToHtml.setRootPath("./war/");
        odsToHtml.setCompleteHTML(true);
        odsToHtml.printPage();
    }

    public static void testXls () throws Exception {
        Appendable out;
        out = new PrintWriter(new FileWriter("./war/test/testxls.html"));
        XlsToHtml xlsToHtml = XlsToHtml.create("./war/test/test.xls", out);
        xlsToHtml.setRootPath("./war/");
        xlsToHtml.setCompleteHTML(true);
        xlsToHtml.printPage();
    }

    public static void testXlsx () throws Exception {
        Appendable out;
        out = new PrintWriter(new FileWriter("./war/test/testxlsx.html"));
        XlsToHtml xlsToHtml = XlsToHtml.create("./war/test/test.xlsx", out);
        xlsToHtml.setRootPath("./war/");
        xlsToHtml.setCompleteHTML(true);
        xlsToHtml.printPage();
    }

    public String buildHtml(final String xlsPath, boolean completeHTML) {
        System.out.println("filePath = " + xlsPath);

        if (xlsPath != null && xlsPath.toLowerCase().endsWith(".ods")) {
            // открываем средствами для ODS
            try {
                Appendable out;
                String contextPath = getServletContext().getContextPath();
                String htmlName = "/out/h" + System.currentTimeMillis() + ".html";
                if (completeHTML) {
                    String realPath = getServletContext().getRealPath("");
                    File file = new File(realPath + "/out");
                    if (!file.exists())
                        file.mkdirs();
                    out = new PrintWriter(new FileWriter(realPath + htmlName));
                } else {
                    out = new StringWriter();
                }

                OdsToHtml odsToHtml = OdsToHtml.create(xlsPath, out);
                odsToHtml.setRootPath(getServletContext().getRealPath(""));
                odsToHtml.setCompleteHTML(completeHTML);
                odsToHtml.printPage();

                if (completeHTML) {
                    return getThreadLocalRequest().getScheme() + "://" + getThreadLocalRequest().getServerName() +
                            ":" + getThreadLocalRequest().getLocalPort() + contextPath + htmlName;
                } else {
                    return ((StringWriter)out).toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // открываем средствами для XLS/XLSX
            try {
                Appendable out;
                String contextPath = getServletContext().getContextPath();
                String htmlName = "/out/h" + System.currentTimeMillis() + ".html";
                if (completeHTML) {
                    String realPath = getServletContext().getRealPath("");
                    File file = new File(realPath + "/out");
                    if (!file.exists())
                        file.mkdirs();
                    out = new PrintWriter(new FileWriter(realPath + htmlName));
                } else {
                    out = new StringWriter();
                }

                XlsToHtml xlsToHtml = XlsToHtml.create(xlsPath, out);
                xlsToHtml.setRootPath(getServletContext().getRealPath(""));
                xlsToHtml.setCompleteHTML(completeHTML);
                xlsToHtml.printPage();

                if (completeHTML) {
                    return getThreadLocalRequest().getScheme() + "://" + getThreadLocalRequest().getServerName() +
                            ":" + getThreadLocalRequest().getLocalPort() + contextPath + htmlName;
                } else {
                    return ((StringWriter)out).toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // xls/xlsx

        return null;
    }
}
