package nextstep.subway.path.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import nextstep.subway.auth.domain.LoginMember;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.path.dto.PathRequest;
import nextstep.subway.path.dto.PathResponse;
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
class PathServiceTest {

    private Station 교대역;
    private Station 강남역;
    private Station 남부터미널역;
    private Station 양재역;
    private Line 이호선;
    private Line 삼호선;
    private Line 신분당선;
    private LoginMember 로그인_사용자;

    @InjectMocks
    private PathService pathService;

    @Mock
    private StationService stationService;

    @Mock
    private LineRepository lineRepository;

    /**
     * 교대역   --- *2호선*(5) ---   강남역
     * |                            |
     * *3호선(3)*                 *신분당선*(5)
     * |                            |
     * 남부터미널역 --- *3호선*(2) --- 양재역
     */
    @BeforeEach
    public void setUp() {
        교대역 = new Station("교대역");
        강남역 = new Station("강남역");
        남부터미널역 = new Station("남부터미널역");
        양재역 = new Station("양재역");

        신분당선 = new Line("신분당선", "red", 강남역, 양재역, 5, 900);
        이호선 = new Line("이호선", "green", 교대역, 강남역, 5, 0);
        삼호선 = new Line("삼호선", "orange", 교대역, 양재역, 5, 0);
        삼호선.addSection(new Section(삼호선, 남부터미널역, 양재역, 2));

        로그인_사용자 = new LoginMember(1L, "email@email.com", 20);
    }

    @Test
    @DisplayName("교대역 -> 양재역의 최단 경로는 교대역 -> 남부터미널역 -> 양재역")
    void findPathTest() {
        // given
        given(stationService.findStationById(1L)).willReturn(교대역);
        given(stationService.findStationById(4L)).willReturn(양재역);
        given(lineRepository.findAll()).willReturn(Arrays.asList(이호선, 삼호선, 신분당선));

        // when
        PathResponse pathResponse = pathService.findPath(로그인_사용자, new PathRequest(1L, 4L));

        // then
        assertAll(
                () -> assertThat(pathResponse.getStations()).hasSize(3),
                () -> assertThat(pathResponse.getDistance()).isEqualTo(5),
                () -> assertThat(pathResponse.getFare()).isEqualTo(1250)
        );
    }

    @Test
    @DisplayName("조회하려는 최단 구간의 출발역과 도착역은 달라야 한다")
    void 출발역과_도착역이_같은_경우() {
        // given
        given(stationService.findStationById(1L)).willReturn(교대역);
        given(lineRepository.findAll()).willReturn(Arrays.asList(이호선, 삼호선, 신분당선));

        // when & then
        assertThatIllegalArgumentException().isThrownBy(
                () -> pathService.findPath(로그인_사용자, new PathRequest(1L, 1L))
        ).withMessageContaining("출발역과 도착역은 다른 역으로 지정되어야 합니다.");
    }

    @Test
    @DisplayName("조회하려는 최단 구간의 출발역과 도착역이 연결이 되어 있어야 한다")
    void 출발역과_도착역이_연결이_되어_있지_않은_경우() {
        // given
        Station 신논현역 = new Station("신논현역");
        Station 고속터미널역 = new Station("고속터미널역");
        Line 구호선 = new Line("구호선", "gold", 신논현역, 고속터미널역, 10);
        given(stationService.findStationById(5L)).willReturn(신논현역);
        given(stationService.findStationById(3L)).willReturn(강남역);
        given(lineRepository.findAll()).willReturn(Arrays.asList(이호선, 삼호선, 신분당선, 구호선));

        // when & then
        assertThatIllegalArgumentException().isThrownBy(
                () -> pathService.findPath(로그인_사용자, new PathRequest(5L, 3L))
        ).withMessageContaining("출발역과 도착역은 서로 연결이 되어있어야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 출발역이나 도착역을 조회 할 경우 예외를 반환한다")
    void 존재하지_않는_출발역이나_도착역을_조회() {
        // given
        final Station 신논현역 = new Station("신논현역");
        given(stationService.findStationById(5L)).willReturn(신논현역);
        given(stationService.findStationById(3L)).willReturn(강남역);
        given(lineRepository.findAll()).willReturn(Arrays.asList(이호선, 삼호선, 신분당선));

        // when & then
        assertAll(
                () -> assertThatIllegalArgumentException().isThrownBy(
                        () -> pathService.findPath(로그인_사용자, new PathRequest(5L, 3L))
                ).withMessageContaining("graph must contain"),
                () -> assertThatIllegalArgumentException().isThrownBy(
                        () -> pathService.findPath(로그인_사용자, new PathRequest(3L, 5L))
                ).withMessageContaining("graph must contain")
        );
    }
}

