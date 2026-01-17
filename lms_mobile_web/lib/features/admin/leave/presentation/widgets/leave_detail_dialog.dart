import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_request.dart';
import 'package:lms_mobile_web/features/admin/leave/domain/models/leave_status.dart';

class LeaveDetailDialog extends StatelessWidget {
  final LeaveRequest request;

  const LeaveDetailDialog({
    super.key,
    required this.request,
  });

  @override
  Widget build(BuildContext context) {
    final dateFormat = DateFormat('yyyy-MM-dd (E)', 'ko_KR');
    final dateTimeFormat = DateFormat('yyyy-MM-dd HH:mm', 'ko_KR');

    return AlertDialog(
      title: Row(
        children: [
          const Text('휴가 신청 상세'),
          const Spacer(),
          _buildStatusChip(),
        ],
      ),
      content: SizedBox(
        width: 500,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // 근로자 정보
              _buildSection(
                '근로자 정보',
                [
                  _buildInfoRow('이름', request.employeeName ?? request.employeeId),
                  if (request.storeName != null)
                    _buildInfoRow('매장', request.storeName!),
                ],
              ),
              const SizedBox(height: 16),

              // 휴가 정보
              _buildSection(
                '휴가 정보',
                [
                  _buildInfoRow('휴가 종류', request.leaveType.displayName),
                  _buildInfoRow('시작일', dateFormat.format(request.startDate)),
                  _buildInfoRow('종료일', dateFormat.format(request.endDate)),
                  _buildInfoRow('총 일수', '${request.totalDays}일'),
                  _buildInfoRow('신청일', dateTimeFormat.format(request.createdAt)),
                ],
              ),
              const SizedBox(height: 16),

              // 신청 사유
              _buildSection(
                '신청 사유',
                [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.grey.shade100,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      request.reason,
                      style: const TextStyle(fontSize: 14),
                    ),
                  ),
                ],
              ),

              // 승인/반려 정보
              if (request.isApproved || request.isRejected) ...[
                const SizedBox(height: 16),
                _buildSection(
                  request.isApproved ? '승인 정보' : '반려 정보',
                  [
                    if (request.approverName != null)
                      _buildInfoRow('처리자', request.approverName!),
                    if (request.approvedAt != null)
                      _buildInfoRow('처리일시', dateTimeFormat.format(request.approvedAt!)),
                    if (request.rejectionReason != null) ...[
                      const SizedBox(height: 8),
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.red.shade50,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.red.shade200),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '반려 사유',
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.red.shade900,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              request.rejectionReason!,
                              style: TextStyle(
                                fontSize: 14,
                                color: Colors.red.shade900,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('닫기'),
        ),
      ],
    );
  }

  Widget _buildStatusChip() {
    final color = _getStatusColor(request.status);
    return Chip(
      label: Text(
        request.status.displayName,
        style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
      ),
      backgroundColor: color.withOpacity(0.1),
      labelStyle: TextStyle(color: color),
      padding: EdgeInsets.zero,
      visualDensity: VisualDensity.compact,
    );
  }

  Widget _buildSection(String title, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 8),
        ...children,
      ],
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              label,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade700,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
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
}
