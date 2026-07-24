package com.talented.buttie.user.mapper;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface EmploymentPreparationMapper {

    void insertEmploymentPreparation(@Param("ep") EmploymentPreparationVO ep);
}