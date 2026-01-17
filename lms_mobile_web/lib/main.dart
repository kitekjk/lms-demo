import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:lms_mobile_web/app.dart';
import 'package:lms_mobile_web/core/config/env_config.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await EnvConfig.load();
  await initializeDateFormatting('ko_KR');

  runApp(const ProviderScope(child: MyApp()));
}
