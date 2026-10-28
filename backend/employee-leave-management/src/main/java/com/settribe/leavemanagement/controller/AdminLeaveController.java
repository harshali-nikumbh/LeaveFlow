
package com.settribe.leavemanagement.controller;

import com.settribe.leavemanagement.LeaveRequest;
import com.settribe.leavemanagement.service.AdminLeaveService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/leaves")
public class AdminLeaveController {

    private final AdminLeaveService adminLeaveService;

    public AdminLeaveController(AdminLeaveService adminLeaveService) {
        this.adminLeaveService = adminLeaveService;
    }

    public record StatusRequest(@NotBlank String status) {
    }

    @GetMapping
    public ResponseEntity<?> getAllLeaves(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType) {

        return ResponseEntity.ok(
                adminLeaveService.getAllLeaves(status, leaveType)
                        .stream()
                        .map(LeaveController.LeaveResponse::from)
                        .toList());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        try {
            LeaveRequest leave = adminLeaveService.updateStatus(
                    id, request.status());

            return ResponseEntity.ok(
                    LeaveController.LeaveResponse.from(leave));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
