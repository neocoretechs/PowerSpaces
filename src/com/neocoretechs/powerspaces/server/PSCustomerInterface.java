package com.neocoretechs.powerspaces.server;
import java.net.Socket;
/**
* PSCustomerInterface for customer types such as
* PSPowerPlantCustomer where the main function here is to
* provide a "Connect" method that can be invoked when a PSTransformerThread
* grabs an instance of this off of a work Vector.
* @author Groff Copyright (C) NeoCoreTechs 1998-2000
*/
public interface PSCustomerInterface {
        public void Connect();
}
