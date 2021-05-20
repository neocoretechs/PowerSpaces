/**
* PowerSpaces Pole class.
* A pole is an abstraction that defines IO for SubstationTransmissionLine
* and Customer connections.
* @author Groff (C) NeoCoreTechs 1998
*/
package com.neocoretechs.powerspaces.gridtie;
import java.util.*;
import java.net.*;
import java.io.*;

import com.neocoretechs.powerspaces.Packet;
import com.neocoretechs.powerspaces.PowerSpace;
//
public abstract class PSOutputPole implements PSPoleInterface {
        //
        OutputStream os;

        public PSOutputPole() {
                try {
                  pis = new PipedInputStream();
                  pos = new PipedOutputStream(pis);
                } catch(IOException ioe) {
                        System.out.println(ioe.getMessage());
                        ioe.printStackTrace();
                }
        }

        public abstract Socket getSocket();
        public abstract PowerSpace getPowerSpace();
        public abstract void Connect();

        private PipedInputStream pis;
        private PipedOutputStream pos;

        // write to this to send over this line
        public OutputStream getOutputStream()
        {
                return pos;
        }

        public void startFlow() {
                  //
                try {
                  Packet p, pc;
                  int rbyte, numavail;
                  os = getSocket().getOutputStream();
                  // loop to keep getting piped input stream and write it
                  //
                  for(;;) {
                  while( (rbyte = pis.read()) != -1) {
//                        synchronized(this) {
                                numavail = pis.available();
                                do {
                                        os.write(rbyte);
                                        if( numavail-- == 0 ) break;
                                } while( (rbyte = pis.read()) != -1);
//                        }
                  }
                  }
                } catch(Exception e) { System.out.println("! EXCEPTION LOGGED !"); System.err.println(e.getMessage()); e.printStackTrace(); }
       }
}
