package uk.me.mjt.ch;

import org.junit.Test;
import uk.me.mjt.ch.impl.DirectedEdgeFactoryJ;
import uk.me.mjt.ch.impl.NodeFactoryJ;

import static org.junit.Assert.*;

public class UtilTest {
    
    public UtilTest() {
    }
    DirectedEdgeFactory edgeFactory = new DirectedEdgeFactoryJ();
    NodeFactory nodeFactory = new NodeFactoryJ();

    MakeTestData makeTestData = new MakeTestData(edgeFactory, nodeFactory);

    @Test
    public void testDeepEquals_HashMap_HashMap() {
        System.out.println("deepEquals");
        MapData a = makeTestData.makeSimpleThreeEntry();
        MapData b = makeTestData.makeSimpleThreeEntry();
        assertTrue(Util.deepEquals(a, b, true));
        
        Node n = a.getNodeById(2L);
        n.edgesFrom().remove(0);
        
        assertFalse(Util.deepEquals(a, b, true));
    }
    
    @Test
    public void testNodeEquality() {
        MapData a = makeTestData.makeSimpleThreeEntry();
        MapData b = makeTestData.makeSimpleThreeEntry();
        
        assertTrue(Util.deepEquals(a.getNodeById(2L), b.getNodeById(2L), true));
        assertFalse(Util.deepEquals(a.getNodeById(2L), b.getNodeById(3L), true));
    }
    
}
