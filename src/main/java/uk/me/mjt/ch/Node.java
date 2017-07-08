package uk.me.mjt.ch;

import java.util.*;

public class Node implements Comparable<Node> {
    public static final int UNCONTRACTED = Integer.MAX_VALUE;
    
    public final long nodeId;
    public final long sourceDataNodeId;
    public final ArrayList<DirectedEdge> edgesFrom = new ArrayList<DirectedEdge>();
    public final ArrayList<DirectedEdge> edgesTo = new ArrayList<DirectedEdge>();
    public final float lat;
    public final float lon;
    public boolean contractionAllowed = true;
    public Barrier barrier;

    public int contractionOrder = UNCONTRACTED;
    
    public Node(long nodeId, float lat, float lon, Barrier barrier) {
        this(nodeId, nodeId, lat, lon, barrier);
    }
    
    public Node(long nodeId, Node copyFrom) {
        this(nodeId, copyFrom.sourceDataNodeId, copyFrom.lat, copyFrom.lon, copyFrom.barrier);
    }
    
    public Node(long nodeId, long sourceDataNodeId, float lat, float lon, Barrier barrier) {
        this.nodeId = nodeId;
        this.lat = lat;
        this.lon = lon;
        this.barrier = barrier;
        this.sourceDataNodeId = sourceDataNodeId;
    }

    int getCountOutgoingUncontractedEdges() {
        int count = 0;
        for (DirectedEdge de : edgesFrom) {
            if (de.to().contractionOrder == UNCONTRACTED)
                count++;
        }
        return count;
    }

    int getCountIncomingUncontractedEdges() {
        int count = 0;
        for (DirectedEdge de : edgesTo) {
            if (de.from().contractionOrder  == UNCONTRACTED)
                count++;
        }
        return count;
    }
    
    public boolean isContracted() {
        return contractionOrder!=UNCONTRACTED;
    }
    
    public boolean isSynthetic() {
        return (nodeId!=sourceDataNodeId);
    }
    
    public List<Node> getNeighbors() {
        HashSet<Node> neighbors = new HashSet<>();
        for (DirectedEdge de : edgesFrom) {
            neighbors.add(de.to());
        }
        for (DirectedEdge de : edgesTo) {
            neighbors.add(de.from());
        }
        return new ArrayList(neighbors);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        return this.nodeId==other.nodeId;
    }

    @Override
    public int hashCode() {
        return (int) (this.nodeId ^ (this.nodeId >>> 32));
    }
    
    public String toString() {
        return nodeId + "@" + lat + "," + lon;
    }
    
    /**
     * Sort incoming and outgoing lists of edges. Follows the following rules:
     * 1. Higher contraction order first. We do this so, if we want to find 
     * connected nodes with a higher contraction order than this one, they'll
     * be at the start of the list. When all nodes are contracted, every node
     * will have a different contraction order.
     * 2. Shorter distance first. If contraction orders are equal for two edges,
     * it means either the node on the other end is uncontracted. Shorter edges
     * are usually more interesting, and we want the sort order to be 
     * unambiguous, so this is our second means of ordering.
     * 3. If results are equal for both those tests, sort by edge ID, which 
     * should always be unique, to give us an unambiguous ordering.
     */
    
    public void sortNeighborLists() {
        Collections.sort(edgesFrom, new Comparator<DirectedEdge>() {
            @Override
            public int compare(DirectedEdge t, DirectedEdge t1) {
                if (t1.to().contractionOrder != t.to().contractionOrder) {
                    return Integer.compare(t1.to().contractionOrder, t.to().contractionOrder);
                } else if (t1.driveTimeMs() != t.driveTimeMs()) {
                    return Integer.compare(t.driveTimeMs(), t1.driveTimeMs());
                } else if (t.to().nodeId != t1.to().nodeId) {
                    return Long.compare(t.to().nodeId, t1.to().nodeId);
                } else {
                    return Long.compare(t.edgeId(), t1.edgeId());
                }
            }
        });
        
        Collections.sort(edgesTo, new Comparator<DirectedEdge>() {
            @Override
            public int compare(DirectedEdge t, DirectedEdge t1) {
                if (t1.from().contractionOrder != t.from().contractionOrder) {
                    return Integer.compare(t1.from().contractionOrder, t.from().contractionOrder);
                } else if (t1.driveTimeMs() != t.driveTimeMs()) {
                    return Integer.compare(t.driveTimeMs(), t1.driveTimeMs());
                } else if (t.from().nodeId != t1.from().nodeId) {
                    return Long.compare(t.from().nodeId, t1.from().nodeId);
                } else {
                    return Long.compare(t.edgeId(), t1.edgeId());
                }
            }
        });
    }
    
    public Set<DirectedEdge> getEdgesFromAndTo() {
        HashSet<DirectedEdge> result = new HashSet();
        result.addAll(edgesFrom);
        result.addAll(edgesTo);
        return result;
    }
    
    public boolean anyEdgesAccessOnly() {
        for (DirectedEdge de : edgesFrom) {
            if (de.accessOnly() == AccessOnly.TRUE) {
                return true;
            }
        }
        for (DirectedEdge de : edgesTo) {
            if (de.accessOnly() == AccessOnly.TRUE) {
                return true;
            }
        }
        return false;
    }
    
    public boolean allEdgesAccessOnly() {
        for (DirectedEdge de : edgesFrom) {
            if (de.accessOnly() == AccessOnly.FALSE) {
                return false;
            }
        }
        for (DirectedEdge de : edgesTo) {
            if (de.accessOnly() == AccessOnly.FALSE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(Node o) {
        if (o == null) return -1;
        return Long.compare(this.nodeId, o.nodeId);
    }
    
    public static void sortNeighborListsAll(Collection<Node> nodes) {
        for (Node n : nodes) {
            n.sortNeighborLists();
        }
    }
    
}
