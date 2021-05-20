package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.server.PSTransformerThread;
import com.neocoretechs.powerspaces.server.TCPServer;

import java.net.*;
import java.io.*;
import java.util.*;
/**
* A transmission line represents a connection to another PowerSpace.
* Typically 1 is started for parent Space in the tree.
* This is a server process by which ancestor connects to
* this Space node.
* Conversely, a client connection is made to talk to the ones we are
* connected TO, left and right.
*/
public final class ParentInputTransmissionLine extends TCPServer {
        private static Vector PSPowerPlantCustomerQueue = new Vector(10);
        private PSInputOfPowerPlantCustomer pss = null; // current customer
        private Vector queuePackets;
        private Socket So;
        private int leg;
        //
        public ParentInputTransmissionLine(Vector tqueue, int tleg) {
                super();
                queuePackets = tqueue;
                leg = tleg;
                //
                try {
                        // thread pool
                        new PSTransformerThread(PSPowerPlantCustomerQueue).start();
                        // only have 1
                        //
                } catch(Exception e) {
                	System.out.println(e.getMessage());
                    e.printStackTrace();
                }
        }
        /**
        * starts a PSInputOfPowerPlantCustomer when socket connects
        * from remote server (PowerSpace)
        * @param s the socket passed down by TCPServer StartServer()
        */
        public void run(Socket s) {
                System.out.println("ParentInputTransmissionLine "+s);
                So = s;
                // we expect a transport packet with a command
                try {
                  //
                  pss = new PSInputOfPowerPlantCustomer(this, queuePackets);
                  //
                  synchronized(PSPowerPlantCustomerQueue) {
                        PSPowerPlantCustomerQueue.addElement(pss);
                        PSPowerPlantCustomerQueue.notify();
                  }
		} catch(Exception e) {
			System.out.println(e.getMessage());
                        e.printStackTrace();
		}
	}

        public Socket getSocket() { return So; }
        public InputStream getInputStream() throws IOException
        {
                return So.getInputStream();
        }

        public int getLeg() { return leg; }

}
