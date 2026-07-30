package com.talented.buttie.admin.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminVO {

    private Long adminId;
    private String adminEmail;
    @ToString.Exclude
    private String adminPasswordHash;
    private AdminRole adminRole;
    private AdminStatus adminStatus;
    private LocalDateTime adminCreatedAt;
    private LocalDateTime adminUpdatedAt;
}
