import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class StoreFormScreen extends ConsumerStatefulWidget {
  final String? storeId;

  const StoreFormScreen({super.key, this.storeId});

  @override
  ConsumerState<StoreFormScreen> createState() => _StoreFormScreenState();
}

class _StoreFormScreenState extends ConsumerState<StoreFormScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();
  bool _isLoading = false;

  bool get isEditMode => widget.storeId != null;

  @override
  void initState() {
    super.initState();
    if (isEditMode) {
      _loadStore();
    }
  }

  Future<void> _loadStore() async {
    final store = await ref.read(storeProvider(widget.storeId!).future);
    _nameController.text = store.name;
    _locationController.text = store.location;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _locationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AdminLayout(
      title: isEditMode ? '매장 수정' : '매장 등록',
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
                        isEditMode ? '매장 정보 수정' : '새 매장 등록',
                        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 32),
                      TextFormField(
                        controller: _nameController,
                        decoration: const InputDecoration(
                          labelText: '매장명',
                          hintText: '예: 강남점',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.store),
                        ),
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return '매장명을 입력하세요';
                          }
                          if (value.trim().length < 2) {
                            return '매장명은 최소 2자 이상이어야 합니다';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 24),
                      TextFormField(
                        controller: _locationController,
                        decoration: const InputDecoration(
                          labelText: '위치',
                          hintText: '예: 서울특별시 강남구 테헤란로 123',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.location_on),
                        ),
                        maxLines: 2,
                        validator: (value) {
                          if (value == null || value.trim().isEmpty) {
                            return '위치를 입력하세요';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 32),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          TextButton(
                            onPressed: _isLoading ? null : () => context.go(RouteNames.adminStores),
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
      final name = _nameController.text.trim();
      final location = _locationController.text.trim();

      if (isEditMode) {
        await ref.read(storeNotifierProvider.notifier).updateStore(
          id: widget.storeId!,
          name: name,
          location: location,
        );
      } else {
        await ref.read(storeNotifierProvider.notifier).createStore(
          name: name,
          location: location,
        );
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isEditMode ? '매장이 수정되었습니다' : '매장이 등록되었습니다'),
          ),
        );
        context.go(RouteNames.adminStores);
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
