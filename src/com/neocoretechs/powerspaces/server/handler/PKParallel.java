package com.neocoretechs.powerspaces.server.handler;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.*;
import com.neocoretechs.powerspaces.server.cluster.IPCLink;
import com.neocoretechs.powerspaces.server.cluster.MessageRoutingPacketField;

import java.io.*;
/**
* PKParallel is a type of "handler" that provides parallel computation capability
* throught various types of message passing, broadcast, and barrier synchronization
* methods.<p>
* The idea is to have static methods invoking on behalf of customers arriving at
* random from elsewhere in the cluster.<p>
* As far as session state goes, The CustomerConnectionPanel
* may or may not exist for the customer, so in order to have a persistent
* panel for synch operation and such, we need to make it permanent in each method.
* Some methods will not need or want a permanent panel, such as those that
* probe remote nodes.  (The node being probed is where the command is executed)
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
public class PKParallel {
        public static String PKParallelClass = PKParallel.class.getName();
        /**
        * ReduceSynch command is sent by child and
        * will cause the number of expected Packets to be reduced by 1.
        * If our synch array size matches expected number after reduction,
        * it's time to queue. <p>
        * We always have at least 1 on Vector  representing original arg passed down,
        * So we set numberToCollect with maximum expected, and gradually reduce
        * based on availability of left and right PowerPlant and this command.
        * This command is sent if no session is present on attached PowerPlants (i.e.
        * they were not used by 'Balance' and no session exists for this Customer there.)
        */
        public static void PowerKernel_ReduceSynch(Integer leg, CustomerConnectionPanel tccp, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
                // make sure it's perm
                if( !tccp.getTable() ) ConnectionPanel.putCustomerConnectionPanel(tccp);
                tccp.reduceSynchPacket();
        }

        public static void PowerKernel_Send(Integer leg, CustomerConnectionPanel tccp, Integer toleg, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
                // make sure it's perm
                if( !tccp.getTable() ) ConnectionPanel.putCustomerConnectionPanel(tccp);
                // create a PKTransportMethodCall with the arg Packet of passed packet
                // format Packet[class,method,Packet[arg1,arg2...]]
                PKTransportMethodCall pktmc = PKServerInvokeMethod.makeMethodCall(tccp, pc); 
                //
                ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[toleg.intValue()].getTowerLink(), tccp, pktmc);
        }

        /**
        * Forward message (a command) to PowerPlant. <p>
        * Store and forward at each node.  The MessageRoutingPacketField determines the
        * necessary path to take at each turn and results are synched back to origin node.
        * At the end of the synchronization (we have enough Packets) we will take
        * the first Packet pushed and exec command in field 0, with args all remaining Packets
        */
        public static void PowerKernel_SendMessage(Integer leg, CustomerConnectionPanel ccp, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
		//
                PKTransportMethodCall pktmc = PKServerInvokeMethod.makeMethodCall(ccp, pc);
                System.out.println("PKParallel.SendMessage "+pc+" to session "+ccp.getSession());
                // make sure CCP is perm
                if( !ccp.getTable() ) ConnectionPanel.putCustomerConnectionPanel(ccp);
                //
                Long tclusterID = (Long)(pc.getField(0).value());
                MessageRoutingPacketField mrpf = new MessageRoutingPacketField(PowerPlant.clusterID, tclusterID.longValue());
                // call with current PowerPlant clusterID
                int toleg = mrpf.getPacketForwardDirection();		
                System.out.println("Sending message to "+String.valueOf(toleg)+" for session "+ccp.getSession());
                if( toleg == -1 ) {
                	// we're here
                    // create a PKTransportMethodCall with the arg Packet of passed packet
                    // format Packet[class,method,Packet[arg1,arg2...]]
                    try { 
                                ccp.queuePacket( PowerPlant.commandCluster(leg, ccp, pktmc) );
                    } catch(Exception e) {
                                System.out.println("PKParallel.SendMessage Exception "+e+" session "+ccp.getSession());
                                e.printStackTrace();
                                ccp.queuePacket(new Packet(e));
                    }
                } else
                    ccp.queueMessagePacket(pktmc, mrpf);
        }

        public static void PowerKernel_SwitchBalance(Integer leg, CustomerConnectionPanel ccp) throws PowerSpaceException, IOException, ClassNotFoundException {
                /**
                * switch balance , this is received from child node
                * and causes us to switch the flow for forwarded, balanced commands
                */
                if( ccp.balance == 2 )
                           ccp.balance = 0;
                else
                           ++ccp.balance;
        }
        /**
        * balance command flow, we are going to send it left or
        * right depending on spinner in customer connection panel
        * when 2 (end of spin seq.) we send a spin command to
        * parent and reset this. eventually, if we go to root first
        * we can have even flo throughout.  This node will have the spinner
        * reset by nodes below for this CCP, or not if none exist below.
        */
        public static void PowerKernel_Balance(Integer leg, CustomerConnectionPanel ccp, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
                PKTransportMethodCall pktmc;
                Packet pc2;
                IPCLink plants[] = new IPCLink[2];
                boolean isPlant[] = new boolean[2];
                plants[0] = PowerPlant.powerPlantLegConnections[1].getTowerLink();
                plants[1] = PowerPlant.powerPlantLegConnections[2].getTowerLink();
                isPlant[0] = PowerPlant.powerPlantLegConnections[1].isLinked();
                isPlant[1] = PowerPlant.powerPlantLegConnections[2].isLinked();
                // make sure it's perm
                if( !ccp.getTable() ) ConnectionPanel.putCustomerConnectionPanel(ccp);
                // if 2, set this to 0, send parent switch
                // exec command this node
                System.out.println("PKParallel.Balance "+String.valueOf(ccp.balance)+" session "+ccp.getSession());
                if( ccp.balance == 2 ) {
                        ccp.balance = 0;
                        pktmc = PKServerInvokeMethod.makeMethodCall(ccp, pc);
                        try {
                                pc2 = PowerPlant.commandCluster(leg, ccp, pktmc);
                        } catch(Exception e) {
                                System.out.println("Balance Exception "+e);
                                pc2 = new Packet(e);
                        }
                        ccp.queueReturnPacket(pc2);
                        // see if we need to switch balance
                        // don't send to parent if we're root
                        if(PowerPlant.powerPlantLegConnections[0].isLinked()) {
                                 pktmc = new PKTransportMethodCall(ccp.getSession(), PKParallelClass, "xx", "SwitchBalance");
                                 ConnectionPanel.forwardCmd(
                                            PowerPlant.powerPlantLegConnections[0].getTowerLink(),
                                            ccp, pktmc);
                       }
                } else {
                         // here we send left or right
                         if( isPlant[ccp.balance] ) {
                                   pktmc = new PKTransportMethodCall(ccp.getSession(), PKParallelClass, "xx", "Balance", pc);
                                   ConnectionPanel.forwardCmd(plants[ccp.balance], ccp, pktmc);
                          } else {
                                   // we have no plant this way, we have to inc balance
                                   // cause no child to send us message to do so..
                                   ++ccp.balance;
                                   // now do command
                                   pktmc = PKServerInvokeMethod.makeMethodCall(ccp, pc);
                                   try {
                                        pc2 = PowerPlant.commandCluster(leg, ccp, pktmc);
                                   } catch(Exception e) {
                                        System.out.println("Balance Exception "+e);
                                        e.printStackTrace();
                                        pc2 = new Packet(e);
                                   }
                                   ccp.queueReturnPacket(pc2);
                          }
                }
        }
        /**
        * Forward command left and right if such PowerPlants exist.<p>
        * The execution of the command method is contingent on a CustomerConnectionPanel
        * of the same session existing in the other PowerPlant.  If the connection panel is not there,
        * a reduceSynch command is sent to parent and
        * this will cause the number of expected Packets to be reduced by 1.
        * We always push the original arg passed down so we always have at leat one on
        * Vector. <p>
        * At the end of the synchronization (we have enough Packets) we will take
        * the first Packet pushed and exec command in field 0, with args all remaining Packets
        */
        public static void PowerKernel_Broadcast(Integer leg, CustomerConnectionPanel ccp, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
                // if no session here, reduce synch to parent
                if( !ccp.getTable() ) {
                        if( PowerPlant.powerPlantLegConnections[0].isLinked() ) {
                                // create a PKTransportMethodCall with the arg Packet of passed packet
                                // format Packet[class,method,Packet[arg1,arg2...]]
                                PKTransportMethodCall pktmc = new PKTransportMethodCall(ccp.getSession(), PKParallelClass, "xx", "ReduceSynch");
                                ConnectionPanel.forwardCmd(
                                            PowerPlant.powerPlantLegConnections[0].getTowerLink(), ccp, pktmc);
                        }
                }
                //
                System.out.println("PKParallel.Broadcast "+pc+" session "+ccp.getSession());
                PKTransportMethodCall pktmc = PKServerInvokeMethod.makeMethodCall(ccp, pc);
                // construct a Broadcast with command to send onward
                PKTransportMethodCall pktmcf = PKServerInvokeMethod.makeMethodCall(ccp, new Packet(PKParallelClass, "Broadcast", pc));
                ccp.numberToCollect = 3;
                ccp.queueSynchPacket(pktmc);
                if(PowerPlant.powerPlantLegConnections[1].isLinked() )
                        ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[1].getTowerLink(),
                                                ccp, pktmcf);
                else
                	--ccp.numberToCollect;
                if(PowerPlant.powerPlantLegConnections[2].isLinked() )
                        ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[2].getTowerLink(),
                                                ccp, pktmcf);
                else
                	--ccp.numberToCollect;
                Packet rp = null;
                // check here quickly for no left or right
                // numberToCollect will be 1 if no left or right
                // or a reduceSynch cmd is had.
                // reduceSynch also checks, thereby picking up slack from here
                if( ccp.numberToCollect == 1 ) {
                System.out.println("PKParallel.PSGeneratorBroadcast, no left or right PowerPlants. session "+ccp.getSession());
				// get command, packet
                                pktmc = (PKTransportMethodCall)((Packet)(ccp.synchPackets.elementAt(0))).getField(0).value();
                                Object[] opa = pktmc.getParamArray();
                                // check param array to see if we need to extract result Packet
                                // if it has something and its Packet, put it in results
                                // if it has something and its not Packet, wrap it
                                // if nothing put result of command
                                if( opa.length > 0 && opa[0] instanceof Packet ) {
                                        rp = (Packet)(opa[0]);
                                        for(int i = 1; i < ccp.synchPackets.size() ; i++)
                                                rp.add((Serializable)(ccp.synchPackets.elementAt(i)));
                                }
				ccp.numberToCollect = 0;
				ccp.synchPackets.removeAllElements();
                                // invoke final command
				try {
					rp = PowerPlant.commandCluster(leg, ccp, pktmc);
				} catch(Exception e) {
                    System.out.println("PKParallel.Broadcast Exception "+e+" sesssion "+ccp.getSession());
                    e.printStackTrace();
					rp = new Packet(e,rp);
				}
				// queue the synched Packets
				ccp.queueSynchPacketUp(rp);
                }
        }
        /**
        * Forward command left and right if such PowerPlants exist. <p>
        * A CustomerConnectionPanel need not exist on remote PowerPlant.
        * If a connection panel is not there, reduceSynch command is sent to parent
        * this will cause the number of expected Packets to be reduced by 1.<p>
        * We always push the original arg passed down so we always have at leat one on
        * Vector. <p>
        * At the end of the synchronization (we have enough Packets) we will take
        * the first Packet pushed and exec command in field 0, with args all remaining Packets
        * The first packet in the queue is method call transport.
        * The param in the method call will be used as results come up from other
        * nodes:
        * The param to a parallel method is a Packet that
        * is stuffed with results from other nodes and passed to the
        * method called on this node.
        */
        public static synchronized void PowerKernel_BroadcastAll(Integer leg, CustomerConnectionPanel ccp, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
                // make sure it's perm
                if( !ccp.getTable() ) ConnectionPanel.putCustomerConnectionPanel(ccp);
                System.out.println("PKParallel.PSGeneratorBroadcastAll "+pc+" for session "+ccp.getSession());
                PKTransportMethodCall pktmc = PKServerInvokeMethod.makeMethodCall(ccp, pc);
                // construct a BroadcastAll with command to send onward
                PKTransportMethodCall pktmcf = PKServerInvokeMethod.makeMethodCall(ccp, new Packet(PKParallelClass, "BroadcastAll", pc));
                ccp.numberToCollect = 3;
                // first packet in queue is method call transport
                // this will be used as results come up from below
                // the param to a parallel method is a Packet that
                // is stuffed with results from other nodes and passed to the
                // method called on this node
                ccp.queueSynchPacket(pktmc);
                if(PowerPlant.powerPlantLegConnections[1].isLinked() )
                        ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[1].getTowerLink(),
                                                ccp, pktmcf);
                else
                	--ccp.numberToCollect;
                if(PowerPlant.powerPlantLegConnections[2].isLinked() )
                        ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[2].getTowerLink(),
                                                ccp, pktmcf);
                else
                	--ccp.numberToCollect;
		
                Packet rp = null;
                // check here quickly for no left or right
                // numberToCollect will be 1 if no left or right
                // or a reduceSynch cmd is had.
                // reduceSynch also checks, thereby picking up slack from here
                if( ccp.numberToCollect == 1 ) {
                System.out.println("PKParallel.PSGeneratorBroadcastAll, no left or right PowerPlants. session "+ccp.getSession());
				// get command, packet
                                pktmc = (PKTransportMethodCall)((Packet)(ccp.synchPackets.elementAt(0))).getField(0).value();
                                Object[] opa = pktmc.getParamArray();
                                // check param array to see if we need to extract result Packet
                                // if it has something and its Packet, put it in results
                                if( opa.length > 0 && opa[0] instanceof Packet ) {
                                        rp = (Packet)(opa[0]);
                                        for(int i = 1; i < ccp.synchPackets.size() ; i++)
                                                rp.add((Serializable)(ccp.synchPackets.elementAt(i)));
                                }
				ccp.numberToCollect = 0;
				ccp.synchPackets.removeAllElements();
                                // invoke final command
				try {
                                        rp = PowerPlant.commandCluster(leg, ccp, pktmc);
				} catch(Exception e) {
                    System.out.println("PKParallel.BroadcastAll Exception "+e+" session "+ccp.getSession());
                    e.printStackTrace();
					rp = new Packet(e,rp);
				}
                ccp.queueSynchPacketUp(rp);
  			}
        }
        /**
        * Forward command left and right if such PowerPlants exist.
        * No synchronization, just stream results back
        * We use the PSKicker object and kickerVector to set up
        * seperate threads for each invocation.  PSTransformers started
        * in PowerPlant grab the kickerVector PSKickers and kick them off
        */
        public static void PowerKernel_BroadcastAllNoSynch(Integer leg, CustomerConnectionPanel ccp, Packet pc) throws PowerSpaceException, IOException, ClassNotFoundException {
                // make sure CCP is perm
                if( !ccp.getTable() ) ConnectionPanel.putCustomerConnectionPanel(ccp);
                System.out.println("PKParallel.PSGeneratorBroadcastAllNoSynch "+pc+" session "+ccp.getSession());
                PKTransportMethodCall pktmc = PKServerInvokeMethod.makeMethodCall(ccp, pc);
                // construct a BroadcastAll with command to send onward
                PKTransportMethodCall pktmcf = PKServerInvokeMethod.makeMethodCall(ccp, new Packet(PKParallelClass, "BroadcastAllNoSynch", pc));
                if(PowerPlant.powerPlantLegConnections[1].isLinked() )
                        ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[1].getTowerLink(),
                                                ccp, pktmcf);
                if(PowerPlant.powerPlantLegConnections[2].isLinked() )
                        ConnectionPanel.forwardCmd( PowerPlant.powerPlantLegConnections[2].getTowerLink(),
                                                ccp, pktmcf);
                // invoke final command...start kicker thread
                try {
                        // PSTransformerThread will grab kicker object and call Connect
                        PSKicker psk = new PSKicker(leg, ccp, pktmc);
                        synchronized(PowerPlant.kickerVector) {
                                PowerPlant.kickerVector.addElement(psk);
                                PowerPlant.kickerVector.notify();
                        }
                        // 
                } catch(Exception e) {
                        System.out.println("PKParallel.BroadcastAllNoSynch Exception "+e+" session "+ccp.getSession());
                        e.printStackTrace();
                        ccp.queueReturnPacket(new Packet(e));
                }
        }


        /**
        * collect, get result packets from queue
        */
        public static Object PowerKernel_Collect(Integer leg, CustomerConnectionPanel ccp) throws PowerSpaceException {
                Object o = null;
                try {
                        synchronized(ccp.resultPackets) {
                                while( ccp.resultPackets.size() == 0 ) ccp.resultPackets.wait();
                                o = ccp.resultPackets.elementAt(0);
                                ccp.resultPackets.removeElementAt(0);
                         }
                } catch(InterruptedException ie) {}
                return o;
        }

       /**
       * Return cluster Id
       */
       public static Object PowerKernel_GetClusterID(Integer leg, CustomerConnectionPanel ccp, Packet p) throws PowerSpaceException, IOException, ClassNotFoundException {
                p.add(new Long(PowerPlant.clusterID));
                return p;
       }
}
