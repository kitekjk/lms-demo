import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/core/router/route_names.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class StoreListScreen extends ConsumerWidget {
  const StoreListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final storesAsync = ref.watch(storesProvider);

    return AdminLayout(
      title: '매장 관리',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '매장 목록',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              ElevatedButton.icon(
                onPressed: () {
                  context.push('${RouteNames.adminStores}/new');
                },
                icon: const Icon(Icons.add),
                label: const Text('매장 추가'),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Expanded(
            child: storesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (error, stack) => Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.error_outline, size: 48, color: Colors.red),
                    const SizedBox(height: 16),
                    Text('오류: ${error.toString()}'),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () => ref.invalidate(storesProvider),
                      child: const Text('다시 시도'),
                    ),
                  ],
                ),
              ),
              data: (stores) {
                if (stores.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.store_outlined, size: 48, color: Colors.grey),
                        const SizedBox(height: 16),
                        const Text('등록된 매장이 없습니다'),
                        const SizedBox(height: 16),
                        ElevatedButton.icon(
                          onPressed: () {
                            context.push('${RouteNames.adminStores}/new');
                          },
                          icon: const Icon(Icons.add),
                          label: const Text('매장 추가'),
                        ),
                      ],
                    ),
                  );
                }

                return Card(
                  child: SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: DataTable(
                      columns: const [
                        DataColumn(label: Text('매장명')),
                        DataColumn(label: Text('위치')),
                        DataColumn(label: Text('등록일')),
                        DataColumn(label: Text('작업')),
                      ],
                      rows: stores.map((store) {
                        final dateFormat = DateFormat('yyyy-MM-dd');

                        return DataRow(
                          cells: [
                            DataCell(Text(store.name)),
                            DataCell(Text(store.location)),
                            DataCell(Text(dateFormat.format(store.createdAt))),
                            DataCell(
                              Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  IconButton(
                                    icon: const Icon(Icons.edit, size: 20),
                                    tooltip: '수정',
                                    onPressed: () {
                                      context.push('${RouteNames.adminStores}/${store.id}/edit');
                                    },
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.delete, size: 20, color: Colors.red),
                                    tooltip: '삭제',
                                    onPressed: () => _confirmDelete(context, ref, store.id, store.name),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        );
                      }).toList(),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  void _confirmDelete(BuildContext context, WidgetRef ref, String storeId, String storeName) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('매장 삭제'),
        content: Text('정말 "$storeName" 매장을 삭제하시겠습니까?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.of(context).pop();
              await ref.read(storeNotifierProvider.notifier).deleteStore(storeId);
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('매장이 삭제되었습니다')),
                );
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('삭제'),
          ),
        ],
      ),
    );
  }
}
