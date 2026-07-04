import java.io.*;
import java.net.*;

public class MyServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                    String filename = in.readUTF();
                    File file = new File(filename);

                    if (!file.exists()) {
                        out.writeLong(-1);
                        out.flush();
                        continue;
                    }

                    out.writeLong(file.length());

                    byte[] buffer = new byte[4 * 1024 * 1024];

                    try (FileInputStream fileIn = new FileInputStream(file)) {
                        int bytesRead;
                        while ((bytesRead = fileIn.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }

                    out.flush();
                    System.out.println("Sent: " + filename);
                }
            }
        }
    }
}