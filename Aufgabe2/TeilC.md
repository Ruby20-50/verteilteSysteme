1. Welchen Zweck erfüllt das Protokoll?

    Das Protokoll liefert ein site-unabhängiges, maschinenlesbares Datum/Uhrzeit. Der Zeitdienst sendet die Zeit in Sekunden seit dem 1. Januar 1900 zurück, damit Systeme ohne eigene Uhr oder mit fehlerhafter Uhr ihre Zeit schnell abgleichen können.

2. Welche Transportschicht-Protokolle benutzt das Time Protocol?

    Das Protokoll kann sowohl über TCP als auch über UDP verwendet werden.

3. Welche Portnummern werden vom Server genutzt?

    Der Server hört auf Port 37 (oktal 45) — bei beiden Transportprotokollen (TCP und UDP) derselbe Port.

4. Was muss der Client senden?

    Der Server sendet die Zeit als 32-Bit-Binärzahl zurück und schließt bei TCP anschließend die Verbindung. Kann der Server die Zeit nicht bestimmen, verweigert/schließt er bei TCP die Verbindung ohne Antwort bzw. verwirft bei UDP das Paket kommentarlos.

5. Was schickt der Server als Antwort?

    Der Server sendet die Zeit als 32-Bit-Binärzahl zurück und schließt bei TCP anschließend die Verbindung. Kann der Server die Zeit nicht bestimmen, verweigert/schließt er bei TCP die Verbindung ohne Antwort bzw. verwirft bei UDP das Paket kommentarlos.


6. Wie wird das Problem unterschiedlicher Zeitzonen gelöst?

     Der RFC löst das implizit, indem die Zeit ausschließlich in GMT angegeben wird (Sekunden seit Mitternacht, 1. Januar 1900 GMT). Keine lokale Zeitzone im Protokoll. Umrechnung in lokale Zeit ist Aufgabe des Clients.

7. Welche Probleme können in Bezug auf die Genauigkeit der Zeiteinstellung entstehen?

    Übertragungsverzögerung (Netzwerk-Latenz) wird nicht kompensiert — die empfangene Zeit ist bereits leicht "alt".
    Auflösung nur in ganzen Sekunden, keine Sub-Sekunden-Genauigkeit.
    32-Bit-Feld läuft über im Jahr 2036 an seine Grenze (Wertebereich reicht nur bis dahin).