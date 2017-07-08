package uk.me.mjt.ch.impl;

import uk.me.mjt.ch.AccessOnly;
import uk.me.mjt.ch.DirectedEdge;
import uk.me.mjt.ch.DirectedEdgeFactory;
import uk.me.mjt.ch.Node;

public class DirectedEdgeFactoryJ implements DirectedEdgeFactory {
    public DirectedEdge makeEdgeWithNoSourceDataEquivalent(long edgeId, Node from, Node to, int driveTimeMs, AccessOnly isAccessOnly) {
        return new DirectedEdgeJ(edgeId, DirectedEdge.PLACEHOLDER_ID_NO_SOURCE_DATA_EQUIVALENT, from, to, driveTimeMs, isAccessOnly);
    }

    public DirectedEdge makeZeroLengthEdgeAddingToLists(long newEdgeId, Node from, Node to, AccessOnly accessOnly) {
        DirectedEdge de = new DirectedEdgeJ(newEdgeId, DirectedEdge.PLACEHOLDER_ID_NO_SOURCE_DATA_EQUIVALENT, from, to, 0, accessOnly);
        de.addToToAndFromNodes();
        return de;
    }

    public DirectedEdge makeDelayEdge(Node delayAt, int delayLengthMs, AccessOnly accessOnly) {
        return new DirectedEdgeJ(DirectedEdgeJ.PLACEHOLDER_ID_DO_NOT_SERIALIZE, DirectedEdgeJ.PLACEHOLDER_ID_DO_NOT_SERIALIZE, delayAt, delayAt, delayLengthMs, accessOnly, null, null);
    }

    public DirectedEdge create(long edgeId, long sourceDataEdgeId, Node from, Node to, int driveTimeMs, AccessOnly isAccessOnly) {
        return new DirectedEdgeJ(edgeId, sourceDataEdgeId, from, to, driveTimeMs, isAccessOnly);
    }

    public DirectedEdge create(long edgeId, long sourceDataEdgeId, DirectedEdge first, DirectedEdge second) {
        return new DirectedEdgeJ(edgeId, sourceDataEdgeId, first, second);
    }

    public DirectedEdge create(Node from, Node to, int driveTimeMs, DirectedEdge first, DirectedEdge second) {
        return new DirectedEdgeJ(from, to, driveTimeMs, first, second);
    }


}
