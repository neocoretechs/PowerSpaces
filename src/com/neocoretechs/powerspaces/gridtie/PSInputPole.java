/**
* PowerSpaces Pole class.
* A pole is an abstraction that defines IO for SubstationTransmissionLine
* and customer connections.
* @author Groff (C) NeoCoreTechs 1998
*/
package com.neocoretechs.powerspaces.gridtie;
import java.util.*;
import java.net.*;
import java.io.*;

import com.neocoretechs.powerspaces.Packet;
import com.neocoretechs.powerspaces.PowerSpace;
//
public abstract class PSInputPole implements PSPoleInterface {
        //
        //
        public abstract Socket getSocket();
        public abstract void Connect();
        public abstract PowerSpace getPowerSpace();

        public void startFlow() {
                //
                try {
                  ObjectInputStream obin;
                  Packet p = null;
                  InputStream sin = getSocket().getInputStream();
                  for(;;) {
                        // read customer session header
                        System.out.println("PSInputPole.startFlow getting input stream");
//                        synchronized(this) {
                                obin = new ObjectInputStream(sin);
                                p = (Packet)(obin.readObject());
//                        }
                        System.out.println("PSInputPole.startFlow read "+p);
                        //
                 } // for
                } catch(Exception e) { System.out.println("! EXCEPTION LOGGED !"); System.err.println(e.getMessage()); e.printStackTrace(); }
       }
}
