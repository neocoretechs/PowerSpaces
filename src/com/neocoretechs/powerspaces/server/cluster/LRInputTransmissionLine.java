package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSTransformerThread;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.handler.*;

import java.net.*;
import java.io.*;
import java.util.*;
/**
* A left/right transmission line represents a connection to another PowerSpace
* this is a client process by which this Space node connects to descendant.
* Created in the PowerPlant.
* invoking the 'run' method causes a PSInputOfPowerPlantConnection to be created
* which is placed on the PSInputofPowerPlantConnectionQueue where 'Connect'
* is called from PSTransformerThread
*/
public final class LRInputTransmissionLine {
        private static Vector PSInputOfPowerPlantConnectionQueue = new Vector(10);
        private PSInputOfPowerPlantConnection pss = null; // current customer
        private Vector queuePackets;
        private int leg;
        //
        private String linkedHost;
        private int linkedPort;
        Socket So = null;
        //
        public LRInputTransmissionLine(Vector tqueue, int tleg) {
                leg = tleg;
                queuePackets = tqueue;
                // set ports here to bind through, input is leg*2+2
                PowerPlant.powerPlantLegConnections[leg].setInputPort(PowerPlant.PowerPlantPort+(leg*2)+2);
                //
		try {
                        // thread pool
                        new PSTransformerThread(PSInputOfPowerPlantConnectionQueue).start();
                        new PSTransformerThread(PSInputOfPowerPlantConnectionQueue).start();
                        //
		} catch(Exception e) {
			System.out.println(e);
                        e.printStackTrace();
		}
	}
        /**
        * Connect to 'parent ports' of remote node.  We specify the local host and port to bind this
	* connection through (remember we can use multiple IP's for bandwidth, but don't have to).<dd>
        * Starts a PSInputOfPowerPlantConnection when socket connects
        * to remote server (PowerSpace).  
        * @param host remote powerspace host
	* @param port remote powerspace port
        */
        public PSInputOfPowerPlantConnection run(String host, int port) {
                linkedHost = host;
                linkedPort = port;
                // we expect a transport packet with a command
                try {
                  // we'll flow this thru local port
                  // unless we don't have a good one
                  if( PowerPlant.powerPlantLegConnections[leg].getLocalBindIn() != null &&
                      PowerPlant.powerPlantLegConnections[leg].getLocalBindIn().length() > 1 )
                        So = new Socket(linkedHost, linkedPort,
                                        InetAddress.getByName(PowerPlant.powerPlantLegConnections[leg].getLocalBindIn()),
                                        PowerPlant.powerPlantLegConnections[leg].getInputPort());
                  else
                        So = new Socket(linkedHost, linkedPort);
                  // set no recombining of small packets to larger ones
                  So.setTcpNoDelay(true);
                  // make close block for 1 sec
                  So.setSoLinger(true, 1); 
		  //
                  System.out.println("LRInputTransmissionLine "+So);
                  //
                  pss = new PSInputOfPowerPlantConnection(this, So, queuePackets);
                  //
                  synchronized(PSInputOfPowerPlantConnectionQueue) {
                        PSInputOfPowerPlantConnectionQueue.addElement(pss);
                        PSInputOfPowerPlantConnectionQueue.notify();
                  }
		} catch(Exception e) {
			System.out.println(e);
                        e.printStackTrace();
                        pss = null;
		}
                return pss;
	}


        public int getLeg() { return leg; }

        public void reInit() throws IOException {
                if( So != null ) So.close();
        }

}
