package com.collabera.consolebankapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collabera.consolebankapp.dto.AdminResponse;
import com.collabera.consolebankapp.dto.CreateAdminRequest;
import com.collabera.consolebankapp.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admins")
public class AdminManagementController {

    private final AdminService adminService;

    public AdminManagementController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<AdminResponse> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {
        AdminResponse response = AdminResponse.from(
                adminService.createAdmin(request.username(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
