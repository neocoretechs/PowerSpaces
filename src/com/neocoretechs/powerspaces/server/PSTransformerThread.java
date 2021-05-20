package com.neocoretechs.powerspaces.server;
import java.util.*;

import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.servlet.ServletTransmissionLine;
/**
* These objects represent threads that form the thread pool
* for work to be performed on queued customer and connection requests. <p>
* These differ from generator threads in that these only grab the
* top element of Vector passed upon construction and call the
* Connect method (implemented from PSCustomerInterface interface).<p>
* So, these are used for customers to connect from transmission lines, and
* outgoing connections.  The power plant customer queue and the power plant connection queue
* are two examples of queues serviced by this.
* @see ServletTransmissionLine
* @author Groff Copyright (C) NeoCoreTechs 1998-2000
*/
public class PSTransformerThread extends Thread {
        Vector v;
        boolean shouldRun = true;
        public PSTransformerThread(Vector tv) {
                v = tv;
        }
        public void stopRun() { shouldRun = false; }

        /**
        * Endlessly grab elements from Vector and call Connect method
        * of PSCustomerInterface-derived objects thereon.
        */
        public void run() {
                Object o;
                try {
                while(shouldRun) {
                        synchronized(v) {
                                while( v.size() == 0 ) v.wait();
                                o = v.elementAt(0);
                                v.removeElementAt(0);
                        }
                        ((PSCustomerInterface)o).Connect();
                }
                } catch(Exception e) {
                        System.out.println("PSTransformerThread Exception "+e.getMessage());
                        e.printStackTrace();
                }
      }
}
