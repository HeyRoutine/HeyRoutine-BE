package com.saeparam.HeyRoutine.domain.user.entity;


import com.saeparam.HeyRoutine.domain.user.enums.Role;
import com.saeparam.HeyRoutine.global.common.util.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class User extends BaseTime implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_id", updatable = false, unique = true, nullable = false, columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column
  private String profileImage;

  //우리가 만들어준 계좌
  @Column
  private String bankAccount;

  // 금융 API 계정
  @Column
  private String userKey;

  // 1원 계좌인증 상태
  @Column
  private boolean accountCertificationStatus;

  @Column
  private Long point;

  public void setUserKey(String userKey) {
    this.userKey = userKey;
  }

  public void setBankAccount(String bankAccount) {
    this.bankAccount = bankAccount;
  }

  @ElementCollection(fetch = FetchType.EAGER)
  @Builder.Default
  @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
  private List<Role> roles = new ArrayList<>();
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
        .collect(Collectors.toList());
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public static boolean hasRole(UserDetails user, Role role) {
    return user.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(role.getRoleName()));
  }



  // 유저의 포인트 차감
  public void usePoints(Long price) {
    this.point=this.point-price;
  }



  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }


}
