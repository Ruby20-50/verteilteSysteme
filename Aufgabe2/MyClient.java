import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class MyClient {

    public static void main(String[] args) throws IOException {

        if (args.length != 1 || !args[0].contains(":")) {
            System.out.println("Benutzung: java MyClient <server>:<dateiname>");
            return;
        }

        String[] parts = args[0].split(":", 2);
        String serverName = parts[0];
        String fileName = parts[1];

        int port = 8088;

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverName, port));

        long startTime = System.nanoTime();

        ByteBuffer requestBuffer = ByteBuffer.wrap(fileName.getBytes());
        socketChannel.write(requestBuffer);

        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);

        while (sizeBuffer.hasRemaining()) {
            socketChannel.read(sizeBuffer);
        }

        sizeBuffer.flip();
        long fileSize = sizeBuffer.getLong();

        if (fileSize == -1) {
            System.out.println("Datei wurde auf dem Server nicht gefunden.");
            socketChannel.close();
            return;
        }

        RandomAccessFile raf =
                new RandomAccessFile("copy_" + fileName, "rw");

        FileChannel fileChannel = raf.getChannel();

        long position = 0;

        while (position < fileSize) {
            position += fileChannel.transferFrom(
                    socketChannel,
                    position,
                    fileSize - position
            );
        }

        long endTime = System.nanoTime();

        fileChannel.close();
        raf.close();
        socketChannel.close();

        double seconds =
                (endTime - startTime) / 1_000_000_000.0;

        System.out.println("Datei empfangen: copy_" + fileName);
        System.out.println("Bytes empfangen: " + fileSize);
        System.out.println("Zeit: " + seconds + " Sekunden");
    }
}