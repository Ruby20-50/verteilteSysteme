#Commands and configurations
- yml configeration
 sysctls:
    - net.ipv4.ip_forward=1

- docker compose -f docker-compose-routing.yml up -d
- docker compose -f docker-compose-routing.yml down

--- on each router --- 

-  docker exec -it router-name sh
opens a shell in each router with
- sysctl net.ipv4.ip_forward
- ip route add <destination-network> via <next-hop-router-interface-ip>
- ip route


--- on each host ---
- docker exec -it host-name sh
- apk add iproute2
- ip route add <destination-network>/<subnet-mask> via <my-gate-way>
- ip route
exit

--- assignment question:

- docker exec -it aufgabe4-hostD-1 sh
- ping 10.0.0.10
- traceroute 192.168.0.10