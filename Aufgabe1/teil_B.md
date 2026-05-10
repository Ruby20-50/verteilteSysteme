---- English Version below ---

# Halten Sie Ihre Beobachtungen strukturiert fest.

- ip link

System: student@nuco03 (Ubuntu Noble / 24.04)

lo: <LOOPBACK, UP, LOWER_UP>
    interface_name <Interface spricht mit sich selbst, vom OS aktiviert, physikalisches Signal ist aktiv>
    mtu 65536
        Maximum Transmission Unit. Sehr groß --> da kein echtes physikalisches Limit besteht,
        weil das Signal zurückgeloopt wird
    qdisc noqueue
        Definiert, wie Pakete vor dem Senden in eine Warteschlange eingereiht werden.
        Kein Puffern nötig (loopback ist sofort, kein echtes Netzwerk).
    00:00:00:00:00:00
        Dies ist die MAC-Adresse
    brd 00:00:00:00:00:00
        Die Broadcast-Adresse: eine spezielle Adresse, um eine Nachricht gleichzeitig
        an ALLE (virtuellen) Geräte im Netzwerk zu senden

eno1: <BROADCAST, MULTICAST, UP, LOWER_UP>
    Kabel ist physisch eingesteckt und das Interface ist aktiviert
    eno --> steht für Ethernet onboard
        BROADCAST  --> kann Broadcast-Pakete an alle Geräte im Netzwerk senden
        MULTICAST  --> kann Pakete an bestimmte Geräte senden
        UP         --> Interface ist vom OS aktiviert
        LOWER_UP   --> Kabel eingesteckt, Signal erkannt
    mtu 1500
        Maximale Paketgröße
    qdisc fq_codel
        qdisc    --> Die Queuing Discipline zur Verwaltung ausgehender Pakete.
        fq_codel --> Ein moderner Algorithmus, der Latenz reduziert und Bufferbloat verhindert.
    state UP     --> Das Interface ist aktiv und in Betrieb.
    mode DEFAULT --> Betrieb im Standardmodus — keine besondere Konfiguration.
    group DEFAULT --> Standard
    link/ether   --> Typ ist Ethernet
        1c:69:7a:af:7f:1b --> MAC-Adresse
    brd ff:ff:ff:ff:ff:ff --> Die Broadcast-MAC-Adresse

wlo2: <BROADCAST, MULTICAST>
    WLAN-Interface, im Mainboard verbaut, Index 2:
        <Fähig zu Broadcast, Fähig zu Multicast> nicht UP, nicht LOWER_UP
    mtu 1500
        Maximale Paketgröße beträgt 1500 Bytes, Standard für Ethernet-Netzwerke
    qdisc noop
        No Operation / Verwirft ALLE Pakete, da das Interface DOWN ist
    state DOWN --> Es ist ausgeschaltet
    Warteschlangenlänge: Maximal 1000 Pakete können eingereiht werden
        Allerdings irrelevant, da noop ohnehin alles verwirft

    link/ether c4:23:60:53:cf:8c --> MAC-Adresse
    brd ff:ff:ff:ff:ff:ff
    altname wlp0s12f0


- ip address

lo: <LOOPBACK, UP, LOWER_UP> — wie oben
    mtu 65536
        Maximum Transmission Unit. Sehr groß --> da kein physikalisches Limit,
        weil das Signal zurückgeloopt wird
    qdisc noqueue
        Kein Puffern nötig (loopback ist sofort, kein echtes Netzwerk).
    00:00:00:00:00:00
        MAC-Adresse
    brd 00:00:00:00:00:00
        Broadcast-Adresse: sendet gleichzeitig an ALLE (virtuellen) Geräte im Netzwerk

    Unterschied:
        inet 127.0.0.1/8 scope host lo
            127.0.0.1 --> Die Loopback-Adresse
            /8        --> Die Subnetzmaske. Die ersten 8 Bits der Adresse definieren das Netzwerk.
                         In der Praxis: 127.0.0.0 bis 127.255.255.255
                         In der Realität wird nur 127.0.0.1 verwendet
            host      --> Nur auf diesem Rechner gültig
        Alle Laufzeiten: forever --> statisch vergeben, nicht per DHCP
        IPv6: ::1/128 --> Loopback-Adresse in IPv6

eno1: <BROADCAST, MULTICAST, UP, LOWER_UP>
    mtu 1500
        Maximale Paketgröße beträgt 1500 Bytes
    MAC: 1c:69:7a:af:7f:1b
    IPv4: 172.16.3.20/24 — zugewiesene IP, Broadcast 172.16.3.255, scope global
    IPv6: fe80::1e69:7aff:feaf:7f1b/64 — Link-Local-IPv6-Adresse (scope link)
    Alle Laufzeiten: forever — statisch vergeben, nicht per DHCP
    link --> Gültig im lokalen Netzwerksegment

wlo2 (Wi-Fi)
    state DOWN --> Keine IP-Adressen zugewiesen
    MAC: c4:23:60:53:cf:8c
    Keine inet- oder inet6-Zeilen --> da das Interface vollständig inaktiv ist

# Dokumentieren Sie Ihre Beobachtungen, insbesondere ob die Rechner erreichbar sind und welche Antwortzeiten (Round-Trip-Zeiten) gemessen werden.

    Befehl: ping 172.16.3.100
client1 ist erreichbar.
    Es wurden 27 Pakete gesendet und 27 Pakete empfangen, 0 Paketverlust.
    Gesamtdauer: 26664 ms --> 26,664 Sekunden
    Round-Trip-Zeit min  /avg  /max  /mdev
                    0.416/0.550/0.745/0.082 ms

                ----------------------------------------------

                
#halten Sie Ihre Beobachtungen strukturiert fest.
-ip link
System: student@nuco03 (Ubuntu Noble / 24.04)
lo: <loopback, up , lower_up>
        interface_name <interface talk to itself, enabled by the OS, physical signal is active>
    mtu 65536 
        it is the maximum Transmission Unit. it is huge --> because it has no real physical limit as it loops back 
    qdisc noqueue 
        how packets are queued before sending.
         means no buffering needed (loopback is instant, no real network).
    00:00:00:00:00:00
        this is the MAC address
    brd 00:00:00:00:00:00 
        the broadcast address: is a special address used to send a message to ALL (virtual) devices on a network simultaneously
eno1 <BROADCAST,MULTICAST,UP,LOWER_UP> 
 cable is physically plugged in and the interface is enabled
    eno --> stands for ethernet onboard
        Broadcast --> can send broadcase packets on all devices on the network
        multicast --> can send packets to specific devices
        up --> interface is enabled by the OS
        LOWER_UP --> cable plugged in and signal detected
    mtu 1500 max packet size
    qdisc: fq_codel 
        qdisc --> The queuing discipline used to manage outgoing packets. 
        fq_codel is a modern algorithm that reduces latency and prevents bufferbloat.
    state Up --> The interface is active and running.
    mode Default --> Operating in default mode — no special configured.
    group Default --> standard
    link/etherLink --> type is Ethernet
        1c:69:7a:af:7f:1bThe --> MAC address 
    brd ff:ff:ff:ff:ff:ff --> The broadcast MAC address

wlo2: <Broadcast, Multicast>
    wifi interface built into motherboard index 2: 
        <Capable of broadcasting, Capable of multicast> not up, not LOWER_UP
    mtu 1500 --> max packet size is 1500 bytes, standard ether network
    qdisc noop --> no operation / Drops ALL packets, because the interface is DOWN
    state down --> it is off
    queue length Maximum 1000 packets can be queued 
        though irrelevant here since noop drops everything anyway

    link/ether c4:23:60:53:cf:8c --> MAC Address
    brd ff:ff:ff:ff:ff:ff
    altname wlp0s12f0



-ip address
lo: <loopback, up , lower_up> the same 
    mtu 65536 
        it is the maximum Transmission Unit. it is huge --> because it has no real physical limit as it loops back 
    qdisc noqueue 
        how packets are queued before sending.
         means no buffering needed (loopback is instant, no real network).
    00:00:00:00:00:00
        this is the MAC address
    brd 00:00:00:00:00:00 
        the broadcast address: is a special address used to send a message to ALL (virtual) devices on a network simultaneously

    difference:
        inet 127.0.0.1/8 scope host lo
            127.0.0.1 --> This is the loopback address
            /8 --> the subnet mask. This means the first 8 bits of the address define the network
            in practice 127.0.0.0 to 127.255.255.255
            in reality only 127.0.0.1 is used 
            host --> Only valid on this machine
        All lifetimes: forever --> statically assigned, not from DHCP
        IPv6: ::1/128 --> loopback in IPv6

eno1: <BROADCAST,MULTICAST,UP,LOWER_UP>
    mtu 1500
        Maximum packet size is 1500 bytes 
    MAC: 1c:69:7a:af:7f:1b
    IPv4: 172.16.3.20/24 — assigned IP, broadcast 172.16.3.255, scope global
    IPv6: fe80::1e69:7aff:feaf:7f1b/64 — link-local IPv6 address (scope link)
    All lifetimes: forever — statically assigned, not from DHCP
    link --> Valid on the local network segment

wlo2 (Wi-Fi)
State: DOWN --> no IP addresses assigned at all
MAC: c4:23:60:53:cf:8c
No inet or inet6 lines--> because completely inactive

 # Führen Sie anschließend Verbindungstests vom Operator-Rechner zu client1 und client2 durch. Dokumentieren Sie Ihre Beobachtungen, insbesondere ob die Rechner erreichbar sind und welche Antwortzeiten (Round-Trip-Zeiten) gemessen werden.

nicht gemacht!