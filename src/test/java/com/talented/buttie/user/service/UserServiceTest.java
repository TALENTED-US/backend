package com.talented.buttie.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.EmploymentPreparationType;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.ModifyUserProfileRequestDTO;
import com.talented.buttie.user.mapper.UserMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("취업 준비 정보를 저장하면 수정된 VO를 반환한다.")
    void saveEmploymentPreparation() {
        Long userId = 1L;
        UpdateEmploymentPreparationRequestDTO request = new UpdateEmploymentPreparationRequestDTO(
            LocalDate.of(1999, 3, 15),
            "서울특별시",
            1,
            EmploymentPreparationType.FIRST_JOB,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2027, 1, 1),
            0
        );

        given(employmentPreparationMapper.updateEmploymentPreparation(any(EmploymentPreparationVO.class)))
            .willReturn(1);

        EmploymentPreparationVO result = userService.saveEmploymentPreparation(userId, request);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("서울특별시", result.getEmploymentPrepRegion());
        verify(employmentPreparationMapper).updateEmploymentPreparation(any(EmploymentPreparationVO.class));
    }

    @Test
    @DisplayName("취업 준비 정보 저장 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenSaveTargetNotFound() {
        Long userId = 999L;
        UpdateEmploymentPreparationRequestDTO request = new UpdateEmploymentPreparationRequestDTO(
            LocalDate.of(1999, 3, 15),
            "서울특별시",
            1,
            EmploymentPreparationType.FIRST_JOB,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2027, 1, 1),
            0
        );

        given(employmentPreparationMapper.updateEmploymentPreparation(any(EmploymentPreparationVO.class)))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.saveEmploymentPreparation(userId, request)
        );

        assertEquals(
            UserErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND,
            exception.getCode()
        );
    }
    @Test
    @DisplayName("닉네임을 수정하면 수정된 사용자 ID를 반환한다.")
    void modifyUserProfile() {
        Long userId = 1L;
        ModifyUserProfileRequestDTO request = new ModifyUserProfileRequestDTO("새닉네임");

        given(userMapper.countByNickname("새닉네임"))
            .willReturn(0);
        given(userMapper.updateUser(any(UserVO.class)))
            .willReturn(1);

        Long result = userService.modifyUserProfile(userId, request);

        assertEquals(userId, result);
        verify(userMapper).countByNickname("새닉네임");
        verify(userMapper).updateUser(any(UserVO.class));
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임이면 예외가 발생한다.")
    void throwWhenDuplicateNickname() {
        Long userId = 1L;
        ModifyUserProfileRequestDTO request = new ModifyUserProfileRequestDTO("중복닉네임");

        given(userMapper.countByNickname("중복닉네임"))
            .willReturn(1);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.modifyUserProfile(userId, request)
        );

        assertEquals(
            UserErrorCode.DUPLICATE_NICKNAME,
            exception.getCode()
        );
        verify(userMapper, never()).updateUser(any(UserVO.class));
    }
    @Test
    @DisplayName("프로필 수정 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenModifyTargetNotFound() {
        Long userId = 999L;
        ModifyUserProfileRequestDTO request = new ModifyUserProfileRequestDTO("새닉네임");

        given(userMapper.countByNickname("새닉네임"))
            .willReturn(0);
        given(userMapper.updateUser(any(UserVO.class)))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.modifyUserProfile(userId, request)
        );

        assertEquals(
            UserErrorCode.USER_NOT_FOUND,
            exception.getCode()
        );
    }
}