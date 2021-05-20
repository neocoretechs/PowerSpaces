package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.cluster.ParentInputTransmissionLine;
import com.neocoretechs.powerspaces.server.cluster.LRInputTransmissionLine;
import com.neocoretechs.powerspaces.server.cluster.LROutputTransmissionLine;
import com.neocoretechs.powerspaces.server.cluster.ParentOutputTransmissionLine;
import com.neocoretechs.powerspaces.server.cluster.PSTurbineThread;
import com.neocoretechs.powerspaces.server.handler.*;
import com.neocoretechs.relatrix.server.HandlerClassLoader;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
/**
* A PowerPlant is the top level of a PowerSpaces server. <p>
* The following defines the objects constructed with a PowerPlant: <p>
* 
* <b>Generator:</b> a thread(s) that processes incoming serialized objects (packets)
* from anther PowerPlant.  The packet is either processed (a method is invoked)
* and a return packet is queued back to the origin with result, or the packet
* is routed to another powerplant destination.  Multiple pooled generators are started
* which grab elements from a vector (a packet queue) and process them.<p>
* Note: Another PowerPlant has to connect you into the grid.
* <p>
* <b>Legs:</b> these are combinations of Sockets and ServerSockets in pairs.  One for
* inbound and one for outbound traffic in each pair.<br>
* <li> Parent leg: ServerSockets connected to by parent plant left and right socket legs<br>
* <li> Left and right crossbar legs: provides cross-connect between nodes, Left is
* ServerSocket connected to and right is Socket doing the connection to its left<br>
* <li> Left and Right connection legs: connect down to a lower parent leg<br>
* <p>
* <b>Transmission line:</b> handles the IO to/from the other powerplants (or other clients)
* and places the packets on the packet queue vector mentioned above.  Transmission
* lines use the Transformer thread pool to get work done.
* <p>
* <b>Transformers:</b> pooled worker threads that are used within transmission lines.
* their main job is to take elements from the vector of objects implementing the
* PSCustomer interface and call the "Connect" method on them when a powerplant
* connects another into the grid.
* <p>
* <b>Customers:</b> queued in the Customer Vector and passed to the
* "worker" Transformer thread pool.
* <p>
* <b>Towers:</b> a sort of proxy customer between PowerPlants that facilitates
* connection.
* <p>
* <b>ConnectionPanel:</b> the object holding the connections of powerplants and
* methods to queue packets to other powerplants.  Also maintains
* CustomerConnectionPanels.
* <p>
* <b>CustomerConnectionPanel:</b> represents a session, its properties, and
* its network IO connections.  These are created when a client enters the
* PowerSpace or when a parallel operation visits a new node.
* <p>
* <b>Station Handlers:</b> Server side code where the actual work gets done.  Uses reflection
* to determine methods starting with PowerKernel_ that may be invoked.
* Each method is passed the PowerPlant, leg, and CustomerConnectionPanel along with
* whatever parameters the developer specifies as the last arguments.  The 'leg'
* is the ordinal of the other powerplant or customer from which the invocation originated.
* <p>
* Connections are made from parent through left or right tansmission lines to
* waiting ServerSockets on input and out transmission lines of children; these are
* 'parent' port on child.  Crossbar connections are used for redundancy and are made
* from right node left crossbar to left node right crossbar.  The theory is that a parent must be a member of the cluster
* and hands down an ID to child.  A node must be connected in to be crossbarred
* and hands over its cluster ID to connector, hence the right to left connection, since
* the tree is built left node, right node.
*
* @author Groff (C) NeoCoreTechs, Inc. 1998-2000,2014
*/
public final class PowerPlant {
        // the main station from which others can be booted
        private static PKServerInvokeMethod mainStation = null;
        private static String mainStationName = "com.neocoretechs.powerspaces.server.handler.PKRemoteClass";
        // the cluster enabling station
        private static PKServerInvokeMethod clusterStation = null;
        private static String clusterStationName = "com.neocoretechs.powerspaces.server.handler.PSIPCHandler";
        // the other PowerPlants, these are the ServerSockets to be connected to
        public static int PowerPlantPort = 8202;
        // parsed from PowerSpaces.ini
        private static String args[] = new String[11];
        //
        // here we have each leg encapsulated with ports, input queues, and towers
        // we use 5 legs for now
        public static int numberLegs = 5;
        public static PowerPlantLegConnections[] powerPlantLegConnections = new PowerPlantLegConnections[numberLegs];
        //
        // number of generator threads
        public static int numberGenerators = 3;
        //
        // number of "kicker" threads for user asynchronous commands
        public static int numberKickers = 3;
        public static Vector kickerVector = new Vector();
	//
        public static LRInputTransmissionLine LeftInputTransmissionLine;
        public static LRInputTransmissionLine RightInputTransmissionLine;
        public static LRInputTransmissionLine LeftCrossbarInputTransmissionLine;
        public static LROutputTransmissionLine LeftOutputTransmissionLine;
        public static LROutputTransmissionLine RightOutputTransmissionLine;
        public static LROutputTransmissionLine LeftCrossbarOutputTransmissionLine;
        //
        public static long clusterID = 1L; // 1 starts sentinel bit
		//
        public static HandlerClassLoader theClassLoader = new HandlerClassLoader();

        public static PowerPlant getPowerPlant() { return instance; }
        public static CustomerConnectionPanel SystemCCP = null;

        public static Hashtable handlerClasses = new Hashtable();

        // Singleton setups:
        // 1.) privatized constructor; no other class can call
        private PowerPlant() { main(); }
        // 2.) create only instance, save it to private static
        private static PowerPlant instance = new PowerPlant();
        // 3.) make the instance available
        public static PowerPlant getInstance() { return instance; }

        /**
        * The ports are for the servers that other PowerPlants use to
        * connect to this.  A left and right port define the ways we can be
        * connected TO.  We can connect through parent to another left or right server
        * <dd>
        * TCPServer (from which transmission lines are derived), clones itself and runs
        * with socket on each new connect.  So the ref to the PowerPlant gets you a ref
        * to the customer even though the Transformer thread pool is yanking
        * elements off the synch. Vector willy nilly and executing customers
        */
        public static void main() {
                String tstring;
                String sopts[] = {"MainPort:",
                                  "ParentInBind:","ParentOutBind:",
                                  "LeftInBind:","LeftOutBind:",
                                  "RightInBind:","RightOutBind:",
                                  "LeftCrossbarInBind:","LeftCrossbarOutBind:",
                                  "RightCrossbarInBind:", "RightCrossbarOutBind:"};
                //
                BufferedReader fis = null; 
                //turn debug/log on or off
                try {
                        System.setOut(new PrintStream(new FileOutputStream("/jserv/dout")));
                } catch(FileNotFoundException fnfe) {}
                //
                // set up each leg of this PowerPlant
                //
                for(int i=0; i < numberLegs; i++) powerPlantLegConnections[i] = new PowerPlantLegConnections();
                // leg 0 on main port +2,+3
                powerPlantLegConnections[0].setInputPort(PowerPlantPort + 2);
                powerPlantLegConnections[0].setOutputPort(PowerPlantPort+ 3);
                // right crossbar on main +10,+11
                powerPlantLegConnections[4].setInputPort(PowerPlantPort + 10);
                powerPlantLegConnections[4].setOutputPort(PowerPlantPort+ 11);
                //
                try {
                        // read props file
                        //
                        fis = new BufferedReader(new FileReader(File.separator+"PowerSpaces.ini"));
                        while(true) {
                                tstring = fis.readLine();
                                if( tstring == null ) break;
                                System.out.println(tstring);
                                for(int i=0; i < sopts.length; i++) {
                                        if( sopts[i].startsWith(tstring.substring(0, tstring.indexOf(":"))) ) {
                                                args[i] = tstring.substring(tstring.indexOf(":") + 1);
                                                System.out.println(args[i]);
                                        }
                                }
                        }
                }
                catch(FileNotFoundException fnfe) {
                        System.out.println("Parameter file not found");
                }
                catch(IOException ioe) {
//                        System.out.println("Parameter file is incorrect");
                        // signals end of read
                        try {
                                if( fis != null ) fis.close();
                        } catch(IOException ioe2) {} // we can do no more
                }

                // see if we overrode power plant port default
                try {
                        if( args[0] != null && args[0].length() > 1 )
                                PowerPlantPort = Integer.parseInt(args[0]);
                } catch(NumberFormatException nfe) {
                        System.out.println("Bad power plant main port number format");
                }
                powerPlantLegConnections[0].setInputPort(PowerPlantPort + 2);
                powerPlantLegConnections[0].setOutputPort(PowerPlantPort + 3);
                //System.out.println("PowerPlant Ports: "+String.valueOf(PowerPlantPort)+", "+String.valueOf(OutputPowerPlantPort));
                //
		try {
                        //
                        // generators for each tower
                        for( int igen = 0 ; igen < numberGenerators ; igen++ ) {
                                for(int ileg = 0; ileg < numberLegs; ileg++ ) {
                                        new PSTurbineThread(powerPlantLegConnections[ileg].getInputPacketQueue(), ileg).start();
                                }
                        }

                        // set up some kickers for asynch user commands

                        for( int igen = 0 ; igen < numberKickers ; igen++ )
                                new PSTransformerThread(kickerVector).start();

                        // finally configure the main station

                        configureMainStation();

                        System.out.println("PowerPlant started..");

			//
                        // start the transmission lines
                        //
                        for(int ileg = 0; ileg < numberLegs; ileg++ ) {
                                        startInputLeg(ileg);
                                        startOutputLeg(ileg);
                        }

                        //
                        // Start our socket server on the main port
                        //
                        SocketTransmissionLine stl = new SocketTransmissionLine(getInstance());
                        stl.startServer(PowerPlantPort);

                        System.out.println("Transmission lines up...");
                        
		} catch(Exception e) {
			System.out.println(e);
		}
	}

        /**
        * Start a right crossbar and parent node server. <br>
        * Ancestors and children will connect here via one of these. <br>
        * Crossbar right node connects to crossbar left node.  <br>
        * Crossbar right this node is a server socket...
        *
        */
        public static void startInputLeg(int leg) throws Exception {
                switch(leg) {
                        case 0:
                                ParentInputTransmissionLine ITL = new ParentInputTransmissionLine(powerPlantLegConnections[0].getInputPacketQueue(), 0);
                                powerPlantLegConnections[0].setLocalBindIn(args[1]);
                                if( args[1] != null && args[1].length() > 1 )
                                        ITL.startServer(powerPlantLegConnections[0].getInputPort(), InetAddress.getByName(args[1]));
                                else
                                        ITL.startServer(powerPlantLegConnections[0].getInputPort());

                                break;
                        //
                        // start server sockets for right crossbar
                        //
                        case 4:
                                ParentInputTransmissionLine RCITL = new ParentInputTransmissionLine(powerPlantLegConnections[4].getInputPacketQueue(), 4);
                                powerPlantLegConnections[4].setLocalBindIn(args[9]);
                                if( args[9] != null && args[9].length() > 1 )
                                        RCITL.startServer(powerPlantLegConnections[4].getInputPort(), InetAddress.getByName(args[9]));
                                else
                                        RCITL.startServer(powerPlantLegConnections[4].getInputPort());

                                break;
                        // prime the connections out
                        case 1:
                                LeftInputTransmissionLine = new LRInputTransmissionLine(powerPlantLegConnections[1].getInputPacketQueue(), 1);
                                powerPlantLegConnections[1].setLocalBindIn(args[3]);
                                break;
                        case 2:
                                RightInputTransmissionLine = new LRInputTransmissionLine(powerPlantLegConnections[2].getInputPacketQueue(), 2);
                                powerPlantLegConnections[2].setLocalBindIn(args[5]);
                                break;
                        case 3:
                                LeftCrossbarInputTransmissionLine = new LRInputTransmissionLine(powerPlantLegConnections[3].getInputPacketQueue(), 3);
                                powerPlantLegConnections[3].setLocalBindIn(args[7]);
                }
        }
        /**
        * Start a right crossbar and parent node server. <br>
        * Ancestors and children will connect here via one of these. <br>
        * Crossbar right node connects to crossbar left node.  <br>
        * Crossbar right this node is a server socket...
        *
        */
        public static void startOutputLeg(int leg) throws Exception {
                switch(leg) {
                        case 0:

                                ParentOutputTransmissionLine OTL = new ParentOutputTransmissionLine(0);
                                powerPlantLegConnections[0].setLocalBindOut(args[2]);

                                if( args[2] != null && args[2].length() > 1 )
                                        OTL.startServer(powerPlantLegConnections[0].getOutputPort(), InetAddress.getByName(args[2]));
                                else
                                        OTL.startServer(powerPlantLegConnections[0].getOutputPort());
                                break;
                        //
                        // start server sockets for right crossbar
                        //
                        case 4:
                                ParentOutputTransmissionLine RCOTL = new ParentOutputTransmissionLine(4);
                                powerPlantLegConnections[4].setLocalBindOut(args[10]);
                                if( args[10] != null && args[10].length() > 1 )
                                        RCOTL.startServer(powerPlantLegConnections[4].getOutputPort(), InetAddress.getByName(args[10]));
                                else
                                        RCOTL.startServer(powerPlantLegConnections[4].getOutputPort());
                                break;
                        // prime the connections out
                        case 1:
                                LeftOutputTransmissionLine = new LROutputTransmissionLine(1);
                                powerPlantLegConnections[1].setLocalBindOut(args[4]);
                                break;
                        case 2:
                                RightOutputTransmissionLine = new LROutputTransmissionLine(2);
                                powerPlantLegConnections[2].setLocalBindOut(args[6]);
                                break;
                        case 3:
                                LeftCrossbarOutputTransmissionLine = new LROutputTransmissionLine(3);
                                powerPlantLegConnections[3].setLocalBindOut(args[8]);
                }
        }
        /**
        * Command-line compatible main, static singleton constructor really does the work
        */
        public static void main(String[] args) {}

        /**
        * Bootstrap the getter of remote classes into the table of remote classes
        * so it can be got so it can do the getting
        */
        public static void configureMainStation() throws Exception {
                  if( mainStation == null ) {
			// make a customer panel for the system
                        SystemCCP = ConnectionPanel.makeCustomerPanel("SYSTEM", -1, true);
                        mainStation = new PKServerInvokeMethod(mainStationName , true);
                        handlerClasses.put(mainStationName, mainStation);
                        clusterStation = new PKServerInvokeMethod(clusterStationName , true);
                        handlerClasses.put(clusterStationName, clusterStation);
                  }
        }

	/**
	* Issue a command to this PowerPlant via system customer.
	* @arg cmd command to issue.
	* @arg p The Packet with the payload.
	* @return The result Packet of command.
	* @exception FinishedException if command signals a finish state.
	* @exception PowerSpaceException if return packet cannot be constructed. (We trap for it in routine but may not be able to return a Packet).
	*/
        public static Object command(String sclass, String smeth, Object[] p) throws Exception
                //FinishedException, PowerSpaceException, ClassNotFoundException,
                //NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException
	{
                System.out.println("Attempt SYSTEM invoke class "+sclass+" method "+smeth);
                PKServerInvokeMethod pksim = (PKServerInvokeMethod)(handlerClasses.get(sclass));
                if( pksim == null )
                        throw new ClassNotFoundException("No such class loaded: "+sclass);
                PKTransportMethodCall pktrans = new PKTransportMethodCall("SYSTEM", sclass, "null", smeth, p);
                return pksim.invokeMethod(new Integer(-1), SystemCCP, pktrans);
        }

	/**
	* Issue a command to this PowerPlant via customer.
	* @arg leg The origin leg of command Packet.
	* @arg ccp The customer connection panel of issuer.
	* @arg cmd command to issue.
	* @arg p The Packet with the payload.
	* @return The result Packet of command.
	* @exception FinishedException if command signals a finish state.
	* @exception PowerSpaceException if return packet cannot be constructed. (We trap for it in routine but may not be able to return a Packet).
	*/
        public static Object command(int leg, CustomerConnectionPanel ccp, String sclass, String smeth, Object[] p) throws Exception
                //FinishedException, ClassNotFoundException,
                //NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException
	{
                System.out.println("Attempt Customer invoke class "+sclass+" method "+smeth);
                PKServerInvokeMethod pksim = (PKServerInvokeMethod)(handlerClasses.get(sclass));
                if( pksim == null )
                        throw new ClassNotFoundException("No such class loaded: "+sclass);
                PKTransportMethodCall pktrans = new PKTransportMethodCall("SYSTEM", sclass, "null", smeth, p);
                return pksim.invokeMethod(new Integer(leg), ccp, pktrans);
        }
	/**
	* Issue a command to this PowerPlant via customer.
	* @arg leg The origin leg of command Packet.
	* @arg ccp The customer connection panel of issuer.
        * @arg pktrans The PKTransportMethodCall object from remote.
	* @arg p The Packet with the payload.
	* @return The result Packet of command.
	* @exception FinishedException if command signals a finish state.
	* @exception PowerSpaceException if return packet cannot be constructed. (We trap for it in routine but may not be able to return a Packet).
	*/
        public static Object command(int leg, CustomerConnectionPanel ccp, PKTransportMethodCall pktrans) throws Exception
                //throws FinishedException, ClassNotFoundException,
                //NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException
	{
//                System.out.println("Attempt transport invoke "+pktrans);
                PKServerInvokeMethod pksim = (PKServerInvokeMethod)(handlerClasses.get(pktrans.getClassName()));
                if( pksim == null ) {
                        // load the class
                        pksim = new PKServerInvokeMethod( pktrans.getClassName(), true);
                        handlerClasses.put(pktrans.getClassName(), pksim);
                }
                return pksim.invokeMethod(new Integer(leg), ccp, pktrans);
        }
	/**
        * Issue a command to this PowerPlant via remote customer who just arrived.
	* @arg leg The origin leg of command Packet.
	* @arg ccp The customer connection panel of issuer.
        * @arg pktrans The PKTransportMethodCall object from remote.
	* @arg p The Packet with the payload.
	* @return The result Packet of command.
	* @exception FinishedException if command signals a finish state.
	* @exception PowerSpaceException if return packet cannot be constructed. (We trap for it in routine but may not be able to return a Packet).
	*/
        public static Packet commandCluster(int leg, CustomerConnectionPanel ccp, PKTransportMethodCall pktmc) throws Exception
               //throws FinishedException, ClassNotFoundException,
               //NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException
	{
                return commandCluster(new Integer(leg), ccp, pktmc);
        }

	/**
        * Issue a command to this PowerPlant via remote customer who just arrived.
	* @arg leg The origin leg of command Packet.
	* @arg ccp The customer connection panel of issuer.
        * @arg pktmc The PKTransportMethodCall object from remote.
	* @return The result Packet of command.
	* @exception FinishedException if command signals a finish state.
	* @exception PowerSpaceException if return packet cannot be constructed. (We trap for it in routine but may not be able to return a Packet).
	*/
        public static Packet commandCluster(Integer leg, CustomerConnectionPanel ccp, PKTransportMethodCall pktmc) throws Exception
                //throws FinishedException, ClassNotFoundException,
                //NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException
	{
                System.out.println("commandCluster "+pktmc);
                PKServerInvokeMethod pksim = (PKServerInvokeMethod)(handlerClasses.get(pktmc.getClassName()));
                if( pksim == null ) {
                        // load the class for this node, remote node is requesting use
                        pksim = new PKServerInvokeMethod( pktmc.getClassName(), true);
                        handlerClasses.put(pktmc.getClassName(), pksim);
                }
                // invoke, if not a packet return, wrap it as such
                Serializable s = (Serializable)(pksim.invokeMethod(leg, ccp, pktmc));
                if( s == null ) return null;
                if( ! (s instanceof Packet) )
                                return new Packet(s);
                return (Packet)s;        
        }
}
