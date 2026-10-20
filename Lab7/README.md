# LAB 7 -- Web Protection với pfSense + Web Proxy

## 1. Mục tiêu

Xây dựng môi trường thực hành chính sách bảo vệ truy cập web bằng
pfSense, máy chủ Web Proxy và các máy khách trong mạng nội bộ.

Các thành phần dự kiến: - **pfSense**: tường lửa, định tuyến và kiểm
soát lưu lượng giữa các vùng mạng. - **DC01 -- Windows Server 2025**: dự
kiến dùng làm máy chủ DNS cho lab. - **Client-A và Client-B**: máy khách
kiểm tra chính sách web. - **DMZ-WEB**: máy chủ web thử nghiệm. -
**WEB-PROXY -- Ubuntu Server 24.04**: chạy E2Guardian, Squid và ClamAV.

> Trạng thái hiện tại: VMware Workstation đã có các máy ảo pfSense,
> Windows Server 2025 và Ubuntu 64-bit. Chưa xác nhận rằng toàn bộ các
> máy còn lại đã được tạo và cấu hình hoàn chỉnh.

## 2. Sơ đồ địa chỉ IP dự kiến

  -----------------------------------------------------------------------
  Thành phần              Địa chỉ IP              Ghi chú
  ----------------------- ----------------------- -----------------------
  pfSense WAN             DHCP, hiện đã thấy      Không đổi nếu vẫn cần
                          `192.168.123.203/24`    kết nối mạng bên ngoài

  pfSense LAN             `10.6.0.1/24`           Gateway của mạng LAN

  DC01                    `10.6.0.10/24`          DNS: `10.6.0.10`,
                                                  gateway: `10.6.0.1`

  Client-A                `10.6.0.21/24`          Gateway: `10.6.0.1`,
                                                  DNS: `10.6.0.10`

  Client-B                `10.6.0.22/24`          Gateway: `10.6.0.1`,
                                                  DNS: `10.6.0.10`

  WEB-PROXY               `10.6.0.30/24`          Gateway: `10.6.0.1`,
                                                  DNS: `10.6.0.10`

  pfSense DMZ (OPT1)      `172.16.6.1/24`         Gateway cho vùng DMZ

  DMZ-WEB                 `172.16.6.10/24`        Gateway: `172.16.6.1`
  -----------------------------------------------------------------------

**Quan trọng:** Đây là sơ đồ dự kiến theo tài liệu LAB 7. Hãy kiểm tra
lại màn hình console pfSense để xác nhận LAN là `10.6.0.1/24` và
DMZ/OPT1 là `172.16.6.1/24` trước khi tiếp tục. Không nên coi các địa
chỉ là đã được xác minh chỉ dựa trên ghi chú này.

## 3. Cấu hình mạng VMware đã thực hiện

Theo các ảnh cấu hình trong quá trình chuẩn bị:

-   `VMnet0`: Bridged, dùng cho WAN của pfSense.
-   `VMnet1`: Host-only, subnet hiện thấy là `10.0.0.0`; cần xem xét lại
    để tránh xung đột với LAN lab.
-   `VMnet2`: Host-only, subnet `172.16.6.0`, subnet mask
    `255.255.255.0`, DHCP VMware đã tắt.
-   Card mạng thứ ba của pfSense đã được chuyển từ LAN Segment `dmz-`
    sang `Custom: VMnet2`.

Không thay đổi VMnet0 hoặc VMnet1 nếu chưa xác định rõ vai trò và ảnh
hưởng. Cần đảm bảo card mạng của từng máy ảo được nối đúng VMnet trước
khi cấu hình IP tĩnh.

## 4. Thành phần Web Proxy

Máy WEB-PROXY dự kiến sử dụng Ubuntu Server 24.04 LTS với: - E2Guardian:
`10.6.0.30:8080` -- proxy mà trình duyệt máy khách sẽ kết nối tới. -
Squid: `127.0.0.1:3128` -- proxy upstream cục bộ. - ClamAV: quét tệp tải
xuống theo cấu hình lab.

Không cài Squid/E2Guardian trực tiếp trên pfSense; theo tài liệu LAB 7,
các dịch vụ này chạy trên máy Ubuntu riêng.

## 5. Các website thử nghiệm và chính sách dự kiến

  -----------------------------------------------------------------------
  Hostname                            Chính sách
  ----------------------------------- -----------------------------------
  `normal.lab7.test`                  Cho phép

  `china.lab7.test`                   Chặn cứng

  `mideast.lab7.test`                 Chặn cứng

  `jobs.lab7.test`                    Chặn cứng

  `weapons.lab7.test`                 Cảnh báo, có thể bỏ qua tạm thời

  `politics.lab7.test`                Cảnh báo, có thể bỏ qua tạm thời

  `religion.lab7.test`                Cảnh báo, có thể bỏ qua tạm thời

  `education.lab7.test`               Cho phép

  `entertainment.lab7.test`           Áp dụng lịch

  `games.lab7.test`                   Áp dụng lịch

  `streaming.lab7.test`               Cảnh báo, có thể bỏ qua tạm thời

  `blockedgame.lab7.test`             Chặn cứng

  `download.lab7.test`                Cho phép, có kiểm soát
                                      antivirus/kích thước tệp
  -----------------------------------------------------------------------

Các bản ghi DNS của những hostname trên dự kiến trỏ về `172.16.6.10`
trên DC01.

## 6. Luồng truy cập dự kiến

1.  Client-A/Client-B dùng proxy thủ công `10.6.0.30`, cổng `8080`.
2.  E2Guardian xử lý chính sách web.
3.  Squid chuyển tiếp yêu cầu ra ngoài hoặc tới máy chủ web lab.
4.  pfSense chặn máy khách truy cập web trực tiếp qua cổng 80/443 để
    tránh bỏ qua proxy.
5.  pfSense cho phép WEB-PROXY truy cập web theo các luật cần thiết.

## 7. Các bước tiếp theo

-   [ ] Xác nhận địa chỉ LAN và DMZ trên console pfSense.
-   [ ] Kiểm tra cấu hình Network Adapter của Windows Server 2025.
-   [ ] Nối Windows Server vào mạng LAN lab và cấu hình IP tĩnh.
-   [ ] Cài đặt/cấu hình DNS trên Windows Server.
-   [ ] Chuẩn bị Client-A và Client-B.
-   [ ] Chuẩn bị máy DMZ-WEB và thiết lập các website thử nghiệm.
-   [ ] Chuẩn bị WEB-PROXY trên Ubuntu Server 24.04.
-   [ ] Cấu hình các alias và firewall rules trên pfSense.
-   [ ] Cấu hình proxy trên trình duyệt máy khách.
-   [ ] Kiểm thử các chính sách và chụp ảnh làm báo cáo.

## 8. Lưu ý an toàn và khắc phục sự cố

-   Đây là môi trường lab cô lập; không áp dụng cấu hình chặn hoặc SSL
    inspection cho lưu lượng cá nhân/thực tế.
-   Không bật HTTPS inspection nếu chưa hiểu rõ chứng chỉ CA và chỉ sử
    dụng chứng chỉ lab trên máy khách lab.
-   Trước khi sửa luật firewall, giữ quyền truy cập quản trị pfSense và
    ghi lại cấu hình đang hoạt động.
-   Nếu máy khách không truy cập được hostname, kiểm tra lần lượt:
    IP/gateway, DNS, kết nối giữa các VMnet, luật firewall, proxy và log
    dịch vụ.
-   Log dự kiến:
    -   E2Guardian: `/var/log/e2guardian/access.log`
    -   Squid: `/var/log/squid/access.log`
-   Tệp EICAR là tệp kiểm thử antivirus chuẩn, không phải malware thật.

## 9. Trạng thái ghi chú

README này ghi lại kế hoạch và các thiết lập đã được mô tả trong quá
trình chuẩn bị. Những mục chưa được đánh dấu hoàn thành cần được kiểm
tra thực tế trong VMware/pfSense trước khi chuyển sang bước tiếp theo.
