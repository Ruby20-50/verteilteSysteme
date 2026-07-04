# Wer ist der Server und wer ist der Client?
- Server is docker container (name a4c99ab8f9c4) listening on port 8088 waiting for requests.
- client is the router in the container with ip address IP: 172.19.0.1:43966
# Warum unterscheiden sich die Werte im Abschnitt „Server“ und im Abschnitt „Client“?
- because docker engin managing its own virtual network. virtually docker container and my browser aren't the same machine
They differ because server and browser are on different sides of that virtual network door. The server is inside; the browser is outside, and its traffic gets NATted through 172.19.0.1 before it reaches the container.
so this is the ip information from the server side Docker container
# Erklären Sie die Bedeutung der ersten zwei Zeilen aus dem Abschnitt „Request Headers“.
GET -> is the HTTP method for requesting data. / is the path (the root of the server). HTTP/1.1 is the protocol version.
localhost: 8088 --> it tells the container which hostname is targeted
# Erklären Sie die Bedeutung von zwei weiteren beliebigen Zeilen aus dem Abschnitt „Request Headers“.
connection: keep-alive → Don't close the TCP connection after this response. Reuse it for subsequent requests (images, CSS, etc.) — avoids repeated TCP handshake overhead.
accept-encoding: gzip, deflate, br, zstd → Browser tells server: "I can decompress these formats — feel free to compress the response." Saves bandwidth.
# Überprüfen Sie, ob die auf der Webseite angezeigten Anfrage-Header-Felder mit den Anfrage-Header-Feldern übereinstimmen, die das Wireshark-Programm protokolliert

172.19.0.1 is the docker gateway

# Rufen Sie jetzt die Webseite http://127.0.0.1:8088/ auf und beantworten Sie die folgenden Fragen:
# Was hat sich in der Antwort im Vergleich zum ersten Aufruf (localhost) geändert und warum?
127.0.0.1 is a loopback address specified by the localhost 8088 which reaches the same site with different title 
indeed localhost is just a hostname that OS resolves to 127.0.0.1 — they point to the same place.

cross-site --> The request initiator and the server hosting the resource have a different site (i.e., a request by "potentially-evil.com" for a resource at "example.com").

none -->This request is a user-originated operation. For example: entering a URL into the address bar, opening a bookmark, or dragging-and-dropping a file into the browser window.

# Was passiert, wenn Sie schnell hintereinander die „Neu Laden“-Taste im Browser drücken?
- the same nothing changes
# Was passiert, wenn Sie 30 Sekunden zwischen dem Neuladen der Webseite warten?
the client port number changes compared to the previous visit.
# Wie erklären Sie das Verhalten in Punkt 2 und 3?
because the keep-alive connection keeps the open tcp socket 
# Wie ändert sich die HTML-Seite, wenn Sie die Shift-Taste gedrückt halten und auf „Neuladen“ klicken? Warum?
then appears pragma: no-cache
cache-control: no-cache
why --> normally browser might use cached resources. 
Shift+Reload --> ignore everything cached, fetch everything fresh from the server.To communicate this to the server, the browser adds two headers, the two mentioned above. 


# what is a socket 
ip + port + protocol (udp / tcp)