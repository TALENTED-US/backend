package com.talented.buttie.user.service;

import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.UpdateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.talented.buttie.common.exception.ApplicationException;
import com.talented.buttie.user.exception.UserErrorCode;

@Service
@RequiredArgsConstructor
public class UserService {
    private final EmploymentPreparationMapper employmentPreparationMapper;
    public void saveEmploymentPreparation(Long userId, UpdateEmploymentPreparationRequestDTO request) {

        EmploymentPreparationVO vo = EmploymentPreparationVO.builder()
            .userId(userId)
            .birthDate(request.birthDate())
            .region(request.region())
            .familyCount(request.familyCount())
            .employmentPrepType(request.employmentPrepType())
            .prepStartDate(request.prepStartDate())
            .targetEmploymentDate(request.targetEmploymentDate())
            .livingFundThreshold(request.livingFundThreshold())
            .build();

        int updated = employmentPreparationMapper.updateEmploymentPreparation(vo);
        if (updated == 0) {
            throw ApplicationException.from(UserErrorCode.EMPLOYMENT_PREPARATION_NOT_FOUND);
        }
    }
}
