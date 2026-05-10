Die gemessenen Zeiten waren ähnlich, aber nicht komplett gleich
Mögliche Einflussfaktoren sind
Netzwerkauslastung
Auslastung der Festplatten von client1 und client2
Der  Switch kann gleichzeitig Daten anderer Gruppen verarbeiten
Die Übertragungszeit schwankt leicht zwischen den Messungen
 🧝🏻‍♀️🧚‍♀️
 
# führen Sie nun ein Experiment zur Datenübertragung durch.
# Bestimmen Sie nach Abschluss des Befehls die Größe der erzeugten Datei 
19GB
# und machen Sie eine begründete Abschätzung, wie lange die Übertragung dieser Datei von client1 zu client2 dauern wird. Halten Sie Ihre Schätzung fest.
2.5 - 5 minuten 
weil auf Windows das würde quasi 5 bis 10 minuten dauern 
aber bei linux geht's schneller
# Überlegen Sie anschließend, welche Methode sich für eine möglichst schnelle Dateiübertragung eignet.
wir haben für rsync entscheidet
# protokollieren Sie die gemessenen Zeiten.
Übertragung 1: 0min 10, 820s
Übertragung 2: 3m 5,493s
Übertragung 3: 3m 14, 176s

#Vergleichen Sie die Ergebnisse: Sind die gemessenen Zeiten ähnlich oder 
# unterscheiden sie sich deutlich? Diskutieren Sie mögliche Einflussfaktoren
erst mal unterscheidet sich deutlich mit der zwei weitern mals
1- gleichzeitige mehrere Übertragung.
2- die Ausnutzung einer Festplatte währscheinlich von client2
# und formulieren Sie dazu begründete Hypothesen.
Beim ersten Mal war der Netzwerkverkehr völlig frei, sodass die
 Pakete direkt gesendet wurden. Beim zweiten und dritten Mal mussten 
 die Pakete hingegen warten, bis andere Übertragungen abgeschlossen 
 waren. Eine weitere Möglichkeit ist, dass die Festplatte fast voll 
 ist, was die Übertragung verlangsamt – denn dann müssen die Daten 
 gezielt in die verbleibenden freien Bereiche geschrieben werden,
  was mehr Zeit in Anspruch nimmt.