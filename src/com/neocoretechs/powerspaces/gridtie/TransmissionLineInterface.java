package com.neocoretechs.powerspaces.gridtie;

import com.neocoretechs.powerspaces.PowerSpace;

public interface TransmissionLineInterface {
    public PowerSpace getPowerSpace();
    /**
    * starts a PSOutputPoleConnection when socket connects
    * to remote server (PowerSpace)
    */
    public PSPoleInterface run(String host, int port);
}
