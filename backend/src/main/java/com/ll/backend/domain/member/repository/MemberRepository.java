package com.ll.backend.domain.member.repository;

import com.ll.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByUsername(String username);

    Member findByUsername(String username);
}
