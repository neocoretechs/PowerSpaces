package com.neocoretechs.powerspaces.server;
import java.util.*;
import java.net.*;
import java.io.*;

import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.cluster.IPCLink;
import com.neocoretechs.powerspaces.server.cluster.PSOutputOfPowerPlantConnection;
import com.neocoretechs.powerspaces.server.cluster.PSOutputOfPowerPlantCustomer;
import com.neocoretechs.powerspaces.server.cluster.synch.PSTowerSynch;
/**
* A tower is an abstraction that defines IO for transmission line
* and powerplant connections, and in this case handles the output Packet queue.
* @see PSOutputOfPowerPlantCustomer
* @see PSOutputOfPowerPlantConnection
* @see PSTowerSynch
* @see IPCLink
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
public abstract class PSAbstractOutputTower implements PSCustomerInterface {
        //
        private Vector queuePackets;
        Socket So;
        OutputStream os = null;
        //
        public PSAbstractOutputTower(Socket tso) {
                So = tso;
                try {
                        os = So.getOutputStream();
                } catch(Exception e) {
                        System.out.println("! BAD SOCKET IN PSAbstractOutputTower CONSTRUCTOR !");
                }
        }

        public OutputStream getOutputStream() { return os; }

        public Socket getSocket() { return So; }

        public abstract int getLeg();

        public abstract void reInit() throws IOException;

        public abstract void Connect();

        public void setPacketQueue(Vector tv) { queuePackets = tv; }

        /**
        * startFlow begins the process of getting packets from queue
        * and writing them to the output socket in endless repetition.
        */
        public void startFlow() throws Exception {
        //
           Object o;
                for(;;) {
                  synchronized(queuePackets) {
                        while( queuePackets.size() == 0 ) queuePackets.wait();
                        // we can manually stop the tower
                        if( Thread.interrupted() ) {
                                System.out.println("Output Tower queue wait interrupted!");
                                return;
                        }
                        ObjectOutputStream obout = new PSObjectOutputStream(os, PowerPlant.theClassLoader);
                        o = queuePackets.elementAt(0);
                        obout.writeObject(o);
                        obout.flush();
                        queuePackets.removeElementAt(0);
                  }
                }
       }
}
