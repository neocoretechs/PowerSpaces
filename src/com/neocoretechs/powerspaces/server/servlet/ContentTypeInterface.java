package com.neocoretechs.powerspaces.server.servlet;

/**
* Interface to return content type and bytes from a handler method to
* ContenServletTransmissionLine
* Implementors of this are specific content types.
* @author Groff Copyright (C) NeoCoreTechs 2001
*/
public interface ContentTypeInterface {
        public String getContentType();
        public byte[] getBytes();
}
