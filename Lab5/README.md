# Lab 3 – pfSense Firewall (VMware trên Ubuntu host)

## Mô hình

| Thiết bị | Interface | IP | GW | DNS |
|---|---|---|---|---|
| pfSense | WAN (em0) | DHCP | - | - |
| pfSense | LAN (em1) | 10.0.0.1/8 | - | - |
| pfSense | DMZ (em2, OPT1) | 172.16.0.1/16 | - | - |
| Máy thật (vmnet1) | Host-only | 10.0.0.100/8 | không | không |
| Domain Controller | LAN | 10.0.0.2/8 | 10.0.0.1 | 10.0.0.2 |
| LAN-Test (Ubuntu) | LAN | 10.0.0.3/8 | 10.0.0.1 | - |
| DMZ-Web (Win Server + IIS) | DMZ | 172.16.0.2/16 | 172.16.0.1 | 8.8.8.8 (đặt sau khi có Pass DMZ→Internet) |

WAN tuyệt đối không nằm trong 10.0.0.0/8 hoặc 172.16.0.0/16.

## Tiến độ

- [x] Tạo VM pfSense (3 card: VMnet0 bridged / VMnet1 host-only / LAN Segment `dmz-net`)
- [x] Cài pfSense, tháo ISO
- [x] Console đặt LAN = 10.0.0.1/8 (không bật DHCP)
- [x] vmnet1 trên máy thật = 10.0.0.100/8
- [x] Ping 10.0.0.1, vào được https://10.0.0.1
- [ ] Setup Wizard, đổi mật khẩu admin, chụp Dashboard
- [ ] Cấu hình DMZ (OPT1)
- [ ] Outbound NAT
- [ ] Chuẩn hóa rule LAN
- [ ] Dựng DC, DMZ-Web, LAN-Test
- [ ] 5 tình huống phần C
- [ ] Trả lời câu hỏi D, nộp báo cáo

## Máy thật Ubuntu: đặt IP cho vmnet1

Virtual Network Editor (`sudo vmware-netcfg`): vmnet1 = Host-only, tắt DHCP, **Subnet IP = 10.0.0.0**, mask 255.0.0.0.

Sau đó đặt IP host (mất khi reboot hoặc restart VMware, chạy lại):

```bash
sudo ip addr flush dev vmnet1
sudo ip addr add 10.0.0.100/8 dev vmnet1
sudo ip link set vmnet1 up
ping -c 4 10.0.0.1
```

## Phần B: dựng nền tảng

### 1. Setup Wizard
https://10.0.0.1 (admin / pfsense). LAN giữ 10.0.0.1/8, đổi mật khẩu admin. Chụp Dashboard.

### 2. DMZ
- Interfaces → Assignments → Add `em2` → OPT1.
- OPT1: Enable, Description = `DMZ`, Static IPv4 = `172.16.0.1/16` → Save → Apply.

### 3. Outbound NAT
Firewall → NAT → Outbound → Automatic hoặc Hybrid → Save. Kiểm tra Automatic Rules có 10.0.0.0/8 và 172.16.0.0/16.

### 4. Ruleset LAN
1. Firewall → Rules → LAN: Disable 2 rule *Default allow LAN to any* (IPv4, IPv6). **Giữ Anti-Lockout**. Apply.
2. Diagnostics → States → Reset States.
3. Add rule: Pass / Any / Source `LAN net` / Dest Any / Desc `LAN to Internet`. Apply.
4. Diagnostics → Ping `google.com` để check WAN của pfSense.

### 5. Domain Controller (Windows Server 2019/2022)
- 2 GB RAM, card = VMnet1.
- IP 10.0.0.2 / 255.0.0.0 / GW 10.0.0.1 / DNS 10.0.0.2 (không nhập Alternate DNS).
- Add Roles → Active Directory Domain Services → promote, forest mới `vietnam.local`.
- DNS Manager → Properties → Forwarders → thêm 8.8.8.8.

### 6. Test rule nền tảng từ DC
Rule LAN net → Any đang Enabled:
```powershell
ping 8.8.8.8
Resolve-DnsName example.com
curl.exe -4 https://example.com
```
Rồi: dừng ping -t, Disable rule, Apply, Reset States, ping 8.8.8.8 **mới** → phải bị chặn. Chụp cả hai trường hợp, xong Enable lại + Apply + Reset States.

### 7. DMZ-Web
Windows Server, không join domain, card = LAN Segment `dmz-net`. IP 172.16.0.2/16, GW 172.16.0.1, DNS để trống. Cài IIS (PowerShell admin):
```powershell
Install-WindowsFeature Web-Server -IncludeManagementTools
curl.exe http://localhost
```

### 8. LAN-Test
Ubuntu Server minimal, 1 GB RAM, card = VMnet1, IP 10.0.0.3/8, GW 10.0.0.1. Tạo VM độc lập, **không clone DC**.

## Phần C: 5 tình huống

Trước MỖI tình huống: xác nhận 2 Default allow LAN vẫn Disabled → Disable rule thừa của tình huống trước → Apply Changes → Reset States. Mỗi tình huống chụp: ảnh rule, ảnh kiểm thử, 1–2 câu giải thích.

### TH1: Chặn ICMP, cho Web/DNS (pfSense + DC)
Disable rule nền tảng. Tạo 3 rule trên tab LAN theo thứ tự:

| Action | Protocol | Source | Dest |
|---|---|---|---|
| Block | ICMP | LAN net | Any |
| Pass | TCP/UDP, port 53 | LAN net | Any |
| Pass | TCP, port 80 và 443 | LAN net | Any |

Test trên DC:
```powershell
ping 8.8.8.8                              # phải FAIL
Resolve-DnsName example.com -Server 8.8.8.8   # phải OK
curl.exe -4 https://example.com           # phải OK
```

### TH2: Chỉ cho 1 host ra Internet (pfSense + DC + LAN-Test)
Disable rule nền tảng. Tạo theo thứ tự:

| Action | Protocol | Source | Dest |
|---|---|---|---|
| Pass | Any | 10.0.0.2 | Any |
| Block | Any | LAN net | Any |

Test: `ping 8.8.8.8` từ DC → OK; từ LAN-Test (10.0.0.3) → FAIL.

### TH3: Cô lập DMZ khỏi LAN (pfSense + DC + DMZ-Web)
- **A.** Trên DC (admin) cho phép ping đến nó:
  ```
  netsh advfirewall firewall add rule name="LAB-Allow-ICMPv4-Echo" protocol=icmpv4:8,any dir=in action=allow
  ```
- **B. Baseline:** tab DMZ chỉ bật rule Pass DMZ net → Any, Apply, Reset States. Từ DMZ-Web `ping 10.0.0.2` phải có reply (chụp).
- **C. Cô lập:** thêm Block Any / DMZ net → LAN net **nằm TRÊN** rule Pass. Apply, Reset States. `ping 10.0.0.2` mới từ DMZ-Web phải FAIL (chụp). `ping 8.8.8.8` và `Resolve-DnsName example.com` vẫn phải OK (đặt DNS 8.8.8.8 trên DMZ-Web).
- Xong xóa rule tạm: `netsh advfirewall firewall delete rule name="LAB-Allow-ICMPv4-Echo"`

### TH4: Port Forward WAN → DMZ (pfSense + DMZ-Web)
- Điều kiện: IIS chạy, `curl.exe http://localhost` OK. Có thể test từ pfSense: Diagnostics → Test Port tới 172.16.0.2:80.
- Interfaces → WAN: bỏ tick **Block private networks** và **Block bogon networks** (chỉ trong lab). Save/Apply.
- Firewall → NAT → Port Forward → Add: Interface WAN, Protocol TCP, Destination = WAN address, Dest port 8080, Redirect IP 172.16.0.2, Redirect port 80, Filter rule association = Add associated filter rule. Save → Apply → Reset States.
- Test: từ máy thật mở `http://<IP-WAN-pfSense>:8080` (IP WAN hiện ở Dashboard, hiện tại dạng 192.168.123.x), phải thấy trang IIS. Không dùng 10.0.0.1.
- Lỗi: check WAN rule tự tạo, Windows Firewall DMZ-Web mở TCP/80, IIS đang Running.

### TH5: Logging
- Bật **Log packets that are handled by this rule** ở rule Block quan trọng (ví dụ Block DMZ → LAN của TH3 hoặc Block ICMP của TH1).
- Tạo traffic chắc chắn bị chặn.
- Status → System Logs → Firewall: tìm bản ghi, xác định rule nào chặn. Đối chiếu Diagnostics → States nếu không thấy log.

## Phần D: câu hỏi cần trả lời

1. Phân biệt NAT và firewall rule.
2. Vì sao tách Web/Mail/FTP vào DMZ?
3. Rule Block nằm dưới rule Pass tổng quát thì sao?
4. Chặn ping nhưng cho web cần những rule nào?
5. Logging giúp gì khi xử lý sự cố?
6. Ít nhất 3 biện pháp hardening pfSense.

## Checklist ảnh nộp bài

- [ ] Settings 3 card mạng của VM pfSense
- [ ] Console đặt IP LAN 10.0.0.1
- [ ] Dashboard, DMZ, Outbound NAT, LAN rule
- [ ] ≥ 4 tình huống (ảnh rule + ảnh test + giải thích)
- [ ] TH3: ảnh baseline trước Block + ảnh fail sau Block
- [ ] Phần disable LAN rule: thể hiện 2 Default allow đã Disabled và đã Reset States

Nộp file Word (.docx), tên: `[Mã lớp]-Lab3_MSSV-TênSV`.

## Lỗi hay gặp

| Triệu chứng | Cách xử lý |
|---|---|
| Ping 10.0.0.1 không được | Check `ip a show vmnet1` = 10.0.0.100/8; Adapter 2 của VM = VMnet1 |
| Ping reply nhưng thật ra từ máy mình | vmnet1 đang giữ 10.0.0.1, đổi lại .100 |
| Lỗi "subnet IP and mask mismatch" | Subnet IP phải là 10.0.0.0, không phải .100 |
| Boot lại vào installer | Chưa tháo ISO |
| Rule không có tác dụng | Quên Apply Changes hoặc Reset States; rule Block phải nằm trên rule Pass |
| Mất IP vmnet1 sau reboot | Chạy lại 3 lệnh `ip addr` |
