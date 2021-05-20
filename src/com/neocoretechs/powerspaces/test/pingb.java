package com.neocoretechs.powerspaces.test;import com.neocoretechs.powerspaces.*;
import java.io.*;
import java.util.*;/** * Cluster balanced ping operation * @author jg * */
public class pingb {
        public static void main(String[] argv) throws Exception {
            PowerSpace PS = new PowerSpace(argv[0]);
            PKRemote pkp = PS.getRemote("com.neocoretechs.powerspaces.server.PKParallel");
			for(int i= 0 ; i < 100000 ; i++) {
                long st = System.currentTimeMillis();
                Packet ppp = (Packet)(pkp.invoke("Balance",
                	new Packet("com.neocoretechs.powerspaces.server.handler.PSIPCHandler","Ping", new Packet())));
                System.out.println("Cluster balance response time "+String.valueOf(System.currentTimeMillis()-st)+" ms.  Count: " + i);
                System.out.println(ppp);
			}
			PS.Unplug();
        }
}
