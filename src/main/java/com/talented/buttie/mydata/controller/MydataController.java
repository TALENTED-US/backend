package com.talented.buttie.mydata.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetRegistrationResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetsResponse;
import com.talented.buttie.mydata.dto.response.MydataConnectionResponse;
import com.talented.buttie.mydata.dto.response.MydataInstitutionResponse;
import com.talented.buttie.mydata.service.FixedExpenseCandidateService;
import com.talented.buttie.mydata.service.MydataAssetQueryService;
import com.talented.buttie.mydata.service.MydataConnectionService;
import com.talented.buttie.mydata.service.MydataOnboardingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Mydata")
@RestController
@RequestMapping("/api/mydata")
@RequiredArgsConstructor
public class MydataController {

    private final MydataAssetQueryService mydataAssetQueryService;
    private final MydataConnectionService mydataConnectionService;
    private final MydataOnboardingService mydataOnboardingService;
    private final FixedExpenseCandidateService fixedExpenseCandidateService;

    @ApiOperation("마이데이터 연결 및 인증정보 저장")
    @PostMapping("/connect")
    public ApplicationResponse<MydataConnectionResponse> connect(
        @AuthUser AuthenticationUser user
    ) {
        return ApplicationResponse.onSuccess(
            MydataConnectionResponse.from(mydataConnectionService.connect(user.userId()))
        );
    }

    @ApiOperation("마이데이터 계좌 및 체크카드 목록 조회")
    @GetMapping("/assets")
    public ApplicationResponse<MydataAssetsResponse> getAssets(
        @AuthUser AuthenticationUser user
    ) {
        return ApplicationResponse.onSuccess(mydataAssetQueryService.getAssets(user.userId()));
    }

    @ApiOperation("마이데이터 금융기관 목록 조회")
    @GetMapping("/institutions")
    public ApplicationResponse<List<MydataInstitutionResponse>> getInstitutions(
        @AuthUser AuthenticationUser user
    ) {
        return ApplicationResponse.onSuccess(
            mydataAssetQueryService.getInstitutions(user.userId())
        );
    }

    @ApiOperation("선택 자산 등록 및 거래내역 분석")
    @PostMapping("/assets")
    public ApplicationResponse<MydataAssetRegistrationResponse> registerAssets(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody RegisterMydataAssetsRequest request
    ) {
        return ApplicationResponse.onSuccess(
            mydataOnboardingService.registerAndAnalyze(user.userId(), request)
        );
    }

    @ApiOperation("마이데이터 고정지출 후보 조회")
    @GetMapping("/fixed-expense-candidates")
    public ApplicationResponse<List<FixedExpenseCandidateResponse>> getFixedExpenseCandidates(
        @AuthUser AuthenticationUser user
    ) {
        return ApplicationResponse.onSuccess(
            fixedExpenseCandidateService.findCandidates(user.userId())
        );
    }

}
