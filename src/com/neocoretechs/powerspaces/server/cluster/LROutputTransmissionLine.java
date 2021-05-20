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
* this is a client process by which this Space node connect to descendant.
* Created in PowerPlant.
* @author Groff (C) NeoCoreTechs 1998, 1999
*/
public final class LROutputTransmissionLine {
        private static Vector PSOutputOfPowerPlantConnectionQueue = new Vector(10);
        private PSOutputOfPowerPlantConnection pss = null; // current customer
        private int leg;
        //
        private String linkedHost;
        private int linkedPort;
        private Socket So = null;
        //
        public LROutputTransmissionLine(int tleg) {
                leg = tleg;
                // set ports here to bind through, output is leg*2+3
                PowerPlant.powerPlantLegConnections[leg].setOutputPort(PowerPlant.PowerPlantPort+(leg*2)+3);
                //
		try {
                        // thread pool
                        new PSTransformerThread(PSOutputOfPowerPlantConnectionQueue).start();
                        new PSTransformerThread(PSOutputOfPowerPlantConnectionQueue).start();
                        //
		} catch(Exception e) {
			System.out.println(e.getMessage());
                        e.printStackTrace();
		}
	}
        /**
	* Connect to "parent ports' of remote node.  We specify the local host and port to bind this
	* connection through (remember we can use multiple IP's for bandwidth, but don't have to).<dd>
        * Starts a PSOutputOfPowerPlantConnection when socket connects
        * to remote server (PowerSpace).  
        * @param host remote powerspace host
	* @param port remote powerspace port
        */
        public PSOutputOfPowerPlantConnection run(String host, int port) {
                linkedHost = host;
                linkedPort = port;
                try {
                  // we'll flow this thru local powerplant port
                  // if we have a valid one
                  if( PowerPlant.powerPlantLegConnections[leg].getLocalBindOut() != null &&
                      PowerPlant.powerPlantLegConnections[leg].getLocalBindOut().length() > 1 )
                        So = new Socket(linkedHost, linkedPort,
                                        InetAddress.getByName(PowerPlant.powerPlantLegConnections[leg].getLocalBindOut()),
                                        PowerPlant.powerPlantLegConnections[leg].getOutputPort());
                  else
                        So = new Socket(linkedHost, linkedPort);
                  // disable Nagles algoritm; do not combine small packets into larger ones 
                  So.setTcpNoDelay(true);
                  // make close block for 1 sec
                  So.setSoLinger(true, 1);
		  //
                  System.out.println("LROutputTransmissionLine "+So);
                  // we expect a transport packet with a command
                  //
                  pss = new PSOutputOfPowerPlantConnection(this, So);
                  //
                  synchronized(PSOutputOfPowerPlantConnectionQueue) {
                        PSOutputOfPowerPlantConnectionQueue.addElement(pss);
                        PSOutputOfPowerPlantConnectionQueue.notify();
                  }
		} catch(Exception e) {
			System.out.println(e.getMessage());
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
