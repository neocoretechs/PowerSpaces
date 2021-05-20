package com.neocoretechs.powerspaces.server.handler;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.*;
import com.neocoretechs.powerspaces.server.cluster.IPCLink;
import com.neocoretechs.powerspaces.server.cluster.PSInputOfPowerPlantConnection;
import com.neocoretechs.powerspaces.server.cluster.PSOutputOfPowerPlantConnection;
import com.neocoretechs.relatrix.server.*;

import java.util.*;
import java.io.*;

/**
* PSIPCHandler - Provides the handlers for various operations involving
* setting up inter-powerspace connection and communication.<p>
* The order of the legs (ports) on a PowerPlant (main port+below) is as follows:<br>
* main +2,+3   parent leg input/output ServerSockets <br>
* main +0,+1   main connect point for plant clients (currently unused)  <br>
*  <br>
* main +8,+9   right crossbar leg i/o ServerSockets <br>
* main +10,+11 left crossbar leg i/o Sockets <br>
*  <br>
* main +4,+5   left leg i/o Sockets <br>
* main +6,+7   right leg i/o Sockets<br>
* @author Groff Copyright (C) NeoCoreTechs 1998-2000,2014
*/
public class PSIPCHandler {
        //
        /**
        * Start a new PowerPlantConnection client
        * to the target parent TransmissionLine server
        */
        public static synchronized Object PowerKernel_Connect( Integer leg, CustomerConnectionPanel ccp, Integer thruLeg, Integer tport, String tihost, String tohost) throws PowerSpaceException, FinishedException {
                IPCLink lrIPC;
                PSInputOfPowerPlantConnection ioppc;
                PSOutputOfPowerPlantConnection ooppc;
                try {
                        int port = tport.intValue();
                        // connect to the input port of remote node
                        String ihost =  tihost;
                        // connect to the output port of remote node
                        String ohost =  tohost;
                        switch(thruLeg.intValue()) {
                                // connect output of left to input of child
                                case 1:
                                        ooppc = PowerPlant.LeftOutputTransmissionLine.run(ihost, port+2);
                                        ioppc = PowerPlant.LeftInputTransmissionLine.run(ohost, port+3);
                                        break;
                                case 2:
                                        // connect right output to input of child
                                        ooppc = PowerPlant.RightOutputTransmissionLine.run(ihost, port+2);
                                        ioppc = PowerPlant.RightInputTransmissionLine.run(ohost, port+3);
                                        break;
                                case 3:
                                        // connect left to the right crossbar of left node
                                        ooppc = PowerPlant.LeftCrossbarOutputTransmissionLine.run(ihost, port+10);
                                        ioppc = PowerPlant.LeftCrossbarInputTransmissionLine.run(ohost, port+11);
                                        break;
                                default:
                                        throw new PowerSpaceException("Connect: Bad through leg specified");
                        }
                        // a whole powerplant connection is pending
                        System.out.println("IPCHandler connection to "+ohost+":"+String.valueOf(port)+" through leg "+thruLeg);
                        return "Connect pending";
                } catch(Exception e) {
                        System.out.println("Connect exception "+e);
                        return e;
                }
        }


	/**
	* The ubiquitous 'ping', all nodes will take their timing and send it back up the tree, this is a node op
	*/
    public static Object PowerKernel_Ping( Integer leg, CustomerConnectionPanel ccp, Packet p) throws PowerSpaceException, FinishedException 
	{
		Long tim = new Long(System.currentTimeMillis());
        //System.out.println("Ping time at "+String.valueOf(PowerPlant.clusterID)+" "+tim.toString());
		// box up args to return all synched packets from attached nodes
        //return tim;
        p.add(tim);
        return p;
    }

	/**
        * In conjunction with a broadcast, determine lowest numbered node with an eligible connect point.<br>
        * The command is broadcast to all nodes and the results are percolated up. <br>
        * During broadcast, this command is sent to each node and it waits
        * (via synch queue) till nodes below return, at that point the args
        * from all nodes below are passed in the 'args' Packet here.<br>
        * The number of fields in args determines how many nodes below.
        * Only really look at the ones with none below
	* (if we have more than 1 field in arg...)
	*/
        public static synchronized Object PowerKernel_FindConnectPoint( Integer leg, CustomerConnectionPanel ccp, Packet args) throws PowerSpaceException, FinishedException {
        	// compare the other nodes to see if eligible
        	// if it's 1 arg (just original passed arg) then no nodes below. 
        	// if it's 2 then 1 node below, if 3 then we do the full compare and send up lowest
            switch( args.numberOfFields() ) {
				case 0:
					// none coming up, send this ID
				case 1:
					// only 1 coming up, must be less eligible than this since its connected lower
					return new Long(PowerPlant.clusterID);
					// 2 below, send the lowest
                case 2:
                    Packet p0 = (Packet)(args.getField(0).value());
                    Packet p1 = (Packet)(args.getField(1).value());
                    long cid1 = ( (Long)(p0.getField(0).value()) ).longValue();
                    long cid2 = ( (Long)(p1.getField(0).value()) ).longValue();
                    if( cid1 < cid2 ) 
                    	return new Long(cid1);
                    return new Long(cid2);
                default:
                    return "TOO MANY ARGS TO FindConnectPoint:"+args.numberOfFields();
		}
	}

	/**
	* Connect a new plant at proper point
	*/
        public static synchronized Object PowerKernel_ConnectNewPowerPlant( Integer leg, CustomerConnectionPanel ccp, Integer tport, String tihost, String tohost ) throws PowerSpaceException, FinishedException {
                if( PowerPlant.powerPlantLegConnections[1].isLinked() ) {
                        if( PowerPlant.powerPlantLegConnections[2].isLinked() ) return "connect fail: PowerPlant already has 2 connections"; 
                        return PowerKernel_Connect(leg, ccp, new Integer(2), tport, tihost, tohost);
                } 
                return PowerKernel_Connect(leg, ccp, new Integer(1), tport, tihost, tohost);
        }

        /**
        * Collect - this is a no-op command that causes a wait for a synch packet
        * to appear on an outbound queue
        */
        public static void PowerKernel_Collect( Integer leg, CustomerConnectionPanel ccp) throws PowerSpaceException, FinishedException {}

        /**
        * Install classes from byte array to the HandlerClassLoader
        * @see HandlerClassLoader
        */
        public static synchronized Object PowerKernel_InstallHandler( Integer leg, CustomerConnectionPanel ccp, byte[] handlerClasses ) throws Exception {
                PowerPlant.theClassLoader = new com.neocoretechs.relatrix.server.HandlerClassLoader();
                System.gc();
                PowerPlant.theClassLoader.defineClasses(handlerClasses);
                // re-gen elements of PowerPlant.handlerClasses
                String hName = null;
                for( Enumeration eh =  PowerPlant.handlerClasses.keys();
                     eh.hasMoreElements(); hName = (String)eh.nextElement()) {
                        PKServerInvokeMethod pksim = new PKServerInvokeMethod(hName, true);
                        PowerPlant.handlerClasses.put(hName, pksim);
                }
                return "OK";
        }
        /**
        * Install a class from byte array to the HandlerClassLoader
        * @see HandlerClassLoader
        */
        public static synchronized Object PowerKernel_InstallHandler( Integer leg, CustomerConnectionPanel ccp, String className, byte[] handlerClass ) throws Exception {
                // dump the ref in PowerPlant HandlerClasses for PKServerInvokeMethod
                PowerPlant.theClassLoader = new HandlerClassLoader();
                System.gc();
                PowerPlant.theClassLoader.defineAClass(className, handlerClass);
                PKServerInvokeMethod pksim = new PKServerInvokeMethod(className, true);
                PowerPlant.handlerClasses.put(className, pksim);
                return "OK";
        }
}

