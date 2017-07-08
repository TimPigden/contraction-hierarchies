
package uk.me.mjt.ch;

import org.junit.Test;
import uk.me.mjt.ch.impl.DirectedEdgeFactoryJ;
import uk.me.mjt.ch.impl.NodeFactoryJ;

import static org.junit.Assert.*;

public class AdjustGraphForRestrictionsTest {

    public AdjustGraphForRestrictionsTest() {
    }

    DirectedEdgeFactory edgeFactory = new DirectedEdgeFactoryJ();
    NodeFactory nodeFactory = new NodeFactoryJ();

    MakeTestData makeTestData = new MakeTestData(edgeFactory, nodeFactory);

    @Test
    public void testAvoidAccessOnlySegmentOfRing() {
        MapData graph = makeTestData.makePartlyAccessOnlyRing();
        assertDijkstraResult(graph,2,6,"2--1000-->3--1000-->4--1000-->5--1000-->6");
        assertDijkstraResult(graph,1,6,"1--1000-->7--1000-->6");
    }
    
    @Test
    public void testEnterAccessOnlyWhenUnavoidable() {
        MapData graph = makeTestData.makePartlyAccessOnlyRing();
        assertDijkstraResult(graph,1,6,"1--1000-->7--1000-->6");
        assertDijkstraResult(graph,2,7,"2--1000-->1--1000-->7");
    }
    
    @Test
    public void testPathTouchingBorderOfAccessOnlyRegion() {
        MapData graph = makeTestData.makePartlyAccessOnlyThorn();
        assertDijkstraResult(graph,1,5,"1--1000-->2--1000-->3--1000-->4--1000-->5");
    }
    
    @Test
    public void testLongRouteTakenOnTurnRestrictedA() {
        MapData graph = makeTestData.makeTurnRestrictedA();
        assertDijkstraResult(graph,3,6,"3--1000-->2--1000-->1--1000-->4--1000-->5--1000-->6");
    }
    
    @Test
    public void testGoThroughGateWhenItsTheOnlyOption() {
        MapData graph = makeTestData.makeGatedRow();
        assertDijkstraResult(graph,1,3,"1--1000-->2--1000-->3");
    }
    
    @Test
    public void testImplicitWorksOnDoubleGatedRow() {
        MapData graph = makeTestData.makeDoubleGatedRow();
        assertDijkstraResult(graph,1,7,"1--1000-->2--1000-->3--1000-->4--1000-->5--1000-->6--1000-->7");
        assertDijkstraResult(graph,1,6,"1--1000-->2--1000-->3--1000-->4--1000-->5--1000-->6");
        assertDijkstraResult(graph,1,4,"1--1000-->2--1000-->3--1000-->4");
        assertDijkstraResult(graph,1,2,"1--1000-->2");
    }
    
    @Test
    public void testImplicitWorksOnDoubleAccessOnlyRow() {
        MapData graph = makeTestData.makeDoubleAccessOnlyRow();
        assertDijkstraResult(graph,1,7,"1--1000-->2--1000-->3--1000-->4--1000-->5--1000-->6--1000-->7");
        assertDijkstraResult(graph,1,6,"1--1000-->2--1000-->3--1000-->4--1000-->5--1000-->6");
        assertDijkstraResult(graph,1,5,"1--1000-->2--1000-->3--1000-->4--1000-->5");
        assertDijkstraResult(graph,1,4,"1--1000-->2--1000-->3--1000-->4");
        assertDijkstraResult(graph,1,3,"1--1000-->2--1000-->3");
        assertDijkstraResult(graph,1,2,"1--1000-->2");
    }
    
    @Test
    public void testImplicitWorksOnDoubleAccessOnlyRow2() {
        MapData graph = makeTestData.makeDoubleAccessOnlyRow();
        graph = AdjustGraphForRestrictions.makeNewGraph(graph, graph.getNodeById(4), edgeFactory, nodeFactory);
        
        ColocatedNodeSet startNodes = graph.getNodeBySourceDataId(1);
        ColocatedNodeSet endNodes = graph.getNodeBySourceDataId(7);
        DijkstraSolution ds = Dijkstra.dijkstrasAlgorithm(startNodes, endNodes, Dijkstra.Direction.FORWARDS);
        
        String expected="1--1000-->2--1000-->3--1000-->4--1000-->5--1000-->6--1000-->7";
        assertEquals(expected,solutionToSimpleString(ds));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAdjustMayNotStartAtGateNode() {
        MapData graph = makeTestData.makeDoubleGatedRow();
        AdjustGraphForRestrictions.makeNewGraph(graph, graph.getNodeById(3), edgeFactory, nodeFactory);
    }
    
    @Test
    public void testAvoidGateWhenPossible() {
        MapData graph = makeTestData.makeGatedThorn();
        assertDijkstraResult(graph,1,5,"1--1000-->2--1000-->3--1000-->4--1000-->5");
    }
    
    @Test
    public void testDelayedUTurnOnTurnRestrictedH() {
        MapData graph = makeTestData.makeTurnRestrictedH();
        assertDijkstraResult(graph,3,6,"3--1000-->2--1000-->1--60000-->1--1000-->2--1000-->5--1000-->6");
    }
    
    @Test
    public void testTurnRestrictionsDontBreakStraightOn() {
        MapData graph = makeTestData.makeTurnRestrictedThorn();
        assertDijkstraResult(graph,1,4,"1--1000-->2--1000-->3--1000-->4");
    }
    
    @Test
    public void testOnlyRestriction() {
        MapData graph = makeTestData.makeOffsetCrossroadWithOnlyStraightOn();
        
        assertDijkstraResult(graph,1,4,"1--1000-->2--1000-->3--1000-->4");
        assertDijkstraResult(graph,1,5,"1--1000-->2--2000-->5");
        assertDijkstraResult(graph,5,6,"5--2000-->2--1000-->3--1000-->6");
        assertDijkstraResult(graph,1,6,"1--1000-->2--1000-->3--1000-->4--60000-->4--1000-->3--1000-->6");
    }
    
    private void assertDijkstraResult(MapData graph, long startNodeId, long endNodeId, String expected) {
        Node startNode = graph.getNodeById(startNodeId);
        Node endNode = graph.getNodeById(endNodeId);
        
        String result = AdjustGraphForRestrictions.testRestrictedDijkstra(graph, startNode, endNode, edgeFactory, nodeFactory);
        
        assertNotNull(result);
        assertEquals(expected, result);
        
        assertModifiedGraph(AdjustGraphForRestrictions.makeNewGraph(graph, startNode, edgeFactory, nodeFactory),startNodeId,endNodeId,expected);
        assertModifiedGraph(AdjustGraphForRestrictions.makeNewGraph(graph, endNode, edgeFactory, nodeFactory),startNodeId,endNodeId,expected);
        
    }
    
    @Test
    public void testTrivialDoesntCrash() {
        MapData graph = makeTestData.makeSimpleFiveEntry();
        Node startNode = graph.getNodeById(1);
        AdjustGraphForRestrictions.makeNewGraph(graph, startNode, edgeFactory, nodeFactory);
    }
    
    @Test
    public void testSpuriousUturnsRemoved() {
        MapData graph = makeTestData.makeSimpleFiveEntry();
        Node startNode = graph.getNodeById(1);
        MapData adjusted = AdjustGraphForRestrictions.makeNewGraph(graph, startNode, edgeFactory, nodeFactory);
        assertEquals(10, adjusted.getNodeCount());
    }
    
    private void assertModifiedGraph(MapData modifiedGraph, long startNodeId, long endNodeId, String expected) {
        ColocatedNodeSet startNodes = modifiedGraph.getNodeBySourceDataId(startNodeId);
        ColocatedNodeSet endNodes = modifiedGraph.getNodeBySourceDataId(endNodeId);
        DijkstraSolution ds = Dijkstra.dijkstrasAlgorithm(startNodes, endNodes, Dijkstra.Direction.FORWARDS);
        
        if (ds==null) {
            System.out.println("Unable to route between " + startNodeId + " and " + endNodeId);
        } else {
            System.out.println("Successfully routed between " + startNodeId + " and " + endNodeId);
        }
        
        assertNotNull(ds);
        assertEquals(expected,solutionToSimpleString(ds));
        
        ds = Dijkstra.dijkstrasAlgorithm(endNodes, startNodes, Dijkstra.Direction.BACKWARDS);
        assertNotNull(ds);
        assertEquals(expected,backwardsSolutionToSimpleString(ds));
    }
    
    private String solutionToSimpleString(DijkstraSolution ds) {
        StringBuilder sb = new StringBuilder();
        sb.append(ds.edges.get(0).from().sourceDataNodeId());
        for (DirectedEdge de : ds.edges) {
            sb.append("--").append(de.driveTimeMs())
                    .append("-->")
                    .append(de.to().sourceDataNodeId());
        }
        return sb.toString();
    }
    
    private String backwardsSolutionToSimpleString(DijkstraSolution ds) {
        StringBuilder sb = new StringBuilder();
        sb.append(ds.edges.get(0).to().sourceDataNodeId());
        for (DirectedEdge de : ds.edges) {
            sb.insert(0, de.from().sourceDataNodeId()+"--"+de.driveTimeMs()+"-->");
        }
        return sb.toString();
    }

}