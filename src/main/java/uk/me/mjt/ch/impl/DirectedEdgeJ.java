package uk.me.mjt.ch.impl;

import uk.me.mjt.ch.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DirectedEdgeJ implements DirectedEdge {
    @Override
    public Node from() {
        return _from;
    }

    @Override
    public Node to() {
        return _to;
    }

    @Override
    public long edgeId() {
        return _edgeId;
    }

    @Override
    public int driveTimeMs() {
        return _driveTimeMs;
    }

    @Override
    public AccessOnly accessOnly() {
        return _accessOnly;
    }

    @Override
    public DirectedEdge first() {
        return _first;
    }

    @Override
    public DirectedEdge second() {
        return _second;
    }

    @Override
    public int contractionDepth() {
        return _contractionDepth;
    }

    @Override
    public long sourceDataEdgeId() {
        return _sourceDataEdgeId;
    }

    final long _edgeId;
    final long _sourceDataEdgeId;
    final Node _from;
    final Node _to;
    final int _driveTimeMs;
    AccessOnly _accessOnly;

    // Parameters for graph contraction:
    final DirectedEdge _first;
    final DirectedEdge _second;
    final int _contractionDepth;
    final UnionList<DirectedEdge> _uncontractedEdges;
    
    public DirectedEdgeJ(long edgeId, long sourceDataEdgeId, Node from, Node to, int driveTimeMs, AccessOnly isAccessOnly) {
        this(checkId(edgeId), sourceDataEdgeId, from,to,driveTimeMs,isAccessOnly,null,null);
    }
    
    public DirectedEdgeJ(long edgeId, long sourceDataEdgeId, DirectedEdge first, DirectedEdge second) {
        this(checkId(edgeId), sourceDataEdgeId, first.from(), second.to(),
                first.driveTimeMs()+second.driveTimeMs(), null, first, second);
    }
    
    public DirectedEdgeJ(Node from, Node to, int driveTimeMs, DirectedEdge first, DirectedEdge second) {
        this(PLACEHOLDER_ID_DO_NOT_SERIALIZE, PLACEHOLDER_ID_DO_NOT_SERIALIZE, from, to, driveTimeMs, null, first, second);
    }

    protected DirectedEdgeJ(long edgeId, long sourceDateEdgeId, Node from, Node to, int driveTimeMs, AccessOnly accessOnly, DirectedEdge first, DirectedEdge second) {
        Preconditions.checkNoneNull(from, to);
        Preconditions.require(driveTimeMs >= 0);
        if (edgeId>0 && first!=null && second!=null) {
            // If this check starts failing, your edge IDs for shortcuts probably start too low.
            Preconditions.require(edgeId>first.edgeId(), edgeId>second.edgeId());
        }
        this._edgeId = edgeId;
        this._sourceDataEdgeId = sourceDateEdgeId;
        this._from = from;
        this._to = to;
        this._driveTimeMs = driveTimeMs;
        this._first = first;
        this._second = second;
        if (first == null && second == null) {
            _contractionDepth = 0;
            _uncontractedEdges = null;
            Preconditions.checkNoneNull(accessOnly);
            this._accessOnly = accessOnly;
        } else if (first != null && second != null){
            _contractionDepth = Math.max(first.contractionDepth(), second.contractionDepth())+1;
            _uncontractedEdges = new UnionList<>(first.getUncontractedEdges(),second.getUncontractedEdges());
            // Eliminate access only nodes edges before performing contraction.
            Preconditions.require(first.accessOnly()==AccessOnly.FALSE,second.accessOnly()==AccessOnly.FALSE);
            this._accessOnly = AccessOnly.FALSE;
        } else {
            throw new IllegalArgumentException("Must have either both or neither child edges set. Instead had " + first + " and " + second);
        }
    }
    
    private static long checkId(long proposedId) {
        if (proposedId == PLACEHOLDER_ID_DO_NOT_SERIALIZE || proposedId == PLACEHOLDER_ID_NO_SOURCE_DATA_EQUIVALENT) 
            throw new IllegalArgumentException("Attempt to create DirectedEdge with reserved ID, " + proposedId);
        return proposedId;
    }
    
    @Override public boolean isShortcut() {
        return (_contractionDepth != 0);
    }

    @Override public List<DirectedEdge> getUncontractedEdges() {
        if (!isShortcut()) {
            return Collections.singletonList(this);
        } else {
            return _uncontractedEdges;
        }
    }
    
    @Override public DirectedEdge cloneWithEdgeId(long edgeId) {
        Preconditions.require(this._edgeId==PLACEHOLDER_ID_DO_NOT_SERIALIZE, _contractionDepth>0);
        return new DirectedEdgeJ(edgeId, PLACEHOLDER_ID_NO_SOURCE_DATA_EQUIVALENT, _from, _to, _driveTimeMs, _accessOnly, _first, _second);
    }
    
    @Override public DirectedEdge cloneWithEdgeIdAndFromToNodeAddingToLists(long newEdgeId, Node from, Node to) {
        return cloneWithEdgeIdAndFromToNodeAddingToLists(newEdgeId, from, to, _accessOnly);
    }
    
    @Override public DirectedEdge cloneWithEdgeIdAndFromToNodeAddingToLists(long newEdgeId, Node from, Node to, AccessOnly accessOnly) {
        Preconditions.require(_edgeId!=PLACEHOLDER_ID_DO_NOT_SERIALIZE, _contractionDepth==0);
        DirectedEdge de = new DirectedEdgeJ(newEdgeId,_sourceDataEdgeId, from, to, _driveTimeMs, accessOnly, _first, _second);
        de.addToToAndFromNodes();
        return de;
    }
    
    @Override public boolean hasPlaceholderId() {
        return (_edgeId==PLACEHOLDER_ID_DO_NOT_SERIALIZE);
    }
    
    @Override public void addToToAndFromNodes() {
        if (!_from.edgesFrom().contains(this)) {
            _from.edgesFrom().add(this);
            _from.sortNeighborLists();
        }
        if (!_to.edgesTo().contains(this)) {
            _to.edgesTo().add(this);
            _to.sortNeighborLists();
        }
    }
    
//    @Override public void removeFromToAndFromNodes() {
//        this._from.edgesFrom.remove(this);
//        this._to.edgesTo.remove(this);
//    }

    @Override
    public String toString() {
        return _from.nodeId()+"--"+_driveTimeMs+"("+_contractionDepth+")-->"+_to.nodeId();
    }
    
    @Override public String toDetailedString() {
        return "DirectedEdge{" + "edgeId=" + _edgeId + ", from=" + _from + ", to=" + _to + ", driveTimeMs=" +
                _driveTimeMs + ", accessOnly=" + _accessOnly + ", first=" + _first + ", second=" + _second +
                ", contractionDepth=" + _contractionDepth + ", uncontractedEdges=" + _uncontractedEdges + '}';
    }
    
    @Override
    public int compareTo(DirectedEdge o) {
        if (o==null) return -1;
        if (this._edgeId==PLACEHOLDER_ID_DO_NOT_SERIALIZE || o.edgeId()==PLACEHOLDER_ID_DO_NOT_SERIALIZE) {
            throw new RuntimeException("Michael didn't write a very thorough comparator.");
        }
        return Long.compare(this._edgeId, o.edgeId());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (int) (this._edgeId ^ (this._edgeId >>> 32));
        hash = 19 * hash + Objects.hashCode(this._from);
        hash = 19 * hash + Objects.hashCode(this._to);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DirectedEdge other = (DirectedEdge) obj;
        if (this._edgeId != other.edgeId()
                || !Objects.equals(this._from, other.from())
                || !Objects.equals(this._to, other.to())) {
            return false;
        }
        return true;
    }
    
    
    
}
