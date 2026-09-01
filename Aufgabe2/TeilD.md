# Lösungsdokumentation: UDP TimeClient

## 1. Protokoll-Grundlagen (RFC 868, UDP-Modus)

Das Time-Protokoll funktioniert wie folgt über UDP:

- Der Client sendet ein **leeres Datagramm** an Port **37** des Servers.
- Der Server antwortet mit einem Datagramm, das genau **4 Bytes** enthält: eine vorzeichenlose 32-Bit-Ganzzahl in Network Byte Order, die die Sekunden seit **00:00:00 UTC, 1. Januar 1900** angibt.

Zwei Details sind für eine korrekte Implementierung wichtig:

1. Es wird kein Payload für die Anfrage benötigt, der Server reagiert auf jedes eintreffende UDP-Paket an Port 37, auch auf ein 0-Byte-Paket.
2. Das Ergebnis ist *unsigned* 32-Bit. Ein vorzeichenbehafteter Java-`int` läuft bei diesem Wert über, sobald die Sekundenanzahl seit 1900 `2^31` überschreitet — was im Jahr 2036 passiert. Naives Parsen funktioniert also heute, würde aber in ~10 Jahren stillschweigend brechen, wenn man das Vorzeichen nicht berücksichtigt.

## 2. Zuordnung von Code zu Anforderungen

### Kommandozeilenargument (Servername)

```java
if (args.length != 1) { ... }
String serverName = args[0];
```

Erfüllt direkt "Programm nimmt den Namen des Servers als Kommandozeilenargument entgegen" — mit einer Usage-Meldung als Fallback bei falscher Argumentanzahl.

### Aufbau des UDP-Sockets (NIO `DatagramChannel`)

```java
DatagramChannel channel = DatagramChannel.open();
channel.configureBlocking(true);
```

Verwendung von `java.nio.channels.DatagramChannel` statt der älteren `java.net.DatagramSocket`. Funktional äquivalent für diese Aufgabe, aber die NIO-Channel-API ist Buffer-basiert (`ByteBuffer`) statt Packet-Objekt-basiert (`DatagramPacket`), weshalb der restliche Code direkt mit Buffern arbeitet. `configureBlocking(true)` sorgt dafür, dass `receive()` wartet, bis eine Antwort eintrifft — passend für einen einfachen synchronen Client.

### Senden der leeren Anfrage

```java
ByteBuffer requestBuffer = ByteBuffer.allocate(0);
channel.send(requestBuffer, serverAddress);
```

Erfüllt "sendet eine UDP-Anfrage (formatiert gemäß der RFC-Spezifikation)" — für das Time-Protokoll *ist* ein leerer Payload die korrekt formatierte Anfrage. Es gibt keinen Header und keine Felder zu konstruieren.

### Empfang und Parsen der Antwort

```java
ByteBuffer responseBuffer = ByteBuffer.allocate(4);
channel.receive(responseBuffer);
responseBuffer.flip();
long secondsSince1900 = Integer.toUnsignedLong(responseBuffer.getInt());
```

- 4 Bytes werden alloziert, da die RFC eine feste 32-Bit-Antwort vorgibt.
- `flip()` wechselt den Buffer vom Schreib- in den Lesemodus (nach dem Füllen durch `receive`), sodass `getInt()` ab Position 0 liest.
- `ByteBuffer` verwendet standardmäßig **Big-Endian**, was der Network Byte Order entspricht — kein manuelles Byte-Swapping nötig. Ein Detail, das man in anderen Sprachen/APIs (oft Little-Endian als native Ordnung) explizit beachten muss.
- `Integer.toUnsignedLong(...)` ist der entscheidende Korrektheits-Fix: `getInt()` liefert einen vorzeichenbehafteten `int`; die Reinterpretation der Bits als unsigned in einem `long` vermeidet den oben genannten Jahr-2036-Überlauf.

### Umrechnung in Unix-Epoch und Berliner Ortszeit

```java
long unixSeconds = secondsSince1900 - SECONDS_1900_TO_1970; // 2208988800L
Instant instant = Instant.ofEpochSecond(unixSeconds);
ZonedDateTime berlinTime = instant.atZone(ZoneId.of("Europe/Berlin"));
```

- `SECONDS_1900_TO_1970 = 2208988800L` ist der Standard-Offset zwischen den beiden Epochen.
- `Instant` repräsentiert einen absoluten Zeitpunkt (UTC-basiert, ohne Zeitzone); `atZone(...)` wendet anschließend die Regeln der Zeitzone `Europe/Berlin` (inkl. Sommerzeit) an, um die lokale Uhrzeit zu erhalten. Erfüllt korrekt "im Format ... für die lokale Zeitzone Europe/Berlin."

### Formatierung der Ausgabe

```java
DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
System.out.println(dateFormatter.format(berlinTime));
System.out.println(timeFormatter.format(berlinTime));
```

Entspricht exakt dem geforderten zweizeiligen Ausgabeformat (`2024-11-18` / `11:04:45`).

### Aufräumen

```java
channel.close();
```

Gibt die Socket-Ressource frei — guter Stil, auch wenn es für ein kurzlebiges CLI-Tool nicht kritisch ist.

## 3. Bibliotheks-Constraint-Check

Die Aufgabe verlangt ausschließlich mitgelieferte Standardbibliotheken. Die Imports (`java.net.InetSocketAddress`, `java.nio.*`, `java.time.*`) gehören alle zur JDK-Standardbibliothek — keine Drittanbieter-Abhängigkeiten.

## 4. Hinweis zum Testen

Test gegen `time.nist.gov`:

```
java TimeClient time.nist.gov
```

Ergebnis mit einer Referenzuhr vergleichen. NIST-Server drosseln teils schnelle wiederholte Anfragen — ein Timeout beim Testen ist dann eher serverseitiges Rate-Limiting als ein Client-Fehler, was sich als Anmerkung im Pflichtenheft/Bericht festhalten lässt.