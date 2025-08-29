package com.saeparam.HeyRoutine.domain.routine.service;

import com.saeparam.HeyRoutine.domain.routine.dto.response.RankResponseDto;
import com.saeparam.HeyRoutine.domain.user.entity.University;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.MajorMiddleRepository;
import com.saeparam.HeyRoutine.domain.user.repository.UniversityRepository;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.domain.user.repository.MajorMiddleRepository.MajorRankProjection;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RankService {

	private final UniversityRepository universityRepository;
	private final MajorMiddleRepository majorMiddleRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public RankResponseDto.RankPage getRanking(UUID userId, String type, Pageable pageable) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

		if ("university".equalsIgnoreCase(type)) {
			return getUniversityRanking(user, pageable);
		} else if ("major".equalsIgnoreCase(type)) {
			return getMajorRanking(user, pageable);
		}
		throw new UserHandler(ErrorStatus.INVALID_RANK_TYPE);
	}

	private RankResponseDto.RankPage getUniversityRanking(User user, Pageable pageable) {
		PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
			Sort.by(Sort.Direction.DESC, "score"));
		Page<University> page = universityRepository.findAll(pageRequest);

		List<RankResponseDto.RankInfo> items = new ArrayList<>();
		for (University u : page.getContent()) {
			int rank = (int) universityRepository.countByScoreGreaterThan(u.getScore()) + 1;
			items.add(RankResponseDto.RankInfo.builder()
				.rank(rank)
				.name(u.getName())
				.score(u.getScore())
				.build());
		}

		University myUniversity = user.getUniversity();
		long higherCount = universityRepository.countByScoreGreaterThan(myUniversity.getScore());
		RankResponseDto.MyRankInfo myItem = RankResponseDto.MyRankInfo.builder()
			.rank((int) higherCount + 1)
			.universityName(myUniversity.getName())
			.majorName(user.getMajor().getName())
			.score(myUniversity.getScore())
			.build();

		return RankResponseDto.RankPage.builder()
			.page(page.getNumber())
			.pageSize(page.getSize())
			.totalItems(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.myItem(myItem)
			.items(items)
			.build();
	}

	private RankResponseDto.RankPage getMajorRanking(User user, Pageable pageable) {
		List<MajorRankProjection> rankingList = majorMiddleRepository.findMajorRanking();
		int totalItems = rankingList.size();
		int fromIndex = pageable.getPageNumber() * pageable.getPageSize();
		int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalItems);
		List<MajorRankProjection> pageList = fromIndex >= totalItems ? List.of() : rankingList.subList(fromIndex, toIndex);

		// 전체 순위를 동점 처리하여 계산
		List<Integer> ranks = new ArrayList<>();
		long prevScore = Long.MIN_VALUE;
		int rankCounter = 0;
		for (int i = 0; i < rankingList.size(); i++) {
			long score = rankingList.get(i).getScore();
			if (score != prevScore) {
				rankCounter = i + 1;
				prevScore = score;
			}
			ranks.add(rankCounter);
		}

		List<RankResponseDto.RankInfo> items = new ArrayList<>();
		for (int i = 0; i < pageList.size(); i++) {
			MajorRankProjection proj = pageList.get(i);
			items.add(RankResponseDto.RankInfo.builder()
				.rank(ranks.get(fromIndex + i))
				.name(proj.getMajorName())
				.score(proj.getScore().intValue())
				.build());
		}

		int myRank = 0;
		int myScore = 0;
		for (int i = 0; i < rankingList.size(); i++) {
			MajorRankProjection proj = rankingList.get(i);
			if (proj.getMajorId().equals(user.getMajor().getId())) {
				myRank = ranks.get(i);
				myScore = proj.getScore().intValue();
				break;
			}
		}

		RankResponseDto.MyRankInfo myItem = RankResponseDto.MyRankInfo.builder()
			.rank(myRank)
			.universityName(user.getUniversity().getName())
			.majorName(user.getMajor().getName())
			.score(myScore)
			.build();

		int totalPages = (int) Math.ceil((double) totalItems / pageable.getPageSize());
		return RankResponseDto.RankPage.builder()
			.page(pageable.getPageNumber())
			.pageSize(pageable.getPageSize())
			.totalItems(totalItems)
			.totalPages(totalPages)
			.myItem(myItem)
			.items(items)
			.build();
	}
}