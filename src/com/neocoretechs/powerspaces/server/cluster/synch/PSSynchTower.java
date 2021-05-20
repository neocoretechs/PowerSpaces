package com.neocoretechs.powerspaces.server.cluster.synch;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;

import java.io.*;
import java.net.*;
/**
* PSSynchTower synchs up the input and output towers when this one connects to another PowerPlant.<dd>
* The clusterID is passed down from here, ( later we should check the left and right powerplant to see if connected.  If so
* we need to create a virtual translation for a cluster connecting to this cluster.  The parent send and receive should
* be modified to change the cluster id.)
* @see PSTowerSynch
* @author Groff (C) NeoCoreTechs 1998-2000
*/
public final class PSSynchTower implements PSSynchInterface {
        public synchronized void SynchIn(PSAbstractInputTower tin) throws Exception
        {
                  String session = tin.getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(tin.getSocket().getLocalPort());
                  System.out.println("PSSynchTower session "+session);
                  PowerPlant.powerPlantLegConnections[tin.getLeg()].getTowerLink().setInput(tin);
                  if( PowerPlant.powerPlantLegConnections[tin.getLeg()].isLinked() ) {
                        System.out.println("This PowerPlant is now connected to "+String.valueOf(PowerPlant.powerPlantLegConnections[tin.getLeg()].getLinkedClusterID())+" through leg "+tin.getLeg());
                  }
        }
        public synchronized void SynchOut(PSAbstractOutputTower tout) throws Exception
        {
                  // read session packet
                  ConnectionPanel.setupConnectionLeg(tout.getOutputStream(), tout.getLeg());
                  String session = tout.getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(tout.getSocket().getLocalPort()-1);
                  System.out.println("PSSynchTower session "+session);
                  PowerPlant.powerPlantLegConnections[tout.getLeg()].getTowerLink().setOutput(tout);
                  if( PowerPlant.powerPlantLegConnections[tout.getLeg()].isLinked() ) {
                        System.out.println("This PowerPlant is now connected to "+String.valueOf(PowerPlant.powerPlantLegConnections[tout.getLeg()].getLinkedClusterID())+" through leg "+tout.getLeg());
                  }
        }
}
