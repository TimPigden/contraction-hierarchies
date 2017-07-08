package uk.me.mjt.ch.impl;

import uk.me.mjt.ch.AccessOnly;
import uk.me.mjt.ch.Barrier;
import uk.me.mjt.ch.DirectedEdge;
import uk.me.mjt.ch.Node;

import java.util.*;

public class NodeJ implements Node {
    
    final long _nodeId;
    final long _sourceDataNodeId;
    final ArrayList<DirectedEdge> _edgesFrom = new ArrayList<DirectedEdge>();
    final ArrayList<DirectedEdge> _edgesTo = new ArrayList<DirectedEdge>();
    final float _lat;
    final float _lon;
    boolean _contractionAllowed = true;
    Barrier _barrier;
    int _contractionOrder = UNCONTRACTED;

    @Override
    public long nodeId() {
        return _nodeId;
    }

    @Override
    public long sourceDataNodeId() {
        return _sourceDataNodeId;
    }

    @Override
    public ArrayList<DirectedEdge> edgesFrom() {
        return _edgesFrom;
    }

    @Override
    public ArrayList<DirectedEdge> edgesTo() {
        return _edgesTo;
    }

    @Override
    public float lat() {
        return _lat;
    }

    @Override
    public float lon() {
        return _lon;
    }

    @Override
    public boolean contractionAllowed() {
        return _contractionAllowed;
    }

    @Override
    public Barrier barrier() {
        return _barrier;
    }

    @Override
    public int contractionOrder() {
        return _contractionOrder;
    }

    @Override
    public Node setContractionAllowed(boolean allowed) {
        _contractionAllowed = allowed;
        return this;
    }

    @Override
    public Node setContractionOrder(int contractionOrder) {
        _contractionOrder = contractionOrder;
        return this;
    }

    public NodeJ(long nodeId, float lat, float lon, Barrier barrier) {
        this(nodeId, nodeId, lat, lon, barrier);
    }
    
    public NodeJ(long nodeId, Node copyFrom) {
        this(nodeId, copyFrom.sourceDataNodeId(), copyFrom.lat(), copyFrom.lon(), copyFrom.barrier());
    }
    
    public NodeJ(long nodeId, long sourceDataNodeId, float lat, float lon, Barrier barrier) {
        _nodeId = nodeId;
        _lat = lat;
        _lon = lon;
        _barrier = barrier;
        _sourceDataNodeId = sourceDataNodeId;
    }

    public int getCountOutgoingUncontractedEdges() {
        int count = 0;
        for (DirectedEdge de : edgesFrom()) {
            if (de.to().contractionOrder() == UNCONTRACTED)
                count++;
        }
        return count;
    }

    public int getCountIncomingUncontractedEdges() {
        int count = 0;
        for (DirectedEdge de : edgesTo()) {
            if (de.from().contractionOrder()  == UNCONTRACTED)
                count++;
        }
        return count;
    }
    
    @Override public boolean isContracted() {
        return contractionOrder () !=UNCONTRACTED;
    }
    
    @Override public boolean isSynthetic() {
        return (nodeId() !=sourceDataNodeId());
    }
    
    @Override public List<Node> getNeighbors() {
        HashSet<Node> neighbors = new HashSet<>();
        for (DirectedEdge de : edgesFrom()) {
            neighbors.add(de.to());
        }
        for (DirectedEdge de : edgesTo()) {
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
        return this.nodeId() ==other.nodeId();
    }

    @Override
    public int hashCode() {
        return (int) (this.nodeId() ^ (this.nodeId() >>> 32));
    }
    
    public String toString() {
        return nodeId() + "@" + lat() + "," + lon();
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
    
    @Override public void sortNeighborLists() {
        Collections.sort(edgesFrom(), new Comparator<DirectedEdge>() {
            @Override
            public int compare(DirectedEdge t, DirectedEdge t1) {
                if (t1.to().contractionOrder() != t.to().contractionOrder()) {
                    return Integer.compare(t1.to().contractionOrder(), t.to().contractionOrder());
                } else if (t1.driveTimeMs() != t.driveTimeMs()) {
                    return Integer.compare(t.driveTimeMs(), t1.driveTimeMs());
                } else if (t.to().nodeId() != t1.to().nodeId()) {
                    return Long.compare(t.to().nodeId(), t1.to().nodeId());
                } else {
                    return Long.compare(t.edgeId(), t1.edgeId());
                }
            }
        });
        
        Collections.sort(edgesTo(), new Comparator<DirectedEdge>() {
            @Override
            public int compare(DirectedEdge t, DirectedEdge t1) {
                if (t1.from().contractionOrder() != t.from().contractionOrder()) {
                    return Integer.compare(t1.from().contractionOrder(), t.from().contractionOrder());
                } else if (t1.driveTimeMs() != t.driveTimeMs()) {
                    return Integer.compare(t.driveTimeMs(), t1.driveTimeMs());
                } else if (t.from().nodeId() != t1.from().nodeId()) {
                    return Long.compare(t.from().nodeId(), t1.from().nodeId());
                } else {
                    return Long.compare(t.edgeId(), t1.edgeId());
                }
            }
        });
    }
    
    @Override public Set<DirectedEdge> getEdgesFromAndTo() {
        HashSet<DirectedEdge> result = new HashSet();
        result.addAll(edgesFrom());
        result.addAll(edgesTo());
        return result;
    }
    
    @Override public boolean anyEdgesAccessOnly() {
        for (DirectedEdge de : edgesFrom()) {
            if (de.accessOnly() == AccessOnly.TRUE) {
                return true;
            }
        }
        for (DirectedEdge de : edgesTo()) {
            if (de.accessOnly() == AccessOnly.TRUE) {
                return true;
            }
        }
        return false;
    }
    
    @Override public boolean allEdgesAccessOnly() {
        for (DirectedEdge de : edgesFrom()) {
            if (de.accessOnly() == AccessOnly.FALSE) {
                return false;
            }
        }
        for (DirectedEdge de : edgesTo()) {
            if (de.accessOnly() == AccessOnly.FALSE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(Node o) {
        if (o == null) return -1;
        return Long.compare(this.nodeId(), o.nodeId());
    }

}
