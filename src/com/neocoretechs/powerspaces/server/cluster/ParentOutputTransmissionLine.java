package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSTransformerThread;
import com.neocoretechs.powerspaces.server.TCPServer;
import com.neocoretechs.powerspaces.server.handler.*;

import java.net.*;
import java.io.*;
import java.util.*;
/**
* A transmission line represents a connection to another PowerSpace.
* Typically 1 is started for parent Space in the tree.
* This is a server processes by which ancestors connect to
* this Space node. This is how you get connected to the power grid.
* Conversely, client connections are made to talk to the ones we are
* connected TO, left and right.
*
* PSTransformerThread will wait on the PSPowerPlantCustomerQueue
* until notify.  Then will issue 'Connect' method in
* PSCustomerInterface that PSOutputOfPowerPlantCustomer implements
* @see ParentInputTransmissionLine
*/
public final class ParentOutputTransmissionLine extends TCPServer {
        private static Vector PSPowerPlantCustomerQueue = new Vector(10);
        private PSOutputOfPowerPlantCustomer pss = null; // current customer
        private Socket So;
        private int leg;
        //
        public ParentOutputTransmissionLine(int tleg) {
                super();
                leg = tleg;
                //
                try {
                        // thread pool
                        new PSTransformerThread(PSPowerPlantCustomerQueue).start();
                        //
                        //
                } catch(Exception e) {
                	System.out.println(e.getMessage());
                }
        }
        public void run(Socket s) {
                System.out.println("ParentOutputTransmissionLine "+s);
                So = s;
                // we expect a transport packet with a command
                try {
                  //
                  pss = new PSOutputOfPowerPlantCustomer(this);
                  //
                  // PSTransformerThread will wait on the PSPowerPlantCustomerQueue
                  // until notify.  Then will issue 'Connect' method in
                  // PSCustomerInterface that PSOutputOfPowerPlantCustomer implements
                  //
                  synchronized(PSPowerPlantCustomerQueue) {
                        PSPowerPlantCustomerQueue.addElement(pss);
                        PSPowerPlantCustomerQueue.notify();
                  }
                } catch(Exception e) {
                	System.out.println(e.getMessage());
                }
        }

        public OutputStream getOutputStream() throws IOException
        {
                return So.getOutputStream();
        }

        public Socket getSocket() { return So; }
        public int getLeg() { return leg; }
}
