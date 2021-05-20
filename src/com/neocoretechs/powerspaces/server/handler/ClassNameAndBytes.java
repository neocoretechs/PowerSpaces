package com.neocoretechs.powerspaces.server.handler;
import java.io.*;

public class ClassNameAndBytes implements Serializable, Comparable {
        private String name;
        private byte[] bytes;

        public ClassNameAndBytes() {}

        public ClassNameAndBytes(String tname) {
                name = tname;
        }
        public ClassNameAndBytes(String tname, byte[] tbytes) {
                name = tname;
                bytes = tbytes;
        }
        public byte[] getBytes() { return bytes; }
        public String getName() { return name; }
        public void setBytes(byte[] tbytes) { bytes = tbytes; }
        public void setName(String tname) { name = tname; }

        public int compareTo(Object o) {
        		ClassNameAndBytes tcb = (ClassNameAndBytes)o;
                return name.compareTo(tcb.getName());
        }
}
