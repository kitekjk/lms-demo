import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:lms_mobile_web/app.dart';
import 'package:lms_mobile_web/core/config/env_config.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await EnvConfig.load();

  runApp(
    const ProviderScope(
      child: MyApp(),
    ),
  );
}
