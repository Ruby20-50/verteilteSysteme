# Verteilte Systeme

Coursework for the **Verteilte Systeme** (Distributed Systems) module. Four assignments (`Aufgabe1`–`Aufgabe4`) covering networking fundamentals, socket-based file transfer, HTTP internals, and reverse proxying / IP routing with Docker.

Each assignment folder contains the written answers (`teil_*.md` / `Teil*.md`), plus source code and configuration files where applicable.

## Contents

- [Aufgabe1 — Network Fundamentals](#aufgabe1--network-fundamentals)
- [Aufgabe2 — IP Routing, Time Protocol & Socket-Based File Transfer](#aufgabe2--ip-routing-time-protocol--socket-based-file-transfer)
- [Aufgabe3 — HTTP Internals & Raw-Socket Downloader](#aufgabe3--http-internals--raw-socket-downloader)
- [Aufgabe4 — Reverse Proxy & Docker IP Routing](#aufgabe4--reverse-proxy--docker-ip-routing)

---

## Aufgabe1 — Network Fundamentals

Hands-on lab work on physical setup, interface inspection, connectivity testing, bulk transfer, and integrity — plus two paper/video analyses.

**Teil A — Physical setup (`teil_A.md`).** 

Documents how the `operator` machine (keyboard, monitor, network cable) was integrated into the lab topology shared with `client1`/`client2` via the switch.

**Teil B — Interface inspection & reachability (`teil_B.md`).** 

`ip link`
/ `ip address` analysis of the operator's loopback, Ethernet (`eno1`), and Wi-Fi (`wlo2`) interfaces (MTU, qdisc, link-state flags, MAC/IP addressing), followed by a `ping` test to client1 (0% loss, 0.416/0.550/0.745 ms min/avg/max). 

**Teil C — client1 ↔ client2 via SSH (`teil_C.md`).**

 SSHes from the operator into client1 (no monitor/keyboard on the client itself), then runs `ping` from inside that session to confirm client1 can reach client2 directly. 

**Teil D — Large-file transfer experiment (`teil_D.md`).**

 A ~19 GB test file is generated and a transfer-time estimate is reasoned out in advance; `rsync` is chosen as the transfer tool. Three runs are measured (~10.8 s, ~3 m 5 s, ~3 m 14 s) and the variance is explained via network contention and possible disk-utilization effects on the receiving side.

**Teil E — File integrity (`teil_E.md`).**

Argues that file-size comparison alone is not a reliable integrity check, then answers E1–E3 on checksum purpose, why both sides must compute it independently, and the role of the avalanche/"chaos" property in making corruption obvious.

**Teil F — Paper reading: TeraScale SneakerNet (`teil_F.md`).** A

Answers on efficiently moving terabyte-scale datasets: the four approaches evaluated, why disk bricks are recommended, and the meaning of Tanenbaum's "station wagon full of tapes."

**Teil G — Video analysis (`teil_G.md`).** 

Notes on the factors governing effective transfer speed (stability, bandwidth, file-size limits) and when physical media transport outperforms network transfer at very large data volumes. 

## Aufgabe2 — IP Routing, Time Protocol & Socket-Based File Transfer

**Teil A — traceroute & Wireshark (`TeilA.md`).** 
Question set on what `traceroute` does, the Wireshark filter to isolate a traceroute run by server address, the ICMP packet types involved, the role of the IPv4 TTL field, and how traceroute's on-screen output maps to Wireshark's capture.

**Teil B — Router & DHCP setup (`TeilB.md`).**
 Connects two subnets (LAN1, LAN2) through a router with two NICs: static IPs assigned to therouter's interfaces (`ip addr add` / `ip link set ... up`), `isc-dhcp-server` configured with a `subnet` block per LAN in `/etc dhcp/dhcpd.conf`, the two client machines switched to DHCP via Netplan (`dhcp4: true`, applied safely with `netplan try`), and `net.ipv4.ip_forward=1` enabled so the router actually forwards traffic between the two LANs — verified afterwards with `ping`/`traceroute` across the router. Complete, including a practical note on locating a client's new DHCP-assigned IP via the lease file or the operator console.

**Teil C — RFC 868 Time Protocol (`TeilC.md`).**

 Answers on the protocol's purpose (machine-readable time sync), its use of both TCP and UDP on port 37, the 32-bit binary response format, GMT-only time handling, and accuracy limitations (unaccounted latency, whole-second resolution, the 2036 overflow).

**Teil D — UDP `TimeClient` (`TeilD.md`, `TimeClient.java`).** 

A NIO-based UDP client implementing RFC 868 in Java:
- Takes the server hostname as a CLI argument and opens a blocking **`DatagramChannel`**.
- Sends an empty datagram to port 37 (the RFC-correct request format for this protocol) and reads exactly 4 response bytes.
- Uses `Integer.toUnsignedLong(...)` on the parsed `int` — the key correctness fix that avoids a Year-2036 overflow a naive signed read would hit once the seconds-since-1900 count exceeds `2^31`.
- `ByteBuffer` defaults to big-endian (network byte order), so no manual byte-swapping is needed.
- Converts to Unix epoch via the standard 1900→1970 offset (`2208988800L`), then to `Europe/Berlin` local time via `Instant`/`ZonedDateTime` (correctly handling DST), using only JDK standard-library classes.
- Prints the date and time on two lines in the required `YYYY-MM-DD` / `HH:mm:ss` format.
- Tested against `time.nist.gov`, with a note that NIST throttles rapid repeat requests. 

**Teil E — Socket-based file transfer, `Datei-Transfer/` (`TeilE.md`, `MyServer.java`, `MyClient.java`).** a zero-copy TCP file-transfer system built to move a
50 GB file as fast as possible.

- **Design:** Java NIO (`FileChannel`, `SocketChannel`) throughout; the core mechanism is `FileChannel.transferTo()` `transferFrom()`, which on Linux maps to the **`sendfile()`** syscall — the kernel moves bytes directly from the page cache into the socket buffer without ever entering the JVM heap. Complemented by 8 MB socket send/receive buffers (vs. the OS default 64–128 KB) and `TCP_NODELAY` to disable Nagle's algorithm.

- **Protocol:** client connects on port 8088, sends the filename as a newline-terminated line; server replies with an 8-byte length header (`-1` if not found) then streams the file via `transferTo()`; client writes incoming bytes straight to disk via `transferFrom()`; timing runs from request sent to last byte written.

- **Testing:** 

1. a localhost run against the full 50 GB file (three passes, 67 → 76 → 80 MB/s, attributed to page-cache warm-up) with matching SHA-1 checksums; 

2. a Docker Compose run (two containers on a bridge network, exercising the full TCP/IP stack and Docker DNS) against a scaled-down 500 MB file (~22 MB/s, slower mainly due to the WSL2↔Windows filesystem bridge used for the project volume), again with matching SHA-1 hashes confirming a correct transfer. Official benchmark numbers for submission require the two physical lab machines, as specified in the assignment.

**Teil F — Paper reading: `dsync` (`TeilF.md`).** Summary of Knauth & Fetzer's USENIX LISA '13 paper on efficient periodic synchronization of large binary datasets (e.g. replicated VM disks): 
why full-copy and checksum-based diffing (à la `rsync`) both waste resources, how `dsync` tracks changes at the block level via a Device Mapper extension instead, `rsync`'s three named weaknesses (disk I/O, CPU cost, page-cache
pollution), and the surprising finding that sorted SSD block access can be up to 10× faster than unsorted access to the same blocks.

## Aufgabe3 — HTTP Internals & Raw-Socket Downloader

**Docker setup (`docker-compose-1.yaml`).** 

Runs a single `vs-info` service from the course registry, exposed on host port 8088.

**Teil A — HTTP headers & browser behavior (`aufgabe03-teil-a.md`).**

Analysis of the `vs-info` page and a matching Wireshark capture:
identifies server vs. client and explains why their addresses differ (Docker's virtual network / NAT through the bridge gateway), explains the request-line and `Host:` header plus two further headers (`Connection: keep-alive`, `Accept-Encoding`), compares `localhost:8088` vs `127.0.0.1:8088`, and explains reload behavior (kept-alive connection on rapid reloads vs. a fresh client port after a gap) and Shift+Reload's cache-busting headers (`Cache-Control`/`Pragma: no-cache`). Complete.

**Teil B — `Downloader.java` (raw-socket HTTP client).**

A command-line HTTP downloader built entirely on raw `java.net.Socket` connections — no `HttpURLConnection`, no third-party HTTP library, no `curl`/`wget`. 
- Manually parses the target URL into scheme (`http://` only — HTTPS rejected), host, path, and a hardcoded port 80.
- Sends a `HEAD` request first to read `Content-Length` and compares it against `new File(".").getFreeSpace()`, aborting if there isn't enough disk space.
- Performs the actual transfer with a `GET` request; the response is parsed **byte-by-byte** (not line-by-line, to avoid a `BufferedReader` swallowing bytes that belong to the binary body) up to the literal `\r\n\r\n` header/body boundary, then the body is streamed to disk in 8192-byte chunks with a live 0–100% progress indicator.
- Verified against a real file (`BelWue_logo.svg`) from `speedtest.belwue.net`. Complete, with one documented deviation from the assignment's example code (`new File(".").getFreeSpace()` instead of `new  File("/").getFreeSpace()`, argued as more correct since the file is written to the working directory).

**Teil C — Paper reading: ST-TCP (`aufgabe03-teil-c.md`).** 

*"TCP Server Fault Tolerance Using Connection Migration to a Backup Server"*: the problem ST-TCP addresses (TCP's intolerance of server failure), what an active backup server is and how it distinguishes performance failure from crash failure, what "tapping TCP traffic" means and why switched Ethernet requires port mirroring to do it, how ST-TCP's failover compares to FT-TCP's, and the purpose of the power switch in the paper's system architecture.

## Aufgabe4 — Reverse Proxy & Docker IP Routing

**Teil A — Reverse proxy (`aufgabe4-teil-a.md`, `proxy.java`, `Dockerfile`).**
- **`proxy.java`** — a path-based HTTP reverse proxy in plain Java sockets, one thread per connection. Routes `/appA/*` (and `/`) to backend `vs-app-a` and `/appB/*` to `vs-app-b`, resolving both via Docker's built-in DNS on the shared `proxynet` network rather than hardcoded IPs. Before forwarding, it strips the path prefix so each backend sees a request valid from its own point of view, rewrites the `Host:` header to the backend's hostname (required since the backend apps reject requests whose `Host` doesn't match their own name), drops incoming `Connection`/`Proxy-Connection` headers, and relays the raw response back to the client byte-for-byte (works for text and binary alike).
- **`Dockerfile`** — a multi-stage build: an Alpine + Eclipse Temurin **JDK** stage compiles `proxy.java`; a separate, much smaller **JRE** stage copies only the compiled `.class` file and runs it, keeping the final image minimal.
- Written answers (A1–A4) cover the port each backend listens on, DNS resolution via `proxynet`, path-prefix rewriting, and `Host` header rewriting — plus a short comparison of reverse-proxy concerns (scaling, hiding backend exposure, avoiding repeated work, cross-cutting concerns like TLS termination) versus forward-proxy concerns (client-side policy, shared caching, privacy).

**Teil B — Docker IP routing (`aufgabe4-teil-b.md` `docker-compose-routing.yml`).**
- **Topology:** five Alpine containers with `NET_ADMIN` capability — three end hosts (`hostA` on `192.168.0.0/16`, `hostC` on `10.0.0.0/8`, `hostD` on `172.33.0.0/16`) connected through two routers (`routerAB`, `routerBCD`), with no two hosts sharing a subnet directly.
- **Goal:** let `hostA` and `hostD` reach `hostC` purely via routing — without changing any host's IP address — proven via `ping 10.0.0.10` and `traceroute 192.168.0.10` from `hostD`.
- **Process:** IP forwarding enabled on both routers (`sysctls: net.ipv4.ip_forward=1` in the compose file); `iproute2`  installed on each container (`apk add iproute2`, not included by default on Alpine); static routes added by hand on every router and host with `ip route add <destination-network> via <next-hop>`, so each node knows how to reach the networks it isn't directly attached to.
- **Verification:** on `hostD`, `ping -c 4 10.0.0.10` returns 4/4 replies from hostC, and `traceroute 192.168.0.10` shows the expected two-hop path through `routerBCD` and `routerAB`. Only routes were added — no address or NAT changes — exactly as required.
