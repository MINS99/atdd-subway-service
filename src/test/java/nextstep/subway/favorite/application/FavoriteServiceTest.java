package nextstep.subway.favorite.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import nextstep.subway.auth.domain.LoginMember;
import nextstep.subway.favorite.domain.Favorite;
import nextstep.subway.favorite.domain.FavoriteRepository;
import nextstep.subway.favorite.dto.FavoriteRequest;
import nextstep.subway.favorite.dto.FavoriteResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    private Station 강남역;
    private Station 잠실역;
    private LoginMember 로그인_사용자;
    private Favorite 즐겨찾기;

    @InjectMocks
    FavoriteService favoriteService;

    @Mock
    FavoriteRepository favoriteRepository;

    @Mock
    StationService stationService;

    @BeforeEach
    void setUp() {
        강남역 = new Station("강남역");

        잠실역 = new Station("잠실역");

        로그인_사용자 = new LoginMember(1L, "email@email.com", 20);

        즐겨찾기 = new Favorite(강남역, 잠실역, 로그인_사용자.getId());
    }

    @Test
    @DisplayName("즐겨찾기를 저장한다")
    void save() {
        // given
        given(stationService.findStationById(1L)).willReturn(강남역);
        given(stationService.findStationById(2L)).willReturn(잠실역);
        given(favoriteRepository.save(any())).willReturn(즐겨찾기);

        // when
        FavoriteResponse favorite = favoriteService.createFavorite(로그인_사용자, new FavoriteRequest(1L, 2L));

        // then
        assertAll(
                () -> assertThat(favorite).isNotNull(),
                () -> assertThat(favorite.getSource().getName()).isEqualTo("강남역"),
                () -> assertThat(favorite.getTarget().getName()).isEqualTo("잠실역")
        );
    }

    @Test
    @DisplayName("즐겨찾기를 조회한 결과 1개의 즐겨찾기가 반환된다")
    void findFavorites() {
        // given
        given(favoriteRepository.findAllByMemberId(any())).willReturn(Collections.singletonList(즐겨찾기));

        // when
        List<FavoriteResponse> favoriteResponseList = favoriteService.findFavorite(로그인_사용자);

        // then
        assertThat(favoriteResponseList).hasSize(1);
    }
}
