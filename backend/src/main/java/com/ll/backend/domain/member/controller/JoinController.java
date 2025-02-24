package com.ll.backend.domain.member.controller;

import com.ll.backend.domain.member.dto.JoinDto;
import com.ll.backend.domain.member.service.JoinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "JoinController", description = "회원가입")
@RestController
@RequiredArgsConstructor
@RequestMapping("/join")
public class JoinController {

    private final JoinService joinService;

    @PostMapping
    public String joinProcess(JoinDto joinDTO) {

        System.out.println(joinDTO.getUsername());
        joinService.joinProcess(joinDTO);

        return "ok";
    }
}
