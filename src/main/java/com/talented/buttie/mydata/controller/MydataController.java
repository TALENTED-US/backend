package com.talented.buttie.mydata.controller;

import com.talented.buttie.common.response.ApplicationResponse;
import com.talented.buttie.common.security.AuthenticationUser;
import com.talented.buttie.common.security.annotation.AuthUser;
import com.talented.buttie.mydata.dto.request.RegisterMydataAssetsRequest;
import com.talented.buttie.mydata.dto.response.FixedExpenseCandidateResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetDeletionResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetRegistrationResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetsResponse;
import com.talented.buttie.mydata.dto.response.MydataConnectionResponse;
import com.talented.buttie.mydata.dto.response.MydataInstitutionResponse;
import com.talented.buttie.mydata.dto.response.MydataTransactionSyncResponse;
import com.talented.buttie.mydata.domain.MydataAssetType;
import com.talented.buttie.mydata.service.FixedExpenseCandidateService;
import com.talented.buttie.mydata.service.MydataAssetDeleteService;
import com.talented.buttie.mydata.service.MydataAssetQueryService;
import com.talented.buttie.mydata.service.MydataAssetRegistrationService;
import com.talented.buttie.mydata.service.MydataConnectionService;
import com.talented.buttie.mydata.service.MydataTransactionSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final MydataAssetRegistrationService mydataAssetRegistrationService;
    private final MydataTransactionSyncService mydataTransactionSyncService;
    private final MydataAssetDeleteService mydataAssetDeleteService;
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

    @ApiOperation("선택한 마이데이터 자산 등록")
    @PostMapping("/assets")
    public ApplicationResponse<MydataAssetRegistrationResponse> registerAssets(
        @AuthUser AuthenticationUser user,
        @Valid @RequestBody RegisterMydataAssetsRequest request
    ) {
        MydataAssetRegistrationService.RegisteredAssets assets =
            mydataAssetRegistrationService.registerSelectedAssets(user.userId(), request);
        return ApplicationResponse.onSuccess(
            new MydataAssetRegistrationResponse(
                assets.accounts().size(),
                assets.cards().size()
            )
        );
    }

    @ApiOperation("마이데이터 거래내역 동기화 및 분석")
    @PostMapping("/transactions/sync")
    public ApplicationResponse<MydataTransactionSyncResponse> syncTransactions(
        @AuthUser AuthenticationUser user
    ) {
        return ApplicationResponse.onSuccess(
            mydataTransactionSyncService.syncAndAnalyze(user.userId())
        );
    }

    @ApiOperation("선택한 마이데이터 자산 연동 해제")
    @DeleteMapping("/assets/{assetType}/{assetId}")
    public ApplicationResponse<MydataAssetDeletionResponse> deleteAsset(
        @AuthUser AuthenticationUser user,
        @PathVariable MydataAssetType assetType,
        @PathVariable String assetId
    ) {
        return ApplicationResponse.onSuccess(
            mydataAssetDeleteService.deactivate(user.userId(), assetType, assetId)
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
