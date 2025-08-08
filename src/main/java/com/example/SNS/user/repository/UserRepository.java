package com.example.SNS.user.repository;

import com.example.SNS.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // username으로 회원 조회
    Optional<UserEntity> findByUsername(String username);

    // email로 회원 조회
    Optional<UserEntity> findByEmail(String email);

    // username 중복 체크용 존재 여부 반환
    boolean existsByUsername(String username);

    // email 중복 체크용 존재 여부 반환
    boolean existsByEmail(String email);
}
