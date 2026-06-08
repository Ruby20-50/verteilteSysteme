import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MyServer {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Benutzung: java MyServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));

        System.out.println("Server wartet auf Port " + port);

        SocketChannel clientChannel = serverChannel.accept();
        System.out.println("Client verbunden");

        ByteBuffer nameBuffer = ByteBuffer.allocate(1024);
        clientChannel.read(nameBuffer);
        nameBuffer.flip();

        byte[] nameBytes = new byte[nameBuffer.remaining()];
        nameBuffer.get(nameBytes);

        String fileName = new String(nameBytes).trim();

        File file = new File(fileName);

        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);

        if (!file.exists()) {
            sizeBuffer.putLong(-1);
            sizeBuffer.flip();
            clientChannel.write(sizeBuffer);
            System.out.println("Datei nicht gefunden: " + fileName);
        } else {
            long fileSize = file.length();

            sizeBuffer.putLong(fileSize);
            sizeBuffer.flip();
            clientChannel.write(sizeBuffer);

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel fileChannel = raf.getChannel();

            long position = 0;

            while (position < fileSize) {
                position += fileChannel.transferTo(
                        position,
                        fileSize - position,
                        clientChannel
                );
            }

            fileChannel.close();
            raf.close();

            System.out.println("Datei gesendet: " + fileName);
            System.out.println("Größe: " + fileSize + " Bytes");
        }

        clientChannel.close();
        serverChannel.close();
    }
}