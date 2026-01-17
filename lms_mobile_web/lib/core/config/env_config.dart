import 'package:flutter_dotenv/flutter_dotenv.dart';

class EnvConfig {
  static String get apiBaseUrl =>
      dotenv.env['API_BASE_URL'] ?? 'http://localhost:8080/api';
  static int get apiTimeout =>
      int.parse(dotenv.env['API_TIMEOUT'] ?? '30000');
  static String get logLevel => dotenv.env['LOG_LEVEL'] ?? 'info';
  static String get storageEncryptionKey =>
      dotenv.env['STORAGE_ENCRYPTION_KEY'] ?? '';

  static Future<void> load() async {
    await dotenv.load();
  }
}
