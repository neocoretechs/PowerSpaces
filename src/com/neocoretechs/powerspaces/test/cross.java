package com.neocoretechs.powerspaces.test;
import com.neocoretechs.powerspaces.*;
/**
 * Send connect right crossbar to available cluster nodes
 * @author jg
 *
 */
public class cross {
        public static void main(String[] argv) throws Exception {
            PowerSpace PS = new PowerSpace(argv[0]);
            PKRemote pkr = PS.getRemote("com.neocoretechs.powerspaces.server.handler.PSIPCHandler");
            PKRemote pkp = PS.getRemote("com.neocoretechs.powerspaces.server.PKParallel");
            long st = System.currentTimeMillis();
            System.out.println("Start time "+String.valueOf(st));
            Object ppp = pkr.invoke("ConnectToRightCrossbar", new Integer(8202) , argv[1], argv[2]);
            System.out.println("Cluster synch time "+String.valueOf(System.currentTimeMillis()-st)+" ms");
			System.out.println(ppp);
        }
}
