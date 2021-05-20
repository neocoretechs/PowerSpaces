package com.neocoretechs.powerspaces.gridtie;
import java.net.*;
import java.io.*;
import java.util.*;

import com.neocoretechs.powerspaces.PowerSpace;
//
/**
* A customer transmission line represents a connection to a PowerSpace
* this is a client process by which this node connects to PowerSpace.
*/
public final class CustomerInputTransmissionLine implements TransmissionLineInterface {
        private static Vector PSInputOfCustomerConnectionQueue = new Vector(10);
        private PSInputPoleConnection pss = null; // current customer
        private PowerSpace pS;
        //
        public CustomerInputTransmissionLine(PowerSpace powerSpace) {
        //
		try {
			// thread pool
            new PSGeneratorThread(PSInputOfCustomerConnectionQueue).start();
            //
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
                pS = powerSpace;
        }

		public PowerSpace getPowerSpace() { return pS; }

        /**
        * starts a PSInputPoleConnection when socket connects
        * from remote server (PowerSpace)
        * @param host the host powerspace
        * @param port main port
        */
        public PSPoleInterface run(String host, int port) {
                // we expect a transport packet with a command
                try {
                  Socket So = new Socket(host, port);
                  System.out.println("CustomerInputTransmissionLine "+So);
                  //
                  pss = new PSInputPoleConnection(this, So);
                  //
                  synchronized(PSInputOfCustomerConnectionQueue) {
                        PSInputOfCustomerConnectionQueue.addElement(pss);
                        PSInputOfCustomerConnectionQueue.notify();
                  }
		} catch(Exception e) {
			System.out.println(e.getMessage());
            e.printStackTrace();
            pss = null;
		}
        return pss;
	}


}
