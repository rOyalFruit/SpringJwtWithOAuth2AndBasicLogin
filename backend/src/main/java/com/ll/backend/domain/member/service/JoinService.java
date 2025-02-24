package com.ll.backend.domain.member.service;

import com.ll.backend.domain.member.dto.JoinDto;
import com.ll.backend.domain.member.entity.Member;
import com.ll.backend.domain.member.repository.MemberRepository;
import com.ll.backend.global.exception.business.InvalidInputException;
import com.ll.backend.global.exception.business.UserAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional(readOnly = true)
    public Member joinProcess(JoinDto joinDto) {
        validatePasswords(joinDto.password(), joinDto.confirmPassword());
        checkIfUserExists(joinDto.username());

        Member member = createMember(joinDto);
        return memberRepository.save(member);
    }

    private void validatePasswords(String password, String password2) {
        if (!password.equals(password2)) {
            throw new InvalidInputException("Password does not match");
        }
    }

    private void checkIfUserExists(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new UserAlreadyRegisteredException("User already exists");
        }
    }

    private Member createMember(JoinDto joinDto) {
        Member member = new Member();
        member.setUsername(joinDto.username());
        member.setPassword(bCryptPasswordEncoder.encode(joinDto.password()));
        member.setEmail(joinDto.email());
        member.setName(joinDto.nickname());
        member.setRole("ROLE_ADMIN");
        return member;
    }
}
