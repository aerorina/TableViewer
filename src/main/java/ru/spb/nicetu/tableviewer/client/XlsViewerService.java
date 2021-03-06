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

package ru.spb.nicetu.tableviewer.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("xlsviewer_service")
public interface XlsViewerService extends RemoteService {
    /**
     * Получение доступа к службе из любого места
     * Использовать ControlPanelService.App.getInstance()
     */
    class App {
        private static XlsViewerServiceAsync ourInstance = GWT.create(XlsViewerService.class);

        public static synchronized XlsViewerServiceAsync getInstance() {
            return ourInstance;
        }
    }

    String buildHtml(final String xlsPath, boolean completeHTML);
}
