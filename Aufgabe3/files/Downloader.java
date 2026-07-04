
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;

public class Downloader {
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
    System.err.println("Usage: java Downloader <url>");
    System.exit(1);
        }
    String url = args[0];

     if (!url.startsWith("http://")) {
            System.err.println("Error: Only unencrypted HTTP (http://) is supported.");
            System.exit(1);
        }

        String withoutScheme = url.substring("http://".length());
        int slashIndex = withoutScheme.indexOf('/');
        String host;
        String path;
        if (slashIndex == -1) {
            host = withoutScheme;
            path = "/";
        } else {
            host = withoutScheme.substring(0, slashIndex);
            path = withoutScheme.substring(slashIndex); // includes leading "/"
        }
        int port = 80;

        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.isEmpty()) {
            fileName = "downloaded_file";
        }  
        
        // send HEAD request to get Content-Length
        long contentLength = sendHeadRequest(host, port, path);
          downloadFile(host, port, path, fileName, contentLength);
    }
    private static long sendHeadRequest(String host, int port, String path) throws IOException {
       try( Socket socket = new Socket(host, port)){
        PrintWriter out = new PrintWriter( new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

         out.print("HEAD " + path + " HTTP/1.0\r\n");
            out.print("Host: " + host + "\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

        // Read response
        String line;
        long contentLength = -1;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                        contentLength = Long.parseLong(line.split(":", 2)[1].trim());
                    } catch (NumberFormatException ignored) { /* leave -1 */ }
            }
        }
        return contentLength;
        }
      
    }
    private static void downloadFile(String host, int port, String path,
         String fileName, long contentLength) throws IOException {
        System.out.println("Downloading... ");

        try (Socket socket = new Socket(host, port)) {

            OutputStream out = socket.getOutputStream();
            String request =
                    "GET " + path + " HTTP/1.0\r\n" +
                    "Host: " + host + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            out.write(request.getBytes("UTF-8"));
            out.flush();

            InputStream in = socket.getInputStream();
            int statusCode = parseResponseHeader(in);
            if (statusCode != 200) {
                System.err.println("Error: HTTP response code " + statusCode);
                System.exit(1);
            }

            // Stream body to file
            Path outPath = Paths.get(fileName);
            try (FileOutputStream fileOut = new FileOutputStream(outPath.toFile())) {
                byte[] buffer = new byte[8192];
                long totalRead = 0;
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    if (contentLength > 0) {
                        int percent = (int) ((totalRead * 100) / contentLength);
                        System.out.print("\r{" + percent + "%}");
                    }
                }
                System.out.printf("\rDownload %,d bytes", totalRead);
            }
        }
    }
     private static int parseResponseHeader(InputStream in) throws IOException {
  
        StringBuilder headerBuilder = new StringBuilder();
        int prev1 = -1, prev2 = -1, prev3 = -1;

        while (true) {
            int b = in.read();
            if (b == -1) break; // connection closed early

            headerBuilder.append((char) b);

            // Detect end of headers: \r\n\r\n
            if (prev3 == '\r' && prev2 == '\n' && prev1 == '\r' && b == '\n') {
                break;
            }
            prev3 = prev2;
            prev2 = prev1;
            prev1 = b;
        }

        String header = headerBuilder.toString();
        // System.out.print(header);
       
        // Extract status code from first line: "HTTP/1.x NNN ..."
        String firstLine = header.split("\r\n")[0];
        String[] parts = firstLine.split(" ");
        if (parts.length < 2) {
            throw new IOException("Invalid HTTP response: " + firstLine);
        }

        return Integer.parseInt(parts[1].trim());
    }

//    private static void printProgressBar(int percent, long bytesReceived) {
//     System.out.print("\r{" + percent + "%}");
// }

   
}