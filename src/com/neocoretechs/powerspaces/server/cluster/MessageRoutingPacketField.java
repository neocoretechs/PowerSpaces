package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PowerPlant;

import java.io.Serializable;
/**
* This class is used to route a message from a source PowerPlant to a destination and back.
* It exists as a field in a Packet and the control byte at the beginning of session Packet field
* indicates its use.  A comparison is made to whatever clusterID the message is on and a proper
* routing of the message to the next node is performed.
* @author Groff (C) NeoCoreTechs 1999
*/
public class MessageRoutingPacketField implements Serializable {
	private long source;
	private long destination;
	public MessageRoutingPacketField(long tsource, long tdestination) {
		source = tsource;
		destination = tdestination;
	}

	public void swap() {
		long tl = destination;
		destination = source;
		source = tl;
	}

	/**
	* @return The direction to xfer the packet or -1 if we're there.  0 - Parent, 1 - Left, 2 - right
	*/
	public int getPacketForwardDirection() {
		// are we there
		if( destination == PowerPlant.clusterID) return -1;
		long id1 = PowerPlant.clusterID;
		long id2 = destination;
		// save the bit we are shifting away
		long id2bit;
		// check for permutations of sign bit set
		// when we have, god forbid, over 9223 trillion nodes.
		// after that we check current > destination
		if( id1 < 0  && id2 > 0 || id1 > id2 ) return 0;
		// destination is greater.. start shifting
		// shift the destination right, if it becomes = we use
		// the bit shifted away.
		while( id2 != 1L ) {
			id2bit = id2 & 1L;
			id2 >>>= 1;
			if( id1 == id2 ) return ((int)id2bit)+1;
		}
		return 0;
	}
}
