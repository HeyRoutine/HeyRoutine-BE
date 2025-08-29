package com.saeparam.HeyRoutine.domain.user.repository;

import com.saeparam.HeyRoutine.domain.user.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
	List<University> findTop10ByNameContainingIgnoreCase(String keyword);
}
