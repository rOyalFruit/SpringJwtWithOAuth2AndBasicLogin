package com.ll.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MainController", description = "메인 페이지 관련 기능")
@RestController
public class MainController {

    @GetMapping("/")
    public String mainAPI() {

        return "main route";
    }
}
