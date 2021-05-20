package com.neocoretechs.powerspaces.gridtie;
import com.neocoretechs.powerspaces.Packet;
import com.neocoretechs.powerspaces.PowerSpace;
import com.neocoretechs.powerspaces.PowerSpaceException;
import com.neocoretechs.powerspaces.server.*;

import java.net.*;
import java.io.*;
import java.util.*;
/**
* this is the object that handles customer output connections
*/
public class PSOutputPoleConnection extends PSOutputPole {
        CustomerOutputTransmissionLine lrt;
        Socket So;

        public PSOutputPoleConnection(CustomerOutputTransmissionLine tlrt, Socket tso) {
                super();
                lrt = tlrt;
                So = tso;
        }
        public Socket getSocket() { return So; }
        public PowerSpace getPowerSpace() { return lrt.getPowerSpace(); }

        public void Connect() {
             try {
                OutputStream ios;
                ObjectOutputStream oos;
                InputStream iis;
                ObjectInputStream ois;
                ios = getSocket().getOutputStream();
                // open the output stream to input of flow node and vice versa
                oos = new ObjectOutputStream(ios);
                oos.writeObject(new Packet(getPowerSpace().toString()));
                oos.flush();
                System.out.println("PSOutputPoleConnection sent Customer Id "+getPowerSpace().toString());
                // see if OK
                iis = getSocket().getInputStream();
                ois = new ObjectInputStream(iis);
                Packet p = (Packet)(ois.readObject());
                if( !((String)(p.getField(0).value())).equals("OK") )
                        throw new PowerSpaceException("Connect fail");
                //
                startFlow();
                //
           } catch(Exception e) {
                System.out.println("! EXCEPTION LOGGED !");
                System.out.println(e.getMessage());
                e.printStackTrace();
           }
        }
        
}
