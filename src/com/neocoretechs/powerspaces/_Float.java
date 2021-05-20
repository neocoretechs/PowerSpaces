package com.neocoretechs.powerspaces;
import java.io.Serializable;
    public class _Float implements Serializable {
        static final long serialVersionUID = -7345767151699907243L;
        public float f;
        public _Float(float v) {
               f = v;
        }
        public String toString() { return String.valueOf(f); }

        public int compareTo(Serializable o)
        {
                System.out.println("Float compare");
                return  f > ((_Float)o).f ? 1 :
                        f < ((_Float)o).f ? -1 : 0;
        }
    }
