import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:provider/provider.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (context) => AuthProvider(),
      child: const MyApp(),
    ),
  );
}

class AuthProvider extends ChangeNotifier {
  String? _username;
  List<String> _roles = [];
  bool _isLoggedIn = false;

  String? get username => _username;
  bool get isLoggedIn => _isLoggedIn;
  bool get isAdmin => _roles.contains('ROLE_ADMIN');

  void login(String user, List<String> roles) {
    _username = user;
    _roles = roles;
    _isLoggedIn = true;
    notifyListeners();
  }

  void logout() {
    _username = null;
    _roles = [];
    _isLoggedIn = false;
    notifyListeners();
  }
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'VNeID Civic Point',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: Consumer<AuthProvider>(
        builder: (context, auth, child) {
          return auth.isLoggedIn ? const DashboardScreen() : const LoginScreen();
        },
      ),
    );
  }
}

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;

  // Sử dụng IP máy tính của bạn: 192.168.0.110
  final String baseUrl = "http://192.168.0.110:8080/api/v1";

  Future<void> _handleLogin() async {
    setState(() => _isLoading = true);
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'username': _usernameController.text,
          'password': _passwordController.text,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        if (data['status'] == 'SUCCESS') {
          if (mounted) {
            Provider.of<AuthProvider>(context, listen: false)
                .login(data['username'], List<String>.from(data['roles']));
          }
        } else {
          _showError(data['message']);
        }
      } else {
        _showError("Sai tên đăng nhập hoặc mật khẩu");
      }
    } catch (e) {
      _showError("Không thể kết nối tới server (IP: 192.168.0.110): $e");
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.red),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("VNeID Civic Point - Login")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              controller: _usernameController,
              decoration: const InputDecoration(labelText: "Username (CCCD)"),
            ),
            TextField(
              controller: _passwordController,
              decoration: const InputDecoration(labelText: "Password"),
              obscureText: true,
            ),
            const SizedBox(height: 24),
            _isLoading
                ? const CircularProgressIndicator()
                : ElevatedButton(
                    onPressed: _handleLogin,
                    style: ElevatedButton.styleFrom(minimumSize: const Size(double.infinity, 50)),
                    child: const Text("Đăng nhập"),
                  ),
          ],
        ),
      ),
    );
  }
}

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  Map<String, dynamic>? _data;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchDashboardData();
  }

  Future<void> _fetchDashboardData() async {
    final auth = Provider.of<AuthProvider>(context, listen: false);
    final String cccd = auth.username ?? "";
    
    try {
      // Sử dụng IP máy tính của bạn: 192.168.0.110
      // Gửi kèm cccd trong query để chắc chắn lấy được dữ liệu
      final response = await http.get(
        Uri.parse("http://192.168.0.110:8080/api/v1/dashboard-data?cccd=$cccd"),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      );
      debugPrint("Dashboard Response Status: ${response.statusCode}");
      debugPrint("Dashboard Response Body: ${response.body}");
      
      if (response.statusCode == 200) {
        setState(() {
          _data = jsonDecode(response.body);
          _isLoading = false;
        });
      } else {
        setState(() {
          _isLoading = false;
        });
        debugPrint("Failed to load dashboard: ${response.statusCode}");
      }
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      debugPrint("Error fetching data: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = Provider.of<AuthProvider>(context);
    final profile = _data?['profile'];
    final ledgers = _data?['recentLedgers'] as List?;

    return Scaffold(
      appBar: AppBar(
        title: const Text("VNeID Civic Point"),
        backgroundColor: const Color(0xFF0056B3),
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            onPressed: () {
              setState(() => _isLoading = true);
              _fetchDashboardData();
            },
            icon: const Icon(Icons.refresh),
            tooltip: "Làm mới dữ liệu",
          ),
          IconButton(onPressed: () => auth.logout(), icon: const Icon(Icons.logout))
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Hồ sơ công dân card
                  Card(
                    elevation: 4,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text("Hồ sơ Công dân",
                                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                              _buildRankBadge(profile?['currentRank'], profile?['rankDisplayName']),
                            ],
                          ),
                          const Divider(),
                          const SizedBox(height: 8),
                          _buildProfileInfo("Họ và tên:", profile?['fullName']),
                          _buildProfileInfo("Số định danh (CCCD):", profile?['maskedCccd']),
                          _buildProfileInfo("Trạng thái:", profile?['status']),
                          const SizedBox(height: 16),
                          const Text("Tổng quan về Điểm",
                              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                          const SizedBox(height: 10),
                          Row(
                            children: [
                              Expanded(
                                  child: _buildStatBox(
                                      "Tổng tích lũy", "${profile?['totalPoints']}")),
                              const SizedBox(width: 10),
                              Expanded(
                                  child: _buildStatBox(
                                      "Điểm trong năm", "${profile?['fiscalYearPoints']}")),
                            ],
                          ),
                          const SizedBox(height: 16),
                          const Text("Tiến trình lên Hạng kế tiếp",
                              style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
                          const SizedBox(height: 8),
                          _buildProgressBar(profile?['rankProgressBar']),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  const Text("Giao dịch Điểm gần đây",
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF0056B3))),
                  const SizedBox(height: 8),
                  // List of transactions
                  if (ledgers == null || ledgers.isEmpty)
                    const Center(child: Text("Không tìm thấy giao dịch nào."))
                  else
                    ListView.separated(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: ledgers.length,
                      separatorBuilder: (context, index) => const Divider(),
                      itemBuilder: (context, index) {
                        final item = ledgers[index];
                        final points = item['pointsAwarded'];
                        return ListTile(
                          contentPadding: EdgeInsets.zero,
                          title: Text(item['activityName'], style: const TextStyle(fontWeight: FontWeight.w500)),
                          subtitle: Text("${item['createdAt']} • ${item['typeDisplayName']}"),
                          trailing: Text(
                            "${points > 0 ? '+' : ''}$points",
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                              color: points > 0 ? Colors.green : (points < 0 ? Colors.red : Colors.grey),
                            ),
                          ),
                        );
                      },
                    ),
                ],
              ),
            ),
    );
  }

  Widget _buildProfileInfo(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(width: 8),
          Text(value ?? ""),
        ],
      ),
    );
  }

  Widget _buildStatBox(String label, String value) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.blue.withOpacity(0.1),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        children: [
          Text(label, style: const TextStyle(fontSize: 12, color: Colors.black54)),
          const SizedBox(height: 4),
          Text(value, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF0056B3))),
        ],
      ),
    );
  }

  Widget _buildRankBadge(String? rank, String? displayName) {
    Color color;
    switch (rank) {
      case 'ACTIVE': color = Colors.green; break;
      case 'BASIC': color = Colors.blue; break;
      case 'HIGH': color = Colors.orange; break;
      default: color = Colors.grey;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        displayName ?? "HẠNG",
        style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildProgressBar(String? progressStr) {
    double progress = 0;
    if (progressStr != null && progressStr.contains('%')) {
      progress = double.tryParse(progressStr.replaceAll('%', '')) ?? 0;
    }
    return Column(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(10),
          child: LinearProgressIndicator(
            value: progress / 100,
            minHeight: 15,
            backgroundColor: Colors.grey[300],
            valueColor: const AlwaysStoppedAnimation<Color>(Colors.green),
          ),
        ),
        const SizedBox(height: 4),
        Text(progressStr ?? "0%", style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
      ],
    );
  }
}
