package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.cluster.synch.PSSynchTower;

import java.net.*;
import java.io.*;
import java.util.*;
/**
* This class initiates and handles input inter-powerplant connections through
* the legs, which means it is opening a Socket to another PowerSpace parent
* leg listening with a ServerSocket (TCPServer class).<p>
* Created by LRInputTransmissionLine.  If we are connecting a child in, we hand
* down the cluster ID based on left or right connection depending on what leg this is.
* If we connect crossbar, pass the cluster id of connector. 
* A "connection" is made OUT to another powerplant.  A "customer" connects
* IN from another plant or elsewhere.
* @see LRInputTransmissionLine
* @author Groff (C) NeoCoreTechs 1998,1999,2014
*/
public class PSInputOfPowerPlantConnection extends PSAbstractInputTower {
        LRInputTransmissionLine lrt;

        public PSInputOfPowerPlantConnection(LRInputTransmissionLine tlrt, Socket tso, Vector tqueue) {
                super(tqueue, tso);
                lrt = tlrt;
        }
        public int getLeg() { return lrt.getLeg(); }

        public void reInit() throws IOException { lrt.reInit(); }

        /**
        * Perform the connection, then start the flow from the InputTower
        */
        public void Connect() {
           try {
                new PSSynchTower().SynchIn(this);
                //
                startFlow();
                //
           } catch(Exception e) {
                System.out.println("! EXCEPTION LOGGED shutting down towers !");
                System.out.println(e.getMessage());
                e.printStackTrace();
                PowerPlant.powerPlantLegConnections[getLeg()].unLink();
           }
        }
        
}
