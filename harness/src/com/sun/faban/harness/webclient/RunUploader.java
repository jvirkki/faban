/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://faban.dev.java.net/public/CDDLv1.0.html or
 * install_dir/license.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at faban/src/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: RunUploader.java,v 1.2 2006/10/07 07:33:40 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.harness.webclient;

import static com.sun.faban.harness.util.FileHelper.*;

import com.sun.faban.harness.common.Config;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * The RunUploader represents the upload servlet as well as an upload client
 * maintained in a single class.
 *
 * @author Akara Sucharitakul
 */
public class RunUploader extends HttpServlet {

    static Logger logger = Logger.getLogger(Deployer.class.getName());

    /**
     * Post method to upload the run.
     * @param request The servlet request
     * @param response The servlet response
     * @throws ServletException If the servlet fails
     * @throws IOException If there is an I/O error
     */
    public void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String host = null;
        String key = null;
        boolean origin = false; // Whether the upload is to the original
        // run requestor. If so, key is needed.

        DiskFileUpload fu = new DiskFileUpload();
        // No maximum size
        fu.setSizeMax(-1);
        // maximum size that will be stored in memory
        fu.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        fu.setRepositoryPath(Config.TMP_DIR);

        List fileItems = null;
        try {
            fileItems = fu.parseRequest(request);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
        // assume we know there are two files. The first file is a small
        // text file, the second is unknown and is written to a file on
        // the server
        for (Iterator i = fileItems.iterator(); i.hasNext();) {
            FileItem item = (FileItem) i.next();
            String fieldName = item.getFieldName();
            if (item.isFormField()) {
                if ("host".equals(fieldName)) {
                    host = item.getString();
                } else if ("key".equals(fieldName)) {
                    key = item.getString();
                } else if ("origin".equals(fieldName)) {
                    String value = item.getString();
                    origin = Boolean.parseBoolean(value);
                }
                continue;
            }

            if (host == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                break;
            }

            if (origin &&
                    (key == null && !RunRetriever.authenticate(host, key))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                break;
            }

            if (!"jarfile".equals(fieldName)) // ignore
                continue;

            String fileName = item.getName();

            if (fileName == null) // We don't process files without names
                continue;

            // The host, origin, key info must be here before we receive
            // any file.
            if (host == null || (origin && key == null)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
            }
            // Now, this name may have a path attached, dependent on the
            // source browser. We need to cover all possible clients...
            char[] pathSeparators = {'/', '\\'};
            // Well, if there is another separator we did not account for,
            // just add it above.

            for (int j = 0; j < pathSeparators.length; j++) {
                int idx = fileName.lastIndexOf(pathSeparators[j]);
                if (idx != -1) {
                    fileName = fileName.substring(idx + 1);
                    break;
                }
            }

            // Ignore all non-jarfiles.
            if (!fileName.toLowerCase().endsWith(".jar"))
                continue;

            File uploadFile = File.createTempFile("run", ".jar",
                    new File(Config.TMP_DIR));
            try {
                item.write(uploadFile);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            File runTmp = unjarTmp(uploadFile);

            String runName = null;

            if (origin) {
                // Change origin file to know where this run came from.
                File metaInf = new File(runTmp, "META-INF");
                File originFile = new File(metaInf, "origin");
                if (!originFile.exists()) {
                    response.sendError(
                            HttpServletResponse.SC_NOT_ACCEPTABLE,
                            "Origin file does not exist!");
                    break;
                }

                String originSpec = readStringFromFile(originFile);
                int idx = originSpec.lastIndexOf('.');
                if (idx == -1) { // This is wrong, we do not accept this.
                    response.sendError(
                            HttpServletResponse.SC_NOT_ACCEPTABLE,
                            "Origin file error!");
                    break;
                }
                idx = originSpec.lastIndexOf('.', idx - 1);
                if (idx == -1) {
                    response.sendError(
                            HttpServletResponse.SC_NOT_ACCEPTABLE,
                            "Origin file error!");
                    break;
                }

                runName = originSpec.substring(idx + 1);
                String localHost = originSpec.substring(0, idx);
                if (!localHost.equals(Config.FABAN_HOST)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    break;
                }
                writeStringToFile(host + '.' + runTmp.getName(), originFile);
            }  else {
                runName = host + '.' + runTmp.getName();
            }

            if (!recursiveCopy(runTmp, new File(Config.OUT_DIR, runName))) {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                break;
            }

            response.setStatus(HttpServletResponse.SC_CREATED);
            break;
        }
    }


    /**
     * Client call to upload the run back to the originating server.
     * This method does nothing if the run is local.
     * @param runName The name of the run
     * @throws IOException If the upload fails
     */
    public static void uploadIfOrigin(String runName) throws IOException {

        // 1. Check origin
        File originFile = new File(Config.OUT_DIR + File.separator + runName +
                    File.separator + "META-INF" + File.separator + "origin");

        if (!originFile.isFile())
            return; // Is local run, do nothing.

        String originSpec = readStringFromFile(originFile);
        int idx = originSpec.lastIndexOf('.');
        if (idx == -1) { // This is wrong, we do not accept this.
            logger.severe("Bad origin spec.");
            return;
        }
        idx = originSpec.lastIndexOf('.', idx - 1);
        if (idx == -1) {
            logger.severe("Bad origin spec.");
            return;
        }

        String host = originSpec.substring(0, idx);
        String key = null;
        URL target = null;

        // Search the poll hosts for this origin.
        for (int i = 0; i < Config.pollHosts.length; i++) {
            Config.HostInfo pollHost = Config.pollHosts[i];
            if (host.equals(pollHost.name)) {
                key = pollHost.key;
                target = new URL(pollHost.url, "upload");
                break;
            }
        }

        if (key == null) {
            logger.severe("Origin host/url/key not found!");
            return;
        }

        // 2. Jar up the run
        File jarFile = new File(Config.TMP_DIR, runName + ".jar");
        jar(Config.OUT_DIR, runName, jarFile.getAbsolutePath());

        // 3. Upload the run
        MultipartPostMethod post = new MultipartPostMethod(target.toString());
        post.addParameter("host", Config.FABAN_HOST);
        post.addParameter("key", key);
        post.addParameter("origin", "true");
        post.addParameter("jarfile", jarFile);
        HttpClient client = new HttpClient();
        client.setConnectionTimeout(5000);
        int status = client.executeMethod(post);
        if (status == HttpStatus.SC_UNAUTHORIZED)
            logger.severe("Server " + host + "denied permission to upload run "
                            + runName + '!');
        else if (status == HttpStatus.SC_NOT_ACCEPTABLE)
            logger.severe("Run " + runName + " origin error!");
        else if (status != HttpStatus.SC_CREATED)
            logger.severe("Server responded with status code " +
                    status + ". Status code 201 (SC_CREATED) expected.");
    }

    // TODO: General upload client for result server.
}
