package com.neocoretechs.powerspaces.server.cluster;
import java.util.*;

import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.CustomerConnectionPanel;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.handler.*;
/**
* TurbineThreads represent threads that form the thread pool
* for work to be performed on queued PowerPlant customer requests.
* This is a general queue that all incoming tower traffic is put to.
* It differs from a transformer in that it handles Packets incoming
* from PowerPlants.
* The CustomerConnectionPanel determines routing based on session path.
* @see CustomerConnectionPanel
* @author Groff (C) NeoCoreTechs 1998
*/
public class PSTurbineThread extends Thread {
        private Vector queuePackets;
        private CustomerConnectionPanel ccp;
        boolean shouldRun = true;
        private Packet pc;
        private SystemPacket sp; 
        private int leg, toleg;
        private String cmd;
        private	MessageRoutingPacketField mrpf;
        private PKTransportMethodCall pktmc;

        public PSTurbineThread(Vector tqueue, int tleg) {
                queuePackets = tqueue;
                leg = tleg;
        }
        public void stopRun() { shouldRun = false; }
        public void run() {
                while(shouldRun) {
                try {
                        // yank a packet
                        synchronized(queuePackets) {
                                while( queuePackets.size() == 0 ) queuePackets.wait();
                                sp = (SystemPacket)(queuePackets.elementAt(0));
                                queuePackets.removeElementAt(0);
                        }
                        String session = sp.getSession();
                        System.out.println("PSTurbineThread leg "+String.valueOf(leg)+" "+sp);
                        // extract the control operation
                        // control is:
                        // 0 - command Packet
                        // 1 - return arg Packet from command
                        // 2 - synchronizing Packet sent up
                        // 3 - message routing Packet
                        // 4 - message routing return Packet
                        // if its a return from command , queue it up
                        // customer is waiting to read
                        // if command, do it and queue
                        int cb = sp.getOp();
                        // switch op via control byte
                        switch(cb) {
                        //
                        // terminal command
                        //
                                case(0) :
                                        System.out.println("PSTurbineThread terminal command "+sp);
                                        // get session from table
                                        ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(session));
                                        // process as terminal node command
                                        // format is Packet[PKTransportMethodCall[resultPacket] ]
                                        pktmc = (PKTransportMethodCall)(sp.getField(0).value());
                                        // make sure we have good panel
                                        // first see if we need to make a stateless or statefull
                                        // panel, sometimes we might just want to probe to
                                        // see if we've been here..in which case we still need a panel
                                        // but not a permanent one
                                        //
                                        if( ccp == null ) {
                                        	// it's not there
                                        	// we have to create one 
                                        	// with this leg as origin
                                                boolean putTable = true;
                                                if( Class.forName(pktmc.getClassName()).isAssignableFrom(PKParallel.class) )
                                                        putTable = false;
                                                ccp =  ConnectionPanel.makeCustomerPanel(session, leg, putTable);
                                        }
                                        // get a command a goin' and send result back
                                        try {
                                                pc = PowerPlant.commandCluster(leg, ccp, pktmc);
                                        } catch(Exception e) {
                                                System.out.println("Command Exception "+e);
                                                e.printStackTrace();
                                                pc = new Packet(e);
                                        }
                                        // send status packet
                                        if( pc != null ) {
                                                ccp.queueReturnPacket(pc);
                                                System.out.println("PSTurbineThread terminal command "+sp+" queued OK");
                                        } else
                                                System.out.println("PSTurbineThread terminal command "+sp+" asynchronous wait");
                                        break;
                                        //
                                        // return from command, routed through to source
                                        //
                                case(1) : 
                                        // get session from table
                                        ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(session));
                                        // send status packet
                                        System.out.println("PSTurbineThread attempt routed return Packet "+sp);
                                        ccp.queuePacket(sp);
                                        System.out.println("PSTurbineThread return Packet queued OK "+sp);
                                        break;
                                        //
                                        // synchronizing Packet, placed in the customer connection panel synch buffer
                                        //	
                                case(2) :
                                        System.out.println("PSTurbineThread attempt synch Packet "+sp);
                                        // get session from table
                                        ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(session));
                                        ccp.queueSynchPacket((Packet)(sp.getField(0).value()));
                                        System.out.println("PSTurbineThread synch Packet queued OK "+sp);
                                        break;	
                                        //
                                        // message routing Packet, sent onward to destination
                                        //
                                case(3):
                                        System.out.println("PSTurbineThread message routed Packet "+sp);
                                        // format is Packet[PKTransportMethodCall[resultPacket], MessageRoutingPacketField ]
                                        mrpf = (MessageRoutingPacketField)(sp.getField(1).value()); 
                                        toleg = mrpf.getPacketForwardDirection();
                                        if( toleg == -1 ) {
                                                // we're there
                                                // get session from table
                                                ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(session));
                                                // process as terminal node command
                                                // format is Packet[PKTransportMethodCall[resultPacket] ]
                                                pktmc = (PKTransportMethodCall)(sp.getField(0).value());
                                                // make sure we have good panel
                                                if( ccp == null ) {
                                                        // it's not there
                                                        // we have to create one 
                                                        // with this leg as origin
                                                        boolean putTable = true;
                                                        if( Class.forName(pktmc.getClassName()).isAssignableFrom(PKParallel.class) )
                                                                putTable = false;
                                                        ccp =  ConnectionPanel.makeCustomerPanel(session, leg, putTable);
                                                }
                                                // get a command a goin' and send result back
                                                try {
                                                        pc = PowerPlant.commandCluster(leg, ccp, pktmc);
                                                } catch(Exception e) {
                                                        System.out.println("Command Exception "+e);
                                                        e.printStackTrace();
                                                        pc = new Packet(e);
                                                }
                                                System.out.println("PSTurbineThread "+String.valueOf(leg)+" message routed terminal node cmd "+sp+" result "+pc);
                                                // this should queue the packet to origin
                                                // and it's on its way
                                                if( pc != null ) {
                                                        ccp.queueMessagePacketReturn(pc, mrpf);
                                                        System.out.println("PSTurbineThread message packet terminal command "+sp+" queued OK");
                                                } else
                                                        System.out.println("PSTurbineThread message terminal command "+sp+" asynchronous wait");
                                        } else 
                                                // queue message packet onward
                                                ConnectionPanel.queuePacket(sp, toleg);
                                        break;

                                        // message routing return Packet, sent onward to destination
                                case(4):
                                        System.out.println("PSTurbineThread Message routed return Packet "+sp);
                                        mrpf = (MessageRoutingPacketField)(sp.getField(1).value()); 
                                        toleg = mrpf.getPacketForwardDirection();
                                        if( toleg == -1 ) {
                                                // we're there
                                                // get session from table
                                                ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(session));
                                                // we know we have a good ccp, as we sent the message
                                                // from here
                                                //
                                                ccp.queueResultPacket(sp.getField(0).value());
                                        } else
                                                // queue message packet 
                                                ConnectionPanel.queuePacket(sp, toleg);
						
                        }
                } catch(Exception e) {
                        System.out.println("PSTurbineThread Exception "+e.getMessage());
                        e.printStackTrace();
			// TAKE THIS OUT FOR ALL UPTIME
//                        shouldRun = false;
                        // shut down this leg
                        PowerPlant.powerPlantLegConnections[leg].unLink();
                }
                }
      }
}
