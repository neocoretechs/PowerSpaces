package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.handler.*;
import java.net.*;
import java.io.*;
import java.util.*;
/**
* A transmission line represents a connection to a client
* @see PSTransformerThread
* @author Groff (C) NeoCoreTechs 1998,1999
*/
public final class SocketTransmissionLine extends TCPServer {
        private static Vector PSInputCustomerQueue = new Vector(10);
        private PoleToPlant pss = null; // current customer
        private PowerPlant pp; // the PowerPlant
        private Socket So;
        //
        public SocketTransmissionLine(PowerPlant tpp) {
                super();
                pp = tpp;
                //
		try {
                        // thread pool
                        new PSTransformerThread(PSInputCustomerQueue).start();
                        new PSTransformerThread(PSInputCustomerQueue).start();
                        new PSTransformerThread(PSInputCustomerQueue).start();
                        new PSTransformerThread(PSInputCustomerQueue).start();
                        new PSTransformerThread(PSInputCustomerQueue).start();
                        new PSTransformerThread(PSInputCustomerQueue).start();
                        //
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
        /**
        * starts a PoleToPlant when socket connects
        * from remote server (PowerSpace)
        * @param s the socket passed down by TCPServer StartServer()
        */
	public void run(Socket s) {
                System.out.println("SocketTransmissionLine "+s);
                So = s;
                // we expect a transport packet with a command
                try {
                  pp.configureMainStation();
                  //
                  pss = new PoleToPlant(this);
                  //
                  synchronized(PSInputCustomerQueue) {
                        PSInputCustomerQueue.addElement(pss);
                        PSInputCustomerQueue.notify();
                  }
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

        public PoleToPlant getCurrentCustomer() { return pss; }

        public Socket getSocket() { return So; }
        public InputStream getInputStream() throws IOException
        {
                return So.getInputStream();
        }

        public int getLeg() { return -1; }

        public PowerPlant getPowerPlant() { return pp; }
}
