
package com.settribe.leavemanagement.service;

import com.settribe.leavemanagement.LeaveRequest;
import com.settribe.leavemanagement.User;
import com.settribe.leavemanagement.repository.LeaveRequestRepository;
import com.settribe.leavemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final UserRepository userRepository;

    public LeaveService(
            LeaveRequestRepository leaveRepository,
            UserRepository userRepository) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
    }

    public LeaveRequest applyLeave(
            Long userId,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            String reason) {

        if (fromDate == null || toDate == null
                || toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException(
                    "End date must be on or after start date.");
        }

        if (fromDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Leave start date cannot be in the past.");
        }

        if (leaveType == null || leaveType.isBlank()
                || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Leave type and reason are required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found."));

        LeaveRequest leave = new LeaveRequest();
        leave.setUser(user);
        leave.setLeaveType(leaveType.trim());
        leave.setFromDate(fromDate);
        leave.setToDate(toDate);
        leave.setReason(reason.trim());
        leave.setStatus("PENDING");

        return leaveRepository.save(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getMyLeaves(Long userId) {
        return leaveRepository.findByUserId(userId);
    }

    @Transactional
    public LeaveRequest updatePendingLeave(
            Long userId,
            Long leaveId,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            String reason) {

        LeaveRequest leave = findOwnedLeave(userId, leaveId);

        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending leave requests can be edited.");
        }

        if (fromDate == null || toDate == null
                || toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException(
                    "End date must be on or after start date.");
        }

        if (fromDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Leave start date cannot be in the past.");
        }

        if (leaveType == null || leaveType.isBlank()
                || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Leave type and reason are required.");
        }

        leave.setLeaveType(leaveType.trim());
        leave.setFromDate(fromDate);
        leave.setToDate(toDate);
        leave.setReason(reason.trim());

        return leaveRepository.save(leave);
    }

    @Transactional
    public void cancelPendingLeave(Long userId, Long leaveId) {
        LeaveRequest leave = findOwnedLeave(userId, leaveId);

        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending leave requests can be cancelled.");
        }

        leaveRepository.delete(leave);
    }

    private LeaveRequest findOwnedLeave(Long userId, Long leaveId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave request not found."));

        if (!leave.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You cannot access another employee's leave request.");
        }

        return leave;
    }
}
