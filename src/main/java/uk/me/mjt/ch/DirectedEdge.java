package uk.me.mjt.ch;

import java.util.List;

public interface DirectedEdge extends Comparable<DirectedEdge> {
    static final long PLACEHOLDER_ID_DO_NOT_SERIALIZE = Long.MIN_VALUE;
    static final long PLACEHOLDER_ID_NO_SOURCE_DATA_EQUIVALENT = Long.MIN_VALUE+1;

    Node from();
    Node to();

    long edgeId();

    int driveTimeMs();
    AccessOnly accessOnly();

    // Parameters for graph contraction:
    DirectedEdge first();
    DirectedEdge second();
    int contractionDepth();

    long sourceDataEdgeId();

    boolean isShortcut();

    List<DirectedEdge> getUncontractedEdges();

    DirectedEdge cloneWithEdgeId(long edgeId);

    DirectedEdge cloneWithEdgeIdAndFromToNodeAddingToLists(long newEdgeId, Node from, Node to);

    DirectedEdge cloneWithEdgeIdAndFromToNodeAddingToLists(long newEdgeId, Node from, Node to, AccessOnly accessOnly);

    boolean hasPlaceholderId();

    // nb adds this to it's to and from nodes so doesn't change this directly
    void addToToAndFromNodes();

    String toDetailedString();

    // void removeFromToAndFromNodes()
}
