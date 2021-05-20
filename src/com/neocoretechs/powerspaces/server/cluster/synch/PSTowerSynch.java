package com.neocoretechs.powerspaces.server.cluster.synch;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;

import java.io.*;
import java.net.*;
/**
* PSTowerSynch synchs up the input and output towers when another PowerPlant connects to this one.<dd>
* The clusterID is passed down, ( later we should check the left and right powerplant to see if connected.  If so
* we need to create a virtual translation for a cluster connecting to this cluster.  The parent send and receive should
* be modified to change the cluster id.)
* @author Groff (C) NeoCoreTechs 1998, 1999
*/
public final class PSTowerSynch implements PSSynchInterface {
        public void SynchIn(PSAbstractInputTower tin) throws Exception
        {
                  ConnectionPanel.setupConnectionLeg(tin.getInputStream(), tin.getLeg());
                  // We are working on input which is port to match input
                  String session = tin.getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(tin.getSocket().getLocalPort());
                  System.out.println("PSTowerSynchIn session "+session);
                  PowerPlant.powerPlantLegConnections[tin.getLeg()].getTowerLink().setInput(tin);
                  if( PowerPlant.powerPlantLegConnections[tin.getLeg()].isLinked() ) {
                        System.out.println("The Cluster ID of this PowerPlant is now "+PowerPlant.clusterID);
                  }
//                  ConnectionPanel.sendPacket(So, new Packet("OK"));
        }
        public void SynchOut(PSAbstractOutputTower tout) throws Exception
        {
                  // We are working on input which is port to match input
                  String session = tout.getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(tout.getSocket().getLocalPort());
                  System.out.println("PSTowerSynchOut session "+session);
                  PowerPlant.powerPlantLegConnections[tout.getLeg()].getTowerLink().setOutput(tout);
                  if( PowerPlant.powerPlantLegConnections[tout.getLeg()].isLinked() ) {
                        System.out.println("The Cluster ID of this PowerPlant is now "+PowerPlant.clusterID);
                  }
//                  ConnectionPanel.sendPacket(So, new Packet("OK"));
        }
}
