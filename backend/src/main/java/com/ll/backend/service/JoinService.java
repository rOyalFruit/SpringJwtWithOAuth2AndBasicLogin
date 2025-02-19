package com.ll.backend.service;

import com.ll.backend.dto.JoinDto;
import com.ll.backend.entity.Member;
import com.ll.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDto joinDto) {

        String username = joinDto.getUsername();
        String password = joinDto.getPassword();

        Boolean isExist = memberRepository.existsByUsername(username);

        if (isExist) {

            return;
        }

        Member member = new Member();

        member.setUsername(username);
        member.setPassword(bCryptPasswordEncoder.encode(password));
        member.setRole("ROLE_ADMIN");

        memberRepository.save(member);
    }
}
