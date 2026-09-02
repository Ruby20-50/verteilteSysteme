### 1. Welchen Zweck hat traceroute, was bedeuten die Ausgaben?

traceroute ermittelt den Weg (die Route) von IP-Paketen vom eigenen Rechner zu einem Zielhost, indem es alle Router (Hops) auf diesem Pfad anzeigt. Die Ausgabe zeigt pro Hop die Hop-Nummer, die IP-Adresse (bzw. den Hostnamen) des jeweiligen Routers sowie die gemessene Round-Trip-Time (RTT).

### 2. Wie lautet der Filterausdruck für Wireshark, um ausschließlich die Netzwerkpakete des Traceroute-Laufs anhand der Server-Adresse anzuzeigen?

Um nur die Pakete zu/von der Server-Adresse zu sehen:

```
ip.addr == <Server-IP>
``` 
(ggf. kombiniert mit icmp, z. B. ip.addr == <Server-IP> && icmp, um nur die relevanten ICMP-Pakete des Traceroute-Laufs zu filtern)

### 3. Welche ICMP-Pakettypen werden von traceroute verwendet?

**Anfragen:** meist ICMP Echo Request (Typ 8) – unter Linux werden oft auch UDP-Pakete mit hohen Zielports verwendet
**Antworten:** ICMP Time Exceeded (Typ 11), gesendet von jedem Zwischenrouter, dessen TTL abläuft
**Letzte Antwort:** ICMP Echo Reply (Typ 0) oder bei UDP-Variante ICMP Destination Unreachable/Port Unreachable (Typ 3), vom Zielhost gesendet

### 4. Welche Rolle spielt das TTL-Feld im IPv4-Paket für die Funktionsweise von traceroute?

Das TTL-Feld (Time To Live) begrenzt die Lebensdauer eines Pakets im Netzwerk. traceroute nutzt dies gezielt: Es sendet Pakete mit aufsteigender TTL (1, 2, 3, …). Jeder Router auf dem Weg verringert die TTL um 1; erreicht sie 0, wird das Paket verworfen und der Router sendet ein ICMP „Time Exceeded" zurück an den Absender. So wird Hop für Hop jeder Router auf dem Pfad sichtbar gemacht.

### 5. Wie entstehen die verschiedenen Zahlen (erste Spalte, IP-Adressen, Zeitmessungen) in der Bildschirmausgabe von traceroute? Welche sind direkt im Wireshark nachzulesen?

**Erste Spalte (Hop-Nummer):** entspricht dem TTL-Wert des jeweils gesendeten Pakets (1, 2, 3, …) – direkt im IP-Header in Wireshark nachlesbar (Feld „Time to live").
**IP-Adressen:** stammen aus der Absenderadresse (Source IP) der zurückkommenden ICMP-Time-Exceeded-Pakete – in Wireshark im IP-Header des Antwortpakets sichtbar.
**Zeitmessungen (RTT):**  werden von traceroute selbst berechnet als Differenz zwischen Sende- und Empfangszeitpunkt des jeweiligen Pakets (Zeitstempel) – in Wireshark ablesbar über die Spalte „Time" bzw. durch Vergleich der Timestamps von Anfrage- und Antwortpaket, aber nicht als expliziter Wert im Paket selbst enthalten.