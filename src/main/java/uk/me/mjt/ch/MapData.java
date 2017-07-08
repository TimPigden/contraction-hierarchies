package uk.me.mjt.ch;

import uk.me.mjt.ch.status.StatusMonitor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public interface MapData {
    AtomicLong getEdgeIdCounter();

    Node getNodeById(long nodeId);

    ColocatedNodeSet getNodeBySourceDataId(long nodeId);

    int getNodeCount();

    Collection<Node> getAllNodes();

    Set<Long> getAllNodeIds();

    List<Node> chooseRandomNodes(int howMany);

    Set<TurnRestriction> allTurnRestrictions();

    void validate(StatusMonitor monitor);

    List<Node> nodesInBbox(double lat1, double lon1, double lat2, double lon2);

    public static class InvalidMapDataException extends RuntimeException {
        public InvalidMapDataException(String reason) {
            super(reason);
        }
    }
}
