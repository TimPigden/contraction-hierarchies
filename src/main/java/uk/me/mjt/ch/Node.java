package uk.me.mjt.ch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Tim Pigden on 08/07/2017.
 * Copyright Tim Pigden, Letty Green, Hertford, UK 2015
 */
public interface Node extends Comparable<Node> {
    long nodeId();
    long sourceDataNodeId();
    ArrayList<DirectedEdge> edgesFrom();
    ArrayList<DirectedEdge> edgesTo();
    float lat();
    float lon();
    boolean contractionAllowed();
    Barrier barrier();

    public int contractionOrder();

    int UNCONTRACTED = Integer.MAX_VALUE;

    static void sortNeighborListsAll(Collection<Node> nodes) {
        for (Node n : nodes) {
            n.sortNeighborLists();
        }
    }

    boolean isContracted();

    boolean isSynthetic();

    List<Node> getNeighbors();

    void sortNeighborLists();

    Set<DirectedEdge> getEdgesFromAndTo();

    boolean anyEdgesAccessOnly();

    boolean allEdgesAccessOnly();

    Node setContractionAllowed(boolean allowed);

    Node setContractionOrder(int contractionOrder);

    int getCountOutgoingUncontractedEdges();

    int getCountIncomingUncontractedEdges();

}
