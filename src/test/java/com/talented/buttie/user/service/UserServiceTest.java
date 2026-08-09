package com.talented.buttie.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.domain.ButtieDashboardVO;
import com.talented.buttie.user.domain.EmploymentPreparationType;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.domain.UserProfileVO;
import com.talented.buttie.user.domain.UserVO;
import com.talented.buttie.user.dto.request.user.ModifyUserProfileRequest;
import com.talented.buttie.user.dto.request.user.UpdateEmploymentPreparationRequest;
import com.talented.buttie.user.dto.request.user.WithdrawUserRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private EmploymentPreparationMapper employmentPreparationMapper;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("취업 준비 정보를 수정하면 수정된 사용자의 ID를 반환한다.")
    void modifyEmploymentPreparation() {
        Long userId = 1L;
        UpdateEmploymentPreparationRequest request = new UpdateEmploymentPreparationRequest(
            "서울특별시",
            1,
            EmploymentPreparationType.FIRST_JOB,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2027, 1, 1)
        );

        given(employmentPreparationMapper.updateEmploymentPreparation(any(EmploymentPreparationVO.class)))
            .willReturn(1);

        Long result = userService.modifyEmploymentPreparation(userId, request);

        assertNotNull(result);
        assertEquals(userId, result);
        verify(employmentPreparationMapper).updateEmploymentPreparation(any(EmploymentPreparationVO.class));
    }

    @Test
    @DisplayName("취업 준비 정보 수정 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenModifyTargetNotFound() {
        Long userId = 999L;
        UpdateEmploymentPreparationRequest request = new UpdateEmploymentPreparationRequest(
            "서울특별시",
            1,
            EmploymentPreparationType.FIRST_JOB,
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2027, 1, 1)
        );

        given(employmentPreparationMapper.updateEmploymentPreparation(any(EmploymentPreparationVO.class)))
            .willReturn(0);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.modifyEmploymentPreparation(userId, request)
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
        ModifyUserProfileRequest request = new ModifyUserProfileRequest("새닉네임");

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
        ModifyUserProfileRequest request = new ModifyUserProfileRequest("중복닉네임");

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
    void throwWhenModifyProfileTargetNotFound() {
        Long userId = 999L;
        ModifyUserProfileRequest request = new ModifyUserProfileRequest("새닉네임");

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

    @Test
    @DisplayName("회원 프로필을 조회하면 해당 사용자의 프로필 정보를 반환한다.")
    void getUserProfile() {

        Long userId = 1L;
        UserProfileVO mockVO = new UserProfileVO();

        given(userMapper.selectUserProfile(userId))
            .willReturn(mockVO);

        UserProfileVO result = userService.getUserProfile(userId);

        assertNotNull(result);
        verify(userMapper).selectUserProfile(userId);
    }

    @Test
    @DisplayName("회원 프로필 조회 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenGetUserProfileNotFound() {
        Long userId = 999L;

        given(userMapper.selectUserProfile(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.getUserProfile(userId)
        );

        assertEquals(
            UserErrorCode.USER_NOT_FOUND,
            exception.getCode()
        );
    }

    @Test
    @DisplayName("버티 성장 대시보드를 조회하면 해당 사용자의 버티 정보를 반환한다.")
    void getButiDashboard() {

        Long userId = 1L;
        ButtieDashboardVO mockVO = new ButtieDashboardVO(
            1, "새싹 버티", "이제 막 자산관리를 시작한 기본 버티", 120, 0, "CAUTION", "https://cdn.buttie.com/buttie/lv1_caution.png"
        );

        given(userMapper.selectButtieDashboard(userId))
            .willReturn(mockVO);

        ButtieDashboardVO result = userService.getButtieDashboard(userId);

        assertNotNull(result);
        assertEquals(1, result.getButtieLevel());
        assertEquals("CAUTION", result.getRiskLevel());
        verify(userMapper).selectButtieDashboard(userId);
    }

    @Test
    @DisplayName("버티 성장 대시보드 조회 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenGetButiDashboardNotFound() {
        Long userId = 999L;

        given(userMapper.selectButtieDashboard(userId))
            .willReturn(null);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.getButtieDashboard(userId)
        );

        assertEquals(
            UserErrorCode.USER_NOT_FOUND,
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
        WithdrawUserRequest request = new WithdrawUserRequest(password);

        given(userMapper.getPasswordByUserId(userId)).willReturn(password);
        given(passwordEncoder.matches(password, password)).willReturn(true);
        given(userMapper.updateWithdrawnUser(any(UserVO.class))).willReturn(1);

        Long result = userService.withdrawUser(userId, request);

        assertEquals(userId, result);
        verify(userMapper).updateWithdrawnUser(any(UserVO.class));
    }

    @Test
    @DisplayName("회원 탈퇴 시 해당 사용자가 없으면 예외가 발생한다.")
    void throwWhenWithdrawTargetNotFound() {
        Long userId = 999L;
        WithdrawUserRequest request = new WithdrawUserRequest("password1234");

        given(userMapper.getPasswordByUserId(userId)).willReturn(null);

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
        WithdrawUserRequest request = new WithdrawUserRequest("wrongPassword");

        given(userMapper.getPasswordByUserId(userId)).willReturn("password1234");
        given(passwordEncoder.matches("wrongPassword", "password1234")).willReturn(false);

        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userService.withdrawUser(userId, request)
        );

        assertEquals(UserErrorCode.PASSWORD_MISMATCH, exception.getCode());
    }
}
