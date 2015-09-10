package ru.spb.nicetu.tableviewer.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.shared.UConsts;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

/**
 * Сервлет загрузки прикрепленных файлов к документам (BinaryDocsEntity)
 * User: vvasilyev
 * Date: 17.01.2012
 * Time: 14:55
 */
public class XlsUploadServlet extends UploadAction {

    private static final long serialVersionUID = 125L;

    Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
    /**
     * Maintain a list with received file and their content types.
     */
    Hashtable<String, File> receivedFiles = new Hashtable<String, File>();

    /**
     * Override executeAction to save the received file in a custom place
     * and delete this items from session.
     */
    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        String response = "";
        for(FileItem item : sessionFiles) {
            if(!item.isFormField()) {
                try {
                    String fileExtension = item.getName().contains(".") ? item.getName().substring(item.getName().lastIndexOf(".")) : "";
                    String fileName = "xls" + System.currentTimeMillis() + fileExtension;
                    String fileDir = getServletContext().getRealPath("") + File.separator + "xls";
                    new File(fileDir).mkdirs();
                    File file = new File(fileDir + File.separator + fileName);
                    item.write(file);

                    /// Save a list with the received file
                    receivedFiles.put(item.getFieldName(), file);
                    receivedContentTypes.put(item.getFieldName(), item.getContentType());
                    //response.setHeader("Content-disposition", "attachment; filename*=UTF-8''${URLEncoder.encode(documentEntity.filename)}");

                    response = file.getCanonicalPath();

                } catch(Exception e) {
                    throw new UploadActionException(e);
                }
            }
        }

        /// Remove file from session because we have a copy of them
        removeSessionFileItems(request);

        /// Send your customized message to the client.
        return response;
    }

    /**
     * Get the content of an uploaded file.
     */
    @Override
    public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fieldName = request.getParameter(UConsts.PARAM_SHOW);
        File f = receivedFiles.get(fieldName);
        if(f != null) {
            response.setContentType(receivedContentTypes.get(fieldName));
            FileInputStream is = new FileInputStream(f);
            copyFromInputStreamToOutputStream(is, response.getOutputStream());
        } else {
            renderXmlResponse(request, response, XML_ERROR_ITEM_NOT_FOUND);
        }
    }

    /**
     * Remove a file when the user sends a delete request.
     */
    @Override
    public void removeItem(HttpServletRequest request, String fieldName) throws UploadActionException {
        File file = receivedFiles.get(fieldName);
        receivedFiles.remove(fieldName);
        receivedContentTypes.remove(fieldName);
        if(file != null) {
            file.delete();
        }
    }
}