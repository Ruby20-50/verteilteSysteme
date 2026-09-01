import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

/**
 * MyClient: connects to MyServer, requests a file by name, and writes the
 * received bytes directly to disk via FileChannel.transferFrom (zero-copy
 * receive path). Measures the time from sending the request to receiving
 * the last byte of the file, per the assignment's definition.
 *
 * Usage: java MyClient <server>:<filename>
 * Example: java MyClient server.local:file.bin
 */
public class MyClient {

    private static final int SOCKET_BUFFER_SIZE = 8 * 1024 * 1024; // 8 MB
    private static final int PORT = 8088;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java MyClient <server>:<filename>");
            System.err.println("Example: java MyClient server.local:file.bin");
            System.exit(1);
        }

        // Split on the LAST colon, since IPv6 addresses/hostnames could
        // theoretically contain colons; the filename never will here.
        String arg = args[0];
        int lastColon = arg.lastIndexOf(':');
        if (lastColon == -1) {
            System.err.println("Invalid argument. Expected format server:filename");
            System.exit(1);
        }
        String host = arg.substring(0, lastColon);
        String filename = arg.substring(lastColon + 1);

        System.out.println("Connecting to " + host + ":" + PORT + " for file '" + filename + "'");

        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(host, PORT))) {
            channel.socket().setReceiveBufferSize(SOCKET_BUFFER_SIZE);
            channel.socket().setTcpNoDelay(true);

            // --- Timer starts here: "time between sending the request and
            // --- receiving the last byte of the file" (per assignment spec) ---
            long startTime = System.nanoTime();

            ByteBuffer request = ByteBuffer.wrap((filename + "\n").getBytes("UTF-8"));
            while (request.hasRemaining()) {
                channel.write(request);
            }

            // Read the 8-byte length header
            ByteBuffer header = ByteBuffer.allocate(8);
            while (header.hasRemaining()) {
                if (channel.read(header) == -1) {
                    throw new EOFException("Server closed connection before sending header");
                }
            }
            header.flip();
            long length = header.getLong();

            if (length < 0) {
                System.err.println("Server reported the file was not found.");
                return;
            }

            File outFile = new File(filename);
            try (FileChannel fileChannel = FileChannel.open(outFile.toPath(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

                long position = 0;
                while (position < length) {
                    long transferred = fileChannel.transferFrom(channel, position, length - position);
                    if (transferred == 0 && !channel.isOpen()) {
                        throw new EOFException("Connection closed before file fully received");
                    }
                    position += transferred;
                }
            }

            // --- Timer stops here: last byte has been written to disk ---
            long endTime = System.nanoTime();

            double seconds = (endTime - startTime) / 1_000_000_000.0;
            double megabytes = length / (1024.0 * 1024.0);
            double mbPerSec = megabytes / seconds;

            System.out.printf("Received %d bytes (%.2f MB) in %.3f s  ->  %.2f MB/s%n",
                    length, megabytes, seconds, mbPerSec);
        }
    }
}