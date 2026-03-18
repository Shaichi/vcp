# SWD392_VNeIDCivicPoint

## Giới thiệu

SWD392_VNeIDCivicPoint là một dự án Java Spring Boot quản lý điểm thưởng công dân, hỗ trợ các chức năng quản trị, phê duyệt quy tắc, và dashboard cho admin/citizen.

## Tính năng chính
- Quản lý quy tắc tính điểm (Scoring Rules)
- Quản lý công dân và hoạt động
- Hệ thống phê duyệt (Maker-Checker)
- Dashboard cho admin và citizen
- Quản lý phản hồi, thông báo

## Cấu trúc dự án
```
src/main/java/org/example/swd392_vneidcivicpoint/
    controller/   # Các controller REST/API
    service/      # Xử lý nghiệp vụ
    repository/   # Tương tác database
    entity/       # Định nghĩa entity JPA
    dto/          # Data Transfer Object
    constants/    # Các hằng số
    exception/    # Xử lý ngoại lệ
    util/         # Tiện ích
src/main/resources/
    application.properties  # Cấu hình ứng dụng
    templates/              # Giao diện Thymeleaf
```

## Cài đặt & Chạy thử
1. **Yêu cầu:**
   - Java 17 trở lên
   - Maven 3.6+
   - MS SQL (hoặc cấu hình lại DB trong `application.properties`)

2. **Clone dự án:**
```bash
git clone https://github.com/Shaichi/vcp.git
cd vcp
```

3. **Cấu hình database:**
- Sửa file `src/main/resources/application.properties` cho phù hợp với DB của bạn.

4. **Khởi tạo database:**
- Chạy script `init_database.sql` để tạo bảng và dữ liệu mẫu.

5. **Build & chạy ứng dụng:**
```bash
./mvnw spring-boot:run
```

6. **Truy cập:**
- Admin: http://localhost:8080/admin/login
- Citizen: http://localhost:8080/login

## Đóng góp
- Fork, tạo branch mới, commit và gửi pull request.

## License
MIT

