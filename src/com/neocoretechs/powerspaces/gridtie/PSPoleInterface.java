package com.neocoretechs.powerspaces.gridtie;
import java.net.Socket;

import com.neocoretechs.powerspaces.PowerSpace;
/**
* PSPoleInterface for customer types to use transmission lines
* @author Groff (C) NeoCoreTechs 1998
*/
public interface PSPoleInterface {
        public Socket getSocket();
        public PowerSpace getPowerSpace();
        public void Connect();
}
