# LAB 4: Khảo sát và đánh giá bề mặt mạng bằng Nmap

Môn: An toàn hệ thống thông tin

## 1. Mô tả

Bài lab dùng Nmap để khảo sát các máy ảo trong một mạng riêng (host-only): tìm host đang sống, quét cổng TCP/UDP, nhận diện dịch vụ và hệ điều hành, chạy một số NSE script, rồi xuất kết quả làm bằng chứng và so sánh trước/sau khi hardening.

> Chỉ quét các máy ảo do chính mình dựng trong mạng host-only. Không quét IP/tên miền bên ngoài khi chưa được phép.

## 2. Môi trường

| Thành phần | Chi tiết |
|---|---|
| Máy thật | Ubuntu, chạy VMware Workstation |
| VM 1: Kali Linux | Máy quét chính (cài Nmap bằng `apt`) |
| VM 2: Metasploitable 2 | Máy đích cố ý có lỗ hổng (`msfadmin` / `msfadmin`) |
| VM 3: Windows 11 | Cài Nmap + Npcap + Zenmap; dùng làm máy đích đối chiếu (NSE, before/after hardening) |
| Mạng | Cả 3 VM chung một mạng Host-only (VMnet1), DHCP bật |

Sơ đồ:

```
        Ubuntu (host, VMware)
                 |
          VMnet1 (Host-only)
        /        |         \
     Kali    Metasploitable2   Windows 11
   (máy quét)   (máy đích)    (đích đối chiếu)
```

Bảng IP (điền sau khi lấy IP thật, dải mạng do VMware cấp nên khác 192.168.56.0/24 của đề):

| Thiết bị | IP | Subnet | Ghi chú |
|---|---|---|---|
| Kali VM | ........ | ........ | Máy quét |
| Metasploitable 2 | ........ | ........ | Máy đích |
| Windows 11 VM | ........ | ........ | Đích đối chiếu |

## 3. Chuẩn bị

1. Tắt hẳn cả 3 VM (Powered Off, không để Suspended).
2. Mỗi VM: Settings → Network Adapter → Host-only (hoặc Custom: VMnet1).
3. Tạo snapshot `Before-LAB4` cho từng VM.
4. Bật VM, lấy IP:
   - Kali: `ip -br addr`
   - Metasploitable 2: `ifconfig`
   - Windows 11: `ipconfig`
5. Từ Kali kiểm tra kết nối: `ping -c 4 <IP_MSF>`

## 4. Các nhiệm vụ

| # | Nội dung | Lệnh chính (chạy trên Kali) |
|---|---|---|
| 1 | Host discovery | `sudo nmap -sn <DẢI_MẠNG>` |
| 2 | TCP Connect / SYN scan | `nmap -sT <IP_MSF>`, `sudo nmap -sS <IP_MSF>` |
| 3 | FIN / Xmas / NULL / ACK | `sudo nmap -sF`, `-sX`, `-sN`, `-sA <IP_MSF>` |
| 4 | UDP có kiểm soát | `sudo nmap -sU --top-ports 20 <IP_MSF>` |
| 5 | Version / OS / Aggressive | `sudo nmap -sV`, `-O`, `-A <IP_MSF>` |
| 6 | NSE SMB | `sudo nmap -p 445 --script smb-os-discovery <IP_MSF>`<br>`sudo nmap -p 445 --script smb-vuln-ms17-010 <IP_MSF>` |
| 7 | Xuất kết quả | `-oN ket_qua.txt`, `-oX ket_qua.xml`, `-oG smb.txt`, `xsltproc ket_qua.xml -o bao_cao.html` |
| 8 | Before/after hardening (Windows 11) | `sudo nmap -sV <IP_WIN> -oN before.txt` → thay đổi phòng thủ → chạy lại `-oN after.txt` → `diff before.txt after.txt` |

## 5. Ảnh minh chứng cần chụp

1. `ip -br addr` trên Kali
2. `ifconfig` trên Metasploitable 2
3. Kết quả host discovery `-sn`
4. Kết quả `-sS` hoặc `-sT`
5. Kết quả `-sV`
6. Kết quả `-O` hoặc `-A`
7. Một NSE script kèm kết luận dựa trên output
8. File kết quả đã lưu (.txt / .xml / .html)

## 6. Câu hỏi phân tích

Trả lời 10 câu ở mục 12.2 của đề (open/closed/filtered, `-sS` vs `-sT`, FIN/Xmas/NULL, ACK scan, UDP, vai trò `-sV`, giới hạn `-O`, NSE timeout, so sánh before/after, 3 cấu hình phòng thủ).

## 7. Cấu trúc thư mục nộp

```
LAB4/
├── README.md
├── anh_minh_chung/      # 8 ảnh bắt buộc
├── ket_qua/             # ket_qua.txt, ket_qua.xml, smb.txt, bao_cao.html, before.txt, after.txt
└── bao_cao.docx         # bảng đã điền + câu trả lời phân tích
```

## 8. Lưu ý

- Chỉ bật NAT/Bridged khi cần tải gói, lúc quét phải để Host-only.
- Không Bridge Metasploitable 2 vào mạng thật.
- Thay `<IP_MSF>`, `<IP_WIN>`, `<DẢI_MẠNG>` bằng IP thật của bạn.
- Kết luận NSE dựa trên output: chỉ ghi "có dấu hiệu dễ bị ảnh hưởng" khi script báo VULNERABLE; timeout không có nghĩa là đã vá.
