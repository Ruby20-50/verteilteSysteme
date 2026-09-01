# Datei-Transfer: Server/Client-Lösung

Lösung für die Aufgabe "Entwickeln Sie ein Server- und ein Client-Programm,
das eine Datei zwischen zwei Computern überträgt" (Verteilte Systeme).

## Enthaltene Dateien

```
datei-transfer/
├── MyServer.java          Server-Programm
├── MyClient.java          Client-Programm
├── TeilE.md              diese Datei
└── docker/                lokales Test-Setup
    ├── docker-compose.yml    
    ├──  MyServer.java
    └──  MyClient.java
```

## Funktionsweise

Das Programm verwendet **Java NIO** (`FileChannel`, `SocketChannel`) statt
klassischer `InputStream`/`OutputStream`-Kopien. Der zentrale Mechanismus
ist `FileChannel.transferTo()` bzw. `transferFrom()`, was unter Linux auf
den Syscall `sendfile()` abgebildet wird. Dadurch kopiert der Kernel die
Datei direkt aus dem Page-Cache in den Socket-Puffer, ohne dass die Bytes
jemals in den Java-Heap gelangen. Das spart zwei Kopiervorgänge und mehrere
User-/Kernel-Kontextwechsel gegenüber einer naiven `byte[]`-Schleife — bei
50 GB ein erheblicher Unterschied.

Weitere Maßnahmen zur Beschleunigung:

- **Größere Socket-Puffer** (8 MB statt der oft nur 64–128 KB großen
  Standardwerte des Betriebssystems), damit mehr unbestätigte Daten
  gleichzeitig unterwegs sein können, bevor auf TCP-ACKs gewartet werden muss.
- **`TCP_NODELAY`** deaktiviert Nagle's Algorithmus (wirkt sich vor allem auf
  die kleine Dateinamen-Anfrage aus, nicht auf den eigentlichen Datenstrom).

## Protokoll

1. Client öffnet eine TCP-Verbindung zum Server auf Port `8088`.
2. Client sendet den gewünschten Dateinamen als eine Textzeile,
   abgeschlossen mit `\n`.
3. Server sucht die Datei im eigenen Arbeitsverzeichnis und sendet zunächst
   einen 8-Byte-Header mit der Dateilänge (`long`). Ist die Datei nicht
   vorhanden, wird `-1` gesendet.
4. Server überträgt die Datei per `transferTo()` (Zero-Copy).
5. Client empfängt die Länge, öffnet eine lokale Datei und schreibt die
   eingehenden Bytes per `transferFrom()` direkt auf die Festplatte.
6. Die Zeitmessung beim Client beginnt unmittelbar nach dem Senden der
   Anfrage und endet, sobald das letzte Byte der Datei geschrieben wurde —
   wie in der Aufgabenstellung gefordert.

## Verwendung

### Testdatei erzeugen (auf dem Server-Rechner)

```bash
head -c 50000000000 /dev/urandom > file.bin
```

### Server starten

```bash
javac MyServer.java
java MyServer 8088
```

### Client starten (auf dem zweiten Rechner)

```bash
javac MyClient.java
java MyClient server.local:file.bin
```

Der Client gibt am Ende die gemessene Übertragungszeit und den Durchsatz
in MB/s aus.

### Integrität prüfen

Nach jeder Übertragung auf beiden Rechnern:

```bash
sha1sum file.bin
```

Die Hashes müssen übereinstimmen. Aus Genauigkeitsgründen wird das Hashing
bewusst *nicht* im Java-Code berechnet — eine SHA1-Berechnung über 50 GB ist
selbst langsam und würde die eigentliche Transferzeitmessung verfälschen.

## Lokales Testen ohne zweiten Rechner

Für Debugging gibt es zwei Optionen, beide **nur zum Testen der Korrektheit**, nicht für die offiziellen
Messwerte geeignet:

1. **Zwei Terminals, `localhost`** — schnellster Weg, um Bugs zu finden.
   Läuft aber nur über den Loopback-Adapter, ohne echte Netzwerkkarte,
   Switch oder reale TCP-Kongestionskontrolle. Die gemessenen Werte sind
   unrealistisch hoch.

    **Terminal 1 (server):**
        
      ```bash

      head -c 50000000000 /dev/urandom > file.bin   
      javac MyServer.java
      java MyServer 8088
      ```
      
    **Terminal 2 (client)** in a separate directory so it doesn't overwrite the original:

      ```bash

      mkdir client_side && cd client_side
      cp ../MyClient.java .
      javac MyClient.java
      java MyClient localhost:file.bin
      ```

### Ergebnis von Terminal test

1. erster Versuch      

    ```$ java MyClient localhost:file.bin
    Connecting to localhost:8088 for file 'file.bin'
    Received 50000000000 bytes (47683,72 MB) in 708,232 s  ->  67,33 MB/s
    ```

2. zweiter Versuch

    ```$ java MyClient localhost:file.bin
    Connecting to localhost:8088 for file 'file.bin'
    Received 50000000000 bytes (47683,72 MB) in 625,581 s  ->  76,22 MB/s
    ```
3. dritter Vesuch
    ```$ java MyClient localhost:file.bin
    Connecting to localhost:8088 for file 'file.bin'
    Received 50000000000 bytes (47683,72 MB) in 592,681 s  ->  80,45 MB/s
    ```

  eine mögliche Erklärung ist: das Page-cash spielt hier eine Rolle, indem mehr Daten sind im cach gespeichert, deswegen ist das schneller für jeden weiteren Versuch


Für die im LMS einzutragenden Ergebnisse und die Live-Demonstration sind
**zwei separate physische Rechner im Übungsraum** erforderlich, wie in der
Aufgabenstellung explizit gefordert.

## Zu erklärende Mechanismen (für die Bonuspunkte-Demo)

- **Zero-Copy-Transfer** (`sendfile()`/`splice()`): Kernel kopiert Daten
  direkt vom Page-Cache in den Socket-Puffer, ohne Umweg über den
  JVM-Heap.
- **Socket-Puffergröße**: begrenzt die Menge an "in flight"-Daten
  (Bandwidth-Delay-Product); zu kleine Puffer bremsen schnelle Verbindungen
  künstlich aus.
- **Flaschenhals-Analyse**: bei 50 GB ist meist entweder die sequentielle
  Lesegeschwindigkeit der Festplatte oder die Netzwerkbandbreite (z. B.
  ca. 119 MB/s bei 1 GbE) der limitierende Faktor — während der Messung mit
  `iostat`/`dstat` beobachten und begründen.
- **Page-Cache-Effekt**: Wurde `file.bin` kurz zuvor erstellt, liegt sie
  eventuell noch im RAM-Cache des Servers, was den ersten Durchlauf
  künstlich beschleunigt. Zwischen den drei Messungen ggf. mit
  `sync; echo 3 > /proc/sys/vm/drop_caches` (als root) den Cache leeren,
  um realistische, wiederholbare Werte zu erhalten.
