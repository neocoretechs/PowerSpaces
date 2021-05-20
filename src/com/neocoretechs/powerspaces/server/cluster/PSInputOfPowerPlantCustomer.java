package com.neocoretechs.powerspaces.server.cluster;
import java.util.*;
import java.net.*;
import java.io.*;

import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.cluster.synch.PSCrossbarTowerSynch;
import com.neocoretechs.powerspaces.server.cluster.synch.PSSynchInterface;
import com.neocoretechs.powerspaces.server.cluster.synch.PSTowerSynch;
/**
* PowerSpaces input leg of PowerPlant "customer", which is connecting
* to the parent node of this PowerPlant.<p>
* These are queued for PSTransformers to connect one PowerPlant stream to another.
* The transformer grabs these off a queue and calls "Connect", which is implemented
* by PSAbstractInputTower from PSCustomerInterface. <p>
* ParentInputTransmissionLine starts the PSTransformerThread.<p>
* The difference between a "connection" and a "customer" is as follows: <dd>
* A "connection" is made OUT to another powerplant.  A "customer" connects
* IN from another plant or elsewhere.
* @see PSAbstractInputTower
* @see ParentInputTransmissionLine
* @author Groff Copyright (C) NeoCoreTechs 1998-2000,2014
*/
public final class PSInputOfPowerPlantCustomer extends PSAbstractInputTower {
        private ParentInputTransmissionLine tl;
        private short transactType;
        //
        //
        public PSInputOfPowerPlantCustomer(ParentInputTransmissionLine ttl, Vector tqueue) {
                super(tqueue, ttl.getSocket());
                tl = ttl;
        }

        public int getLeg() { return tl.getLeg(); }

        public void reInit() throws IOException { tl.reInit(); }
        /**
        * The transformer threads will call this method, which is declared in
        * PSCustomerInterface
        */
        public void Connect() {
                try {
                  System.out.println("PSInputOfPowerPlantCustomer Connect started "+getSocket()+" "+PowerPlant.getInstance());
                  PSSynchInterface legToSynch = null; 
                  if( getLeg() == 0 )
                        legToSynch = new PSTowerSynch();
                  else 
                        legToSynch = new PSCrossbarTowerSynch();
                  //
                  legToSynch.SynchIn(this);
                  startFlow();
                  //
                } catch(Exception e) {
                        System.out.println("! EXCEPTION LOGGED Shutting down towers!");
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                        PowerPlant.powerPlantLegConnections[getLeg()].unLink();
                }
       }
}
