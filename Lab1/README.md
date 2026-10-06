# LAB 1: Bắt gói tin Telnet - SSH

- **Họ và tên:** ................................
- **MSSV:** ................................
- **Lớp:** ................................
- **Video demo:** <link YouTube>
- **Báo cáo:** `Lab1_Lop_MSSV_TenSV.docx`

## 1. Mục tiêu

- Dựng mạng giả lập Client / Server / Attacker.
- Dùng Wireshark bắt gói tin Telnet và SSH.
- So sánh xem giao thức nào lộ thông tin đăng nhập.

## 2. Môi trường

| Máy | Hệ điều hành | Công cụ | IP |
|---|---|---|---|
| Server | ........ | Telnet server / SSH server | ........ |
| Client | ........ | PuTTY | ........ |
| Attacker | ........ | Wireshark | ........ |

Ảo hóa: VMware. Ba máy chung một mạng host-only, đã ping thông nhau.

## 3. Nội dung đã thực hiện

**Phần 1: Telnet**
1. Bật Telnet server trên Server, tạo tài khoản thử nghiệm.
2. Bật Wireshark, lọc `tcp.port == 23`.
3. Client dùng PuTTY (Telnet, port 23) đăng nhập, chạy vài lệnh (`dir`/`ls`, `mkdir`).
4. Dừng bắt gói, dùng Follow TCP Stream để tìm username/password.
5. Đổi mật khẩu phức tạp hơn và bắt lại.

**Phần 2: SSH**
1. Bật SSH server (OpenSSH), kiểm tra dịch vụ đang chạy và cổng 22.
2. Bật Wireshark, lọc `tcp.port == 22`.
3. Client dùng PuTTY (SSH, port 22) đăng nhập, xác nhận host key fingerprint, chạy vài lệnh.
4. Dừng bắt gói, xem nội dung phiên.

## 4. Kết quả

| | Telnet | SSH |
|---|---|---|
| Thấy username/password | ........ | ........ |
| Thấy nội dung lệnh | ........ | ........ |
| Thấy được gì | ........ | IP, port 22, bắt tay, kích thước, thời gian gói tin |
| Đổi mật khẩu phức tạp có giúp không | ........ | ........ |

Nhận xét ngắn:

- Telnet: ........................................
- SSH: ........................................

Ảnh minh chứng nằm trong thư mục `images/`:

- `telnet_capture.png`: gói tin Telnet
- `telnet_follow_stream.png`: lộ username/password
- `ssh_capture.png`: gói tin SSH, payload đã mã hóa

## 5. Trả lời câu hỏi

Xem file báo cáo `.docx` (11 câu hỏi mục C).

## 6. Lưu ý

- Wireshark có thể không thấy lưu lượng nếu Attacker không nằm đúng vị trí trên virtual switch, khi đó bắt trực tiếp trên Client hoặc Server.
- Telnet chỉ chạy trong mạng lab cô lập, không mở cổng 23 ra Internet.
- Mọi ảnh và dữ liệu trong bài đều do mình tự thực hiện và chụp.

## 7. Cấu trúc thư mục

```
LAB1/
├── README.md
├── images/
├── Lab1_Lop_MSSV_TenSV.docx
└── file .pcapng (nếu có)
```
