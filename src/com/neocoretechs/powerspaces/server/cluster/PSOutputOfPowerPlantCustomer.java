package com.neocoretechs.powerspaces.server.cluster;
import java.util.*;
import java.net.*;
import java.io.*;

import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.cluster.synch.PSCrossbarTowerSynch;
import com.neocoretechs.powerspaces.server.cluster.synch.PSSynchInterface;
import com.neocoretechs.powerspaces.server.cluster.synch.PSTowerSynch;
/**
* PowerSpaces output leg of PowerPlant customer, which is connecting to parent
* noe of this PowerPlant.<p>
* These are queued for PSTransformers to connect one PowerPlant stream to another.
* The transformer grabs these off a queue and calls "Connect", which is implemented
* by PSAbstractOutputTower from PSCustomerInterface. <p>
* ParentOutputTransmissionLine starts the PSTransformerThread.
* @see PSAbstractOutputTower
* @see ParentOutputTransmissionLine
* @author Groff Copyright (C) NeoCoreTechs 1998-2000
*/
public final class PSOutputOfPowerPlantCustomer extends PSAbstractOutputTower {

        private ParentOutputTransmissionLine tl;
        private short transactType;
        //
        public PSOutputOfPowerPlantCustomer(ParentOutputTransmissionLine ttl) {
                super(ttl.getSocket());
                tl = ttl;
        }

        public int getLeg() { return tl.getLeg(); }

        public void reInit() throws IOException { tl.reInit(); }

        /**
        * 
        * the connect finds the entry for the input server and adds this,
        * the output server, to the object and notifys the waiting input
        * server, at that point a full connection is made
        */
        public void Connect() {
                try {
                  System.out.println("PSOutputOfPowerPlantCustomer Connect "+getSocket()+" "+PowerPlant.getInstance());
                  PSSynchInterface legToSynch = null; 
                  if( getLeg() == 0 )
                        legToSynch = new PSTowerSynch();
                  else 
                        legToSynch = new PSCrossbarTowerSynch();
                  //
                  legToSynch.SynchOut(this);
                  //
                  // loop to keep getting input stream and write it
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
