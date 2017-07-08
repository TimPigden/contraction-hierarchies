
package uk.me.mjt.ch.cache;

import org.junit.Test;
import static org.junit.Assert.*;

import uk.me.mjt.ch.*;
import uk.me.mjt.ch.impl.DirectedEdgeFactoryJ;
import uk.me.mjt.ch.impl.NodeFactoryJ;

public class CachedContractedDijkstraTest {

    public CachedContractedDijkstraTest() {
    }
    DirectedEdgeFactory edgeFactory = new DirectedEdgeFactoryJ();
    NodeFactory nodeFactory = new NodeFactoryJ();
    MakeTestData makeTestData = new MakeTestData(edgeFactory, nodeFactory);

    @Test
    public void testContractedGraphDijkstra() {
        MapData graph = makeTestData.makeLadder(2,10);

        GraphContractor instance = new GraphContractor(graph, edgeFactory);
        instance.initialiseContractionOrder();
        instance.contractAll();
        
        Node startNode = graph.getNodeById(1L);
        Node endNode = graph.getNodeById(18L);
        
        SimpleCache cache = new SimpleCache();
        
        // This should populate the cache:
        DijkstraSolution result1 = CachedContractedDijkstra.contractedGraphDijkstra(graph, startNode, endNode, cache);
        System.out.println("Contraction: "+result1);
        assertNotNull(result1);
        assertEquals(8000, result1.totalDriveTimeMs);
        assertEquals(9, result1.nodes.size());
        
        // This should be retrieved from the cache:
        DijkstraSolution result2 = CachedContractedDijkstra.contractedGraphDijkstra(graph, startNode, endNode, cache);
        System.out.println("Contraction: "+result2);
        assertNotNull(result2);
        assertEquals(8000, result2.totalDriveTimeMs);
        assertEquals(9, result2.nodes.size());
    }

}