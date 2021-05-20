package com.neocoretechs.powerspaces.test;import com.neocoretechs.powerspaces.*;
import java.io.*;
import java.util.*;/** * General broadcast cluster node ping * @author jg * */
public class ping {
        public static void main(String[] argv) throws Exception {
 
                        PowerSpace PS = new PowerSpace(argv[0]);
                        PKRemote pkr = PS.getRemote("com.neocoretechs.powerspaces.server.handler.PSIPCHandler");
                        PKRemote pkp = PS.getRemote("com.neocoretechs.powerspaces.server.PKParallel");
                        //
                        for(int i= 0 ; i < 100000 ; i++) {
                        	long st = System.currentTimeMillis();
                        	System.out.println("Start time "+String.valueOf(st));
                        	//Object o = pkr.invoke("Ping");
                        	Packet ppp = (Packet)(pkp.invoke("BroadcastAll",
                                new Packet("com.neocoretechs.powerspaces.server.handler.PSIPCHandler","Ping", new Packet())));
                        	// Packet ppp = (Packet)(PS.collect());
                        	System.out.println("Cluster synch time "+String.valueOf(System.currentTimeMillis()-st)+" ms.  Count: " + i);
                        	System.out.println(ppp);
                        }
                        PS.Unplug();
        }
}
