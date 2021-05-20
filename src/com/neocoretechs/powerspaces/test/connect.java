package com.neocoretechs.powerspaces.test;import com.neocoretechs.powerspaces.*;
import java.io.*;
import java.util.*;
public class connect {
        public static void main(String[] argv) {
                try  {
                        PowerSpace PS = new PowerSpace(argv[0]);
                        PKRemote pkr = PS.getRemote("com.neocoretechs.powerspaces.server.handler.PSIPCHandler");
                        PKRemote pkp = PS.getRemote("com.neocoretechs.powerspaces.server.PKParallel");
                        long st = System.currentTimeMillis();                       
                        System.out.println("Start time "+String.valueOf(st));
                        Packet ppp = (Packet)(pkp.invoke("BroadcastAll",
                                new Packet("com.neocoretechs.powerspaces.server.handler.PSIPCHandler",
                                "FindConnectPoint", new Packet())) );
                        System.out.println(ppp);
                        Packet pp = (Packet)(ppp.getField(0).value());
                        Long tclusterID = (Long)(pp.getField(0).value());
                        ppp = (Packet)(pkp.invoke("SendMessage",
                                new Packet(tclusterID, "com.neocoretechs.powerspaces.server.handler.PSIPCHandler",                                		"ConnectNewPowerPlant",
                                		new Packet(new Integer(8202),argv[1], argv[2]))) );
                        //Object ppp = pkr.invoke("Connect", Integer.valueOf(argv[1]), new Integer(8202) , argv[2], argv[3]);
                       System.out.println("Cluster synch time "+String.valueOf(System.currentTimeMillis()-st)+" ms");
                       System.out.println(ppp);
                       //	Thread.sleep(10000);
                       //
                } catch(Exception e) { System.out.println(e); e.printStackTrace(); }
        }
}
