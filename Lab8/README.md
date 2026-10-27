# LAB 8/9 -- LOCAL DNS ATTACK (VMware Workstation + pfSense)

> Hướng dẫn từng bước dựa trên tài liệu
> **LAB8_Local_DNS_Attack_pfSense_2026.pdf**.\
> Tài liệu gốc dùng VirtualBox; README này chuyển phần **Internal
> Network `dns-lab`** sang **VMware Workstation LAN Segment `dns-lab`**.
>
> **AN TOÀN:** Chỉ thực hiện trong mạng LAB cô lập `10.9.0.0/24`. Không
> chạy script spoofing trên Wi‑Fi, mạng trường/công ty, Internet hoặc
> thiết bị của người khác.

------------------------------------------------------------------------

## 0. Mô hình cần đạt

``` text
                         pfSense
                    DNSLAB: 10.9.0.1/24
                          |
                   LAN Segment: dns-lab
              ____________|____________
             |            |            |
       DNS-SERVER       VICTIM        KALI
       10.9.0.10       10.9.0.100    10.9.0.200
       BIND9            DNS client     Attacker

Các mạng LAB cũ phía pfSense:
DMZ-WEB:            172.16.6.10
APP-TEST/AUTH-DNS:  172.16.6.20
```

### Vai trò hai máy DMZ

-   `DMZ-WEB 172.16.6.10`: web hợp lệ. Baseline của
    `www.external.lab9.test` phải trỏ về IP này.
-   `APP-TEST / AUTH-DNS 172.16.6.20`: authoritative DNS mô phỏng.
    Script `auth_dns_delayed.py` trả lời chậm có chủ đích để quan sát
    cuộc đua DNS.

Nếu LAB trước của bạn chưa có hai máy này thì **đừng tự chế cấu hình**;
cần đối chiếu lại LAB 7/8 hoặc bộ tài nguyên giảng viên cấp.

------------------------------------------------------------------------

# PHẦN A -- DỰNG MẠNG `dns-lab` TRÊN VMWARE

## Bước 1 -- Snapshot

Trước khi sửa:

1.  Tắt pfSense.
2.  Snapshot pfSense, ví dụ: `LAB9_START_FROM_LAB8`.
3.  Snapshot Kali trước khi thêm card mạng.

Không sửa/xóa các card VMware cũ của pfSense.

------------------------------------------------------------------------

## Bước 2 -- Thêm card `dns-lab` vào pfSense

Máy hiện tại có thể đang có:

``` text
Network Adapter      Custom (vmnet0)
Network Adapter 2    Custom (vmnet1)
Network Adapter 3    Custom (vmnet2)
```

Giữ nguyên.

### Thêm card mới

1.  Tắt hoàn toàn pfSense.
2.  `pfSense -> Edit virtual machine settings`.
3.  `Add...`
4.  Chọn `Network Adapter`.
5.  `Finish`.
6.  Chọn card vừa thêm, ví dụ `Network Adapter 4`.
7.  Chọn:

``` text
LAN segment: A private network shared with other virtual machines
```

8.  Bấm `LAN Segments...`.
9.  Tạo:

``` text
dns-lab
```

10. Chọn `dns-lab`.
11. Bật `Connect at power on`.
12. Save.

Kết quả:

``` text
Network Adapter 4 -> LAN Segment -> dns-lab
```

> VMware `LAN Segment dns-lab` đóng vai trò tương đương
> `Internal Network: dns-lab` trong tài liệu VirtualBox.

------------------------------------------------------------------------

# PHẦN B -- GÁN INTERFACE DNSLAB TRÊN pfSense

## Bước 3 -- Xác định card mới

Bật pfSense.

Ở console chọn:

``` text
1) Assign Interfaces
```

Nếu hỏi VLAN:

``` text
Should VLANs be set up now [y|n]?
```

chọn:

``` text
n
```

pfSense sẽ liệt kê interface, ví dụ:

``` text
vmx0
vmx1
vmx2
vmx3
```

hoặc tên khác tùy driver.

**Không đoán interface.** Xác định card VMware vừa thêm trước khi thay
đổi WAN/LAN/DMZ.

Nếu các interface LAB cũ đang hoạt động ổn, cách an toàn hơn là vào
WebGUI:

``` text
Interfaces -> Assignments
```

Tại `Available Network Ports`, chọn port mới -\> `Add`.

Mở OPT mới và cấu hình:

``` text
Enable interface:       YES
Description:            DNSLAB
IPv4 Configuration:     Static IPv4
IPv4 Address:           10.9.0.1 /24
Upstream Gateway:       NONE
```

Sau đó:

``` text
Save -> Apply Changes
```

**Không đặt Upstream Gateway cho DNSLAB.**

### Kiểm tra

pfSense phải có:

``` text
DNSLAB = 10.9.0.1/24
```

📸 **Ảnh cần chụp:** interface DNSLAB và IP `10.9.0.1/24`.

------------------------------------------------------------------------

# PHẦN C -- FIREWALL DNSLAB

## Bước 4 -- Tạo rule cô lập

Vào:

``` text
Firewall -> Rules -> DNSLAB
```

Theo tài liệu, ruleset cần có logic:

  ------------------------------------------------------------------------------------
  \#          Action      Source          Destination         Port        Mục đích
  ----------- ----------- --------------- ------------------- ----------- ------------
  1           Pass        `10.9.0.10`     `172.16.6.20`       TCP/UDP 53  DNS-SERVER
                                                                          -\> AUTH-DNS

  2           Pass        `10.9.0.0/24`   `172.16.6.10`       TCP 80      Victim -\>
                                                                          web hợp lệ

  3           Block + Log `10.9.0.0/24`   `10.6.0.0/24`       Any         bảo vệ
                                                                          LAN-DOMAIN

  4           Block + Log `10.9.0.0/24`   `192.168.56.0/24`   Any         bảo vệ MGMT

  5           Block + Log `10.9.0.0/24`   Any                 Any         cô lập mặc
                                                                          định
  ------------------------------------------------------------------------------------

Sau khi tạo:

``` text
Apply Changes
```

và reset states theo yêu cầu của lab.

📸 **Ảnh cần chụp:** `Firewall -> Rules -> DNSLAB`.

> Trong attack phase, DNSLAB không được ra WAN. NAT chỉ nên dùng tạm khi
> cần cài package.

------------------------------------------------------------------------

# PHẦN D -- NỐI 3 VM VÀO `dns-lab`

## Bước 5 -- DNS-SERVER

VMware:

``` text
DNS-SERVER -> Settings -> Network Adapter
LAN Segment -> dns-lab
```

DNS-SERVER:

``` text
IP:      10.9.0.10/24
Gateway: 10.9.0.1
```

Trong giai đoạn cài package có thể có NAT tạm. Sau khi cài xong, tắt NAT
và giữ card `dns-lab`.

------------------------------------------------------------------------

## Bước 6 -- VICTIM

VMware:

``` text
VICTIM -> Settings -> Network Adapter
LAN Segment -> dns-lab
```

Cấu hình:

``` text
IP:      10.9.0.100/24
Gateway: 10.9.0.1
DNS:     10.9.0.10
```

Victim phải dùng DNS-SERVER `10.9.0.10` trong pha thử nghiệm.

Trên Ubuntu 24.04, dùng Netplan/Network Settings; **không sửa
`/etc/resolv.conf` trực tiếp**.

Kiểm tra:

``` bash
ip -br addr
ip route
resolvectl status
```

------------------------------------------------------------------------

## Bước 7 -- KALI

VMware:

``` text
KALI -> Settings -> Network Adapter
LAN Segment -> dns-lab
```

Cấu hình:

``` text
IP:      10.9.0.200/24
Gateway: 10.9.0.1
```

Cài công cụ khi còn NAT nếu cần:

``` bash
sudo apt update
sudo apt install -y python3-scapy wireshark tcpdump
```

Sau đó tắt NAT.

> Tài liệu gốc yêu cầu VirtualBox Promiscuous Mode = Allow VMs. VMware
> có cơ chế mạng khác; trước Task 2/3 phải kiểm tra thực tế rằng Kali có
> thể capture các DNS packet cần thiết trên `dns-lab`.

📸 **Ảnh cần chụp:** IP của DNS-SERVER, Victim và Kali.

------------------------------------------------------------------------

# PHẦN E -- DNS-SERVER BIND9

## Bước 8 -- Cài BIND9

Trên DNS-SERVER:

``` bash
sudo apt update
sudo apt install -y bind9 dnsutils
systemctl status bind9 --no-pager
```

Kiểm tra mạng:

``` bash
ip -br addr
ip route
resolvectl status
```

------------------------------------------------------------------------

## Bước 9 -- Chép cấu hình BIND9

Tài liệu yêu cầu bộ tài nguyên của giảng viên:

``` text
01_DNS_SERVER/
  named.conf.options
  named.conf.local
  zone forward/reverse mẫu
```

Backup trước:

``` bash
sudo cp /etc/bind/named.conf.options /etc/bind/named.conf.options.bak
sudo cp /etc/bind/named.conf.local /etc/bind/named.conf.local.bak
```

Sau đó chép/đối chiếu các file tài nguyên vào `/etc/bind/`.

DNS-SERVER phải:

-   authoritative cho `lab9.test`;
-   recursive/cache cho `10.9.0.0/24`;
-   forward `external.lab9.test` tới `172.16.6.20`.

------------------------------------------------------------------------

## Bước 10 -- Validate BIND9

Chạy kiểm tra tương ứng với các zone/file thực tế:

``` bash
sudo named-checkconf
sudo named-checkzone lab9.test /etc/bind/db.lab9.test
sudo named-checkzone 9.0.10.in-addr.arpa /etc/bind/db.10.9.0
sudo systemctl restart bind9
sudo ss -luntp | grep :53
```

📸 **Ảnh cần chụp:** `named-checkconf` / `named-checkzone` thành công.

------------------------------------------------------------------------

# PHẦN F -- BASELINE

## Bước 11 -- Local zone

Ví dụ theo tài liệu:

``` bash
dig @10.9.0.10 www.lab9.test A +short
dig @10.9.0.10 -x 10.9.0.10 +short
```

------------------------------------------------------------------------

## Bước 12 -- AUTH-DNS trên APP-TEST

Trên APP-TEST `172.16.6.20`, tài liệu sử dụng file tài nguyên:

``` text
auth_dns_delayed.py
```

Script mô phỏng authoritative DNS và trả:

``` text
www.external.lab9.test -> 172.16.6.10
```

với độ trễ khoảng 1.5 giây.

Chỉ chạy script được giảng viên cung cấp trong môi trường LAB.

------------------------------------------------------------------------

## Bước 13 -- Kiểm tra baseline

Trên DNS-SERVER:

``` bash
sudo rndc flush
dig @10.9.0.10 www.external.lab9.test A +noall +answer
```

Trên Victim:

``` bash
resolvectl query www.external.lab9.test
```

### Kết quả bắt buộc trước khi attack

``` text
www.external.lab9.test -> 172.16.6.10
```

📸 **Ảnh cần chụp:** baseline trả `172.16.6.10`.

**Không chuyển Task 2/3 nếu baseline sai.**

------------------------------------------------------------------------

# PHẦN G -- WIRESHARK

## Bước 14 -- Capture trên Kali

Mở Wireshark trên interface `dns-lab`.

Display filter:

``` text
dns
```

hoặc:

``` text
dns.qry.name == "www.external.lab9.test"
```

Cần quan sát được traffic DNS của bài.

Nếu Kali không thấy query, kiểm tra:

-   chọn đúng interface;
-   VM network có đúng `dns-lab`;
-   packet capture/promiscuous behavior của VMware;
-   query có đang lấy từ cache hay không.

------------------------------------------------------------------------

# TASK 1 -- HOSTS FILE REDIRECTION

Task này thực hiện trên chính Victim.

## Bước 15 -- Backup hosts

``` bash
sudo cp /etc/hosts /etc/hosts.lab9.bak
```

Thêm mapping theo yêu cầu tài liệu:

``` text
10.9.0.200    www.lab9.test
```

Kiểm tra:

``` bash
getent hosts www.lab9.test
curl http://www.lab9.test
```

Kết quả mong đợi: hệ thống/curl đi tới Kali `10.9.0.200`.

Nhưng:

``` bash
dig @10.9.0.10 www.lab9.test
```

vẫn hỏi DNS trực tiếp và phải cho kết quả DNS gốc.

📸 **Ảnh cần chụp:** `getent/curl` và `dig` thể hiện sự khác nhau.

## Khôi phục Task 1

``` bash
sudo cp /etc/hosts.lab9.bak /etc/hosts
sudo resolvectl flush-caches
```

------------------------------------------------------------------------

# TASK 2 -- DIRECT DNS SPOOFING

> Chỉ chạy script `dns_spoof_lab9.py` được cung cấp cho lab, trên mạng
> `dns-lab`.

## Bước 16 -- Làm sạch cache

DNS-SERVER:

``` bash
sudo rndc flush
```

Victim:

``` bash
sudo resolvectl flush-caches
```

## Bước 17 -- Xác định interface Kali

``` bash
ip -br addr
```

Ghi lại interface có IP:

``` text
10.9.0.200
```

Không chạy script trên card NAT/Wi‑Fi.

## Bước 18 -- Chạy script lab

Theo tài liệu, script tài nguyên được chạy ở mode `victim` và trỏ vào
interface DNS-LAB. Dùng đúng cú pháp của file `dns_spoof_lab9.py` mà
giảng viên cấp.

## Bước 19 -- Victim tạo query mới

``` bash
resolvectl query www.external.lab9.test
curl http://www.external.lab9.test
```

Nếu forged response thắng cuộc đua, Victim tạm nhận:

``` text
10.9.0.200
```

Nếu nhận:

``` text
172.16.6.10
```

thì phản hồi hợp lệ đã tới trước hoặc capture/spoof chưa đúng.

### Wireshark cần chứng minh

-   Query: `10.9.0.100 -> 10.9.0.10`, UDP destination 53.
-   Legitimate response: A = `172.16.6.10`.
-   Forged response: A = `10.9.0.200`.
-   Transaction ID của query và forged response giống nhau.
-   Query source UDP port = response destination UDP port.

📸 **Ảnh cần chụp:** query + forged response + transaction ID/UDP port.

------------------------------------------------------------------------

# TASK 3 -- DNS CACHE POISONING

> Mục tiêu chuyển từ Victim sang DNS-SERVER. Chỉ dùng script lab trong
> mạng cô lập.

## Bước 20 -- Flush cache

DNS-SERVER:

``` bash
sudo rndc flush
```

Victim:

``` bash
sudo resolvectl flush-caches
```

Xác nhận baseline sạch:

``` bash
dig @10.9.0.10 www.external.lab9.test A +noall +answer
```

## Bước 21 -- Chạy mode resolver

Dùng `dns_spoof_lab9.py` của lab ở mode `resolver`, trên đúng interface
Kali nối `dns-lab`.

## Bước 22 -- Victim kích hoạt lookup

``` bash
sudo resolvectl flush-caches
resolvectl query www.external.lab9.test
curl http://www.external.lab9.test
```

Nếu cache poisoning thành công, query tiếp theo có thể tiếp tục nhận:

``` text
10.9.0.200
```

ngay cả sau khi dừng script Kali, cho tới khi TTL hết hoặc BIND cache
được flush.

Theo tài liệu, TTL thử nghiệm là khoảng:

``` text
120 giây
```

## Bước 23 -- Kiểm tra BIND cache

``` bash
sudo rndc dumpdb -cache
sudo grep -A4 -B4 "www.external.lab9.test" /var/cache/bind/named_dump.db
```

Nếu không thấy file:

``` bash
sudo find /var/cache/bind -maxdepth 1 -type f -name "*dump*"
```

📸 **Ảnh cần chụp:** cache chứa `10.9.0.200`.

------------------------------------------------------------------------

# PHẦN H -- KHÔI PHỤC

## Bước 24 -- Restore

Dừng script spoofing và fake HTTP server trên Kali.

DNS-SERVER:

``` bash
sudo rndc flush
```

Victim:

``` bash
sudo resolvectl flush-caches
```

Khôi phục `/etc/hosts` nếu cần.

Dừng `auth_dns_delayed.py` trên APP-TEST.

Kiểm tra:

``` bash
dig @10.9.0.10 www.external.lab9.test A +noall +answer
```

Kết quả phải trở về:

``` text
172.16.6.10
```

📸 **Ảnh cần chụp:** restore result `172.16.6.10`.

------------------------------------------------------------------------

# PHẦN I -- HARDENING

Các biện pháp tài liệu yêu cầu phân tích:

1.  **DNS egress control** -- client chỉ hỏi resolver được chỉ định.
2.  **Segmentation** -- attacker không nên cùng L2 với DNS
    client/server.
3.  **Promiscuous mode** -- chỉ bật khi thực hành/capture; tắt sau lab.
4.  **DNSSEC validation** -- xác thực/toàn vẹn cho zone có ký DNSSEC.
5.  **DNS COOKIE / source-port randomization** -- tăng độ khó giả mạo.
6.  **Monitoring** -- phát hiện duplicate DNS responses, cache bất
    thường, answer sai chính sách.

------------------------------------------------------------------------

# CHECKLIST ẢNH NỘP BÀI

-   [ ] pfSense DNSLAB `10.9.0.1/24`.
-   [ ] Firewall rules cô lập DNSLAB.
-   [ ] DNS-SERVER `10.9.0.10`.
-   [ ] Victim `10.9.0.100`.
-   [ ] Kali `10.9.0.200`.
-   [ ] BIND9 `named-checkconf` / `named-checkzone` thành công.
-   [ ] Baseline `www.external.lab9.test -> 172.16.6.10`.
-   [ ] Task 1: local resolution đi `10.9.0.200`, direct `dig` vẫn cho
    DNS gốc.
-   [ ] Task 2: Wireshark có query + forged response; Victim nhận
    `10.9.0.200`.
-   [ ] Task 3: forged response tới resolver và cache chứa `10.9.0.200`.
-   [ ] Sau flush/restore: trở về `172.16.6.10`.
-   [ ] Tắt chế độ packet capture/promiscuous không cần thiết sau lab.

------------------------------------------------------------------------

# XỬ LÝ LỖI NHANH

### Victim không phân giải được

Kiểm tra:

``` bash
resolvectl status
ip route
ping 10.9.0.10
```

Xác nhận Victim dùng DNS `10.9.0.10`.

### DNS-SERVER không hỏi APP-TEST

Kiểm tra:

-   forward zone;
-   cache;
-   pfSense rule `10.9.0.10 -> 172.16.6.20:53`.

### Kali không thấy query

Kiểm tra:

-   đúng interface `10.9.0.200`;
-   cả VM có cùng LAN Segment `dns-lab`;
-   capture setting của VMware;
-   flush cache rồi query lại.

### Victim luôn nhận `172.16.6.10`

Theo tài liệu, kiểm tra:

-   phản hồi hợp lệ có đến trước không;
-   Kali có thực sự nhìn thấy query không;
-   AUTH-DNS delay;
-   interface của script.

### BIND restart lỗi

``` bash
sudo named-checkconf
sudo journalctl -u bind9 -n 50
```

------------------------------------------------------------------------

# 10 CÂU HỎI PHÂN TÍCH TRONG BÁO CÁO

1.  Tại sao `/etc/hosts` ảnh hưởng ứng dụng nhưng `dig @DNS-SERVER` vẫn
    trả DNS gốc?
2.  Những trường DNS/UDP nào phải khớp để forged response có cơ hội được
    chấp nhận?
3.  Vì sao Task 2 chỉ ảnh hưởng Victim hiện tại còn Task 3 có thể ảnh
    hưởng nhiều client?
4.  TTL có vai trò gì trong cache poisoning?
5.  Vì sao cần khả năng quan sát unicast traffic trong bài?
6.  Vì sao dùng DNSLAB riêng thay vì tấn công DC01 DNS của LAN-DOMAIN?
7.  Authoritative DNS, recursive resolver và caching resolver khác nhau
    thế nào?
8.  DNSSEC giải quyết vấn đề gì, và vì sao zone `.test` unsigned không
    tự được bảo vệ?
9.  Vì sao chỉ chặn UDP/53 chưa đủ để kiểm soát DNS hiện đại?
10. Nêu ít nhất bốn biện pháp hardening.

------------------------------------------------------------------------

## Quy tắc làm lab

**Làm từng bước. Không chuyển Task 2/3 nếu baseline chưa đúng.**

Mốc quan trọng nhất:

``` text
TRƯỚC ATTACK:
www.external.lab9.test -> 172.16.6.10

ATTACK THÀNH CÔNG TRONG LAB:
-> 10.9.0.200

SAU RESTORE:
-> 172.16.6.10
```
