package com.saeparam.HeyRoutine.domain.user.repository;

import com.saeparam.HeyRoutine.domain.user.entity.Major;
import com.saeparam.HeyRoutine.domain.user.entity.MajorMiddle;
import com.saeparam.HeyRoutine.domain.user.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MajorMiddleRepository extends JpaRepository<MajorMiddle, Long> {

	List<MajorMiddle> findTop10ByUniversityAndMajor_NameContainingIgnoreCase(University university, String keyword);

	Optional<MajorMiddle> findByUniversityAndMajor(University university, Major major);

	@Query("SELECT mm.major.id as majorId, mm.major.name as majorName, SUM(mm.score) as score " +
		"FROM MajorMiddle mm GROUP BY mm.major.id, mm.major.name ORDER BY score DESC")
	List<MajorRankProjection> findMajorRanking();

	interface MajorRankProjection {
		Long getMajorId();
		String getMajorName();
		Long getScore();
	}
}