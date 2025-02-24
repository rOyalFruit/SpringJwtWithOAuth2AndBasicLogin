package com.ll.backend.domain.member.controller;

import com.ll.backend.domain.member.dto.JoinDto;
import com.ll.backend.domain.member.entity.Member;
import com.ll.backend.domain.member.service.JoinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "JoinController", description = "회원가입")
@RestController
@RequiredArgsConstructor
@RequestMapping("/join")
public class JoinController {

    private final JoinService joinService;

    @PostMapping
    public ResponseEntity<?> joinProcess(@RequestBody @Valid JoinDto joinDTO) {

        Member member = joinService.joinProcess(joinDTO);
        log.info("User registration successful for username: {}", joinDTO.username());

        return ResponseEntity.ok("User registration successful. Username: [" + member.getUsername()  + "]");
    }
}
