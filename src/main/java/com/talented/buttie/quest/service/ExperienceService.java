package com.talented.buttie.quest.service;

import com.talented.buttie.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final UserMapper userMapper;

    @Transactional
    public void deductExperience(Long userId, Integer expAmount) {
        if (userId == null || expAmount == null || expAmount <= 0) {
            return;
        }
        userMapper.deductUserExp(userId, expAmount);
    }

    @Transactional
    public void addExperience(Long userId, Integer expAmount) {
        if (userId == null || expAmount == null || expAmount <= 0) {
            return;
        }
        userMapper.addUserExp(userId, expAmount);
    }
}
