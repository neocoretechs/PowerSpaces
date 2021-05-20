package com.neocoretechs.powerspaces.gridtie;
import com.neocoretechs.powerspaces.PowerSpace;

import java.net.*;
import java.util.*;
//
/**
* A Customer transmission line represents a connection to a PowerSpace
* this is a client process by which this customer connects to powerspace.
*/
public final class CustomerOutputTransmissionLine implements TransmissionLineInterface {
        private static Vector PSOutputOfCustomerConnectionQueue = new Vector(10);
        private PSOutputPoleConnection pss = null; // current customer
        private PowerSpace pS;
        //
        public CustomerOutputTransmissionLine(PowerSpace tpS) {
            //
        	try {
        		// thread pool
        		new PSGeneratorThread(PSOutputOfCustomerConnectionQueue).start();
        		//
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
			pS = tpS;
	}

        public PowerSpace getPowerSpace() { return pS; }

        /**
        * starts a PSOutputPoleConnection when socket connects
        * to remote server (PowerSpace)
        */
        public PSPoleInterface run(String host, int port) {
                try {
                  Socket So = new Socket(host, port);;
                  System.out.println("CustomerOutputTransmissionLine "+So);
                  // we expect a transport packet with a command
                  //
                  pss = new PSOutputPoleConnection(this, So);
                  //
                  synchronized(PSOutputOfCustomerConnectionQueue) {
                        PSOutputOfCustomerConnectionQueue.addElement(pss);
                        PSOutputOfCustomerConnectionQueue.notify();
                  }
		} catch(Exception e) {
			System.out.println(e.getMessage());
                        pss = null;
		}
                return pss;
	}

}
