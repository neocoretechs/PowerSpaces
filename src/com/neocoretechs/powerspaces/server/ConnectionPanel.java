package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.cluster.IPCLink;

import java.util.*;
import java.io.*;
import java.net.*;
/**
* The ConnectionPanel maintains the table of CustomerConnectionPanel
* sessions and ultimately provides the methods for communicating with connected
* powerplants.<p>
* Customer connections from CustomerConnectionPanel are rotated
* into the circuit providing the proper cross-connects
* @see CustomerConnectionPanel
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
public final class ConnectionPanel {

        public static Hashtable CustomerConnectionPanelTable = new Hashtable();

        public static void forwardCmd(IPCLink toPowerPlant, CustomerConnectionPanel IPCSession, PKTransportMethodCall pktmc) throws IOException, PowerSpaceException, ClassNotFoundException
        {
                System.out.println("ConnectionPanel forward cmd "+pktmc);
		// form command Packet
                SystemPacket pps = new SystemPacket( 0, IPCSession.getSession(), pktmc);
                // write to output of connection
                toPowerPlant.queuePacket(pps);
        }

        public static CustomerConnectionPanel makeCustomerPanel(String session, int leg, boolean putTable ) {
		CustomerConnectionPanel ccp = new CustomerConnectionPanel(session, leg);
                ccp.setTable(putTable);
                if( putTable ) CustomerConnectionPanelTable.put(session, ccp);
		return ccp;
	}

        public static void putCustomerConnectionPanel(CustomerConnectionPanel ccp) {
                ccp.setTable(true);
                CustomerConnectionPanelTable.put(ccp.getSession(), ccp);
        }

        public static void removeCustomer(String session) {
                CustomerConnectionPanelTable.remove(session);
        }

        /**
        * queue an object to the PowerPlant connected through "leg"
        * @param p The object to queue
        * @param toleg The leg to queue through
        * @throw IOException if queue op fails
        */
        public static void queuePacket(Object p, int toleg) throws IOException, PowerSpaceException, ClassNotFoundException
	{
                // assertion time
//                if( toleg > PowerPlant.numberLegs )
//                                throw new PowerSpaceException("ConnectionPanel.queuePacket "+p+" destination "+String.valueOf(toleg)+" unknown");
                PowerPlant.powerPlantLegConnections[toleg].getTowerLink().queuePacket(p);
	}


        /**
        * read a packet.
        */
        public static Packet receivePacket(InputStream si) throws Exception {
                ObjectInputStream ois = new PSObjectInputStream(si, PowerPlant.theClassLoader);
                Packet p2 = (Packet)(ois.readObject());
                if( p2.getField(0).value() instanceof Exception)
                        throw (Exception)(p2.getField(0).value());
                return p2;
        }
        /**
        * send a packet.
        */
        public static void sendPacket(OutputStream ios, Packet p) throws Exception {
                // open the output stream to input of flow node and vice versa
                ObjectOutputStream oos = new PSObjectOutputStream(ios, PowerPlant.theClassLoader);
                oos.writeObject(p);
                oos.flush();
        }
        /**
        * Verify a connection to another powerspace based on the leg
        * we are specifying.  There could be a better way than hardwiring this.
        * In general, we need a better way to reconfig topology without hardcoding
        * like this, however it's kept to a minimum.
        * @param is The InputStream we are getting from
        * @param tleg The leg that we connected through
        */
        public static synchronized void setupConnectionLeg(InputStream is, int tleg) throws Exception {
                Packet p;
                long nleg;
                switch(tleg) {
                        case 0:
                                p = receivePacket(is);
                                Long clusterID = (Long)(p.getField(0).value());
                                PowerPlant.clusterID = clusterID.longValue();
                                nleg = ((Long)(p.getField(1).value())).longValue();
                                PowerPlant.powerPlantLegConnections[tleg].setLinkedClusterID(nleg);
                                break;
                         // no reception on 1,2 we're only sending
                         case 1:
                         case 2:
                                break;
                         case 3:
                         case 4:
                                p = receivePacket(is);
                                // in this case we get the cluster ID of who we connected to back
                                nleg = ((Long)(p.getField(0).value())).longValue();
                                PowerPlant.powerPlantLegConnections[tleg].setLinkedClusterID(nleg);
                                break;
                         default:
                                // throw exception, bad leg?
                                throw new PowerSpaceException("ConnectionPanel.setupConnectionLeg: Bad leg specified");
                }
        }
        /**
        * Verify a connection to another powerspace based on the leg
        * we are specifying.  There could be a better way than hardwiring this.
        * In general, we need a better way to reconfig topology without hardcoding
        * like this, however it's kept to a minimum.
        * @param os The OutputStream we are sending to remote
        * @param tleg The leg that we connected through
        */
        public static synchronized void setupConnectionLeg(OutputStream os, int tleg) throws Exception {
                Packet p;
		// write the cluster ID
                long nleg;
                switch(tleg) {
                         // for parent, we only *receive* clusterID and linked one
                         case 0:
                                break;
                         case 1:
                                nleg = PowerPlant.clusterID << 1;
                                sendPacket(os, new Packet(new Long(nleg), new Long(PowerPlant.clusterID)));
                                PowerPlant.powerPlantLegConnections[tleg].setLinkedClusterID(nleg);
                                break;
                         case 2:
                                nleg = (PowerPlant.clusterID << 1) | 1;
                                sendPacket(os, new Packet(new Long(nleg), new Long(PowerPlant.clusterID)) );
                                PowerPlant.powerPlantLegConnections[tleg].setLinkedClusterID(nleg);
                                break;
                         case 3:
                         case 4:
                                nleg = PowerPlant.clusterID;
                                sendPacket(os, new Packet(new Long(nleg)) );
                                // in this case we get the cluster ID of who we connected to back
                                break;
                         default:
                                // throw exception, bad leg?
                                throw new PowerSpaceException("ConnectionPanel.setupConnectionLeg: Bad leg specified");
                }
        }
}
