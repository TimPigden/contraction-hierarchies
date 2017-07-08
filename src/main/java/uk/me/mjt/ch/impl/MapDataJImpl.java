
package uk.me.mjt.ch.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import uk.me.mjt.ch.*;
import uk.me.mjt.ch.status.DiscardingStatusMonitor;
import uk.me.mjt.ch.status.MonitoredProcess;
import uk.me.mjt.ch.status.StatusMonitor;

public class MapDataJImpl implements MapData {
    private final HashMap<Long,Node> nodesById;
    private final Set<TurnRestriction> turnRestrictions;
    private final AtomicLong maxEdgeId = new AtomicLong();
    private final Multimap<Long,Node> nodesBySourceDataNodeId = new Multimap<>();
    
    public MapDataJImpl(Collection<Node> nodes) {
        this(indexNodesById(nodes), new HashSet(), new DiscardingStatusMonitor());
    }
    
    public MapDataJImpl(HashMap<Long,Node> nodesById) {
        this(nodesById, new HashSet(), new DiscardingStatusMonitor());
    }
    
    public MapDataJImpl(HashMap<Long,Node> nodesById, Set<TurnRestriction> turnRestrictions, StatusMonitor monitor) {
        Preconditions.checkNoneNull(nodesById, turnRestrictions);
        this.nodesById = nodesById;
        this.turnRestrictions = turnRestrictions;
        generateIndexAndAggregates(monitor);
    }
    
    private void generateIndexAndAggregates(StatusMonitor monitor) {
        long nodesCheckedSoFar = 0;
        monitor.updateStatus(MonitoredProcess.INDEX_MAP_DATA, nodesCheckedSoFar, nodesById.size());
        
        for (Node n : nodesById.values()) {
            findMaxEdgeId(n);
            indexBySourceDataNodeId(n);
            
            nodesCheckedSoFar++;
            if (nodesCheckedSoFar%10000==0)
                monitor.updateStatus(MonitoredProcess.INDEX_MAP_DATA, nodesCheckedSoFar, nodesById.size());
        }
        
        monitor.updateStatus(MonitoredProcess.INDEX_MAP_DATA, nodesCheckedSoFar, nodesById.size());
    }
    
    private void findMaxEdgeId(Node n) {
        for (DirectedEdge de : n.edgesFrom()) {
            if (de.edgeId() > maxEdgeId.get()) {
                maxEdgeId.set(de.edgeId());
            }
        }
    }
    
    private void indexBySourceDataNodeId(Node n) {
        nodesBySourceDataNodeId.add(n.sourceDataNodeId(), n);
    }
    
    private static HashMap<Long,Node> indexNodesById(Collection<Node> nodes) {
        HashMap<Long,Node> hm = new HashMap(nodes.size());
        for (Node n : nodes) {
            hm.put(n.nodeId(), n);
        }
        return hm;
    }
    
    @Override
    public AtomicLong getEdgeIdCounter() {
        return maxEdgeId;
    }
    
    @Override
    public Node getNodeById(long nodeId) {
        return nodesById.get(nodeId);
    }
    
    @Override
    public ColocatedNodeSet getNodeBySourceDataId(long nodeId) {
        if (nodesBySourceDataNodeId.containsKey(nodeId))
            return new ColocatedNodeSet(nodesBySourceDataNodeId.get(nodeId));
        else
            return null;
    }
    
    @Override
    public int getNodeCount() {
        return nodesById.size();
    }
    
    @Override
    public Collection<Node> getAllNodes() {
        return Collections.unmodifiableCollection(nodesById.values());
    }
    
    @Override
    public Set<Long> getAllNodeIds() {
        return Collections.unmodifiableSet(nodesById.keySet());
    }
    
    @Override
    public List<Node> chooseRandomNodes(int howMany) {
        Preconditions.require(howMany>0);
        ArrayList<Node> n = new ArrayList<>(nodesById.values());
        Collections.shuffle(n, new Random(12345));
        return new ArrayList(n.subList(0, howMany));
    }
    
    @Override
    public Set<TurnRestriction> allTurnRestrictions() {
        return Collections.unmodifiableSet(turnRestrictions);
    }
    
    @Override
    public void validate(StatusMonitor monitor) {
        int roughEstimateEdgeCount = 3*nodesById.size();
        HashMap<Long,DirectedEdge> uniqueEdges = new HashMap(roughEstimateEdgeCount);
        
        long nodesCheckedSoFar = 0;
        monitor.updateStatus(MonitoredProcess.VALIDATE_DATA, nodesCheckedSoFar, nodesById.size());
        
        for (Node node : nodesById.values()) {
            validateSingleNode(node.nodeId());
            checkForDuplicateEdges(node, uniqueEdges);
            
            nodesCheckedSoFar++;
            if (nodesCheckedSoFar%10000==0)
                monitor.updateStatus(MonitoredProcess.VALIDATE_DATA, nodesCheckedSoFar, nodesById.size());
        }
        
        monitor.updateStatus(MonitoredProcess.VALIDATE_DATA, nodesCheckedSoFar, nodesById.size());
    }
    
    private void checkForDuplicateEdges(Node node, HashMap<Long,DirectedEdge> uniqueEdges) {
        for (DirectedEdge de : node.edgesFrom()) {
            if (uniqueEdges.containsKey(de.edgeId())) {
                DirectedEdge prevWithThisId = uniqueEdges.get(de.edgeId());
                if (de != prevWithThisId) {
                    throw new InvalidMapDataException("Nonunique edge ID - " + de.edgeId()
                            + " " + de.toDetailedString() + " vs " + prevWithThisId.toDetailedString());
                }
            } else {
                uniqueEdges.put(de.edgeId(), de);
            }
        }
    }
    
    private void validateSingleNode(long nodeId) {
        Node node = nodesById.get(nodeId);
        
        if (node.nodeId() != nodeId)
            throw new InvalidMapDataException("Node IDs don't match - " + nodeId + " vs " + node.nodeId());
        
        validateNeighborLists(node);
        
        for (DirectedEdge de : node.edgesFrom()) {
            validateSingleEdge(de);
        }
    }
    
    private void validateNeighborLists(Node n) {
        ArrayList<DirectedEdge> fromBefore = new ArrayList<>(n.edgesFrom());
        ArrayList<DirectedEdge> toBefore = new ArrayList<>(n.edgesTo());
        n.sortNeighborLists();
        if (!fromBefore.equals(n.edgesFrom()) || !toBefore.equals(n.edgesTo())) {
            throw new InvalidMapDataException("Neigbor lists were unsorted - Node " + n.nodeId());
        }
    }
    
    private void validateSingleEdge(DirectedEdge de) {
        if (de.edgeId() > maxEdgeId.get())
            throw new InvalidMapDataException("DirectedEdge ID exceeds maxEdgeId - " + maxEdgeId.get() + " vs " + de.toDetailedString());
        
        Node from = nodesById.get(de.from().nodeId());
        Node to = nodesById.get(de.to().nodeId());
        if (from != de.from())
            throw new InvalidMapDataException("DirectedEdge from node isn't in nodesById - " + from.nodeId() + " for de " + de.edgeId());
        if (to != de.to())
            throw new InvalidMapDataException("DirectedEdge to node isn't in nodesById - " + to.nodeId() + " for de " + de.edgeId());
        if (!from.edgesFrom().contains(de))
            throw new InvalidMapDataException("From node doesn't have edge in edgesFrom - " + from.nodeId() + " for de " + de.edgeId());
        if (!to.edgesTo().contains(de))
            throw new InvalidMapDataException("To node doesn't have edge in edgesTo - " + to.nodeId() + " for de " + de.edgeId());
    }

    @Override
    public List<Node> nodesInBbox(double lat1, double lon1, double lat2, double lon2) {
        ArrayList<Node> result = new ArrayList();
        for (Node n : nodesById.values()) {
            if (lat1 <= n.lat() && n.lat() <= lat2 && lon1 <= n.lon() && n.lon() <= lon2) {
                result.add(n);
            }
        }
        return result;
    }
    
}

