package uk.me.mjt.ch.impl;

import uk.me.mjt.ch.Barrier;
import uk.me.mjt.ch.Node;
import uk.me.mjt.ch.NodeFactory;

/**
 * Created by Tim Pigden on 08/07/2017.
 * Copyright Tim Pigden, Letty Green, Hertford, UK 2015
 */
public class NodeFactoryJ implements NodeFactory {
    @Override
    public Node create(long nodeId, float lat, float lon, Barrier barrier) {
        return new NodeJ(nodeId, lat, lon, barrier);
    }

    @Override
    public Node create(long nodeId, Node copyFrom) {
        return new NodeJ(nodeId, copyFrom);
    }

    @Override
    public Node create(long nodeId, long sourceDataNodeId, float lat, float lon, Barrier barrier) {
        return new NodeJ(nodeId, sourceDataNodeId, lat, lon, barrier);
    }
}
