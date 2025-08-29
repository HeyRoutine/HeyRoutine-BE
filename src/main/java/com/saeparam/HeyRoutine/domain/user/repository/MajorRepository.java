package com.saeparam.HeyRoutine.domain.user.repository;

import com.saeparam.HeyRoutine.domain.user.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
	List<Major> findTop10ByNameContainingIgnoreCase(String keyword);
}