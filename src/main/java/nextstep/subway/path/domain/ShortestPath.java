package nextstep.subway.path.domain;

import java.util.List;
import nextstep.subway.fare.domain.Fare;
import nextstep.subway.station.domain.Station;
import org.jgrapht.GraphPath;

public class ShortestPath {
    private static final int DEFAULT_FARE = 0;
    private final GraphPath<Station, SectionEdge> shortestPath;

    public ShortestPath(GraphPath<Station, SectionEdge> shortestPath) {
        if (shortestPath == null) {
            throw new IllegalArgumentException("출발역과 도착역은 서로 연결이 되어있어야 합니다.");
        }
        this.shortestPath = shortestPath;
    }

    public List<Station> getShortestStations() {
        return shortestPath.getVertexList();
    }

    public double getShortestDistance() {
        return shortestPath.getWeight();
    }

    public int getFare() {
        return Fare.calculateFare((int) shortestPath.getWeight(), getExtraFare());
    }

    private int getExtraFare() {
        return shortestPath.getEdgeList().stream()
                .mapToInt(station -> station.getLine().getExtraFare())
                .max()
                .orElse(DEFAULT_FARE);
    }
}