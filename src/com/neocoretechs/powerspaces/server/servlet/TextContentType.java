package com.neocoretechs.powerspaces.server.servlet;


/**
* Class to return plain content type and bytes from a handler method to
* ContenServletTransmissionLine
* @author Groff Copyright (C) NeoCoreTechs 2001
*/
public class TextContentType implements ContentTypeInterface {
        private String tcon;
        public String getContentType() { return "text/plain"; }
        public byte[] getBytes() { return tcon.getBytes(); }
        public TextContentType(String ttcon) { tcon = ttcon; }
}
