package com.neocoretechs.powerspaces.server;
import java.util.*;

import com.neocoretechs.powerspaces.server.cluster.IPCLink;
/**
* PowerPlantLegConnections encapsulates the connections and properties for a PowerPlant Leg
* including TCP ports to remote PowerPlants, Inbound Packet queue, and
* local ports to bind through
* to remote PowerPlants.
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
public final class PowerPlantLegConnections {
        private int InputPowerPlantPort;
        private int OutputPowerPlantPort;
        //
        // the local ports to bind to outbound connections
        private String LocalBindIn;
        private String LocalBindOut;
	//
        // this object holds links for input and output towers
        private IPCLink TowerLink = new IPCLink();
        //
        //
        private long linkedClusterID;
	//
        private Vector InputPacketQueue = new Vector();

        public void setInputPort(int tport) { InputPowerPlantPort = tport; }
        public int getInputPort() { return InputPowerPlantPort; }
        public void setOutputPort(int tport) { OutputPowerPlantPort = tport; }
        public int getOutputPort() { return OutputPowerPlantPort; }

        public void setLocalBindIn(String tlbi) { LocalBindIn = tlbi; }
        public String getLocalBindIn() { return LocalBindIn; }
        public void setLocalBindOut(String tlbo) { LocalBindOut = tlbo; }
        public String getLocalBindOut() { return LocalBindOut; }

        public IPCLink getTowerLink() { return TowerLink; }
        public boolean isLinked() { return TowerLink.isLinked(); }
        public void setUnlinked() { TowerLink.setUnlinked(); }

        public void setLinkedClusterID(long tlcid) { linkedClusterID= tlcid; }
        public long getLinkedClusterID() { return linkedClusterID; }

        public Vector getInputPacketQueue() { return InputPacketQueue; }

        /**
        * unLink causes unLink in IPCHandler to be called, which closes sockets.
        * This could cause PSAbstractInputTower or PSAbstractOutputTower to throw exception
        * in read or write loop, thereby causing a call here again, so we check for
        * null in TowerLink var.  Reason: either InputTower or OutputTower could
        * shut down and the other must follow.
        */
        public synchronized void unLink() {
                TowerLink.unLink();
        }
}
