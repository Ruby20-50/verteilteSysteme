# Aufgabe 4 — Teil B: IP-Routing mit Docker

**Aufgabe:** hostA und hostD sollen hostC über IP erreichen können, ohne dass IP-Adressen geändert werden. Nachweis auf hostD via `ping 10.0.0.10` und `traceroute 192.168.0.10`.

## Topologie (aus `docker-compose-routing.yml`)

| Node | Netz(e) | IP-Adresse | Rolle |
|---|---|---|---|
| hostA | networkA (`192.168.0.0/16`) | 192.168.0.10 | Endhost |
| hostC | networkC (`10.0.0.0/8`) | 10.0.0.10 | Endhost |
| hostD | networkD (`172.33.0.0/16`) | 172.33.0.10 | Endhost |
| routerAB | networkA + networkB (`172.44.0.0/16`) | 192.168.0.2 / 172.44.0.2 | Router |
| routerBCD | networkB + networkC + networkD | 172.44.0.3 / 10.0.0.2 / 172.33.0.3 | Router |

Kein Host liegt direkt im selben Netz wie ein anderer Host — jede Verbindung läuft über mindestens einen Router. Pfad hostA ↔ hostC: `hostA → routerAB → routerBCD → hostC`,  `routerBCD → hostD`. Pfad hostD ↔ hostC: beide hängen an routerBCD, aber an unterschiedlichen Interfaces/Subnetzen, also läuft auch dieser Verkehr über routerBCD.

## 1. Umgebung starten

```
docker compose -f docker-compose-routing.yml up -d
```

`down` stoppt und entfernt die Container wieder.

## 2. IP-Forwarding prüfen

> **Ein Lösungsschritt:**
> In der YAML-Datei wurde `sysctls: net.ipv4.ip_forward=1` für `routerAB` und `routerBCD` eingestellt. Ohne diese Einstellung würde ein Router ankommende Pakete, die nicht für ihn selbst bestimmt sind, verwerfen, statt sie weiterzuleiten.

Kontrolle für jeden Router:

```
docker exec -it aufgabe4-routerAB-1 sh
sysctl net.ipv4.ip_forward
# muss 1 zurückgeben
```

(analog für `routerBCD`)

## 3. Routing konfigurieren

>**Ein Lösungsschritt:**

>Auf jedem Container zuerst `iproute2` installieren (Alpine-Images enthalten `ip` standardmäßig nicht):

```
docker exec -it <container> sh
apk add iproute2
```

### routerAB
Direkt verbunden mit networkA und networkB — kennt also 192.168.0.0/16 und 172.44.0.0/16 automatisch. Fehlt: der Weg zu networkC und networkD, die hinter `routerBCD` liegen.

```
ip route add 10.0.0.0/8 via 172.44.0.3
ip route add 172.33.0.0/16 via 172.44.0.3
```

### routerBCD
Direkt verbunden mit networkB, networkC, networkD. Fehlt: der Weg zurück zu networkA.

```
ip route add 192.168.0.0/16 via 172.44.0.2
```

### hostA
Muss wissen, wie es networkC (Ziel) und networkD (für Rückweg des traceroute von hostD) erreicht — beides über sein Gateway `routerAB`:

```
ip route add 10.0.0.0/8 via 192.168.0.2
ip route add 172.33.0.0/16 via 192.168.0.2
```

### hostC
Muss antworten können an hostA *und* hostD — beides über sein Gateway `routerBCD`:

```
ip route add 192.168.0.0/16 via 10.0.0.2
ip route add 172.33.0.0/16 via 10.0.0.2
```

### hostD
Muss hostC (Ziel des ping) und hostA (Ziel des traceroute) erreichen — beides über sein Gateway `routerBCD`:

```
ip route add 10.0.0.0/8 via 172.33.0.3
ip route add 192.168.0.0/16 via 172.33.0.3
```

Kontrolle auf jedem Node: `ip route` — zeigt die direkt verbundenen Netze plus die eben hinzugefügten Einträge.

## 4. Nachweis (auf hostD)

```
docker exec -it aufgabe4-hostD-1 sh
apk add iproute2 traceroute   # traceroute ist kein Alpine-Standardpaket
ping -c 4 10.0.0.10
traceroute 192.168.0.10
```

**Erwartung:** `ping` liefert 4 Antworten von 10.0.0.10 (hostC), `traceroute` zeigt den Pfad hostD → routerBCD (172.33.0.3) → routerAB (172.44.0.2) → hostA (192.168.0.10) — also zwei sichtbare Hops vor dem Ziel.

## Warum das funktioniert, ohne IP-Adressen zu ändern

Jeder Host bekommt nur zusätzliche *Routen* (welches Gateway für welches Zielnetz genutzt wird), keine neue eigene Adresse. Die Router leiten Pakete anhand der Ziel-IP weiter und ändern dabei weder Absender- noch Empfänger-Adresse (kein NAT) — genau das verlangt die Aufgabenstellung.

---

## Commands and configurations

**YAML-Konfiguration** (für `routerAB` und `routerBCD`):

```yaml
sysctls:
  - net.ipv4.ip_forward=1
```

**Umgebung starten / stoppen:**

```
docker compose -f docker-compose-routing.yml up -d
docker compose -f docker-compose-routing.yml down
```

**Auf jedem Router:**

```
docker exec -it router-name sh
```

öffnet eine Shell im jeweiligen Router — darin:

```
sysctl net.ipv4.ip_forward
ip route add <destination-network> via <next-hop-router-interface-ip>
ip route
```

**Auf jedem Host:**

```
docker exec -it host-name sh
apk add iproute2
ip route add <destination-network>/<subnet-mask> via <my-gateway>
ip route
exit
```

**Nachweis der Aufgabe (auf hostD):**

```
docker exec -it aufgabe4-hostD-1 sh
ping 10.0.0.10
traceroute 192.168.0.10
```