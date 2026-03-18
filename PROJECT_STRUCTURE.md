# PROJECT STRUCTURE – VNeID Civic Point (VCP) Web App

**Architecture:** Client – Server | **Pattern:** MVC  
**Backend:** Spring Boot · Java 17 · Spring MVC + Thymeleaf · Spring Security · Spring Data JPA · MS SQL Server · Lombok  
**Base Package:** `org.example.swd392_vneidcivicpoint`  
**Demo Access:** `http://localhost:8080`

---

## 1. MVC Mapping (Web App)

```
┌──────────────────────────────────┐
│  M – MODEL                       │
│  entity/     → JPA Entities      │
│  dto/        → Form/View models  │
└──────────────┬───────────────────┘
               │ Model & ModelAttribute
┌──────────────▼───────────────────┐
│  V – VIEW                        │
│  templates/  → Thymeleaf HTML    │
│  static/     → CSS, JS, images   │
└──────────────┬───────────────────┘
               │ View name (String)
┌──────────────▼───────────────────┐
│  C – CONTROLLER                  │
│  controller/ → @Controller       │
│               returns view name  │
└──────────────────────────────────┘
        + Service (business logic)
        + Repository (JPA data access)
```

---

## 2. Folder Structure

```
SWD392_VNeIDCivicPoint/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/org/example/swd392_vneidcivicpoint/
    │   │   ├── Swd392VNeIdCivicPointApplication.java
    │   │   │
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java          ← Form login, session, roles
    │   │   │   └── SchedulerConfig.java         ← @EnableScheduling
    │   │   │
    │   │   ├── constants/
    │   │   │   ├── AccountStatus.java           ← ACTIVE, SUSPENDED, REVOKED
    │   │   │   ├── RankType.java                ← ACTIVE, BASIC, UNRANKED
    │   │   │   ├── TransactionType.java
    │   │   │   ├── IngestionStatus.java
    │   │   │   ├── FeedbackStatus.java
    │   │   │   ├── NotificationType.java
    │   │   │   ├── ActivityCategory.java
    │   │   │   └── AdminRole.java
    │   │   │
    │   │   ├── entity/                          ← M: JPA Entities
    │   │   │   ├── Citizen.java
    │   │   │   ├── ScoringRule.java
    │   │   │   ├── RankTier.java
    │   │   │   ├── IncentivePolicy.java
    │   │   │   ├── ActivityLog.java
    │   │   │   ├── PointLedger.java
    │   │   │   ├── RankHistory.java
    │   │   │   ├── YearEndProcessing.java
    │   │   │   ├── VulnerableBonus.java
    │   │   │   ├── SuspensionRecord.java
    │   │   │   ├── Notification.java
    │   │   │   ├── Feedback.java
    │   │   │   ├── ExportBatch.java
    │   │   │   ├── IncentiveHistory.java
    │   │   │   └── AdminUser.java
    │   │   │
    │   │   ├── repository/                      ← Data Access (JPA)
    │   │   │   ├── CitizenRepository.java
    │   │   │   ├── ScoringRuleRepository.java
    │   │   │   ├── RankTierRepository.java
    │   │   │   ├── IncentivePolicyRepository.java
    │   │   │   ├── ActivityLogRepository.java
    │   │   │   ├── PointLedgerRepository.java
    │   │   │   ├── RankHistoryRepository.java
    │   │   │   ├── YearEndProcessingRepository.java
    │   │   │   ├── VulnerableBonusRepository.java
    │   │   │   ├── SuspensionRecordRepository.java
    │   │   │   ├── NotificationRepository.java
    │   │   │   ├── FeedbackRepository.java
    │   │   │   ├── ExportBatchRepository.java
    │   │   │   ├── IncentiveHistoryRepository.java
    │   │   │   └── AdminUserRepository.java
    │   │   │
    │   │   ├── service/                         ← Business Logic
    │   │   │   ├── AuthService.java             ← UC-01: load user for Spring Security
    │   │   │   ├── PointInquiryService.java     ← UC-02
    │   │   │   ├── NotificationService.java     ← UC-03
    │   │   │   ├── FeedbackService.java         ← UC-04, UC-07a
    │   │   │   ├── ScoringRuleService.java      ← UC-05
    │   │   │   ├── IncentiveConfigService.java  ← UC-06
    │   │   │   ├── AdminReportService.java      ← UC-07b
    │   │   │   ├── ingestion/
    │   │   │   │   ├── ActivityIngestionService.java  ← UC-08~15 (shared logic)
    │   │   │   │   └── IngestionValidationService.java
    │   │   │   ├── PointCalculationService.java ← UC-16 (core engine)
    │   │   │   ├── RankEvaluationService.java   ← UC-17
    │   │   │   ├── YearEndProcessingService.java← UC-18
    │   │   │   ├── VulnerableBonusService.java  ← UC-19
    │   │   │   ├── SuspensionService.java       ← UC-20
    │   │   │   ├── IncentiveTierService.java    ← UC-21
    │   │   │   └── ExportImportService.java     ← UC-22
    │   │   │
    │   │   ├── controller/                      ← C: @Controller (returns view names)
    │   │   │   ├── AuthController.java          ← GET/POST /login, /logout
    │   │   │   ├── CitizenController.java       ← /citizen/**
    │   │   │   ├── AdminController.java         ← /admin/**
    │   │   │   └── IngestionController.java     ← /api/ingestion/** (webhook, JSON)
    │   │   │
    │   │   ├── dto/                             ← M: Form models & view models
    │   │   │   ├── form/                        ← Bound to HTML <form>
    │   │   │   │   ├── FeedbackForm.java
    │   │   │   │   ├── ScoringRuleForm.java
    │   │   │   │   ├── IncentivePolicyForm.java
    │   │   │   │   └── FeedbackResolveForm.java
    │   │   │   └── view/                        ← Passed to Model for Thymeleaf
    │   │   │       ├── PointDashboardView.java
    │   │   │       ├── PointHistoryView.java
    │   │   │       ├── NotificationView.java
    │   │   │       ├── FeedbackView.java
    │   │   │       ├── ScoringRuleView.java
    │   │   │       ├── IncentiveEligibilityView.java
    │   │   │       └── AdminReportView.java
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── CitizenNotFoundException.java
    │   │   │   ├── AccountSuspendedException.java
    │   │   │   ├── DuplicateActivityException.java
    │   │   │   └── GlobalExceptionHandler.java  ← @ControllerAdvice
    │   │   │
    │   │   ├── scheduler/
    │   │   │   ├── YearEndScheduler.java        ← Dec 31 23:59 (UC-18)
    │   │   │   ├── AutoResumeScheduler.java     ← Daily 01:00 (UC-20)
    │   │   │   └── VulnerableBonusScheduler.java← Monthly 1st (UC-19)
    │   │   │
    │   │   └── util/
    │   │       ├── FiscalYearUtil.java
    │   │       ├── AgeCalculator.java
    │   │       └── TrackingIdGenerator.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       │
    │       ├── templates/                       ← V: Thymeleaf HTML (View layer)
    │       │   ├── layout/
    │       │   │   ├── base.html                ← Shared layout (navbar, footer)
    │       │   │   ├── citizen-layout.html
    │       │   │   └── admin-layout.html
    │       │   │
    │       │   ├── auth/
    │       │   │   └── login.html               ← /login
    │       │   │
    │       │   ├── citizen/
    │       │   │   ├── dashboard.html           ← /citizen/dashboard  (UC-02)
    │       │   │   ├── point-history.html       ← /citizen/points     (UC-02)
    │       │   │   ├── notifications.html       ← /citizen/notifications (UC-03)
    │       │   │   ├── feedback-form.html       ← /citizen/feedback/new  (UC-04)
    │       │   │   ├── feedback-status.html     ← /citizen/feedback/{id} (UC-04)
    │       │   │   └── incentives.html          ← /citizen/incentives    (UC-21)
    │       │   │
    │       │   ├── admin/
    │       │   │   ├── dashboard.html           ← /admin/dashboard
    │       │   │   ├── scoring-rules.html       ← /admin/scoring-rules   (UC-05)
    │       │   │   ├── scoring-rule-form.html   ← /admin/scoring-rules/new
    │       │   │   ├── incentive-policies.html  ← /admin/incentives      (UC-06)
    │       │   │   ├── feedback-list.html       ← /admin/feedback        (UC-07a)
    │       │   │   ├── feedback-detail.html     ← /admin/feedback/{id}
    │       │   │   ├── reports.html             ← /admin/reports         (UC-07b)
    │       │   │   └── export.html             ← /admin/export          (UC-22)
    │       │   │
    │       │   └── error/
    │       │       ├── 403.html
    │       │       ├── 404.html
    │       │       └── 500.html
    │       │
    │       └── static/                          ← V: Static assets
    │           ├── css/
    │           │   ├── style.css                ← Global styles
    │           │   ├── citizen.css
    │           │   └── admin.css
    │           ├── js/
    │           │   ├── citizen.js
    │           │   └── admin.js
    │           └── images/
    │               └── vneid-logo.png
    │
    └── test/
        └── java/org/example/swd392_vneidcivicpoint/
            ├── service/
            │   ├── PointCalculationServiceTest.java
            │   └── RankEvaluationServiceTest.java
            └── controller/
                └── CitizenControllerTest.java
```

---

## 3. Page Map (URL → Template → UC)

| URL | Template | Role | UC |
|-----|----------|------|----|
| `GET /login` | `auth/login.html` | Public | UC-01 |
| `POST /login` | redirect | Public | UC-01 |
| `GET /citizen/dashboard` | `citizen/dashboard.html` | CITIZEN | UC-02 |
| `GET /citizen/points` | `citizen/point-history.html` | CITIZEN | UC-02 |
| `GET /citizen/notifications` | `citizen/notifications.html` | CITIZEN | UC-03 |
| `GET /citizen/feedback/new` | `citizen/feedback-form.html` | CITIZEN | UC-04 |
| `POST /citizen/feedback` | redirect `/citizen/feedback/{id}` | CITIZEN | UC-04 |
| `GET /citizen/feedback/{id}` | `citizen/feedback-status.html` | CITIZEN | UC-04 |
| `GET /citizen/incentives` | `citizen/incentives.html` | CITIZEN | UC-21 |
| `GET /admin/dashboard` | `admin/dashboard.html` | ADMIN | – |
| `GET /admin/scoring-rules` | `admin/scoring-rules.html` | ADMIN | UC-05 |
| `GET /admin/scoring-rules/new` | `admin/scoring-rule-form.html` | ADMIN | UC-05 |
| `POST /admin/scoring-rules` | redirect | ADMIN | UC-05 |
| `POST /admin/scoring-rules/{id}/approve` | redirect | ADMIN | UC-05 |
| `GET /admin/incentives` | `admin/incentive-policies.html` | ADMIN | UC-06 |
| `GET /admin/feedback` | `admin/feedback-list.html` | ADMIN | UC-07a |
| `GET /admin/feedback/{id}` | `admin/feedback-detail.html` | ADMIN | UC-07a |
| `POST /admin/feedback/{id}/resolve` | redirect | ADMIN | UC-07a |
| `GET /admin/reports` | `admin/reports.html` | ADMIN | UC-07b |
| `GET /admin/export` | `admin/export.html` | ADMIN | UC-22 |
| `POST /admin/export/trigger` | redirect | ADMIN | UC-22 |
| `POST /api/ingestion/**` | JSON (webhook) | SYSTEM | UC-08~15 |

---

## 4. Controller Code (True MVC – returns view name)

### 4.1. AuthController.java

```java
@Controller
public class AuthController {

    // GET /login → show login page
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";   // → templates/auth/login.html
    }
}
// Spring Security handles POST /login automatically via SecurityConfig
```

### 4.2. CitizenController.java

```java
@Controller
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class CitizenController {

    private final PointInquiryService pointInquiryService;
    private final NotificationService notificationService;
    private final FeedbackService feedbackService;
    private final IncentiveTierService incentiveTierService;

    // UC-02: Dashboard – points + rank
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        PointDashboardView dashboard = pointInquiryService.getDashboard(userDetails.getUsername());
        model.addAttribute("dashboard", dashboard);
        return "citizen/dashboard";   // → templates/citizen/dashboard.html
    }

    // UC-02: Point history
    @GetMapping("/points")
    public String pointHistory(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        List<PointHistoryView> history = pointInquiryService.getHistory(userDetails.getUsername(), page);
        model.addAttribute("history", history);
        model.addAttribute("page", page);
        return "citizen/point-history";
    }

    // UC-03: Notifications
    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<NotificationView> notifications = notificationService.getForCitizen(userDetails.getUsername());
        model.addAttribute("notifications", notifications);
        return "citizen/notifications";
    }

    // UC-04: Feedback form
    @GetMapping("/feedback/new")
    public String feedbackForm(Model model) {
        model.addAttribute("feedbackForm", new FeedbackForm());
        return "citizen/feedback-form";
    }

    // UC-04: Submit feedback
    @PostMapping("/feedback")
    public String submitFeedback(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute FeedbackForm form,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) return "citizen/feedback-form";
        String trackingId = feedbackService.submit(userDetails.getUsername(), form);
        return "redirect:/citizen/feedback/" + trackingId;
    }

    // UC-04: Feedback status
    @GetMapping("/feedback/{trackingId}")
    public String feedbackStatus(@PathVariable String trackingId, Model model) {
        FeedbackView feedback = feedbackService.getByTrackingId(trackingId);
        model.addAttribute("feedback", feedback);
        return "citizen/feedback-status";
    }

    // UC-21: Incentive eligibility
    @GetMapping("/incentives")
    public String incentives(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        IncentiveEligibilityView eligibility = incentiveTierService.getEligibility(userDetails.getUsername());
        model.addAttribute("eligibility", eligibility);
        return "citizen/incentives";
    }
}
```

### 4.3. AdminController.java

```java
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ScoringRuleService scoringRuleService;
    private final IncentiveConfigService incentiveConfigService;
    private final FeedbackService feedbackService;
    private final AdminReportService reportService;
    private final ExportImportService exportImportService;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", reportService.getSummaryStats());
        return "admin/dashboard";
    }

    // UC-05: Scoring rules list
    @GetMapping("/scoring-rules")
    public String scoringRules(Model model) {
        model.addAttribute("rules", scoringRuleService.findAll());
        return "admin/scoring-rules";
    }

    // UC-05: New rule form
    @GetMapping("/scoring-rules/new")
    public String newRuleForm(Model model) {
        model.addAttribute("ruleForm", new ScoringRuleForm());
        return "admin/scoring-rule-form";
    }

    // UC-05: Save rule (Maker)
    @PostMapping("/scoring-rules")
    public String saveRule(@AuthenticationPrincipal UserDetails admin,
                           @Valid @ModelAttribute ScoringRuleForm ruleForm,
                           BindingResult result, Model model) {
        if (result.hasErrors()) return "admin/scoring-rule-form";
        scoringRuleService.create(admin.getUsername(), ruleForm);
        return "redirect:/admin/scoring-rules";
    }

    // UC-05: Approve rule (Checker)
    @PostMapping("/scoring-rules/{id}/approve")
    public String approveRule(@AuthenticationPrincipal UserDetails admin,
                              @PathVariable Long id, RedirectAttributes attrs) {
        scoringRuleService.approve(admin.getUsername(), id);
        attrs.addFlashAttribute("success", "Quy tắc đã được phê duyệt!");
        return "redirect:/admin/scoring-rules";
    }

    // UC-07a: Feedback list
    @GetMapping("/feedback")
    public String feedbackList(Model model) {
        model.addAttribute("feedbacks", feedbackService.findPending());
        return "admin/feedback-list";
    }

    // UC-07a: Feedback detail
    @GetMapping("/feedback/{id}")
    public String feedbackDetail(@PathVariable Long id, Model model) {
        model.addAttribute("feedback", feedbackService.findById(id));
        model.addAttribute("resolveForm", new FeedbackResolveForm());
        return "admin/feedback-detail";
    }

    // UC-07a: Resolve feedback
    @PostMapping("/feedback/{id}/resolve")
    public String resolveFeedback(@AuthenticationPrincipal UserDetails admin,
                                  @PathVariable Long id,
                                  @Valid @ModelAttribute FeedbackResolveForm form,
                                  BindingResult result) {
        if (result.hasErrors()) return "admin/feedback-detail";
        feedbackService.resolve(admin.getUsername(), id, form);
        return "redirect:/admin/feedback";
    }

    // UC-07b: Reports
    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) Integer year, Model model) {
        int fiscalYear = (year != null) ? year : LocalDate.now().getYear();
        model.addAttribute("report", reportService.getReport(fiscalYear));
        model.addAttribute("selectedYear", fiscalYear);
        return "admin/reports";
    }

    // UC-22: Export page
    @GetMapping("/export")
    public String exportPage(Model model) {
        model.addAttribute("batches", exportImportService.findAllBatches());
        return "admin/export";
    }

    // UC-22: Trigger export
    @PostMapping("/export/trigger")
    public String triggerExport(RedirectAttributes attrs) {
        exportImportService.triggerExport();
        attrs.addFlashAttribute("success", "Xuất dữ liệu thành công!");
        return "redirect:/admin/export";
    }
}
```

### 4.4. IngestionController.java (Webhook – JSON, không render View)

```java
// Chỉ endpoint này giữ @RestController vì nhận webhook từ hệ thống ngoài (JSON)
@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final ActivityIngestionService ingestionService;

    @PostMapping("/profile-completion")    // UC-08: VNeID Core
    public ResponseEntity<Map<String, String>> profileCompletion(@RequestBody @Valid ProfileCompletionRequest req) {
        ingestionService.ingest("PROFILE_COMPLETE", "VNEID_CORE", req.getCccdNumber(),
                req.getExternalReference(), null, req.getActivityDate());
        return ResponseEntity.ok(Map.of("status", "ACCEPTED"));
    }

    @PostMapping("/public-service")        // UC-11: DVC Portal
    public ResponseEntity<Map<String, String>> publicService(@RequestBody @Valid PublicServiceRequest req) {
        ingestionService.ingest("PUBLIC_SERVICE", "DVC_PORTAL", req.getCccdNumber(),
                req.getExternalReference(), req.getPortalId(), req.getCompletedAt());
        return ResponseEntity.ok(Map.of("status", "ACCEPTED"));
    }

    @PostMapping("/financial-transaction")  // UC-12: Financial Partner
    public ResponseEntity<Map<String, String>> financialTransaction(@RequestBody @Valid FinancialTransactionRequest req) {
        ingestionService.ingest("FINANCIAL_TXN", "FINANCIAL_PARTNER", req.getCccdNumber(),
                req.getTransactionId(), req.getPartnerId(), req.getTransactionDate());
        return ResponseEntity.ok(Map.of("status", "ACCEPTED"));
    }
    // ... (UC-09, UC-10, UC-13, UC-14, UC-15 tương tự)
}
```

---

## 5. Thymeleaf Template Mẫu

### 5.1. layout/base.html (Shared layout)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8"/>
    <title th:text="${pageTitle} + ' | VNeID Civic Point'">VNeID Civic Point</title>
    <link rel="stylesheet" th:href="@{/css/style.css}"/>
</head>
<body>
<nav>
    <span>VNeID Civic Point</span>
    <span sec:authentication="name">User</span>
    <a th:href="@{/logout}">Đăng xuất</a>
</nav>
<main th:fragment="content">
    <!-- overridden by each page -->
</main>
</body>
</html>
```

### 5.2. citizen/dashboard.html (UC-02)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Dashboard</title></head>
<body>
<h1>Chào, <span th:text="${dashboard.fullName}">Nguyễn Văn A</span></h1>

<div class="card">
    <h2>Điểm tích lũy</h2>
    <p class="point-value" th:text="${dashboard.totalPoints}">0</p>
    <p>Hạng hiện tại:
        <strong th:text="${dashboard.currentRank}">UNRANKED</strong>
    </p>
    <p>Điểm năm nay: <span th:text="${dashboard.fiscalYearPoints}">0</span></p>
</div>

<div class="card" th:if="${dashboard.incentiveEligible}">
    <h3>🎉 Bạn đủ điều kiện giảm thuế!</h3>
    <p th:text="${dashboard.incentiveMessage}">...</p>
    <a th:href="@{/citizen/incentives}">Xem chi tiết</a>
</div>

<a th:href="@{/citizen/points}">Xem lịch sử điểm</a>
<a th:href="@{/citizen/notifications}">Thông báo</a>
</body>
</html>
```

### 5.3. auth/login.html (UC-01)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Đăng nhập – VNeID Civic Point</title></head>
<body>
<h1>VNeID Civic Point</h1>

<div th:if="${param.error}" class="alert-error">
    Sai CCCD hoặc mật khẩu!
</div>
<div th:if="${param.logout}" class="alert-info">
    Đã đăng xuất.
</div>

<form method="post" th:action="@{/login}">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <label>Số CCCD / Tài khoản Admin</label>
    <input type="text" name="username" required/>
    <label>Mật khẩu</label>
    <input type="password" name="password" required/>
    <button type="submit">Đăng nhập</button>
</form>
</body>
</html>
```

### 5.4. admin/scoring-rules.html (UC-05)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Quy tắc tính điểm</title></head>
<body>
<h1>Quy tắc tính điểm</h1>
<a th:href="@{/admin/scoring-rules/new}">+ Thêm quy tắc mới</a>

<table>
    <tr>
        <th>Mã hoạt động</th><th>Danh mục</th>
        <th>Điểm</th><th>Giới hạn/năm</th>
        <th>Trạng thái</th><th>Phê duyệt</th><th>Hành động</th>
    </tr>
    <tr th:each="rule : ${rules}">
        <td th:text="${rule.activityCode}">-</td>
        <td th:text="${rule.activityCategory}">-</td>
        <td th:text="${rule.pointValue}">0</td>
        <td th:text="${rule.frequencyLimit != null ? rule.frequencyLimit : '∞'}">∞</td>
        <td th:text="${rule.status}">ACTIVE</td>
        <td th:text="${rule.approvalStatus}">PENDING</td>
        <td>
            <form method="post"
                  th:action="@{/admin/scoring-rules/{id}/approve(id=${rule.ruleId})}"
                  th:if="${rule.approvalStatus == 'PENDING'}">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                <button type="submit">Phê duyệt</button>
            </form>
        </td>
    </tr>
</table>
</body>
</html>
```

---

## 6. SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthService authService;   // implements UserDetailsService

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/ingestion/**").hasRole("SYSTEM")   // webhook
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/citizen/**").hasRole("CITIZEN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/citizen/dashboard", false)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> **Lưu ý:** `AuthService` implement `UserDetailsService`, load user từ `CITIZEN` hoặc `ADMIN_USER` table tùy theo `username` (CCCD → CITIZEN role, username admin → ADMIN role).

---

## 7. application.properties

```properties
# MS SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=vcp_db;encrypt=true;trustServerCertificate=true
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.encoding=UTF-8

# Server
server.port=8080

# Session
server.servlet.session.timeout=30m
```

---

## 8. Luồng MVC (UC-02: Xem điểm)

```
Browser (Citizen)
  │  GET /citizen/dashboard
  ▼
CitizenController                        [C – Controller]
  │  @GetMapping("/dashboard")
  │  Gọi pointInquiryService.getDashboard(cccd)
  ▼
PointInquiryService                      [Service – Business Logic]
  │  Gọi citizenRepository.findByCccdNumber()
  │  Gọi pointLedgerRepository.sumPoints()
  │  Build PointDashboardView
  ▼
CitizenRepository / PointLedgerRepository [Repository – Data Access]
  │  SELECT từ CITIZEN, POINT_LEDGER (MS SQL)
  ▼
PointDashboardView                         [M – Model]
  │  model.addAttribute("dashboard", ...)
  ▼
Thymeleaf: citizen/dashboard.html          [V – View]
  │  th:text="${dashboard.totalPoints}"
  ▼
HTML response → Browser
```

---

## 9. Summary

| Layer | Thành phần | Công nghệ |
|-------|-----------|-----------|
| **M (Model)** | `entity/` + `dto/view/` + `dto/form/` | JPA, Lombok |
| **V (View)** | `templates/*.html` + `static/` | Thymeleaf, HTML/CSS/JS |
| **C (Controller)** | `controller/@Controller` | Spring MVC |
| **Service** | `service/` | Spring @Service, @Transactional |
| **Repository** | `repository/` | Spring Data JPA |
| **Security** | `SecurityConfig` | Spring Security (form login, session) |
| **Scheduler** | `scheduler/` | Spring @Scheduled |
