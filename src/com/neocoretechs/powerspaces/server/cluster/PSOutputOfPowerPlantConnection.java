package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.cluster.synch.PSSynchTower;

import java.net.*;
import java.io.*;
import java.util.*;
/**
* This class initiates and handles output inter-powerplant connections through
* the legs, which means it is opening a Socket to another PowerSpace parent
* leg listening with a ServerSocket (TCPServer class).<p>
* This object created by LROutputTransmissionLine.<br>
* A "connection" is made OUT to another powerplant.  A "customer" connects
* IN from antoher plant or elsewhere.
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000,2014
*/
public class PSOutputOfPowerPlantConnection extends PSAbstractOutputTower {
        LROutputTransmissionLine lrt;

        public PSOutputOfPowerPlantConnection(LROutputTransmissionLine tlrt, Socket tso) {
                super(tso);
                lrt = tlrt;
        }
        public int getLeg() { return lrt.getLeg(); }

        public void reInit() throws IOException { lrt.reInit(); }

        public void Connect() {
             try {
                new PSSynchTower().SynchOut(this);
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
