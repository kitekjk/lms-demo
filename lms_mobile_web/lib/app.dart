import 'package:flutter/material.dart';
import 'package:lms_mobile_web/core/config/theme_config.dart';
import 'package:lms_mobile_web/core/router/app_router.dart';

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'LMS 근태 관리',
      theme: ThemeConfig.lightTheme(),
      darkTheme: ThemeConfig.darkTheme(),
      routerConfig: appRouter,
      debugShowCheckedModeBanner: false,
    );
  }
}
