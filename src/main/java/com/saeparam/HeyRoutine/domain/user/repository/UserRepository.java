package com.saeparam.HeyRoutine.domain.user.repository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
//  Optional<User> findById(String username);

  boolean existsByEmail(String username);
  boolean existsByNickname(String nickname);

  Optional<User> findByEmail(String email);
}