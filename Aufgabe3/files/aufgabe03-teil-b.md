# Teil B `Downloader.java`

## Zweck des Programms

Das Programm lädt eine beliebige Datei aus dem World Wide Web über **unverschlüsseltes HTTP** herunter. Die gesamte Netzwerkkommunikation basiert ausschließlich auf **rohen TCP-Sockets** (`java.net.Socket`) — es wird kein `HttpURLConnection`, keine externe HTTP-Bibliothek und kein Systemtool wie `curl` oder `wget` verwendet. Damit erfüllt die Lösung die Kernanforderung der Aufgabenstellung, das HTTP-Protokoll (Header/Body-Trennung, Statuszeilen, Request-Aufbau) selbst zu implementieren.

Der Aufruf erfolgt wie gefordert über ein Kommandozeilenargument:

```
java Downloader http://speedtest.belwue.net/BelWue_logo.svg
```

---

## Programmablauf (Methode `main`)

1. **Argumentprüfung**
   Falls kein Argument übergeben wurde, wird eine Nutzungshinweis (`Usage: java Downloader <url>`) ausgegeben und das Programm mit Exit-Code `1` beendet.

2. **Schema-Prüfung**
   Es wird geprüft, ob die URL mit `http://` beginnt. HTTPS oder andere Schemata werden bewusst abgelehnt, da die Aufgabenstellung ausdrücklich nur unverschlüsseltes HTTP verlangt.

3. **Zerlegung der URI**
   Die URL wird manuell in ihre Bestandteile zerlegt — genau wie in der Aufgabenstellung beschrieben:
   - **Schema** (`http://`) wird abgeschnitten.
   - **Host** wird bis zum ersten `/` extrahiert (z. B. `speedtest.belwue.net`).
   - **Pfad** ist der Rest inklusive führendem `/` (z. B. `/BelWue_logo.svg`). Fehlt ein `/`, wird `"/"` als Pfad angenommen.
   - **Port** wird fest auf `80` gesetzt (Standard-HTTP-Port), wie gefordert.

4. **Dateiname ableiten**
   Aus dem letzten Pfadsegment nach dem letzten `/` wird der lokale Dateiname bestimmt. Ist dieser leer (z. B. bei URLs ohne Dateinamen), wird ersatzweise `"downloaded_file"` verwendet.

5. **Speicherplatzprüfung via HTTP-HEAD**
   Vor dem eigentlichen Download wird `sendHeadRequest(...)` aufgerufen, um per **HTTP-HEAD-Anfrage** den `Content-Length`-Header zu ermitteln, ohne den Dateikörper zu übertragen. Ist die Content-Length bekannt (`> 0`), wird sie mit dem freien Speicherplatz im aktuellen Arbeitsverzeichnis (`new File(".").getFreeSpace()`) verglichen. Reicht der Platz nicht aus, bricht das Programm mit einer Fehlermeldung ab. Dies entspricht exakt der Anforderung, vor dem Download die Kapazität zu prüfen.

6. **Download**
   Ist genug Platz vorhanden (oder die Größe unbekannt), wird `downloadFile(...)` aufgerufen, welche die eigentliche Datei per HTTP-GET herunterlädt und speichert.

---

## Methode `sendHeadRequest(host, port, path)`

- Öffnet eine **eigene, separate** TCP-Socket-Verbindung (try-with-resources, wird automatisch geschlossen).
- Sendet manuell eine HTTP/1.0-HEAD-Anfrage:
  ```
  HEAD <path> HTTP/1.0
  Host: <host>
  Connection: close

  ```
- `Connection: close` sorgt dafür, dass der Server die Verbindung nach der Antwort beendet — wichtig bei HTTP/1.0, da kein Keep-Alive-Standard existiert.
- Liest die Antwort zeilenweise, bis eine **Leerzeile** erscheint (Ende des HTTP-Headers). Dabei wird gezielt nach der Kopfzeile `Content-Length:` gesucht (Groß-/Kleinschreibung wird ignoriert via `toLowerCase()`).
- Gibt die gefundene Content-Length zurück, oder `-1`, falls sie nicht vorhanden oder nicht parsebar ist.

**Wichtig:** Da HEAD laut HTTP-Spezifikation nur den Header, keinen Body liefert, wird hier korrekt kein Body gelesen.

---

## Methode `downloadFile(host, port, path, fileName, contentLength)`

1. Öffnet eine **neue** TCP-Socket-Verbindung (getrennt von der HEAD-Anfrage — HTTP/1.0 unterstützt kein Wiederverwenden derselben Verbindung für zwei Requests ohne Weiteres).
2. Sendet die HTTP-GET-Anfrage im selben Format wie oben, jedoch mit `GET` statt `HEAD`.
3. Ruft `parseResponseHeader(in)` auf, um den **Statuscode** aus der Antwort zu extrahieren (z. B. `200`). Ist der Code ungleich `200`, wird ein Fehler ausgegeben und abgebrochen — der Body wird in diesem Fall nicht verarbeitet.
4. Liest anschließend den **Nachrichtenkörper (Body)** byteweise in einen Puffer (`8192` Bytes) und schreibt ihn direkt in die Zieldatei (`FileOutputStream`).
5. Während des Downloads wird der **Fortschritt in Prozent** live berechnet und ausgegeben (`\r{percent%}`), sofern die Content-Length aus dem HEAD-Request bekannt war. Das `\r` sorgt dafür, dass die Ausgabe in derselben Zeile aktualisiert wird — dies erfüllt die Anforderung, den Fortschritt (0–100 %) auf dem Bildschirm anzuzeigen.
6. Nach Abschluss wird die Gesamtzahl der heruntergeladenen Bytes ausgegeben.

---

## Methode `parseResponseHeader(in)`

- Liest den `InputStream` **Byte für Byte**, nicht zeilenweise — das ist notwendig, weil direkt im Anschluss an den Header (ohne Zwischenschritt) der binäre Dateikörper folgt. Ein `BufferedReader` würde hier Bytes "verschlucken" (Pufferung), die eigentlich zum Body gehören.
- Erkennt das Ende des Headers durch die charakteristische Byte-Sequenz `\r\n\r\n` (CRLF CRLF), die laut HTTP-Spezifikation Header und Body trennt.
- Extrahiert aus der ersten Zeile (Statuszeile, z. B. `HTTP/1.0 200 OK`) den numerischen Statuscode und gibt ihn zurück.

Dies ist der technisch anspruchsvollste Teil des Programms, da hier exakt die von der Aufgabenstellung betonte **Unterscheidung zwischen HTTP-Header und HTTP-Body** korrekt umgesetzt werden muss: Der `InputStream` darf nach dem Header nicht "zu weit" gelesen werden, sonst gehen Body-Bytes verloren.

---

## Abgleich mit den Aufgabenanforderungen

| Anforderung | Erfüllt durch |
|---|---|
| URI als Kommandozeilenargument | `args[0]` in `main` |
| Kein `curl`/`wget`, eigene Implementierung | Reine `Socket`-basierte Implementierung |
| Nur Sockets, keine HTTP-Bibliotheken | `java.net.Socket`, manuelles Request-/Response-Parsing |
| Fortschrittsanzeige 0–100 % | `System.out.print("\r{" + percent + "%}")` in `downloadFile` |
| Speicherung im aktuellen Arbeitsverzeichnis, danach Programmende | `Paths.get(fileName)` (relativer Pfad), Programm endet nach dem `try`-Block |
| Speicherplatzprüfung via HTTP-HEAD vor Download | `sendHeadRequest` + Vergleich mit `getFreeSpace()` |
| Trennung Header/Body beachtet | `parseResponseHeader` liest exakt bis `\r\n\r\n` |
| URI-Zerlegung (Schema/Host/Pfad) | Manuelles String-Parsing in `main` |
| Testfall mit Bild möglich | Funktioniert unverändert mit `BelWue_logo.svg` |

---

## Anmerkung zu einem kleinen Diskrepanzpunkt

In der Aufgabenstellung wird als Beispielbefehl für die Speicherplatzprüfung `new File("/").getFreeSpace()` genannt (Wurzelverzeichnis). Im vorliegenden Code steht stattdessen `new File(".").getFreeSpace()` (aktuelles Arbeitsverzeichnis). Das ist inhaltlich **korrekter**, da die Datei ja tatsächlich im aktuellen Arbeitsverzeichnis gespeichert wird und nicht zwingend im selben Dateisystem wie `/` liegen muss — aber es lohnt sich, dies im Abgabetext kurz zu begründen, falls die Korrektur exakt den Beispielcode erwartet.
