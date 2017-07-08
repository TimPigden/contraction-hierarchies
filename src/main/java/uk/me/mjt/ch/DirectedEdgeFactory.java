package uk.me.mjt.ch;

public interface DirectedEdgeFactory {

    DirectedEdge makeEdgeWithNoSourceDataEquivalent(long edgeId, Node from, Node to, int driveTimeMs, AccessOnly isAccessOnly);

    DirectedEdge makeZeroLengthEdgeAddingToLists(long newEdgeId, Node from, Node to, AccessOnly accessOnly);

    DirectedEdge makeDelayEdge(Node delayAt, int delayLengthMs, AccessOnly accessOnly);

    DirectedEdge create(long edgeId, long sourceDataEdgeId, Node from, Node to, int driveTimeMs, AccessOnly isAccessOnly);

    DirectedEdge create(long edgeId, long sourceDataEdgeId, DirectedEdge first, DirectedEdge second);

    DirectedEdge create(Node from, Node to, int driveTimeMs, DirectedEdge first, DirectedEdge second) ;
}
