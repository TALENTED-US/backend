package com.talented.buttie.user.domain;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class UserProfileVO {

    private Long userId;
    private String buttieImageUrl;
    private Integer buttieLevel;
    private String userNickname;
    private String userName;
    private LocalDate birthDate;
    private String userPhoneNumber;
    private String userEmail;


}