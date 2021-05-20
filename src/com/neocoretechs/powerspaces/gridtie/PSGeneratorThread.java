/**
* these objects represent threads that form the thread pool
* for work to be performed on queued customer requests
* @author Groff (C) NeoCoreTechs 1998
*/
package com.neocoretechs.powerspaces.gridtie;
import java.util.*;

public class PSGeneratorThread extends Thread {
        Vector v;
        boolean shouldRun = true;
        public PSGeneratorThread(Vector tv) {
                v = tv;
        }
        public void stopRun() { shouldRun = false; }
        public void run() {
                Object o;
                try {
                while(shouldRun) {
                        synchronized(v) {
                                while( v.size() == 0 ) v.wait();
                                o = v.elementAt(0);
                                v.removeElementAt(0);
                        }
                        ((PSPoleInterface)o).Connect();
                }
                } catch(Exception e) {
                        System.out.println("PSGeneratorThread Exception "+e.getMessage());
                        e.printStackTrace();
                }
      }
}
