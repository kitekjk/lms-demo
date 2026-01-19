import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/employee/domain/models/employee.dart';
import 'package:lms_mobile_web/features/admin/employee/presentation/providers/employee_provider.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class EmployeeFormScreen extends ConsumerStatefulWidget {
  final String? employeeId;

  const EmployeeFormScreen({super.key, this.employeeId});

  @override
  ConsumerState<EmployeeFormScreen> createState() => _EmployeeFormScreenState();
}

class _EmployeeFormScreenState extends ConsumerState<EmployeeFormScreen> {
  final _formKey = GlobalKey<FormState>();
  final _userIdController = TextEditingController();
  final _nameController = TextEditingController();
  String? _selectedStoreId;
  EmployeeType _selectedEmployeeType = EmployeeType.partTime;
  bool _isLoading = false;

  bool get isEditMode => widget.employeeId != null;

  @override
  void initState() {
    super.initState();
    if (isEditMode) {
      _loadEmployee();
    }
  }

  Future<void> _loadEmployee() async {
    final employee = await ref.read(employeeProvider(widget.employeeId!).future);
    _userIdController.text = employee.userId;
    _nameController.text = employee.name;
    _selectedStoreId = employee.storeId;
    _selectedEmployeeType = employee.employeeType;
    setState(() {});
  }

  @override
  void dispose() {
    _userIdController.dispose();
    _nameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final storesAsync = ref.watch(storesProvider);

    return AdminLayout(
      title: isEditMode ? '근로자 수정' : '근로자 등록',
      child: SingleChildScrollView(
        child: Center(
          child: Container(
            constraints: const BoxConstraints(maxWidth: 600),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(32.0),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Text(
                        isEditMode ? '근로자 정보 수정' : '새 근로자 등록',
                        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 32),
                      TextFormField(
                        controller: _userIdController,
                        decoration: const InputDecoration(
                          labelText: '사용자 ID',
                          hintText: '예: user@example.com',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.person),
                        ),
                        enabled: !isEditMode, // 수정 시 사용자 ID는 변경 불가
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return '사용자 ID를 입력하세요';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 24),
                      TextFormField(
                        controller: _nameController,
                        decoration: const InputDecoration(
                          labelText: '이름',
                          hintText: '예: 홍길동',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.badge),
                        ),
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return '이름을 입력하세요';
                          }
                          if (value.trim().length < 2) {
                            return '이름은 최소 2자 이상이어야 합니다';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 24),
                      DropdownButtonFormField<EmployeeType>(
                        value: _selectedEmployeeType,
                        decoration: const InputDecoration(
                          labelText: '근로자 유형',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.work),
                        ),
                        items: EmployeeType.values.map((type) {
                          return DropdownMenuItem(
                            value: type,
                            child: Text(type.displayName),
                          );
                        }).toList(),
                        onChanged: (value) {
                          if (value != null) {
                            setState(() => _selectedEmployeeType = value);
                          }
                        },
                        validator: (value) {
                          if (value == null) {
                            return '근로자 유형을 선택하세요';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 24),
                      storesAsync.when(
                        loading: () => const LinearProgressIndicator(),
                        error: (error, _) => Text('매장 목록 로드 실패: ${error.toString()}'),
                        data: (stores) => DropdownButtonFormField<String>(
                          value: _selectedStoreId,
                          decoration: const InputDecoration(
                            labelText: '매장',
                            border: OutlineInputBorder(),
                            prefixIcon: Icon(Icons.store),
                          ),
                          items: stores.map((store) {
                            return DropdownMenuItem(
                              value: store.id,
                              child: Text(store.name),
                            );
                          }).toList(),
                          onChanged: (value) {
                            setState(() => _selectedStoreId = value);
                          },
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return '매장을 선택하세요';
                            }
                            return null;
                          },
                        ),
                      ),
                      const SizedBox(height: 32),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          TextButton(
                            onPressed: _isLoading ? null : () => context.go(RouteNames.adminEmployees),
                            child: const Text('취소'),
                          ),
                          const SizedBox(width: 16),
                          ElevatedButton(
                            onPressed: _isLoading ? null : _handleSubmit,
                            child: _isLoading
                                ? const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(strokeWidth: 2),
                                  )
                                : Text(isEditMode ? '수정' : '등록'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() => _isLoading = true);

    try {
      final userId = _userIdController.text.trim();
      final name = _nameController.text.trim();
      final employeeType = _selectedEmployeeType.value;
      final storeId = _selectedStoreId!;

      if (isEditMode) {
        await ref.read(employeeNotifierProvider.notifier).updateEmployee(
          id: widget.employeeId!,
          name: name,
          employeeType: employeeType,
          storeId: storeId,
        );
      } else {
        await ref.read(employeeNotifierProvider.notifier).createEmployee(
          userId: userId,
          name: name,
          employeeType: employeeType,
          storeId: storeId,
        );
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isEditMode ? '근로자가 수정되었습니다' : '근로자가 등록되었습니다'),
          ),
        );
        context.go(RouteNames.adminEmployees);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('오류: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }
}
