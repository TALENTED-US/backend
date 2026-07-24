package com.talented.buttie.user.service;

import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.dto.request.CreateEmploymentPreparationRequestDTO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final EmploymentPreparationMapper employmentPreparationMapper;
    public void createEmploymentPreparation(Long userId, CreateEmploymentPreparationRequestDTO request) {

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

        employmentPreparationMapper.insertEmploymentPreparation(vo);
    }
}
