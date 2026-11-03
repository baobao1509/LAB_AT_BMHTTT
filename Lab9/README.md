# README -- LAB 10: Remote DNS Cache Poisoning (VMware)

## 1. Mục tiêu và phạm vi an toàn

README này hướng dẫn triển khai LAB 10 trên VMware theo tài liệu bài
lab. Chỉ chạy trong các mạng ảo cô lập và chỉ dùng miền thử nghiệm
`.test`. Không kết nối Kali hoặc các script mô phỏng vào LAN thật, mạng
trường học/công ty hay Internet.

## 2. Sơ đồ IP theo tài liệu

  Thành phần                       Địa chỉ
  -------------------------------- ----------------
  pfSense -- DNSLAB                `10.9.0.1/24`
  DNS-SERVER                       `10.9.0.10`
  VICTIM                           `10.9.0.100`
  pfSense -- ATTACKNET             `10.10.0.1/24`
  Kali -- ATTACKER                 `10.10.0.200`
  AUTH-DNS / RELAY trên APP-TEST   `172.16.6.20`
  DMZ-WEB                          `172.16.6.10`
  Host quản trị (theo tài liệu)    `192.168.56.1`

## 3. Chuẩn bị VMware

1.  Tắt các VM trước khi đổi card mạng.
2.  Mở **Edit → Virtual Network Editor** (VMware Workstation) và kiểm
    tra VMnet hiện có.
3.  Ánh xạ gợi ý, nếu các VMnet này chưa được dùng bởi lab khác:
    -   VMnet2: Host-only, `10.9.0.0/24` (DNSLAB).
    -   VMnet3: Host-only, `10.10.0.0/24` (ATTACKNET).
    -   VMnet4: mạng riêng APP-TEST/DMZ, `172.16.6.0/24`.
4.  Tắt DHCP VMware trên các mạng dùng IP tĩnh nếu cấu hình lab yêu cầu.
5.  Gắn card mạng theo topology trong tài liệu:
    -   pfSense: các interface DNSLAB, ATTACKNET và APP-TEST/DMZ.
    -   DNS-SERVER và VICTIM: DNSLAB.
    -   Kali: ATTACKNET בלבד; không nối Kali vào DNSLAB.
    -   AUTH-DNS/RELAY và DMZ-WEB: APP-TEST/DMZ tương ứng.
6.  Không xóa hoặc đổi subnet VMnet đang được LAB7/LAB8/LAB9 hay VM khác
    sử dụng. Nếu chưa rõ, gửi ảnh Virtual Network Editor để đối chiếu
    trước.
7.  Nếu dùng VMware Player và không có Virtual Network Editor, đừng tự ý
    sửa file cấu hình VMnet.

**Lưu ý:** VMnet2/3/4 là cách ánh xạ gợi ý trên VMware, không phải tên
bắt buộc trong tài liệu LAB 10. Hãy đối chiếu subnet thực tế trước khi
áp dụng.

## 4. Quy trình thực hiện

1.  **Kiểm tra topology và firewall:** xác nhận IP, gateway, interface
    pfSense và firewall matrix; bảo đảm mạng lab không bridge ra mạng
    thật.
2.  **DNS baseline:** khởi động DNS-SERVER và VICTIM, chạy các truy vấn
    DNS ban đầu theo tài liệu và lưu ảnh kết quả.
3.  **Chuẩn bị Kali:** đặt IP `10.10.0.200`; xác nhận Kali chỉ nối
    ATTACKNET, không nối DNSLAB; đặt Promiscuous Mode thành **Deny**.
4.  **Chạy mô phỏng:** dùng đúng script/tham số trong gói LAB 10. Theo
    tài liệu, resolver mô phỏng dùng UDP source port cố định `33333`,
    harness thử nghiệm dùng UDP/`5300`. Đây là cấu hình mô phỏng có kiểm
    soát, không dùng để tấn công hệ thống ngoài lab.
5.  **Theo dõi và chụp minh chứng:** ghi nhận log `PENDING`, `HIT`,
    `CACHE` và Wireshark với filter được cung cấp trong tài liệu.
6.  **Kiểm tra VICTIM:** chạy truy vấn kiểm tra theo tài liệu, ghi lại
    kết quả thật và chụp màn hình.
7.  **Khôi phục và hardening:** khôi phục BIND9 theo file cấu hình đi
    kèm; giới hạn recursion ACL cho mạng victim; kiểm tra source-port
    randomization và DNSSEC validation; xác nhận Kali không thể dùng
    DNS-SERVER như open resolver.

## 5. Các file được nhắc đến trong tài liệu

-   `vuln_resolver_lab10.py`
-   `kaminsky_remote_lab10.py`
-   `evil_dns_lab10.py`
-   `auth_dns_relay_lab10.py`
-   `fake_index.html`
-   Các file cấu hình BIND9 baseline
-   Firewall matrix và Wireshark filters
-   Mẫu báo cáo LAB 10

Nếu thiếu file trong gói phát bài, kiểm tra lại thư mục tải về hoặc hỏi
giảng viên; không tự tạo file thay thế bằng nội dung đoán.

## 6. Ảnh minh chứng cần có

-   Cấu hình interface và rule firewall pfSense.
-   Kali có IP thuộc ATTACKNET và không nối DNSLAB.
-   DNS baseline từ VICTIM.
-   Log `PENDING`, `HIT`, `CACHE`.
-   Wireshark capture theo filter của bài.
-   Kết quả truy vấn VICTIM sau mô phỏng.
-   BIND9 sau khôi phục và các kiểm tra hardening.
-   Bằng chứng Kali không được recursion sau khôi phục.

Mỗi ảnh cần chú thích VM, bước thực hiện và nội dung chứng minh. Dùng
ảnh chụp thật từ lần chạy của bạn, không dùng ảnh minh họa thay kết quả
thực tế.

## 7. Báo cáo

Dùng mẫu báo cáo LAB 10 đi kèm. Điền thông tin sinh viên/nhóm, cấu hình
mạng và firewall thực tế, kết quả từng bước, ảnh minh chứng, trả lời 12
câu hỏi phân tích và nhận xét về giới hạn mô phỏng/phòng thủ.

IP, log, thời gian, kết quả truy vấn và ảnh phải lấy từ lần chạy thật.
Nếu bước nào lỗi, ghi rõ lỗi và cách kiểm tra; không ghi kết quả giả
định như đã thực hiện thành công.

## 8. Checklist trước khi nộp

-   [ ] Các VM được gắn đúng mạng ảo.
-   [ ] Kali chỉ ở ATTACKNET, không ở DNSLAB.
-   [ ] Promiscuous Mode của Kali là **Deny**.
-   [ ] Đã thực hiện DNS baseline.
-   [ ] Có log và Wireshark của giai đoạn mô phỏng.
-   [ ] Có kết quả kiểm tra từ VICTIM.
-   [ ] Đã khôi phục BIND9 và kiểm tra hardening.
-   [ ] Kali không được recursion sau khôi phục.
-   [ ] Báo cáo đủ ảnh thật và câu trả lời.
-   [ ] Không có traffic thử nghiệm đi ra mạng thật/Internet.

## 9. Xử lý sự cố cơ bản

-   **Sai IP:** kiểm tra VMnet, DHCP VMware, IP tĩnh, subnet mask và
    interface pfSense.
-   **Không ping được gateway:** kiểm tra card mạng, interface
    assignment, trạng thái interface và firewall rule.
-   **VICTIM không phân giải DNS:** kiểm tra DNS client, dịch vụ
    DNS-SERVER và rule giữa các mạng.
-   **Kali thấy/được nối vào DNSLAB:** dừng lab, tắt VM và gỡ card mạng
    DNSLAB khỏi Kali.
-   **Không thấy Virtual Network Editor:** xác định phiên bản VMware
    Workstation/Player; không tự ý sửa VMnet khi chưa rõ ảnh hưởng.

------------------------------------------------------------------------

Nguyên tắc: làm theo tài liệu LAB 10 và ghi nhận kết quả thực tế. Các
VMnet nêu trên chỉ là gợi ý ánh xạ cho VMware.
