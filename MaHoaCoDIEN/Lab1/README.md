# LAB 1 - Mã hóa đối xứng cổ điển

Học phần An toàn và Bảo mật thông tin. Project Java Swing (code tay, không dùng GUI Builder), Maven, Microsoft SQL Server.

## Nội dung

| Phần | Mô tả |
|---|---|
| 1.1 - 1.6 | Sáu thuật toán: Caesar, Vigenère, Rail Fence, Playfair, Transposition, Autokey |
| Nâng cao 01 | Form tổng hợp chọn 6 thuật toán bằng combo box, lưu lịch sử vào SQL Server (`dbo.history`) |
| Nâng cao 02 | Phân tích mật mã: brute force Caesar, phân tích tần suất, known plaintext (Vigenère) |
| Menu | `frm_Menu` mở tất cả form ở trên |

## Yêu cầu

- JDK 17 hoặc 21
- IntelliJ IDEA (hoặc IDE bất kỳ hỗ trợ Maven)
- Kết nối Internet lần đầu để Maven tải `mssql-jdbc`
- SQL Server 2022 Express + SSMS (chỉ cần cho phần lịch sử, 6 thuật toán chạy được khi chưa có SQL)

## Cấu trúc project

```
LAB1_MaHoaCoDien
├─ pom.xml
└─ src/main/java/week_01
   ├─ Thuật toán / tiện ích
   │  ├─ CaesarCipher.java
   │  ├─ VigenereCipher.java
   │  ├─ RailFenceCipher.java
   │  ├─ PlayfairCipher.java
   │  ├─ TranspositionCipher.java
   │  ├─ AutokeyCipher.java
   │  ├─ CipherTongHop.java      (chọn thuật toán theo tên)
   │  ├─ AttackDemos.java        (3 kỹ thuật phân tích)
   │  ├─ FileTextIO.java         (Open / Save file .txt)
   │  ├─ DbConfig.java           (thông tin kết nối SQL)
   │  └─ HistoryDao.java         (ghi / đọc dbo.history)
   └─ Giao diện
      ├─ CipherFrame.java        (khung chung cho các form mã hóa)
      ├─ frm_Caesar.java
      ├─ frm_Vigenere.java
      ├─ frm_RailFence.java
      ├─ frm_Playfair.java
      ├─ frm_Transposition.java
      ├─ frm_Autokey.java
      ├─ frm_TongHop.java
      ├─ frm_KetNoiSQL.java
      ├─ frm_LichSu.java
      ├─ frm_TanCong.java
      └─ frm_Menu.java           (có hàm main)
```

## Cài đặt và chạy

1. Mở project trong IntelliJ, đợi Maven tải thư viện (hoặc bấm **Load Maven Changes** trong `pom.xml`).
2. Mở `src/main/java/week_01/frm_Menu.java`.
3. Bấm icon ▶ cạnh hàm `main` → **Run 'frm_Menu.main()'**.
4. Cửa sổ menu mở ra, bấm từng nút để mở form tương ứng. Đóng form con không làm tắt chương trình.

## Bộ dữ liệu kiểm thử

Kết quả phải trùng từng ký tự.

| Bài | Plaintext | Key | Ciphertext |
|---|---|---|---|
| 1.1 Caesar | HELLO | 3 | KHOOR |
| 1.2 Vigenère | HELLO | KEY | RIJVS |
| 1.3 Rail Fence | HELLO WORLD | 3 | HOREL OLLWD |
| 1.4 Playfair | HELLOWORLD | KEYWORD | GYIZSCOKCFBU |
| 1.5 Transposition | BAOMAT | 2,3,1 | AAOTBM |
| 1.6 Autokey | HELLO | KEY | RIJSS |

Lưu ý:

- Playfair giải mã ra `HELXLOWORLDX` (có chữ X đệm) là đúng thuật toán.
- Thử key sai (`abc`, `K3Y`, `1`, `KEY WORD`, `1,1,2`, `123`) phải hiện hộp thoại lỗi, chương trình không bị tắt.

Thí nghiệm ở form **Tấn công**:

| Nhập | Nút | Kết quả |
|---|---|---|
| `KHOOR` | Brute Force Caesar | k = 3, bản rõ HELLO |
| `PHHW PH DIWHU WKH WRJD SDUWB` | Frequency Analysis | H và W nhiều nhất (21,7%), tổng 23 chữ |
| HELLO / RIJVS | Known Plaintext (Vigenere) | `KEYKE` |

## Cấu hình SQL Server (làm một lần)

1. Cài SQL Server 2022 Express (Basic) và SSMS. Instance mặc định: `SQLEXPRESS`.
2. Mở `SQLServerManager16.msc` → SQL Server Network Configuration → Protocols for SQLEXPRESS:
   - TCP/IP → chuột phải **Enable**.
   - Nhấp đúp TCP/IP → tab IP Addresses → kéo xuống **IPAll**: xóa trống *TCP Dynamic Ports*, đặt *TCP Port* = `14333`.
   - SQL Server Services → SQL Server (SQLEXPRESS) → **Restart**.
3. Trong SSMS (Windows Authentication, tích Trust server certificate): chuột phải server → Properties → Security → chọn **SQL Server and Windows Authentication mode** → OK → chuột phải server → **Restart**.
4. Chạy hai script dưới đây (New Query → dán → F5).

**Script 1: tạo database và bảng**

```sql
IF DB_ID(N'LAB1_MaHoaCoDien') IS NULL
    CREATE DATABASE [LAB1_MaHoaCoDien];
GO
USE [LAB1_MaHoaCoDien];
GO
IF OBJECT_ID(N'dbo.history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.history (
        id          BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        created_at  DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
        algorithm   NVARCHAR(50) NOT NULL,
        operation   VARCHAR(10)  NOT NULL,
        input_text  NVARCHAR(MAX) NOT NULL,
        result_text NVARCHAR(MAX) NOT NULL,
        CONSTRAINT CK_lab1_operation CHECK (operation IN ('ENCRYPT','DECRYPT'))
    );
END;
GO
SELECT DB_NAME() AS ten_co_so_du_lieu, COUNT(*) AS so_ban_ghi FROM dbo.history;
GO
```

**Script 2: tạo tài khoản `lab1_user`**

```sql
USE [master];
GO
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = N'lab1_user')
BEGIN
    CREATE LOGIN [lab1_user]
        WITH PASSWORD = 'LabUser@2026',
             CHECK_POLICY = ON,
             CHECK_EXPIRATION = OFF;
END;
GO
USE [LAB1_MaHoaCoDien];
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'lab1_user')
    CREATE USER [lab1_user] FOR LOGIN [lab1_user];
GO
GRANT SELECT, INSERT, DELETE ON OBJECT::dbo.history TO [lab1_user];
GO
```

5. (Tùy chọn) Kiểm tra đăng nhập trong SSMS: Server name `127.0.0.1,14333` (dấu phẩy), SQL Server Authentication, login `lab1_user`, password `LabUser@2026`, tích Trust server certificate.

> Mật khẩu `LabUser@2026` chỉ dùng cho lab trên máy cá nhân. Chương trình không lưu mật khẩu vào mã nguồn hay tệp, người dùng gõ vào form kết nối mỗi lần chạy.

## Kiểm tra ghi và đọc lịch sử

1. Chạy `frm_Menu` → **Nâng cao 01 - Tổng hợp** → **Kết nối SQL Server**.
2. Giữ các ô mặc định (`127.0.0.1`, `14333`, `LAB1_MaHoaCoDien`, `lab1_user`), gõ password → **Test Connection**. Phải hiện `Kết nối thành công: Microsoft SQL Server 16...`.
3. Quay lại form Tổng hợp, Encrypt lần lượt 6 thuật toán. Trạng thái phải có "Đã lưu lịch sử vào SQL Server".
4. Bấm **Xem lịch sử**: bảng hiện 6 dòng, dòng mới nhất ở trên.
5. Kiểm tra lại trong SSMS:

```sql
USE [LAB1_MaHoaCoDien];
SELECT TOP (20) id, created_at, algorithm, operation, input_text, result_text
FROM dbo.history ORDER BY id DESC;
```

## Xử lý lỗi thường gặp

| Lỗi | Nguyên nhân / cách sửa |
|---|---|
| `No suitable driver found for jdbc:sqlserver...` | Maven chưa tải `mssql-jdbc`. Bấm Load Maven Changes, cần có Internet |
| `release version 17 not supported` | IDE đang dùng JDK cũ. Đổi Project SDK sang JDK 17/21 |
| `Login failed for user 'lab1_user'` | Chưa chạy script 2, sai mật khẩu, hoặc chưa bật Mixed Mode và restart SQL Server |
| Connection refused / timeout | Chưa đặt port 14333 hoặc chưa restart service SQL Server (SQLEXPRESS) |
| Class báo đỏ | File thiếu dòng `package week_01;`, hoặc chưa tạo đủ các class khác trong `week_01` |
| Dòng "Chưa kết nối SQL Server nên chưa lưu lịch sử" | Bình thường khi chưa bấm Test Connection. 6 thuật toán vẫn chạy đúng |

## Ghi chú

- Giao diện viết bằng Java Swing code tay, tên class, tên biến và package (`week_01`) giữ theo hướng dẫn gốc. Bố cục không giống hệt ảnh trong tài liệu nhưng đủ các ô và nút.
- Chỉ dùng phần phân tích mật mã (`AttackDemos`) cho dữ liệu học tập.
