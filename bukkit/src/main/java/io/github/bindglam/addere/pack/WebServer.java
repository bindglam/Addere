package io.github.bindglam.addere.pack;

import io.github.bindglam.addere.AdderePlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.net.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.Runtime;
import java.util.function.Consumer;

public class WebServer implements Consumer<ScheduledTask> {
    private static final String WEB_ROOT = "./plugins/Addere/";
    public static ServerSocket serverSocket;
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static DataOutputStream dataOutputStream;
    private static HTTPRequestParser httpRequestParser;

    public static ThreadLocal<Boolean> isRunning = ThreadLocal.withInitial(() -> true);

    @Override
    public void accept(ScheduledTask task) {
        if(!AdderePlugin.INSTANCE.getPlugin().getConfig().getBoolean("self-host.enabled")) return;
        int port = AdderePlugin.INSTANCE.getPlugin().getConfig().getInt("self-host.port");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
        } catch (IOException e) {
            System.err.println("Unable to listen on port " + port + ": " + e.getMessage());
            return;
        }

        while (isRunning.get()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                continue;
            }

            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);
                handleRequests();

                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private static void handleRequests() throws IOException {
        httpRequestParser = new HTTPRequestParser(inputStream);

        // GET or POST REQUEST ONLY
        String requestMethod = httpRequestParser.getRequestMethod();
        if (!(requestMethod.equals("GET") || requestMethod.equals("POST"))) {
            invalidRequestErr();
            return;
        }

        String fileName = httpRequestParser.getFileName();
        String filePath = WEB_ROOT + fileName;
        File file = new File(filePath);

        // File permission or not found error.
        if (!file.exists()) {
            fileNotFoundError(fileName);
            return;
        }

        if (!file.canRead()) {
            forbiddenAccessError(fileName);
            return;
        }

        // Assume everything is OK then.  Send back a reply.
        dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");

        String queryString = httpRequestParser.getQueryString();

        if (fileName.endsWith("pl")) {
            Process p;
            String env = "REQUEST_METHOD=" + requestMethod + " ";

            if (requestMethod.equals("POST")) {
                env += "CONTENT_TYPE=" + httpRequestParser.getContentType() + " " +
                        "CONTENT_LENGTH=" + httpRequestParser.getContentLength() + " ";
            } else {
                env += "QUERY_STRING=" + queryString + " ";
            }

            p = Runtime.getRuntime().exec("/usr/bin/env " + env +
                    "/usr/bin/perl " + filePath);

            if (requestMethod.equals("POST")) {
                // Pass form data into Perl process
                DataOutputStream o = new DataOutputStream(p.getOutputStream());
                o.writeBytes(httpRequestParser.getFormData() + "\r\n");
                o.close();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // Write response content
            String l;
            while ((l = br.readLine()) != null) {
                dataOutputStream.writeBytes(l + "\r\n");
            }
            dataOutputStream.writeBytes("\r\n");
        } else {
            if (file.isDirectory()) {
                folderRequests(file);
            } else {
                staticFileRequests(filePath);
            }
        }

        dataOutputStream.flush();
    }

    private static void folderRequests(File folder) {
        try {
            dataOutputStream.writeBytes("Content-type: text/html\r\n");
            dataOutputStream.writeBytes("\r\n");

            dataOutputStream.writeBytes("<html><body>"); // html open tag
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                // calculate relative path
                Path rootPath = Paths.get(new File(WEB_ROOT).toURI()); // this can be static
                Path filePath = Paths.get(file.toURI());
                String pathString = rootPath.relativize(filePath).toString();

                // construct html
                dataOutputStream.writeBytes("<a href=\"/" + pathString + "\">"
                        + file.getName() + "</a><br>");
            }
            dataOutputStream.writeBytes("</body></html>"); // html close tag
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void staticFileRequests(String filePath) {
        try {
            if (filePath.endsWith(".html")) {
                dataOutputStream.writeBytes("Content-type: text/html\r\n");
            } else if (filePath.endsWith(".jpg")) {
                dataOutputStream.writeBytes("Content-type: image/jpeg\r\n");
            } else if (filePath.endsWith("gif")) {
                dataOutputStream.writeBytes("Content-type: image/gif\r\n");
            } else if (filePath.endsWith("css")) {
                dataOutputStream.writeBytes("Content-type: text/css\r\n");
            }
            dataOutputStream.writeBytes("\r\n");
            // Read content at 1KB rate.
            File file = new File(filePath);
            byte[] buffer = new byte[(int)file.length()];
            FileInputStream fis = new FileInputStream(file);
            int size = fis.read(buffer);
            while (size > 0) {
                dataOutputStream.write(buffer, 0, size);
                size = fis.read(buffer);
            }
        } catch (IOException e) {
            System.err.println("Unable to READ/WRITE: "  + e.getMessage());
        }
    }

    private static void invalidRequestErr() throws IOException {
        String errorMessage = "This Web Server only demonstrates GET or POST requests\r\n";
        dataOutputStream.writeBytes("HTTP/1.1 400 Bad Request\r\n");
        dataOutputStream.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
        dataOutputStream.writeBytes(errorMessage);
    }

    private static void fileNotFoundError(String fileName) throws IOException {
        String errorMessage = "Unable to find " + fileName + " on this server.\r\n";
        dataOutputStream.writeBytes("HTTP/1.1 404 Not Found\r\n");
        dataOutputStream.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
        dataOutputStream.writeBytes(errorMessage);
    }

    private static void forbiddenAccessError(String fileName) throws IOException {
        String errorMessage = "You need permission to access " + fileName + " on this server.\r\n";
        dataOutputStream.writeBytes("HTTP/1.1 403 Forbidden\r\n");
        dataOutputStream.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
        dataOutputStream.writeBytes(errorMessage);
    }
}

class HTTPRequestParser {

    private String requestMethod, fileName, queryString, formData;
    private Hashtable<String, String> headers;
    private int[] ver;

    public HTTPRequestParser(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        requestMethod = "";
        fileName = "";
        queryString = "";
        formData = "";
        headers = new Hashtable<>();
        try {
            // Wait for HTTP request from the connection
            String line = br.readLine();

            // Bail out if line is null. In case some client tries to be
            // funny and close immediately after connection.  (I am
            // looking at you, Chrome!)
            if (line == null) {
                return;
            }

            String[] tokens = line.split(" ");

            requestMethod = tokens[0];

            if (tokens[1].contains("?")) {
                String urlComponents[] = tokens[1].split("\\?");
                fileName = urlComponents[0];
                queryString = urlComponents[1];
            } else {
                fileName = tokens[1];
            }

            // Read/Parse HTTP headers
            int idx;
            line = br.readLine();
            while (!line.isEmpty()) {
                idx = line.indexOf(":");
                if (idx < 0) {
                    headers = null;
                    break;
                } else {
                    headers.put(line.substring(0, idx).toLowerCase(),
                            line.substring(idx+1).trim());
                }
                line = br.readLine();
            }

            // Read POST Data
            if (requestMethod.equals("POST")) {
                int contentLength = getContentLength();
                final char[] data = new char[contentLength];
                for (int i = 0; i < contentLength; i++) {
                    data[i] = (char) br.read();
                }
                formData = new String(data);
            }
        } catch (IOException e) {
            System.err.println("Unable to READ | WRITE: "  + e.getMessage());
        }
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getFileName() {
        return fileName;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getContentType() {
        return headers.get("content-type");
    }

    public int getContentLength() {
        return Integer.parseInt(headers.get("content-length"));
    }

    public String getFormData() {
        return formData;
    }

    public static List<File> fileList(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<File>();

        // get all the files from the directory
        File[] fileList = directory.listFiles();
        for(File file : fileList) {
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(fileList(file.getAbsolutePath()));
            }
        }
        return resultList;
    }
}