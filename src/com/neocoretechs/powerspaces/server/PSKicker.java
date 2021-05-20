package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.*;
import java.net.Socket;
/**
* The Kicker is a small generator that boosts power in the grid in the electricity world and
* here it is responsible for executing an asynchronous user "handler"
* in a thread from a PSTransformerThread worker thread pool.<p>
* With this in mind, this Kicker vector is a smaller version of the PSTurbineThread vector
* which will use a PSTransformerThread thread pool to asynchronously execute
* a user handler. PSTransformers started
* in PowerPlant grab the kickerVector PSKickers and kick them off. This is a way for the
* user to pump commands into the cluster asychronously
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000,2014
*/
public class PSKicker implements PSCustomerInterface {
        private Integer leg;
        private CustomerConnectionPanel ccp;
        private PKTransportMethodCall pktmc;
        public PSKicker(Integer tleg, CustomerConnectionPanel tccp, PKTransportMethodCall tpktmc) {
                leg = tleg;
                ccp = tccp;
                pktmc = tpktmc;
        }
        /**
         * ultimately we just do a commandCluster with our method transport
         */
        public void Connect() {
           try {
                PowerPlant.commandCluster(leg, ccp, pktmc);
           } catch(Exception e) {
                System.out.println(e);
                e.printStackTrace();
           }
        }
}                
