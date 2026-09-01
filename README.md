# Verteilte Systeme

Coursework for the **Verteilte Systeme** (Distributed Systems) module. Four assignments (`Aufgabe1`–`Aufgabe4`) covering networking fundamentals, raw-socket file transfer, HTTP internals, and reverse proxying / IP routing with Docker.

Each assignment folder contains the written answers (`teil_*.md` / `Teil*.md`), plus source code and configs where applicable.

## Contents

- [Aufgabe1 — Network Fundamentals](#aufgabe1--network-fundamentals)
- [Aufgabe2 — Socket-Based File Transfer & Time Protocol](#aufgabe2--socket-based-file-transfer--time-protocol)
- [Aufgabe3 — HTTP Internals & Raw-Socket Downloader](#aufgabe3--http-internals--raw-socket-downloader)
- [Aufgabe4 — Reverse Proxy & Docker IP Routing](#aufgabe4--reverse-proxy--docker-ip-routing)

---

## Aufgabe1 — Network Fundamentals

### Teil A — Physical Network Setup (`teil_A.md`)

Documentation of integrating an `operator` machine (keyboard, screen, blue network cables) into an existing lab topology of two switches and two client machines (`client1`, `client2`), including which components were connected and over which ports.

### Teil B — Verifying the Network Configuration (`teil_B.md`)

- `ip link` / `ip address` output analysis on the operator machine: loopback vs. Ethernet (`eno1`) vs. Wi-Fi (`wlo2`) interfaces — MTU, qdisc (`noqueue`, `fq_codel`, `noop`), link state flags (`UP`/`LOWER_UP`/`DOWN`), MAC and broadcast addresses, and assigned IPv4/IPv6 addresses.
- Reachability tests via `ping` from operator to client1, with measured round-trip times (min/avg/max/mdev) and packet loss.

### Teil C — client1 ↔ client2 Connectivity via SSH (`teil_C.md`)

Investigates whether client1 can reach client2 directly, without a monitor/keyboard attached to either, by SSH-ing from the operator into client1, then running `ping` against client2 from there to confirm reachability.

### Teil D — Large File Transfer Experiment (`teil_D.md`)

- A ~19 GB test file (`head -c 20000000000 /dev/urandom > file.bin`) generated on client1, with a reasoned time estimate for transferring it to client2.
- Transfer method chosen: `rsync` (over `scp`), run three times under similar conditions with measured times (~11s, ~3m5s, ~3m14s).
- Discussion of the variance between runs: network contention from concurrent transfers and possible disk-utilization effects on client2.

### Teil E — File Integrity (`teil_E.md`)

- Discussion of whether comparing file sizes alone is a reliable integrity check (it isn't, because it can't detect same-size corruption).
- Answers to E1–E3 based on Ross Williams' CRC article: the purpose of a checksum, why it must be computed independently on both sender and receiver, and the role of the "avalanche"/chaos property in making checksum errors obvious.

### Teil F — Paper Reading: TeraScale SneakerNet (`teil_F.md`)

Answers on Gray et al.'s *"TeraScale SneakerNet"* report: the problem of efficiently moving terabyte-scale datasets, the solutions evaluated (next-gen internet, CDs, tape, disk bricks), why the authors recommend disk bricks (parallel writes, reusability, self-contained recovery metadata), and the meaning of Tanenbaum's "station wagon full of tapes".

### Teil G — Video Analysis: Latency, Bandwidth & Sneakernet (`teil_G.md`, optional/ungraded)

Notes on the factors that determine effective network transfer speed (connection stability, bandwidth, file size limits) and on when physical transport of storage media can outperform a network transfer for very large datasets.

## Aufgabe2 — IP Routing, Time Protocol & Socket-Based File Transfer

### Teil A — traceroute & Wireshark (`TeilA.md`)

Covers `traceroute`/`tracert` fundamentals against a public host: what the tool does and how to read its output, the Wireshark display filter for isolating a traceroute run by server address, which ICMP packet types are involved, the role of the IPv4 TTL field, and which of traceroute's on-screen numbers (hop count, IP addresses, timings) can be cross-checked directly in Wireshark.

### Teil B — Router & DHCP Setup (`TeilB.md`)

A hands-on lab exercise connecting two subnets (LAN1, LAN2) through a router with two NICs: assigning static IPs to the router's interfaces, configuring `isc-dhcp-server` (`/etc/dhcp/dhcpd.conf`) to serve both subnets, switching the two client machines to DHCP via Netplan, and enabling `net.ipv4.ip_forward` so the router forwards packets between LAN1 and LAN2 then verified with `ping` and `traceroute` across the two networks.

### Teil C — RFC 868 Time Protocol Reading (`TeilC.md`)

Questions on the Time Protocol specification: its purpose, which transport-layer protocols it uses, the server port, what the client must send, what the server returns, how time zones are handled, and possible sources of inaccuracy.

### Teil D — UDP Time Protocol Client (`TeilD.md`, `TimeClient.java`)

A socket-based UDP client implementing RFC 868: takes a server hostname as a command-line argument, sends an empty datagram to port 37, parses the 32-bit seconds-since-1900 response using only `java.nio.channels.DatagramChannel`, converts it to Unix time, and prints the date and time for `Europe/Berlin` in the required `YYYY-MM-DD` / `HH:mm:ss` format.

### Teil E — Socket-Based File Transfer Server & Client (`TeilE.md`, `MyServer.java`, `MyClient.java`)

- **`MyServer.java`** — listens on a port passed as a command-line argument, reads a requested filename, and streams the file back in 4 MB chunks using `Socket`-based I/O (no `scp`/`ftp`/`rsync`).
- **`MyClient.java`** — takes `server:filename` as an argument, connects via `SocketChannel`, and receives the file using zero-copy `FileChannel.transferFrom`, timing the transfer from request to last byte received.
- Built to satisfy the assignment's requirements: single concurrent client, no encryption/auth required, and file integrity verifiable via matching SHA-1/MD5 hashes on both ends.

### Teil F — Talk/Paper Reading: SSD Random Reads (`TeilF.md`)

Notes on Thomas Knauth's LISA 2013 talk/paper: the problem the authors address, shortcomings of existing tools they identify, and the surprising finding about random read performance on SSDs.

> Note: `TeilA.md`–`TeilF.md` are currently empty placeholders — the write-ups above summarize the assignment prompts; your actual answers still need to be filled in for Teil A, B, C, and F (Teil D and E already have working code).

## Aufgabe3 — HTTP Internals & Raw-Socket Downloader

### Teil A — HTTP Server & Client (`aufgabe03-teil-a.md`, `docker-compose-1.yaml`)

- **`docker-compose-1.yaml`** — spins up a `vs-info` container that answers HTTP requests with a page detailing the full request (headers, client/server addresses).
- Written answers analyze that page and a Wireshark capture of the same traffic: identifying which side is server vs. client and why their address values differ (Docker's virtual network / NAT through the gateway), the meaning of the first two `Request Headers` lines plus two additional header lines (`Connection: keep-alive`, `Accept-Encoding`), and cross-checking the browser-displayed headers against Wireshark's capture.
- A follow-up comparison between requesting `http://localhost:8088/` and `http://127.0.0.1:8088/`: what changes and why, what happens on rapid successive reloads vs. reloading after a 30-second gap (kept-alive TCP connection vs. a fresh client port), and how a Shift+Reload forces a cache-busting request (`Cache-Control: no-cache`, `Pragma: no-cache`).

### Teil B — Raw-Socket HTTP Downloader (`Downloader.java`)

- A command-line HTTP downloader built entirely on raw `Socket`s — no `curl`/`wget`, no HTTP library. The target URI is passed as a command-line argument and manually parsed into host, port (80), and path.
- Before downloading, it sends a `HEAD` request to read `Content-Length` and compares it against available disk space (`File.getFreeSpace()`), aborting if there isn't enough room.
- The actual download uses a `GET` request; the response status line and headers are parsed byte-by-byte to detect the header/body boundary, then the body is streamed to a file in the current working directory with a live 0–100% progress indicator printed to the console.

### Teil C — Paper Reading: ST-TCP (`aufgabe03-teil-c.md`)

Questions on *"TCP Server Fault Tolerance Using Connection Migration to a Backup Server"* (Marwah, Mishra, Fetzer): the problem ST-TCP addresses (TCP's intolerance to server failure), what an active backup server is and how it detects primary-server failure (performance failure vs. crash failure), what "tapping TCP traffic" means and why switched Ethernet makes plain tapping impractical (port mirroring instead), how ST-TCP's failover compares to FT-TCP's, and the purpose of the power switch in the paper's system architecture diagram.

## Aufgabe4 — Reverse Proxy & Docker IP Routing

### Teil A — Reverse Proxy (`aufgabe4-teil-a.md`, `proxy.java`, `Dockerfile`)

- **`proxy.java`** — a path-based HTTP reverse proxy in plain Java sockets (one thread per connection). Routes `/appA/*` and `/appB/*` requests to two separate backend containers (`vs-app-a`, `vs-app-b`), strips the path prefix before forwarding, rewrites the `Host` header per backend, and relays the raw response back to the client.
- **`Dockerfile`** — a minimal multi-stage build (Alpine + Eclipse Temurin JDK/JRE) that compiles and packages `proxy.java` into a lightweight runtime image.
- Written answers (A1–A4) cover: which ports the proxy uses to reach each backend app, how the proxy resolves the two apps' addresses via Docker's built-in DNS on the shared `proxynet` network (instead of hardcoded IPs), how the requested path must be rewritten (stripping `/appA` or `/appB`) so it's valid from each backend's own point of view, and why the `Host` header must be rewritten to the backend's hostname so the app doesn't reject the request.
- A short comparison of reverse proxy concerns (load distribution, hiding backend exposure, avoiding repeated work, cross-cutting concerns like TLS termination) versus forward proxy concerns (client-side policy control, shared caching, privacy/identity hiding).

### Teil B — Docker IP Routing (`aufgabe4-teil-b.md`, `docker-compose-routing.yml`)

- **`docker-compose-routing.yml`** — a multi-network Docker topology: three hosts (`hostA`, `hostC`, `hostD`) each on their own isolated network, connected through two routers (`routerAB`, `routerBCD`) that bridge the networks and have `net.ipv4.ip_forward` enabled.
- Commands and notes for configuring routing by hand inside each container: enabling `net.ipv4.ip_forward` on the routers, adding routes with `ip route add <destination-network> via <next-hop>` on both routers and hosts, and inspecting the routing table with `ip route`.
- Verification of end-to-end connectivity across the routed topology using `ping` and `traceroute` from `hostD` to a host on a different subnet.

---

*Coursework at BHT Berlin — answers and code are original work submitted for the Verteilte Systeme module.*