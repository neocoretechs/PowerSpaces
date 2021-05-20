package com.neocoretechs.powerspaces.server.cluster.synch;
import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;
import com.neocoretechs.powerspaces.server.PowerPlant;

/**
* PSCrossbarTowerSynch synchs up the input and output crossbar legs when
* another PowerPlant connects to this one from its left crossbar.
* The clusterID is passed across, later we should check the left and right powerplant to see if connected.  If so
* we need to create a virtual translation for a cluster connecting to this cluster.  The parent send and receive should
* be modified to change the cluster id.
* Instead of handing down clusterID like we did connecting to parent, we are
* going to swap the clusterID info here
* @author Groff (C) NeoCoreTechs 1998, 1999,2014
*/
public final class PSCrossbarTowerSynch implements PSSynchInterface {
        public synchronized void SynchIn(PSAbstractInputTower tin) throws Exception
        {
                  ConnectionPanel.setupConnectionLeg(tin.getInputStream(), tin.getLeg());
                  // we are working on input which is port to match input
                  String session = tin.getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(tin.getSocket().getLocalPort());
                  // we might put some IP based security here...
                  System.out.println("PSCrossbarTowerSynch session "+session);
                  PowerPlant.powerPlantLegConnections[tin.getLeg()].getTowerLink().setInput(tin);
                  if( PowerPlant.powerPlantLegConnections[tin.getLeg()].isLinked() ) {
                        System.out.println("The ID of the right crossbar PowerPlant is now "+PowerPlant.powerPlantLegConnections[tin.getLeg()].getLinkedClusterID());
                  }
        }
        public synchronized void SynchOut(PSAbstractOutputTower tout) throws Exception
        {
                  ConnectionPanel.setupConnectionLeg(tout.getOutputStream(), tout.getLeg());
                  String session = tout.getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(tout.getSocket().getLocalPort()-1);
                  // we might put some IP based security here...
                  System.out.println("PSCrossbarTowerSynch session "+session);
                  PowerPlant.powerPlantLegConnections[tout.getLeg()].getTowerLink().setOutput(tout);
                  if( PowerPlant.powerPlantLegConnections[tout.getLeg()].isLinked() ) {
                        System.out.println("The ID of the right crossbar PowerPlant is now "+PowerPlant.powerPlantLegConnections[tout.getLeg()].getLinkedClusterID());
                  }
        }
}
