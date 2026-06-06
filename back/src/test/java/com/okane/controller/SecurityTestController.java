package com.okane.controller;

import com.okane.security.Roles;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class SecurityTestController {

    @GetMapping("/admin")
    @PreAuthorize(Roles.ADMIN)
    public ResponseEntity<String> adminOnly() { return ResponseEntity.ok("admin"); }

    @GetMapping("/manager")
    @PreAuthorize(Roles.MANAGER)
    public ResponseEntity<String> managerOnly() { return ResponseEntity.ok("manager"); }

    @GetMapping("/agent")
    @PreAuthorize(Roles.AGENT)
    public ResponseEntity<String> agentOnly() { return ResponseEntity.ok("agent"); }

    @GetMapping("/client")
    @PreAuthorize(Roles.CLIENT)
    public ResponseEntity<String> clientOnly() { return ResponseEntity.ok("client"); }

    @GetMapping("/staff")
    @PreAuthorize(Roles.ALL_STAFF)
    public ResponseEntity<String> staffOnly() { return ResponseEntity.ok("staff"); }

    @GetMapping("/all")
    @PreAuthorize(Roles.ALL)
    public ResponseEntity<String> allRoles() { return ResponseEntity.ok("all"); }
}