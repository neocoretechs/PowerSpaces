package com.neocoretechs.powerspaces.server.servlet;


/**
* Class to return Gif content type and bytes from a handler method to
* ContenServletTransmissionLine
* @author Groff Copyright (C) NeoCoreTechs 2001
*/
public class GifContentType implements ContentTypeInterface {
        private byte[] imgBytes;
        public String getContentType() { return "image/gif"; }
        public byte[] getBytes() { return imgBytes; }
        public GifContentType(byte[] timg) { imgBytes = timg; }
}
