package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.cluster.MessageRoutingPacketField;

import java.io.*;
import java.util.*;
/**
* State object for client session including synchronization and message result queue. <p>
* The customer connection panel is used to manage interconnects
* for a particular customer session.  One will exist each place the
* original session (final leg -1) moves data to.  This is
* rotated into position by towers
* via session packet ID.  Also used to control io to final leg.
* @author Groff (C) NeoCoreTechs, Inc. 1998,2000
*/
public final class CustomerConnectionPanel {
        private int originLeg;
        public int balance = 0;
        public int numberToCollect = 0;
	private String session;
        private boolean inTable = false;

        public Vector resultPackets;
	public Vector synchPackets;
	/**
        * Hash table to get and set properties.  This allows the handler writer to preserve state info
	* between differnet customers.
	*/
	public Hashtable properties;

	public int getOriginLeg() { return originLeg; }
	public String getSession() { return session; }
        public boolean getTable() { return inTable; }
        public void setTable(boolean tin) { inTable = tin; }

        /**
        * Constructor invoked for origin and inter-powerplant connections. <p>
	* resultPackets Vector used to queue results back to Customer on origin leg
	* so only panels on origin get that Vector.  synchPackets is used for
	* synchronizing nodes below this one.  If we queue a Packet or issue a
	* reduceSynch command from a node with no connection panel for the
	* particular session, we check to see if synch array size is equal to
	* numberToCollect, which is set from Broadcast commands.
	* if they are equal, we queue the Packets in the Vector as one big Packet.
	* We'll always either do a queue or reduceSynch with a packet return scenario.
        */
        public CustomerConnectionPanel(String tsession, int torig) {
		session = tsession;
                originLeg = torig;
		resultPackets = new Vector();
		synchPackets = new Vector();
		properties = new Hashtable();
        }

        /**
        * Queue a Packet to proper destination queue for return. <p>
        * Depending on leg, we queue a Packet to send response to.<p>
        * Leg is 0 - parent, 1- left, 2- right, -1- final leg
        * (final leg is back to original session customer pole).
        * originLeg is original entry point on this node, usually return point
        */
        public void queuePacket(Object p) throws ClassNotFoundException, PowerSpaceException, IOException {
			
                 if( originLeg == -1 ) {
                        // in this case (origin leg), we put results onto
                        // vector for "Collect" command to get later
//                        System.out.println("CCP.queuePacket: Queueing result packet "+p+" for session "+session);
                        synchronized(resultPackets) {
                                resultPackets.addElement(p);
                                resultPackets.notify();
                        }
                        return;
		}
		ConnectionPanel.queuePacket(p, originLeg);
        }

	/**
	* queue return packet, which is the result of a direct command
	*/
        public void queueReturnPacket(Object p) throws ClassNotFoundException, PowerSpaceException, IOException
	{
                queuePacket(new SystemPacket( 1, session, (Serializable)p));
	}

	/**
        * Queue a Packet onto result queue. <p>
	* This queue can be "Collected" by customers at origin or other means.
	*/
        public void queueResultPacket(Object p) throws ClassNotFoundException, PowerSpaceException, IOException 
	{
//                     System.out.println("CCP.queueResultPacket: Queueing result packet "+p+" for session "+session);
        	     synchronized(resultPackets) {
                                        resultPackets.addElement(p);
                                        resultPackets.notify();
                     }
                     return;
	}

	/**
        * Queue a Packet to synchronizing queue. <p>
	* When the predetermined number of Packets is received from descendant PowerPlants
	* the original command Packet at element 0 of synchPackets vector is
        * invoked. The arguments to that command Packet are results from descendents.
        * This method inserts the PKTransportMethodCall at elem 0
        */
        public synchronized void queueSynchPacket(PKTransportMethodCall p) throws ClassNotFoundException, PowerSpaceException, IOException
	{
                // insert in incoming to Vector
                if( synchPackets.size() == 0 )
                        synchPackets.addElement(new Packet(p));
                else        
                        synchPackets.setElementAt(new Packet(p), 0);
        }
	/**
        * Queue a Packet to synchronizing queue. <p>
	* When the predetermined number of Packets is received from descendant PowerPlants
	* the original command Packet at element 0 of synchPackets vector is
	* invoked. The arguments to that command Packet are results from descendents
	* execution of that same command.  Broadcast operations start this machinery in motion.
	* (See constructor)
	*/ 
        public synchronized void queueSynchPacket(Packet p) throws ClassNotFoundException, PowerSpaceException, IOException
	{
		Packet rp = null;
		// add incoming to Vector
		synchPackets.addElement(p);
		//
//                System.out.println("CCP.queueSynchPacket "+session+" numberToCollect "+String.valueOf(numberToCollect)+" "+String.valueOf(synchPackets.size()));
		if( numberToCollect > 0 ) {
		   if( synchPackets.size() == numberToCollect ) {
                                // Packet Packet[PKTransportMethodCall[resultPacket] ]
                                PKTransportMethodCall pktmc = (PKTransportMethodCall)((Packet)(synchPackets.elementAt(0))).getField(0).value();
                                Object[] opa = pktmc.getParamArray();
                                // check param array to see if we need to extract result Packet
                                // if it has something and its Packet, put it in results
                                // 
                                if( opa.length > 0 && opa[0] instanceof Packet ) {
                                        rp = (Packet)(opa[0]);
                                        // add synched packets to result packet
                                        for(int i = 1; i < synchPackets.size() ; i++)
                                                rp.add((Serializable)(synchPackets.elementAt(i)));
                                }
				numberToCollect = 0;
				synchPackets.removeAllElements();
				try {
                                        queueSynchPacketUp((Packet)(PowerPlant.commandCluster(originLeg, this, pktmc)));
				} catch(Exception e) {
                                        System.out.println("QueueSynchPacket Exception "+e+" for session "+session);
                                        rp = new Packet((Serializable)e); 
					queueSynchPacketUp(rp);
				}
		   }
		} else {
		   throw new PowerSpaceException("numberToCollect 0, but synch Packet arrived",p);	
		}
	}

	/**
        * Reduce synchronizing queue. <p>
	* When the predetermined number of Packets is received from descendant PowerPlants
	* the original command Packet at element 0 of synchPackets vector is
	* invoked. The arguments to that command Packet are results from descendents
	* execution of that same command.  Broadcast operations start this machinery in motion.
	* (See constructor)
        *
        * ReduceSynch command is sent by child
        * this will cause the number of expected Packets to be reduced by 1.
        * If our synch array size matches expected number after reduction,
        * it's time to queue.
        * We always have at least 1 on Vector  representing original arg passed down,
        * So we set numberToCollect with maximum expected, and gradually reduce
        * based on availability of left and right PowerPlant and this command.
        * This command is sent if no session is present on attached PowerPlants (i.e.
        * they were not used by 'Balance' and no session exists for this Customer there.)
	*/ 
        public synchronized void reduceSynchPacket() throws ClassNotFoundException, PowerSpaceException, IOException
	{
		Packet rp = null;
		//
//                System.out.println("CCP.reduceSynchPacket "+session+" numberToCollect "+String.valueOf(numberToCollect)+" "+String.valueOf(synchPackets.size()));
                if( --numberToCollect > 0 ) {
                   if( synchPackets.size() >= numberToCollect ) {
                                // Packet Packet[PKTransportMethodCall[resultPacket] ]
                                PKTransportMethodCall pktmc = (PKTransportMethodCall)((Packet)(synchPackets.elementAt(0))).getField(0).value();
                                Object[] opa = pktmc.getParamArray();
                                // check param array to see if we need to extract result Packet
                                // if it has something and its Packet, put it in results
                                if( opa.length > 0 && opa[0] instanceof Packet ) {
                                        rp = (Packet)(opa[0]);
                                        // add synched packets to result packet
                                        for(int i = 1; i < synchPackets.size() ; i++)
                                                rp.add((Serializable)(synchPackets.elementAt(i)));
                                }
				numberToCollect = 0;
				synchPackets.removeAllElements();
                                // invoke post-synch command
                                // we always have at least 1 on Vector
                                // representing original arg passed down
				try {
                                        queueSynchPacketUp((Packet)(PowerPlant.commandCluster(originLeg, this, pktmc)));
				} catch(Exception e) {
                                        System.out.println("reduceSynchPacket Exception "+e+" session "+session);
                                        rp = new Packet((Serializable)e); 
					queueSynchPacketUp(rp);
				}
		   }
		} else {
                   throw new PowerSpaceException("ReduceSynch numberToCollect not > 0. Result Packet:",rp); 
		}
	}
	/**
	* queue synch packet up to parent
	*/
	public void queueSynchPacketUp(Packet p) throws ClassNotFoundException, PowerSpaceException, IOException
	{
                queuePacket(new SystemPacket( 2, session, p));
	}

	/**
	* queue message packet
        * @arg pktmc PKTransportMethodCall to queue
	* @arg mrpf The MessageRoutingPacketField; to determine where to go
	*/
        public void queueMessagePacket(PKTransportMethodCall pktmc, MessageRoutingPacketField mrpf) throws ClassNotFoundException, PowerSpaceException, IOException
	{
		int toleg = mrpf.getPacketForwardDirection();
		// if we're there, queue it up normally
		if( toleg == -1 ) 
                        queuePacket(new SystemPacket( 3, session, pktmc, mrpf));
		else 
			// if not, send it to proper leg
                        ConnectionPanel.queuePacket(new SystemPacket( 3, session, pktmc, mrpf), toleg); 
	}

	/**
	* queue message packet for return
	* @arg p Packet to queue
	* @arg mrpf The MessageRoutingPacketField; to determine where to go
	*/
	public void queueMessagePacketReturn(Packet p, MessageRoutingPacketField mrpf) throws ClassNotFoundException, PowerSpaceException, IOException
	{
		// swap the source and destination address for return
		mrpf.swap();
		int toleg = mrpf.getPacketForwardDirection();
		// if we're there, queue it up normally
		if( toleg == -1 ) 
                        queuePacket(new SystemPacket( 4, session, p, mrpf));
		else 
			// if not, send it to proper leg
                        ConnectionPanel.queuePacket(new SystemPacket( 4, session, p, mrpf), toleg); 
	}

}
