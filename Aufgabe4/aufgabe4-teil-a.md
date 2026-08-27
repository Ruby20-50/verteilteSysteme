# A1: Welche Ports muss proxy.java verwenden, um die beiden Anwendungen zu erreichen?
Port 80. Both apps listen on port 80 inside their own containers
8085 and 8086 are host-side port mappings (-p 8085:80) why exist? so that apps can be reached from windows browser.

# A2: Wie findet der Proxy die IP-Adressen der beiden Anwendungen?
Through Docker's built-in DNS on the proxynet network — the proxy never needs to know a numeric IP. Because all three containers share the user-defined bridge network proxynet, and each app was started with a fixed --hostname (vs-app-a, vs-app-b), Docker runs an internal DNS that resolves those hostnames to the containers' current IP addresses automatically.

# A3: Wie muss der Proxy die angeforderte Ressource im HTTP-Request anpassen, damit die Anfrage aus Sicht der jeweiligen Anwendung gültig ist?
It must strip the /appA (or /appB) prefix from the path. The browser asks for /appA, but App A knows nothing about "appA" — from its own point of view its root is simply /
In the code this is the branch that sets newPath = path.substring("/appA".length()), plus the check that turns an empty result back into /

# A4: Wie muss der Proxy die Host-Zeile im HTTP-Request anpassen, damit die Anfrage aus Sicht der jeweiligen Anwendung gültig ist?
A, Host: vs-app-b for App B. The browser sends Host: localhost:8087 (it thinks it's talking to the proxy), but the apps validate the Host header and reject anything that isn't their own hostname



# Reverse Proxy 
- scaling problem 
- exposure problem --> security
- repeated work
-  cross-cutting-concerns problem --> http encryption
# Forward Proxy
- Control and policy.
- Shared caching. --> bandwidth is saved, faster internet (one benefit)
- Privacy / identity hiding at the client end.
