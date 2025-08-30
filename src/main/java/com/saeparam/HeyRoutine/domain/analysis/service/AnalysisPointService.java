package com.saeparam.HeyRoutine.domain.analysis.service;

import com.saeparam.HeyRoutine.domain.analysis.dto.response.MaxStreakResponseDto;
import com.saeparam.HeyRoutine.domain.analysis.dto.response.WeeklyPointResponseDto;
import com.saeparam.HeyRoutine.domain.user.entity.User;
import com.saeparam.HeyRoutine.domain.user.repository.UserRepository;
import com.saeparam.HeyRoutine.global.error.handler.AnalysisHandler;
import com.saeparam.HeyRoutine.global.error.handler.UserHandler;
import com.saeparam.HeyRoutine.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisPointService {

	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;
	private final AnalysisService analysisService;

	private static final long BONUS_POINT = 100L;

	public WeeklyPointResponseDto giveWeeklyPoint(UUID userId) {
		MaxStreakResponseDto streak = analysisService.calculateMaxStreak(userId);
		if (streak.getStreakDays() < 7) {
			throw new AnalysisHandler(ErrorStatus.WEEKLY_BONUS_NOT_ELIGIBLE);
		}

		String key = createWeeklyKey(userId);
		Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", 8, TimeUnit.DAYS);
		if (Boolean.FALSE.equals(success)) {
			throw new AnalysisHandler(ErrorStatus.WEEKLY_BONUS_ALREADY_RECEIVED);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
		user.addPoint(BONUS_POINT);
		userRepository.save(user);

		return new WeeklyPointResponseDto(BONUS_POINT, user.getPoint());
	}

	private String createWeeklyKey(UUID userId) {
		LocalDate now = LocalDate.now();
		WeekFields wf = WeekFields.ISO;
		int year = now.get(wf.weekBasedYear());
		int week = now.get(wf.weekOfWeekBasedYear());
		return String.format("analysis:weekly-bonus:%s:%d%02d", userId, year, week);
	}
}