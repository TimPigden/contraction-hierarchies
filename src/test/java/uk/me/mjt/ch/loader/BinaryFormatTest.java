package uk.me.mjt.ch.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.Assert.*;

import uk.me.mjt.ch.*;
import uk.me.mjt.ch.impl.DirectedEdgeFactoryJ;
import uk.me.mjt.ch.impl.NodeFactoryJ;
import uk.me.mjt.ch.status.MonitoredProcess;
import uk.me.mjt.ch.status.StdoutStatusMonitor;

public class BinaryFormatTest {

    DirectedEdgeFactory edgeFactory = new DirectedEdgeFactoryJ();
    NodeFactory nodeFactory = new NodeFactoryJ();

    MakeTestData makeTestData = new MakeTestData(edgeFactory, nodeFactory);

    public BinaryFormatTest() {
    }
    

    @org.junit.Test
    public void testLoopback() throws Exception {
        MapData testData = makeTestData.makeSimpleThreeEntry();
        writeAndReadBack(testData);
    }
    
    @org.junit.Test
    public void testLoopbackAccessOnly() throws Exception {
        MapData testData = makeTestData.makePartlyAccessOnlyRing();
        writeAndReadBack(testData);
    }
    
    @org.junit.Test
    public void testLoopbackGate() throws Exception {
        MapData testData = makeTestData.makeGatedRow();
        writeAndReadBack(testData);
    }
    
    @org.junit.Test
    public void testTurnRestricted() throws Exception {
        MapData testData = makeTestData.makeTurnRestrictedH();
        writeAndReadBack(testData);
    }
    
    @org.junit.Test
    public void testSynthetic() throws Exception {
        writeAndReadBack(makeTestData.makeSimpleThreeEntry());
    }
    
    @org.junit.Test
    public void testSourceDataEdgeId() throws Exception {
        MapData testData = makeTestData.makeSimpleThreeEntry();
        DirectedEdge de = edgeFactory.create(100L, 200L, testData.getNodeById(1L), testData.getNodeById(2L), 123, AccessOnly.FALSE);
        de.addToToAndFromNodes();
        writeAndReadBack(testData);
    }
    
    private void writeAndReadBack(MapData testData) throws IOException {
        ByteArrayOutputStream nodesOut = new ByteArrayOutputStream();
        ByteArrayOutputStream waysOut = new ByteArrayOutputStream();
        ByteArrayOutputStream turnRestrictionsOut = new ByteArrayOutputStream();
        DirectedEdgeFactory edgeFactory = new DirectedEdgeFactoryJ();

        BinaryFormat instance = new BinaryFormat(edgeFactory, nodeFactory);
        
        instance.write(testData, new DataOutputStream(nodesOut), new DataOutputStream(waysOut), new DataOutputStream(turnRestrictionsOut));
        
        ByteArrayInputStream nodesIn = new ByteArrayInputStream(nodesOut.toByteArray());
        ByteArrayInputStream waysIn = new ByteArrayInputStream(waysOut.toByteArray());
        ByteArrayInputStream turnRestrictionsIn = new ByteArrayInputStream(turnRestrictionsOut.toByteArray());
        LoggingStatusMonitor monitor = new LoggingStatusMonitor();
        
        MapData loopback = instance.read(nodesIn, waysIn, turnRestrictionsIn, monitor);
        
        assertTrue(Util.deepEquals(testData, loopback, true));
        
        String monitorStatuses = monitor.statuses.toString();
        assertEquals(2*MonitoredProcess.values().length, monitor.statuses.size());
        assertTrue(monitorStatuses.contains(" 0.00%"));
        assertTrue(monitorStatuses.contains(" 100.00%"));
    }
    
    @org.junit.Test(expected=IOException.class)
    public void testExceptionForOldVersion() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(-1);
        dos.writeChars("some other data");
        dos.close();
        DirectedEdgeFactory edgeFactory = new DirectedEdgeFactoryJ();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BinaryFormat instance = new BinaryFormat(edgeFactory, nodeFactory);
        instance.read(bais, bais, bais, new StdoutStatusMonitor());
    }
    
    private class LoggingStatusMonitor extends StdoutStatusMonitor {
        public final ArrayList<String> statuses = new ArrayList();
        
        @Override
        public void updateStatus(MonitoredProcess process, long completed, long total) {
            statuses.add(toString(process, completed, total));
        }
    }
    
}
