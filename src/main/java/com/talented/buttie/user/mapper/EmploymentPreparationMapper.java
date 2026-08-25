package com.talented.buttie.user.mapper;

import com.talented.buttie.user.domain.EmploymentPreparationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmploymentPreparationMapper {

    Integer getLivingThresholdByUserId(@Param("userId") Long userId);

    int updateEmploymentPreparation(@Param("employmentPreparation") EmploymentPreparationVO employmentPreparation);

    Long createEmploymentPreparation(@Param("employmentPreparation") EmploymentPreparationVO employmentPreparation);

    EmploymentPreparationVO selectEmploymentPreparation(@Param("userId") Long userId);
}