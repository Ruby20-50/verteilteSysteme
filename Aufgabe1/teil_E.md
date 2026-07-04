# Nach der erfolgreichen Übertragung stellt sich die Frage, wie Sie überprüfen können, ob die Datei korrekt übertragen wurde und keine Fehler während der Übertragung aufgetreten sind.
Hashfunktion macht jeder Objekt einzelartig!
können wir die Funktion auch auf Dateien implementieren?
# Diskutieren Sie zunächst, ob ein Vergleich der Dateigrößen hierfür ausreicht. Ist die Dateigröße ein zuverlässiges Kriterium, um die Integrität einer Datei sicherzustellen? 

- wie heißt integrität?
    dass jedes einzelne Bit genau so ankam, wie es gesendet wurde – ohne dass während der Übertragung etwas beschädigt wurde, fehlte oder verändert wurde.
    
Laut was Integrität ist, wir schatzen dass wir durch die Dateigroße Messung, ob die Datei etwas fehlt messen können. aber es könnte sein, die Datei verändert oder Beschädigt wurde 


# E1: Welchen Zweck erfüllt eine Prüfsumme?
Mithilfe einer Prüfsumme kann man erkennen, ob die Datei während der Übertragung verändert wurde.
 
# E2: Warum muss für jede Nachricht die Prüfsumme sowohl beim Sender als auch beim Empfänger berechnet werden?
Die Prüfsumme wird beim Sender und beim Empfänger berechnet, damit die beiden Werte miteinander verglichen werden können.

# E3: Welche Rolle spielt das Konzept von „Chaos“ bei der Konstruktion robuster Prüfsummenverfahren?
Chaos sorgt dafür, dass jeder einzelne Byte das Potential dazu hat, große Veränderungen in der Prüfsumme zu verursachen, damit die Fehler offensichtlicht sind.

