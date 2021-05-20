package com.neocoretechs.powerspaces.server.cluster.synch;

import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;

public interface PSSynchInterface {
    public void SynchIn(PSAbstractInputTower tin) throws Exception;
    public void SynchOut(PSAbstractOutputTower tout) throws Exception;
}
