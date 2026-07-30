package com.talented.buttie.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.EmploymentPreparationType;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.domain.UserStatus;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.dto.request.WithdrawUserRequestDTO;
import com.talented.buttie.user.exception.UserErrorCode;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
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
    @DisplayName("취업 준비 정보를 조회하면 해당 사용자의 정보를 반환한다.")
    void getEmploymentPreparation() {
        Long userId = 1L;
        EmploymentPreparationVO employmentPreparation = EmploymentPreparationVO.builder()
            .userId(userId)
            .employmentPrepRegion("서울특별시")
            .familyCount(1)
            .employmentPrepType(EmploymentPreparationType.FIRST_JOB)
            .prepStartDate(LocalDate.of(2026, 7, 1))
            .targetEmploymentDate(LocalDate.of(2027, 1, 1))
            .build();

        given(employmentPreparationMapper.selectEmploymentPreparation(userId))
            .willReturn(employmentPreparation);

        EmploymentPreparationVO result = userService.getEmploymentPreparation(userId);

        assertEquals(userId, result.getUserId());
        assertEquals("서울특별시", result.getEmploymentPrepRegion());
        verify(employmentPreparationMapper).selectEmploymentPreparation(userId);
    }

    @Test
    @DisplayName("취업 준비 정보 조회 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenGetTargetNotFound() {
        Long userId = 999L;

        given(employmentPreparationMapper.selectEmploymentPreparation(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.getEmploymentPreparation(userId)
        );

        assertEquals(
            UserErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND,
            exception.getCode()
        );
    }

    @Test
    @DisplayName("비밀번호가 일치하면 회원을 탈퇴 처리하고 userId를 반환한다.")
    void withdrawUser() {
        Long userId = 1L;
        String password = "password1234";
        WithdrawUserRequestDTO request = new WithdrawUserRequestDTO(password);
        UserVO user = UserVO.builder()
            .userId(userId)
            .userPasswordHash(password)
            .userStatus(UserStatus.ACTIVE)
            .build();

        given(userMapper.selectUserById(userId)).willReturn(user);

        Long result = userService.withdrawUser(userId, request);

        assertEquals(userId, result);
        verify(userMapper).updateWithdrawnUser(any(UserVO.class));
    }

    @Test
    @DisplayName("회원 탈퇴 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenWithdrawTargetNotFound() {
        Long userId = 999L;
        WithdrawUserRequestDTO request = new WithdrawUserRequestDTO("password1234");

        given(userMapper.selectUserById(userId)).willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.withdrawUser(userId, request)
        );

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getCode());
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호가 일치하지 않으면 예외가 발생한다.")
    void throwWhenWithdrawPasswordMismatch() {
        Long userId = 1L;
        WithdrawUserRequestDTO request = new WithdrawUserRequestDTO("wrongPassword");
        UserVO user = UserVO.builder()
            .userId(userId)
            .userPasswordHash("password1234")
            .userStatus(UserStatus.ACTIVE)
            .build();

        given(userMapper.selectUserById(userId)).willReturn(user);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.withdrawUser(userId, request)
        );

        assertEquals(UserErrorCode.PASSWORD_MISMATCH, exception.getCode());
    }

}