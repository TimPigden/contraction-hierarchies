package uk.me.mjt.ch;

/**
 * Created by Tim Pigden on 08/07/2017.
 * Copyright Tim Pigden, Letty Green, Hertford, UK 2015
 */
public interface NodeFactory {
    public Node create(long nodeId, float lat, float lon, Barrier barrier);
    public Node create (long nodeId, Node copyFrom);
    public Node create(long nodeId, long sourceDataNodeId, float lat, float lon, Barrier barrier);
}
