package com.ll.backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyController", description = "마이페이지 관련 기능. USER 권한 필요")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/my")
public class MyController {

    @GetMapping
    public String myAPI() {

        return "my route";
    }
}
