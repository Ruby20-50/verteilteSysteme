import java.io.*;
import java.net.*;

public class proxy {

    // A1: both backend apps listen on port 80 INSIDE their containers
    static final int APP_PORT = 80;

    // A2: on the proxynet network, Docker's internal DNS resolves
    //     these hostnames to each container's IP automatically
    static final String APP_A_HOST = "vs-app-a";
    static final String APP_B_HOST = "vs-app-b";

    // the proxy listens on 80 inside its own container (mapped to 8087 on the host)
    static final int LISTEN_PORT = 80;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(LISTEN_PORT);
        System.out.println("Proxy listening on port " + LISTEN_PORT);
        while (true) {
            Socket client = server.accept();
            new Thread(() -> handle(client)).start(); // one thread per request
        }
    }

    static void handle(Socket client) {
        try (client) {
            InputStream  clientIn  = client.getInputStream();
            OutputStream clientOut = client.getOutputStream();

            // --- 1. read request line + headers, byte by byte, until the blank line ---
            ByteArrayOutputStream headerBuf = new ByteArrayOutputStream();
            int p3 = -1, p2 = -1, p1 = -1, b;
            while ((b = clientIn.read()) != -1) {
                headerBuf.write(b);
                if (p3 == '\r' && p2 == '\n' && p1 == '\r' && b == '\n') break; // \r\n\r\n
                p3 = p2; p2 = p1; p1 = b;
            }
            String headerText = headerBuf.toString("ISO-8859-1");
            if (headerText.isEmpty()) return;

            // --- 2. parse the request line, e.g. "GET /appA/logo.svg HTTP/1.1" ---
            String[] lines = headerText.split("\r\n");
            String[] reqLine = lines[0].split(" ");
            String method  = reqLine[0];
            String path    = reqLine[1];
            String version = reqLine[2];

            // --- 3. pick the backend and rewrite the path (A3) ---
            String targetHost;
            String newPath;
            if (path.equals("/appA") || path.startsWith("/appA/")) {
                targetHost = APP_A_HOST;
                newPath = path.substring("/appA".length());
            } else if (path.equals("/appB") || path.startsWith("/appB/")) {
                targetHost = APP_B_HOST;
                newPath = path.substring("/appB".length());
            } else {
                clientOut.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                return;
            }
            if (newPath.isEmpty()) newPath = "/";

            // --- 4. rebuild the request: new path, rewritten Host (A4), forced close ---
            StringBuilder req = new StringBuilder();
            req.append(method).append(" ").append(newPath).append(" ").append(version).append("\r\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) continue;
                String lower = line.toLowerCase();
                if (lower.startsWith("host:")) {
                    req.append("Host: ").append(targetHost).append("\r\n");
                } else if (lower.startsWith("connection:") || lower.startsWith("proxy-connection:")) {
                    // drop it — we set our own below
                } else {
                    req.append(line).append("\r\n");
                }
            }
            req.append("Connection: close\r\n");
            req.append("\r\n");

            // --- 5. forward to backend, relay the raw response back ---
            try (Socket backend = new Socket(targetHost, APP_PORT)) {
                backend.getOutputStream().write(req.toString().getBytes("ISO-8859-1"));
                backend.getOutputStream().flush();

                InputStream backendIn = backend.getInputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = backendIn.read(buf)) != -1) {
                    clientOut.write(buf, 0, n);   // raw bytes → works for text AND binary
                }
                clientOut.flush();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}