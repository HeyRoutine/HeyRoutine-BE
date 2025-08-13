package com.saeparam.HeyRoutine.domain.user.repository;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String username);

  boolean existsByEmail(String username);
  boolean existsByNickname(String nickname);

}