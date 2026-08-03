package com.talented.buttie.user.domain;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UserProfileVO {

    private Long userId;
    private String userNickname;
    private Integer buttieTotalExp;
    private Integer buttieLevel;
    private String userName;
    private LocalDate birthDate;
    private String userPhoneNumber;
    private String userEmail;
}