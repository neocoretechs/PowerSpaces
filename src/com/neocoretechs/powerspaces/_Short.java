package com.neocoretechs.powerspaces;
import java.io.Serializable;
    public class _Short implements Serializable {
        static final long serialVersionUID = -4390927717588552580L;
        public short s;
        public _Short(short v) {
               s = v;
        }
        public String toString() { return String.valueOf(s); }

        public int compareTo(Serializable o)
        {
                System.out.println("Short compare");
                return  s > ((_Short)o).s ? 1 :
                        s < ((_Short)o).s ? -1 : 0;
        }
    }
