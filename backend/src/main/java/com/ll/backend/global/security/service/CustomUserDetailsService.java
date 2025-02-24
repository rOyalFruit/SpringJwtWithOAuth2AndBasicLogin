package com.ll.backend.global.security.service;

import com.ll.backend.global.security.dto.CustomUserDetails;
import com.ll.backend.domain.member.entity.Member;
import com.ll.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //DB에서 조회
        Member member = memberRepository.findByUsername(username);

        if (member != null) {

            //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
            return new CustomUserDetails(member);
        }

        return null;
    }
}
