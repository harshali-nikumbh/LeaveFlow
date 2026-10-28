
package com.settribe.leavemanagement.controller;

import com.settribe.leavemanagement.LeaveRequest;
import com.settribe.leavemanagement.User;
import com.settribe.leavemanagement.repository.UserRepository;
import com.settribe.leavemanagement.service.LeaveService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;

    public LeaveController(
            LeaveService leaveService,
            UserRepository userRepository) {
        this.leaveService = leaveService;
        this.userRepository = userRepository;
    }

    public record LeaveRequestBody(
            @NotBlank @Size(max = 50) String leaveType,
            @NotNull LocalDate fromDate,
            @NotNull LocalDate toDate,
            @NotBlank @Size(max = 500) String reason) {
    }

    public record LeaveResponse(
            Long id,
            Long userId,
            String leaveType,
            LocalDate fromDate,
            LocalDate toDate,
            String reason,
            String status) {

        public static LeaveResponse from(LeaveRequest leave) {
            return new LeaveResponse(
                    leave.getId(),
                    leave.getUser().getId(),
                    leave.getLeaveType(),
                    leave.getFromDate(),
                    leave.getToDate(),
                    leave.getReason(),
                    leave.getStatus());
        }
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user not found."));

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<?> applyLeave(
            @Valid @RequestBody LeaveRequestBody request) {
        try {
            LeaveRequest leave = leaveService.applyLeave(
                    currentUserId(),
                    request.leaveType(),
                    request.fromDate(),
                    request.toDate(),
                    request.reason());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(LeaveResponse.from(leave));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyLeaves() {
        return ResponseEntity.ok(
                leaveService.getMyLeaves(currentUserId()).stream()
                        .map(LeaveResponse::from)
                        .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestBody request) {
        try {
            LeaveRequest leave = leaveService.updatePendingLeave(
                    currentUserId(),
                    id,
                    request.leaveType(),
                    request.fromDate(),
                    request.toDate(),
                    request.reason());

            return ResponseEntity.ok(LeaveResponse.from(leave));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelLeave(@PathVariable Long id) {
        try {
            leaveService.cancelPendingLeave(currentUserId(), id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
