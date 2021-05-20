package com.neocoretechs.powerspaces;
import java.io.Serializable;
    public class _Integer implements Serializable {
        static final long serialVersionUID = 2624220057997293256L;
        public int i;
        public _Integer(int v) {
               i = v;
        }
        public String toString() { return String.valueOf(i); }

        public int compareTo(Serializable o)
        {
//                System.out.println("Integer compare");
                return  i > ((_Integer)o).i ? 1 :
                        i < ((_Integer)o).i ? -1 : 0;
        }
    }
