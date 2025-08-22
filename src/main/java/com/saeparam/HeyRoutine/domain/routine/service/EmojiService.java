package com.saeparam.HeyRoutine.domain.routine.service;


import com.saeparam.HeyRoutine.domain.routine.entity.Emoji;
import com.saeparam.HeyRoutine.domain.routine.repository.EmojiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmojiService {

    private final EmojiRepository emojiRepository;

    @Transactional(readOnly = true)
    public List<Emoji> showEmoji(Pageable pageable) {
        return emojiRepository.findAll(pageable).stream().toList();
    }
}
