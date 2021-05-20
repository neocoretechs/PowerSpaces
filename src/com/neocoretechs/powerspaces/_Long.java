package com.neocoretechs.powerspaces;
import java.io.Serializable;
    public class _Long implements Serializable {
        static final long serialVersionUID = 3965841773464846795L;
        public long l;
        public _Long(long v) {
               l = v;
        }
        public String toString() { return String.valueOf(l); }

        public int compareTo(Serializable o)
        {
                System.out.println("Long compare");
                return  l > ((_Long)o).l ? 1 :
                        l < ((_Long)o).l ? -1 : 0;
        }
    }
