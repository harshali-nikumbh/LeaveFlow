
package com.settribe.leavemanagement.service;

import com.settribe.leavemanagement.LeaveRequest;
import com.settribe.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdminLeaveService {

    private final LeaveRequestRepository leaveRepository;

    public AdminLeaveService(LeaveRequestRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getAllLeaves(
            String status, String leaveType) {

        return leaveRepository.findAll().stream()
                .filter(leave -> status == null || status.isBlank()
                        || leave.getStatus().equalsIgnoreCase(status))
                .filter(leave -> leaveType == null || leaveType.isBlank()
                        || leave.getLeaveType().equalsIgnoreCase(leaveType))
                .toList();
    }

    @Transactional
    public LeaveRequest updateStatus(Long leaveId, String newStatus) {

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required.");
        }

        String status = newStatus.trim().toUpperCase(Locale.ROOT);

        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED.");
        }

        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave request not found."));

        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending requests can be approved or rejected.");
        }

        leave.setStatus(status);
        return leaveRepository.save(leave);
    }
}
