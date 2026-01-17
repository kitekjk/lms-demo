import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_status.dart';
import 'package:lms_mobile_web/features/admin/leave/presentation/providers/leave_provider.dart';
import 'package:lms_mobile_web/features/admin/leave/presentation/widgets/leave_detail_dialog.dart';
import 'package:lms_mobile_web/features/admin/store/presentation/providers/store_provider.dart';
import 'package:lms_mobile_web/shared/widgets/admin_layout.dart';

class LeaveManagementScreen extends ConsumerStatefulWidget {
  const LeaveManagementScreen({super.key});

  @override
  ConsumerState<LeaveManagementScreen> createState() => _LeaveManagementScreenState();
}

class _LeaveManagementScreenState extends ConsumerState<LeaveManagementScreen> {
  String? _selectedStoreId;
  bool _showPendingOnly = true;

  @override
  Widget build(BuildContext context) {
    final storesAsync = ref.watch(storesProvider);

    return AdminLayout(
      title: '휴가 관리',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 헤더 및 필터
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '휴가 신청 관리',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      // 매장 선택
                      Expanded(
                        flex: 2,
                        child: storesAsync.when(
                          loading: () => const LinearProgressIndicator(),
                          error: (error, _) => Text('매장 목록 로드 실패: ${error.toString()}'),
                          data: (stores) => DropdownButtonFormField<String>(
                            value: _selectedStoreId,
                            decoration: const InputDecoration(
                              labelText: '매장 선택',
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.store),
                            ),
                            hint: const Text('매장을 선택하세요'),
                            items: stores.map((store) {
                              return DropdownMenuItem(
                                value: store.id,
                                child: Text(store.name),
                              );
                            }).toList(),
                            onChanged: (value) {
                              setState(() => _selectedStoreId = value);
                            },
                          ),
                        ),
                      ),
                      const SizedBox(width: 16),
                      // 필터 스위치
                      Expanded(
                        child: SwitchListTile(
                          title: const Text('대기 중만 보기'),
                          subtitle: Text(
                            _showPendingOnly ? '승인 대기 중인 신청만 표시' : '모든 휴가 신청 표시',
                            style: const TextStyle(fontSize: 12),
                          ),
                          value: _showPendingOnly,
                          onChanged: (value) {
                            setState(() => _showPendingOnly = value);
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // 휴가 신청 목록
          Expanded(
            child: _showPendingOnly
                ? _buildPendingRequestsList()
                : (_selectedStoreId == null
                    ? _buildEmptyState()
                    : _buildStoreRequestsList()),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.store_outlined, size: 64, color: Colors.grey.shade400),
          const SizedBox(height: 16),
          Text(
            '매장을 선택하여 휴가 신청을 조회하세요',
            style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
          ),
        ],
      ),
    );
  }

  Widget _buildPendingRequestsList() {
    final requestsAsync = ref.watch(pendingLeaveRequestsProvider);

    return _buildRequestsCard(requestsAsync, '대기 중인 휴가 신청');
  }

  Widget _buildStoreRequestsList() {
    final requestsAsync = ref.watch(leaveRequestsByStoreProvider(_selectedStoreId!));

    return _buildRequestsCard(requestsAsync, '매장 휴가 신청 내역');
  }

  Widget _buildRequestsCard(AsyncValue<List> requestsAsync, String title) {
    final dateFormat = DateFormat('yyyy-MM-dd (E)', 'ko_KR');

    return requestsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, _) => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.red),
            const SizedBox(height: 16),
            Text('오류: ${error.toString()}'),
          ],
        ),
      ),
      data: (requests) {
        if (requests.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.inbox_outlined, size: 64, color: Colors.grey.shade400),
                const SizedBox(height: 16),
                Text(
                  _showPendingOnly ? '승인 대기 중인 휴가 신청이 없습니다' : '휴가 신청 내역이 없습니다',
                  style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                ),
              ],
            ),
          );
        }

        return Card(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    Text(
                      title,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                    const SizedBox(width: 8),
                    Chip(
                      label: Text('총 ${requests.length}건'),
                      visualDensity: VisualDensity.compact,
                    ),
                    const Spacer(),
                    // 상태별 통계
                    _buildStatusChip(
                      '대기',
                      requests.where((r) => r.status == LeaveStatus.pending).length,
                      Colors.orange,
                    ),
                    const SizedBox(width: 8),
                    _buildStatusChip(
                      '승인',
                      requests.where((r) => r.status == LeaveStatus.approved).length,
                      Colors.green,
                    ),
                    const SizedBox(width: 8),
                    _buildStatusChip(
                      '반려',
                      requests.where((r) => r.status == LeaveStatus.rejected).length,
                      Colors.red,
                    ),
                  ],
                ),
              ),
              const Divider(height: 1),
              Expanded(
                child: ListView.separated(
                  itemCount: requests.length,
                  separatorBuilder: (context, index) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final request = requests[index];
                    return ListTile(
                      leading: CircleAvatar(
                        backgroundColor: _getStatusColor(request.status).withOpacity(0.1),
                        child: Icon(
                          _getStatusIcon(request.status),
                          color: _getStatusColor(request.status),
                          size: 20,
                        ),
                      ),
                      title: Row(
                        children: [
                          Text(
                            request.employeeName ?? request.employeeId,
                            style: const TextStyle(fontWeight: FontWeight.w500),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: _getLeaveTypeColor(request.leaveType).withOpacity(0.1),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              request.leaveType.displayName,
                              style: TextStyle(
                                fontSize: 12,
                                color: _getLeaveTypeColor(request.leaveType),
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Chip(
                            label: Text(
                              request.status.displayName,
                              style: const TextStyle(fontSize: 12),
                            ),
                            backgroundColor: _getStatusColor(request.status).withOpacity(0.1),
                            labelStyle: TextStyle(color: _getStatusColor(request.status)),
                            padding: EdgeInsets.zero,
                            visualDensity: VisualDensity.compact,
                          ),
                        ],
                      ),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              Icon(Icons.calendar_today, size: 14, color: Colors.grey.shade600),
                              const SizedBox(width: 4),
                              Text(
                                '${dateFormat.format(request.startDate)} ~ ${dateFormat.format(request.endDate)}',
                                style: TextStyle(color: Colors.grey.shade700),
                              ),
                              const SizedBox(width: 8),
                              Icon(Icons.event_available, size: 14, color: Colors.grey.shade600),
                              const SizedBox(width: 4),
                              Text(
                                '${request.totalDays}일',
                                style: TextStyle(
                                  color: Colors.grey.shade700,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 2),
                          Text(
                            '사유: ${request.reason}',
                            style: TextStyle(fontSize: 12, color: Colors.grey.shade600),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          if (request.rejectionReason != null) ...[
                            const SizedBox(height: 2),
                            Text(
                              '반려 사유: ${request.rejectionReason}',
                              style: const TextStyle(fontSize: 12, color: Colors.red),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ],
                      ),
                      trailing: request.isPending
                          ? Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.check_circle, color: Colors.green),
                                  tooltip: '승인',
                                  onPressed: () => _confirmApprove(context, request),
                                ),
                                IconButton(
                                  icon: const Icon(Icons.cancel, color: Colors.red),
                                  tooltip: '반려',
                                  onPressed: () => _showRejectDialog(context, request),
                                ),
                              ],
                            )
                          : IconButton(
                              icon: const Icon(Icons.info_outline),
                              tooltip: '상세',
                              onPressed: () => _showDetailDialog(context, request),
                            ),
                      onTap: () => _showDetailDialog(context, request),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildStatusChip(String label, int count, Color color) {
    return Chip(
      label: Text('$label: $count', style: const TextStyle(fontSize: 12)),
      backgroundColor: color.withOpacity(0.1),
      labelStyle: TextStyle(color: color),
      padding: EdgeInsets.zero,
      visualDensity: VisualDensity.compact,
    );
  }

  Color _getStatusColor(LeaveStatus status) {
    switch (status) {
      case LeaveStatus.pending:
        return Colors.orange;
      case LeaveStatus.approved:
        return Colors.green;
      case LeaveStatus.rejected:
        return Colors.red;
      case LeaveStatus.cancelled:
        return Colors.grey;
    }
  }

  IconData _getStatusIcon(LeaveStatus status) {
    switch (status) {
      case LeaveStatus.pending:
        return Icons.schedule;
      case LeaveStatus.approved:
        return Icons.check_circle;
      case LeaveStatus.rejected:
        return Icons.cancel;
      case LeaveStatus.cancelled:
        return Icons.block;
    }
  }

  Color _getLeaveTypeColor(leaveType) {
    switch (leaveType.value) {
      case 'ANNUAL':
        return Colors.blue;
      case 'SICK':
        return Colors.red;
      case 'PERSONAL':
        return Colors.purple;
      case 'FAMILY':
        return Colors.green;
      default:
        return Colors.grey;
    }
  }

  void _confirmApprove(BuildContext context, request) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('휴가 승인'),
        content: Text('${request.employeeName ?? request.employeeId}님의 휴가 신청을 승인하시겠습니까?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.of(context).pop();
              await ref.read(leaveNotifierProvider.notifier).approveLeave(request.id);
              if (mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('휴가가 승인되었습니다')),
                );
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.green),
            child: const Text('승인'),
          ),
        ],
      ),
    );
  }

  void _showRejectDialog(BuildContext context, request) {
    final reasonController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('휴가 반려'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${request.employeeName ?? request.employeeId}님의 휴가 신청을 반려합니다.'),
            const SizedBox(height: 16),
            TextField(
              controller: reasonController,
              decoration: const InputDecoration(
                labelText: '반려 사유 *',
                hintText: '반려 사유를 입력하세요',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('취소'),
          ),
          ElevatedButton(
            onPressed: () async {
              final reason = reasonController.text.trim();
              if (reason.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('반려 사유를 입력하세요')),
                );
                return;
              }

              Navigator.of(context).pop();
              await ref.read(leaveNotifierProvider.notifier).rejectLeave(
                    leaveId: request.id,
                    rejectionReason: reason,
                  );
              if (mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('휴가가 반려되었습니다')),
                );
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('반려'),
          ),
        ],
      ),
    );
  }

  void _showDetailDialog(BuildContext context, request) {
    showDialog(
      context: context,
      builder: (context) => LeaveDetailDialog(request: request),
    );
  }
}
