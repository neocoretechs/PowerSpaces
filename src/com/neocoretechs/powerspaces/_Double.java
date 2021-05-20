package com.neocoretechs.powerspaces;
import java.io.Serializable;
    public class _Double implements Serializable {
        static final long serialVersionUID = 4812201799416757995L;
        public double d;
        public _Double(double v) {
               d = v;
        }
        public String toString() { return String.valueOf(d); }

        public int compareTo(Serializable o)
        {
                System.out.println("Double compare");
                return  d > ((_Double)o).d ? 1 :
                        d < ((_Double)o).d ? -1 : 0;
        }
    }
