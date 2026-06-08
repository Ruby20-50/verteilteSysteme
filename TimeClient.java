import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeClient {

    private static final int TIME_PORT = 37;
    private static final long SECONDS_1900_TO_1970 = 2208988800L;

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Benutzung: java TimeClient <server>");
            return;
        }

        String serverName = args[0];

        InetSocketAddress serverAddress =
                new InetSocketAddress(serverName, TIME_PORT);

        DatagramChannel channel =
                DatagramChannel.open();

        channel.configureBlocking(true);

        ByteBuffer requestBuffer =
                ByteBuffer.allocate(0);

        channel.send(requestBuffer, serverAddress);

        ByteBuffer responseBuffer =
                ByteBuffer.allocate(4);

        channel.receive(responseBuffer);

        responseBuffer.flip();

        long secondsSince1900 =
                Integer.toUnsignedLong(responseBuffer.getInt());

        long unixSeconds =
                secondsSince1900 - SECONDS_1900_TO_1970;

        Instant instant =
                Instant.ofEpochSecond(unixSeconds);

        ZonedDateTime berlinTime =
                instant.atZone(ZoneId.of("Europe/Berlin"));

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern("HH:mm:ss");

        System.out.println(dateFormatter.format(berlinTime));
        System.out.println(timeFormatter.format(berlinTime));

        channel.close();
    }
}