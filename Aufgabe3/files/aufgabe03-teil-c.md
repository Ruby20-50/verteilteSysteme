# Abschnitt 1 (Einleitung): Welches Problem adressieren die Autoren mit dem im Papier beschriebenen ST-TCP?

the tcp intolarnce which result in system failure of distributed applications in case of server failure 
and then provides the advantages and feature of the ST-TCP approach
1. lightweight
2. no performance overhead
3. failover is fast
4. no extensive infrastructure 
5. applications run with the same code 

# Abschnitt 3 (Informelle Übersicht): Was ist ein aktiver Backup-Server (active backup)? Wie erkennt der Backup-Server Ausfälle des primären Servers? Was ist ein Leistungsfehler (performance failure)? Was ist ein Absturzfehler (crash failure)?
- active backup server is a server sets between the server and the client and tapps the exchange of segment between them 
when the p. server state is inconsistent or its replies differ from its own.
performance failure: is when the server is still running but fails to meet performance requirements.
crash failure: is when the server stops functioning, halts and sends no more responses (heartbeats).

# Abschnitt 3.1 (Ethernet Tapping): Was verstehen die Autoren unter "Tapping von TCP-Verkehr"? Warum müssen die Autoren den TCP-Verkehr abfangen? Warum ist es heutzutage nicht möglich, Verkehr im Ethernet abzugreifen?
- copying all packets and observing the full state of the flow between the p. server and the clients. 
- what they build is a failure tolarant system. the backup node needs to stay synchronized with primary server's communication states.
- because the broadcast Ethernet was replaced with the switched Ethernet  in which switches forward traffic flow from a port to a port (tapping takes the shape of port mirroring )

# Abschnitt 2 (Verwandte Arbeiten): Was ist der Unterschied zwischen der Lösung der Autoren (ST-TCP) und FT-TCP?
- ST-TCP provides a faster failover. This is because FT-TCP require time of failure detection, time for the backup server to start, and time to update the backup server state from all the data saved in the logger whilst ST-TCP improve that by adopting TCP taffic tapping

# Abschnitt 3.2 (Systemarchitektur): Was ist der Zweck des Stromschalters (power switch - zwei kleine gelbe Kästchen) in Abbildung 3?
- in order to ensure the the primary crashes before the backup takes over, the backup can switch off the primary when failure is detected 
