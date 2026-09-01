import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

/**
 * MyServer: listens on a given port, accepts a filename request as a single
 * newline-terminated line of text, then streams the requested file back to
 * the client using a zero-copy transfer (FileChannel.transferTo, which maps
 * to the sendfile() syscall on Linux). This avoids copying file bytes into
 * the JVM heap at all — the kernel moves data directly from the file's page
 * cache into the socket buffer.
 *
 * Usage: java MyServer <port>
 */
public class MyServer {

    // Socket buffer size: bigger than the OS default (~64-128KB) so more
    // data can be "in flight" before we have to wait for TCP ACKs.
    private static final int SOCKET_BUFFER_SIZE = 8 * 1024 * 1024; // 8 MB

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java MyServer <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.socket().setReuseAddress(true);
            serverChannel.bind(new InetSocketAddress(port));
            System.out.println("MyServer listening on port " + port);

            // Accept clients one at a time (spec: only one concurrent client),
            // but keep serving new connections in a loop.
            while (true) {
                try (SocketChannel client = serverChannel.accept()) {
                    client.socket().setSendBufferSize(SOCKET_BUFFER_SIZE);
                    client.socket().setTcpNoDelay(true);
                    System.out.println("Client connected: " + client.getRemoteAddress());
                    handleClient(client);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        }
    }

    private static void handleClient(SocketChannel client) throws IOException {
        String requestedName = readLine(client);
        System.out.println("Requested file: " + requestedName);

        // Only allow files in the server's own directory (strip any path
        // components the client might have sent, e.g. "../../etc/passwd").
        String safeName = Paths.get(requestedName).getFileName().toString();
        File file = new File(safeName);

        if (!file.exists() || !file.isFile()) {
            System.err.println("File not found: " + safeName);
            sendHeader(client, -1L); // -1 signals "not found" to the client
            return;
        }

        long length = file.length();
        sendHeader(client, length);

        try (FileChannel fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            long position = 0;
            while (position < length) {
                long transferred = fileChannel.transferTo(position, length - position, client);
                if (transferred == 0) {
                    // Socket buffer momentarily full; brief pause avoids a busy-spin.
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during transfer", e);
                    }
                } else {
                    position += transferred;
                }
            }
        }
        System.out.println("Finished sending " + length + " bytes for " + safeName);
    }

    private static void sendHeader(SocketChannel client, long length) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8);
        header.putLong(length);
        header.flip();
        while (header.hasRemaining()) {
            client.write(header);
        }
    }

    /** Reads a single newline-terminated line from the channel (the filename request). */
    private static String readLine(SocketChannel channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        ByteBuffer buf = ByteBuffer.allocate(1);
        while (true) {
            buf.clear();
            int read = channel.read(buf);
            if (read == -1) break; // connection closed
            buf.flip();
            char c = (char) buf.get();
            if (c == '\n') break;
            sb.append(c);
        }
        return sb.toString().trim();
    }
}