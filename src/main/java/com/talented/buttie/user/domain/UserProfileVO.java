package com.talented.buttie.user.domain;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UserProfileVO {

    private Long userId;
    private String nickname;
    private Integer buttieTotalExp;
    private Integer buttieLevel;
    private String username;
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
}