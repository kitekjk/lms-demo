import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_dotenv/flutter_dotenv.dart';

class EnvConfig {
  static String get apiBaseUrl {
    if (kIsWeb) return 'http://localhost:8080/api';
    return dotenv.env['API_BASE_URL'] ?? 'http://localhost:8080/api';
  }

  static int get apiTimeout {
    if (kIsWeb) return 30000;
    return int.parse(dotenv.env['API_TIMEOUT'] ?? '30000');
  }

  static String get logLevel {
    if (kIsWeb) return 'info';
    return dotenv.env['LOG_LEVEL'] ?? 'info';
  }

  static String get storageEncryptionKey {
    if (kIsWeb) return '';
    return dotenv.env['STORAGE_ENCRYPTION_KEY'] ?? '';
  }

  static Future<void> load() async {
    // 웹 환경에서는 .env 파일을 로드하지 않고 기본값 사용
    if (kIsWeb) {
      print('Running on web - using default environment values');
      return;
    }

    try {
      await dotenv.load();
    } catch (e) {
      // .env 파일이 없을 경우 기본값 사용
      print('Warning: .env file not found, using default values');
    }
  }
}
