
package uk.me.mjt.ch;

import java.util.HashMap;

public class MakeTestData {
    
    public static HashMap<Long,Node> makeSimpleThreeEntry() {
        Node n1 = new Node(1, 52f, 0.1f);
        Node n2 = new Node(2, 52f, 0.2f);
        Node n3 = new Node(3, 52f, 0.3f);
        
        DirectedEdge de1 = new DirectedEdge(1001, n1, n2, 1);
        DirectedEdge de2 = new DirectedEdge(1002, n2, n3, 2);
        DirectedEdge de3 = new DirectedEdge(1003, n1, n3, 3, de1, de2);
        
        n1.edgesFrom.add(de1);
        n2.edgesTo.add(de1);
        
        n2.edgesFrom.add(de2);
        n3.edgesTo.add(de2);
        
        n1.edgesFrom.add(de3);
        n3.edgesTo.add(de3);
        
        HashMap<Long,Node> result = new HashMap(3);
        result.put(1L, n1);
        result.put(2L, n2);
        result.put(3L, n3);
        
        return result;
    }
    
    public static HashMap<Long,Node> makeLadder(int rowCount, int colCount) {
        HashMap<Long,Node> result = new HashMap();
        
        float rowSpacing = Math.min(0.05f/rowCount,0.05f/colCount);
        
        for (int row = 0 ; row < rowCount ; row++) {
            for (int column = 0 ; column < colCount ; column++) {
                long nodeId = ladderNodeId(row,column, rowCount,colCount);
                Node n = new Node(nodeId,51.51f+rowSpacing*row,-0.12f+rowSpacing*column);
                result.put(nodeId, n);
            }
        }
        
        int edgeId = 1000;
        
        for (int row = 0 ; row < rowCount ; row++) {
            for (int column = 0 ; column < colCount ; column++) {
                Node fromNode = result.get(ladderNodeId(row,column, rowCount,colCount));
                long[] possibleToNodeIds = {ladderNodeId(row,column+1, rowCount,colCount),
                            ladderNodeId(row+1,column, rowCount,colCount)};
                for (long toNodeId : possibleToNodeIds) {
                    if (result.containsKey(toNodeId)) {
                        Node toNode = result.get(toNodeId);
                        makeEdgeAndAddToNodes(edgeId++,fromNode,toNode,1000);
                        makeEdgeAndAddToNodes(edgeId++,toNode,fromNode,1000);
                    }
                }
            }
        }
        
        return result;
    }

    private static long ladderNodeId(int row, int col, int rowCount, int colCount) {
        if (row >= rowCount || col >= colCount || row < 0 || col < 0) {
            return Integer.MIN_VALUE;
        } else {
            return colCount * row + col;
        }
    }
    
    private static DirectedEdge makeEdgeAndAddToNodes(long edgeId, Node from, Node to, int driveTimeMs) {
        Preconditions.checkNoneNull(from,to);
        DirectedEdge de = new DirectedEdge(edgeId, from, to, 1);
        from.edgesFrom.add(de);
        to.edgesTo.add(de);
        return de;
    }

}