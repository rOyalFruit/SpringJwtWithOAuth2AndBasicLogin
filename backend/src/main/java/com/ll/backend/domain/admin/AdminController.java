package com.ll.backend.domain.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AdminController", description = "어드민 페이지 관련 기능. ADMIN 권한 필요")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Operation(summary = "관리자 페이지")
    @GetMapping
    public String adminAPI() {

        return "admin Controller";
    }
}
