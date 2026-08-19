package com.talented.buttie.simulation.service.llm;

import com.talented.buttie.simulation.dto.response.recommendation.IncomeJobSearchResponse;
import com.talented.buttie.simulation.domain.FinancialSnapshotVO;
import com.talented.buttie.simulation.domain.SimulationItemCategory;
import com.talented.buttie.simulation.domain.SimulationRecurrenceType;
import com.talented.buttie.simulation.domain.SimulationVO;
import com.talented.buttie.simulation.domain.SimulationItemVO;
import com.talented.buttie.simulation.mapper.FinancialSnapshotMapper;
import com.talented.buttie.simulation.mapper.SimulationItemMapper;
import com.talented.buttie.simulation.mapper.SimulationMapper;
import com.talented.buttie.user.domain.EmploymentPreparationVO;
import com.talented.buttie.user.mapper.EmploymentPreparationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class IncomeJobSearchLinkService {

    private static final int DEFAULT_AVAILABLE_HOURS_PER_WEEK = 8;
    private static final double AVERAGE_WEEKS_PER_MONTH = 4.345;
    private static final double HOURLY_WAGE_BUFFER_RATE = 1.10;
    private static final int WAGE_ROUNDING_UNIT = 100;
    // 2026년 적용 최저시급. 다음 연도 최저임금 고시 시 갱신한다.
    private static final int MINIMUM_HOURLY_WAGE = 10_320;
    private static final List<RegionCode> ALBA_REGION_CODES = List.of(
        new RegionCode("서울", "02"),
        new RegionCode("경기", "031"),
        new RegionCode("인천", "032"),
        new RegionCode("강원", "033"),
        new RegionCode("충남", "041"),
        new RegionCode("대전", "042"),
        new RegionCode("충북", "043"),
        new RegionCode("세종", "044"),
        new RegionCode("부산", "051"),
        new RegionCode("울산", "052"),
        new RegionCode("대구", "053"),
        new RegionCode("경북", "054"),
        new RegionCode("경남", "055"),
        new RegionCode("전남", "061"),
        new RegionCode("광주", "062"),
        new RegionCode("전북", "063"),
        new RegionCode("제주", "064")
    );
    private final EmploymentPreparationMapper employmentPreparationMapper;
    private final FinancialSnapshotMapper financialSnapshotMapper;
    private final SimulationMapper simulationMapper;
    private final SimulationItemMapper simulationItemMapper;

    public IncomeJobSearchResponse create(Long userId, String prompt) {
        EmploymentPreparationVO preparation = employmentPreparationMapper.selectEmploymentPreparation(userId);
        String region = preparation == null || preparation.getEmploymentPrepRegion() == null
            ? "내 주변" : preparation.getEmploymentPrepRegion();
        String keyword = region + " 지역 공고";
        IncomeTarget target = calculateIncomeTarget(userId);

        return IncomeJobSearchResponse.builder()
            .searchKeyword(keyword)
            .region(region)
            .requiredMonthlyIncome(target.requiredMonthlyIncome())
            .appliedMonthlyExpenseReduction(target.appliedMonthlyExpenseReduction())
            .availableHoursPerWeek(DEFAULT_AVAILABLE_HOURS_PER_WEEK)
            .recommendedMinimumHourlyWage(target.recommendedMinimumHourlyWage())
            .notice(createNotice(target))
            .links(List.of(
                IncomeJobSearchResponse.JobSearchLink.builder()
                    .platform("알바천국")
                    .url(createAlbaSearchUrl(region))
                    .build(),
                IncomeJobSearchResponse.JobSearchLink.builder()
                    .platform("알바몬")
                    .url(createAlbamonSearchUrl(region))
                    .build()
            ))
            .jobs(List.of())
            .build();
    }

    private IncomeTarget calculateIncomeTarget(Long userId) {
        FinancialSnapshotVO snapshot = financialSnapshotMapper.findLatestByUserId(userId);
        int monthlyDeficit = snapshot == null || snapshot.getMonthlyNetCashflow() == null
            ? 0 : Math.max(0, -snapshot.getMonthlyNetCashflow());

        SimulationVO simulation = simulationMapper.findActiveByUserId(userId);
        int monthlyExpenseReduction = simulation == null ? 0 : simulationItemMapper
            .findAllActiveBySimulationId(simulation.getSimulationId())
            .stream()
            .filter(item -> item.getSimulationItemCategory() == SimulationItemCategory.EXPENSE)
            .filter(item -> item.getRecurrenceType() == SimulationRecurrenceType.MONTHLY)
            .map(SimulationItemVO::getSimulationItemApplyAmount)
            .filter(amount -> amount != null && amount > 0)
            .mapToInt(Integer::intValue)
            .sum();

        int requiredMonthlyIncome = roundDownToHundred(Math.max(0, monthlyDeficit - monthlyExpenseReduction));
        int minimumHourlyWage = requiredMonthlyIncome == 0 ? 0 : Math.max(MINIMUM_HOURLY_WAGE,
            roundUpToHundred(requiredMonthlyIncome / (AVERAGE_WEEKS_PER_MONTH * DEFAULT_AVAILABLE_HOURS_PER_WEEK)
                * HOURLY_WAGE_BUFFER_RATE));
        return new IncomeTarget(requiredMonthlyIncome, monthlyExpenseReduction, minimumHourlyWage);
    }

    private int roundUpToHundred(double amount) {
        return (int) (Math.ceil(amount / WAGE_ROUNDING_UNIT) * WAGE_ROUNDING_UNIT);
    }

    private int roundDownToHundred(int amount) {
        return amount / WAGE_ROUNDING_UNIT * WAGE_ROUNDING_UNIT;
    }

    private String createNotice(IncomeTarget target) {
        if (target.requiredMonthlyIncome() == 0) {
            return "선택한 지출 절감안을 반영하면 추가 소득 없이도 현재 월 적자를 해소할 수 있어요. "
                + "알바천국이나 알바몬에서 지역 조건으로 공고를 확인해 보세요.";
        }
        String summary = "선택한 지출 절감안을 반영하면 매월 "
            + String.format("%,d", target.requiredMonthlyIncome()) + "원의 추가 소득이 필요해요. 주당 "
            + DEFAULT_AVAILABLE_HOURS_PER_WEEK + "시간 기준 시급 "
            + String.format("%,d", target.recommendedMinimumHourlyWage()) + "원 이상 공고를 찾아보세요.";
        return summary + " 아래 채용 플랫폼에서 지역 조건으로 공고를 찾아보세요.";
    }

    private String createAlbaSearchUrl(String region) {
        String regionCode = ALBA_REGION_CODES.stream()
            .filter(code -> region.contains(code.name()))
            .map(RegionCode::code)
            .findFirst()
            .orElse(null);
        if (regionCode == null) {
            return "https://www.alba.co.kr/job/area/MainArea";
        }

        return UriComponentsBuilder.fromHttpUrl("https://www.alba.co.kr/job/main")
            .queryParam("hidListView", "LIST")
            .queryParam("hidSortCnt", 50)
            .queryParam("hidSortFilter", "Y")
            .queryParam("hidJobKindMulti", "11000000")
            .queryParam("page", 1)
            .queryParam("hidSearchyn", "Y")
            .queryParam("strAreaMulti", regionCode + "||전체||")
            .queryParam("selGugun", "전체")
            .queryParam("viewtype", "L")
            .build()
            .encode()
            .toUriString();
    }

    private String createAlbamonSearchUrl(String region) {
        // 알바몬의 서울 전체 지역 코드(I0000000)는 실제 검색 결과 페이지를 기준으로 검증했다.
        // 다른 지역 코드는 검증 전까지 잘못된 공고로 연결하지 않도록 지역 선택 화면으로 보낸다.
        if (region.contains("서울")) {
            return "https://www.albamon.com/jobs/area?areas=I0000000";
        }
        return "https://www.albamon.com/jobs/area";
    }

    private record IncomeTarget(
        int requiredMonthlyIncome,
        int appliedMonthlyExpenseReduction,
        int recommendedMinimumHourlyWage
    ) {
    }

    private record RegionCode(String name, String code) {
    }

}
