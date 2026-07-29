package com.talented.buttie.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserButtieVO {

    private Long userId;
    private Integer buttieTotalExp;
    private Integer buttieLevel;
}
