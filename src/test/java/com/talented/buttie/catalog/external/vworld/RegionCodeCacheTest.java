package com.talented.buttie.catalog.external.vworld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegionCodeCacheTest {

    @Mock
    private VWorldRegionClient vWorldRegionClient;

    @InjectMocks
    private RegionCodeCache regionCodeCache;

    @Test
    @DisplayName("캐시에 없는 zipCd는 예외 없이 전국을 반환한다.")
    void resolveReturnsNationwideOnCacheMiss() {
        assertEquals("전국", regionCodeCache.resolve("99999"));
    }

    @Test
    @DisplayName("캐시 갱신 후에는 시/도, 시군구, 광역 코드를 모두 조회할 수 있다.")
    void resolveAfterRefresh() {
        given(vWorldRegionClient.fetchSidoList())
            .willReturn(List.of(new AdmCodeItem("서울특별시", "11", "서울특별시")));
        given(vWorldRegionClient.fetchSigunguList("11"))
            .willReturn(List.of(new AdmCodeItem("서울특별시 강남구", "11680", "강남구")));

        regionCodeCache.refresh();

        assertEquals("서울특별시", regionCodeCache.resolve("11"));
        assertEquals("서울특별시", regionCodeCache.resolve("11000"));
        assertEquals("강남구", regionCodeCache.resolve("11680"));
        assertEquals("전국", regionCodeCache.resolve("12345"));
    }
}
