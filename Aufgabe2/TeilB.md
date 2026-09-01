1. Physischer Aufbau:

     This step is just orientation — no configuration yet.

     router (NUC-PC) with two NICs: eno0 (faces LAN1/switch1) and enx00e04c200925 (faces LAN2/switch2)
    client1 on LAN1, client2 on LAN2
    operator NUC for physical console access (keyboard/screen)

     Task: match the diagram to the physical rack, and note the static IPs written on the sheet at the rack (you'll need them for SSH before DHCP is running).

2. IP-Konfiguration des Routers:
        
        sudo ip addr add 192.168.x.1/24 dev eno0
        sudo ip link set eno0 up

        sudo ip addr add 10.y.0.1/16 dev enx00e04c200925
        sudo ip link set enx00e04c200925 up
        
    ```ip addr add``` assigns an IP address (with subnet mask) to a network interface.

    ```ip link set <iface> up``` brings the interface online.

3. DHCP-Server und DHCP-Client Konfiguration

    On the router, edit /etc/dhcp/dhcpd.conf and add one subnet block per LAN:

    subnet 192.168.x.0 netmask 255.255.255.0 {
    range 192.168.x.200 192.168.x.210;
    option routers 192.168.x.1;
    }

    subnet 10.y.0.0 netmask 255.255.0.0 {
    range 10.y.0.200 10.y.0.210;
    option routers 10.y.0.1;
    }

    This tells the DHCP server which address pool to hand out on each subnet, and which IP is the default gateway (option routers) for clients on that subnet.

    Then edit /etc/default/isc-dhcp-server:

    INTERFACESv4="eth0 eth1"

    This tells the DHCP daemon which interfaces to actually listen on (should match your router's real interface names, i.e. eno0 and enx00e04c200925).

    Restart the service:

        sudo systemctl restart isc-dhcp-server

     On client1 and client2, edit /etc/netplan/99_config.yaml:

        yaml
        network:
        version: 2
        renderer: networkd
        ethernets:
            eth0:
            dhcp4: true
    This switches the client's network config from static to DHCP (dhcp4: true means "get IP, gateway, etc. automatically from a DHCP server" instead of hardcoding them).

    Apply it:

        sudo netplan try
        sudo systemctl restart systemd-networkd
    
    ```netplan try``` applies the config temporarily and rolls back if you don't confirm it (safety net in case you lock yourself out).
    
    ```restart systemd-networkd``` makes sure the new config is actually picked up.

    The assignment flags a real problem here: once clients get IPs via DHCP, their address changes from the static one on the rack sheet, so your existing SSH session/address won't work anymore. Practical ways to find the new IP:

    - Check the DHCP lease file on the router: ```cat /var/lib/dhcp/dhcpd.leases```
    - Or go to the client physically via the operator console and run ```ip addr show eth0```

4. Routing konfigurieren

Edit /etc/sysctl.conf and uncomment/add:

    net.ipv4.ip_forward=1

Then apply it:

    sudo sysctl -p

This enables IP forwarding — without it, the Linux kernel drops any packet that arrives on one interface addressed to a host on another network; enabling it lets the router actually pass traffic between LAN1 and LAN2.

**Verify from a client:**


    ping <client2-IP>
    traceroute <client2-IP>

```ping``` confirms basic reachability (ICMP echo).
```traceroute``` shows the path packets take hop by hop — useful to confirm the router is actually the single hop between LAN1 and LAN2, and to spot where a failure occurs if ```ping``` doesn't work.

Order matters: A2 (router IPs) → A3 (DHCP + client config) → A4 (forwarding + test). If DHCP is enabled before the router itself has correct static IPs, the option routers gateway address handed to clients would be wrong/unreachable.

