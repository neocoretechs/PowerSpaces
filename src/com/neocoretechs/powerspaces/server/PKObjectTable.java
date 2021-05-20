package com.neocoretechs.powerspaces.server;
import java.util.*;
public class PKObjectTable {
        private static Hashtable ObjectTable = new Hashtable();
        public static Object getObject(String session, String objref) {
                return ObjectTable.get(objref);
        }
}
