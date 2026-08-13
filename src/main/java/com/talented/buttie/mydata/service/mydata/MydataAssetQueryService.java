package com.talented.buttie.mydata.service.mydata;

import com.talented.buttie.mydata.client.MydataApiClient;
import com.talented.buttie.mydata.dto.response.MydataAccountResponse;
import com.talented.buttie.mydata.dto.response.MydataAssetsResponse;
import com.talented.buttie.mydata.dto.response.MydataCardResponse;
import com.talented.buttie.mydata.dto.response.MydataInstitutionResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MydataAssetQueryService {

    private final MydataApiClient mydataApiClient;
    private final MydataConnectionValidator mydataConnectionValidator;

    public MydataAssetsResponse getAssets(Long userId) {
        mydataConnectionValidator.validateConnected(userId);
        List<MydataAccountResponse> accounts = mydataApiClient.getAccounts(userId).stream()
            .map(MydataAccountResponse::from)
            .collect(Collectors.toList());
        List<MydataCardResponse> cards = mydataApiClient.getDebitCards(userId).stream()
            .map(MydataCardResponse::from)
            .collect(Collectors.toList());

        return MydataAssetsResponse.builder()
            .accounts(accounts)
            .cards(cards)
            .build();
    }

    public List<MydataInstitutionResponse> getInstitutions(Long userId) {
        mydataConnectionValidator.validateConnected(userId);
        Map<String, int[]> counts = new LinkedHashMap<>();
        mydataApiClient.getAccounts(userId).forEach(account -> {
            String name = normalizeInstitutionName(account.getInstitutionName());
            counts.computeIfAbsent(name, ignored -> new int[2])[0]++;
        });
        mydataApiClient.getDebitCards(userId).forEach(card -> {
            String name = normalizeInstitutionName(card.getInstitutionName());
            counts.computeIfAbsent(name, ignored -> new int[2])[1]++;
        });

        return counts.entrySet().stream()
            .map(entry -> new MydataInstitutionResponse(
                entry.getKey(),
                entry.getValue()[0],
                entry.getValue()[1]
            ))
            .collect(Collectors.toList());
    }

    private String normalizeInstitutionName(String institutionName) {
        if (institutionName == null) {
            return "기타 금융기관";
        }
        return switch (institutionName) {
            case "KB국민카드" -> "KB국민은행";
            case "신한카드" -> "신한은행";
            case "우리카드" -> "우리은행";
            case "하나카드" -> "하나은행";
            case "NH농협카드" -> "NH농협은행";
            default -> institutionName;
        };
    }
}
