/**
* BigSackHandler -  provides a deep store for PowerKernel
* @author Groff (C) NeoCoreTechs 1998,2000
*/
package com.neocoretechs.powerspaces.server.handler;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.*;
import com.neocoretechs.powerspaces.server.handler.*;
import com.neocoretechs.bigsack.session.BigSackAdapter;
import com.neocoretechs.bigsack.session.BufferedTreeSet;
import com.neocoretechs.bigsack.session.SessionManager;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.*;

public class BigSackHandler {

        public static String path = "/jserv/";
        private static boolean create = true;
        private static int FIRST = 0;
        private static int VALUE = 1;
        private static BufferedTreeSet DS = null;
        static {
        	BigSackAdapter.setTableSpaceDir(path);
        }
        public static Object PowerKernel_Take( Integer leg, CustomerConnectionPanel ccp, String db, Object args) throws PowerSpaceException , FinishedException {
                boolean rollback = false;     
                Exception e2 = null;
                Comparable removed = null;
                try {
                        DS = BigSackAdapter.getBigSackTreeSet((Comparable) args);
                        removed = (Comparable) DS.remove((Comparable) args);
                } catch(Exception e) {
                        e2 = new Exception("Store exception "+e.getMessage());
                        return e2;
                }
                return removed;
        }

//        public Packet PSHandler_AddStation( Integer leg, CustomerConnectionPanel ccp, Object args) throws PowerSpaceException, FinishedException
//        {
//                System.out.println("Handler AddStation");
//                if( ((Packet)args).getField(1).value() instanceof String)
//                        HandlerClassLoader.instantiateClass((String)((Packet)args).getField(0).value(),
//                                                    (String)((Packet)args).getField(1).value());
//                else
//                        HandlerClassLoader.instantiateClass((String)((Packet)args).getField(0).value(),
//                                                    (byte[])((Packet)args).getField(1).value());
//                return new Packet("OK","AddStation");
//        }

//        public Packet PSHandler_AddSupport( Integer leg, CustomerConnectionPanel ccp, Object args) throws PowerSpaceException, FinishedException
//        {
//                System.out.println("Handler AddSupport");
//                HandlerClassLoader.defineClasses((byte[])((Packet)args).getField(0).value());
//                return new Packet("OK","AddSupport");
//        }

//        public Packet PSHandler_StartTransaction( Integer leg, CustomerConnectionPanel ccp, Object args) throws PowerSpaceException, FinishedException
//        {
//                System.out.println("StartTransaction "+pS);
//                return new Packet("return Packet","of StartTransaction");
//        }

//        public Packet PSHandler_EndTransaction( Integer leg, CustomerConnectionPanel ccp, Object args) throws PowerSpaceException, FinishedException
//        {
//                System.out.println("EndTransaction "+args);
//                return new Packet("return Packet","of EndTransaction");
//        }


   public static Object PowerKernel_Store( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
        try {
            DS = BigSackAdapter.getBigSackTreeSet((Comparable) so);       
            DS.put((Comparable) so);
        } catch(Exception e) { 
        	return e;
        }
        return "OK";
   }

   public static Object PowerKernel_StoreAll( Integer leg, CustomerConnectionPanel ccp, String db, Vector eso) throws PowerSpaceException {
        return PowerKernel_StoreAll(leg, ccp, db, eso.elements());
   }

   public static Object PowerKernel_StoreAll( Integer leg, CustomerConnectionPanel ccp, String db, Enumeration eso) throws PowerSpaceException {
        try { 
            while(eso.hasMoreElements()) {
                DS = BigSackAdapter.getBigSackTreeSet((Comparable) eso);      
                DS.put((Comparable) eso.nextElement());
            }
        } catch(Exception e) {
        	return e;
        }
        return "OK";
   }

   public static Object PowerKernel_Update( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Serializable sou, Comparator co) throws PowerSpaceException {
        try {
                int cmpRes = co.compare((Object)so, (Object)sou);
                if( cmpRes == 0 ) {
                    DS = BigSackAdapter.getBigSackTreeSet((Comparable) sou);  
                    DS.put((Comparable)sou);
                }
        } catch(Exception e) { 
        	return e;
        }
        return "OK";
   }

   public static Object PowerKernel_UpdateAll( Integer leg, CustomerConnectionPanel ccp, String db, Vector vo, Vector vou, Comparator co) throws PowerSpaceException {
        return PowerKernel_UpdateAll(leg, ccp, db, vo.elements(), vou.elements(), co);
   }

   public static Object PowerKernel_UpdateAll( Integer leg, CustomerConnectionPanel ccp, String db, Enumeration eo, Enumeration eou, Comparator co) throws PowerSpaceException {
        Object eoElement, eouElement;
        try {
                while(eo.hasMoreElements()) {
                        eoElement = eo.nextElement();
                        eouElement = eou.nextElement();
                        int cmpRes = co.compare(eoElement, eouElement);
                        //
                        // check result of the compare of the 2 elements, if 0 proceed
                        if( cmpRes == 0 ) {      
                            DS= BigSackAdapter.getBigSackTreeSet((Comparable) eouElement);                                    
                            DS.put((Comparable) eouElement);
                        }
                }
        } catch(Exception e) { 
        	return e;
        }
        return "OK";
   }

   public static Object PowerKernel_Delete( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
                try {
                    DS = BigSackAdapter.getBigSackTreeSet((Comparable) so);
                    DS.remove((Comparable) so);
                } catch(Exception e) { 
                	return e;
                }
        return "OK";
   }

   public static Object PowerKernel_DeleteAll( Integer leg, CustomerConnectionPanel ccp, String db, Vector vo) throws PowerSpaceException {
        return PowerKernel_DeleteAll(leg, ccp, db, vo.elements());
   }

   public static Object PowerKernel_DeleteAll( Integer leg, CustomerConnectionPanel ccp, String db, Enumeration eso) throws PowerSpaceException {
        try {    
                while(eso.hasMoreElements()) {
                	Comparable so = (Comparable) eso.nextElement();
                    DS = BigSackAdapter.getBigSackTreeSet(so);
                	DS.remove(so);
                }
        } catch(Exception e) { 
        	return e;
        }
        return "OK";
   }

   public static Object PowerKernel_Find( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
        try {
        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so); 
            return DS.contains((Comparable) so);
        } catch(Exception e) { 
        	return e;
        }
   }

   //-----------------
   // Ascending section
   //-----------------

   public static Object PowerKernel_FindAllAscending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
        return FindAllAscending(db, so, FIRST);
   }

   public static Object PowerKernel_FindAllAscending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Comparator co) throws PowerSpaceException {
        return FindAllAscending(db, so, co, FIRST);
   }

   private static Object FindAllAscending(String db, Serializable so, int fIRST2) throws PowerSpaceException {
        List<Object> v = new ArrayList<Object>();
        try {
        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
        	v = DS.headSetStream((Comparable) so).collect(Collectors.toList());
        } catch(Exception e) {
        	return e;
        }
        return v;
   }


   private static Object FindAllAscending(String db, Serializable so, Comparator co, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(co).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   public static Object PowerKernel_FindAllAscendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
        return FindAllAscending(db, so, VALUE);
   }

   public static Object PowerKernel_FindAllAscendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Comparator co) throws PowerSpaceException {
        return FindAllAscending(db, so, co, VALUE);
   }

   //-------------------------
   // Descending section
   //-------------------------

   public static Object PowerKernel_FindAllDescending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
        return FindAllDescending(db, so, (Comparator)so, FIRST);
   }

   public static Object PowerKernel_FindAllDescending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Comparator co) throws PowerSpaceException {
        return FindAllDescending(db, so, co, FIRST);
   }

   private static Object FindAllDescending(String db, Serializable so, Comparator co, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(co.reversed()).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }


   public static Object PowerKernel_FindAllDescendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   public static Object PowerKernel_FindAllDescendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Comparator co) throws PowerSpaceException {
        return FindAllDescending(db, so, co, VALUE);
   }

   //---------------------------
   // Ascending by index section
   //---------------------------

   public static Object PowerKernel_FindAllAscendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, index, FIRST);
   }

   public static Object PowerKernel_FindAllAscendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, index, FIRST);
   }

   public static Object PowerKernel_FindAllAscendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, (short)index, co, FIRST);
   }
   public static Object PowerKernel_FindAllAscendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, index, co, FIRST);
   }

   private static Object FindAllAscendingByIndex(String db, Serializable so, Object index, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllAscendingByIndex(String db, Serializable so, Object index, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }


   private static Object FindAllAscendingByIndex(String db, Serializable so, Object index, Comparator co, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllAscendingByIndex(String db, Serializable so, Object index, Comparator co, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   public static Object PowerKernel_FindAllAscendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, (short)index, VALUE);
   }
   public static Object PowerKernel_FindAllAscendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, index, VALUE);
   }
   public static Object PowerKernel_FindAllAscendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, (short)index, co, VALUE);
   }
   public static Object PowerKernel_FindAllAscendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllAscendingByIndex(db, so, index, co, VALUE);
   }

   //-------------------------
   // Descending by index section
   //-------------------------
   public static Object PowerKernel_FindAllDescendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, (short)index, FIRST);
   }
   public static Object PowerKernel_FindAllDescendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, index, FIRST);
   }
   public static Object PowerKernel_FindAllDescendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, (short)index, co, FIRST);
   }
   public static Object PowerKernel_FindAllDescendingByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, index, co, FIRST);
   }

   private static Object FindAllDescendingByIndex(String db, Serializable so, Object index, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllDescendingByIndex(String db, Serializable so, Object index, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }


   private static Object FindAllDescendingByIndex(String db, Serializable so, Object index, Comparator co, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllDescendingByIndex(String db, Serializable so, Object index, Comparator co, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }
   public static Object PowerKernel_FindAllDescendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, (short)index, VALUE);
   }
   public static Object PowerKernel_FindAllDescendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, index, VALUE);
   }
   public static Object PowerKernel_FindAllDescendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, (short)index, co, VALUE);
   }
   public static Object PowerKernel_FindAllDescendingFromValueByIndex( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllDescendingByIndex(db, so, index, co, VALUE);
   }

   //--------------------
   // Unique Ascending Section
   //--------------------

   public static Object PowerKernel_FindAllUniqueAscending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, (short)index, FIRST);
   }
   public static Object PowerKernel_FindAllUniqueAscending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, index, FIRST);
   }
   public static Object PowerKernel_FindAllUniqueAscending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, (short)index, co, FIRST);
   }
   public static Object PowerKernel_FindAllUniqueAscending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, index, co, FIRST);
   }

   private static Object FindAllUniqueAscending(String db, Serializable so, Object index, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted().distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllUniqueAscending(String db, Serializable so, Object index, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted().distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }


   private static Object FindAllUniqueAscending(String db, Serializable so, Object index, Comparator co, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(co).distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllUniqueAscending(String db, Serializable so, Object index, Comparator co, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(co).distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }
   public static Object PowerKernel_FindAllUniqueAscendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, (short)index, VALUE);
   }
   public static Object PowerKernel_FindAllUniqueAscendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, index, VALUE);
   }
   public static Object PowerKernel_FindAllUniqueAscendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, (short)index, co, VALUE);
   }
   public static Object PowerKernel_FindAllUniqueAscendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueAscending(db, so, index, co, VALUE);
   }

   //--------------------
   // Unique Descending Section
   //--------------------

   public static Object PowerKernel_FindAllUniqueDescending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, (short)index, FIRST);
   }
   public static Object PowerKernel_FindAllUniqueDescending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, index, FIRST);
   }
   public static Object PowerKernel_FindAllUniqueDescending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, (short)index, co, FIRST);
   }
   public static Object PowerKernel_FindAllUniqueDescending( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, index, co, FIRST);
   }

   private static Object FindAllUniqueDescending(String db, Serializable so, Object index, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllUniqueDescending(String db, Serializable so, Object index, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(((Comparator)so).reversed()).distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }


   private static Object FindAllUniqueDescending(String db, Serializable so, Object index, Comparator co, int fIRST2) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(co.reversed()).distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   private static Object FindAllUniqueDescending(String db, Serializable so, Object index, Comparator co, short starting) throws PowerSpaceException {
	      List<Object> v = new ArrayList<Object>();
	        try {
	        	DS = BigSackAdapter.getBigSackTreeSet((Comparable)so);
	        	v = (List<Object>) DS.headSetStream((Comparable) so).sorted(co.reversed()).distinct().collect(Collectors.toList());
	        } catch(Exception e) {
	        	return e;
	        }
	        return v;
   }

   public static Object PowerKernel_FindAllUniqueDescendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, (short)index, VALUE);
   }
   public static Object PowerKernel_FindAllUniqueDescendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, index, VALUE);
   }
   public static Object PowerKernel_FindAllUniqueDescendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, int index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, (short)index, co, VALUE);
   }
   public static Object PowerKernel_FindAllUniqueDescendingFromValue( Integer leg, CustomerConnectionPanel ccp, String db, Serializable so, Object index, Comparator co) throws PowerSpaceException {
        return FindAllUniqueDescending(db, so, index, co, VALUE);
   }

   // Relation Section
   public static Object StoreRelation( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Serializable tfrom, Serializable tto) throws PowerSpaceException {
	   
        return "OK";
   }

   public static Object StoreAllRelations( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Vector tfrom, Vector tto) throws PowerSpaceException {
   
        return "OK";
   }
   public static Object DeleteRelations( Integer leg, CustomerConnectionPanel ccp, String db, String relName) throws PowerSpaceException {
     
        return "OK";
   }

   public static Object DeleteRelation( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Serializable tfrom, Serializable tto) throws PowerSpaceException {
	   return "Ok";
   }

   public static Object DeleteRelationFrom( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Serializable tfrom) throws PowerSpaceException {
    
        return "OK";
   }

   public static Object DeleteRelationTo( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Serializable tto) throws PowerSpaceException {
   
        return "OK";
   }
   /**
   * @return Vector of all related objects or exception
   */
   public static Object PowerKernel_FindAllRelations( Integer leg, CustomerConnectionPanel ccp, String db) throws PowerSpaceException {
       return "Ok";
   }

   /**
   * @return All relations or exception Object
   */
   public static Object PowerKernel_FindAllRelations( Integer leg, CustomerConnectionPanel ccp, String db, String relName) throws PowerSpaceException {
	   return "Ok";
   }
   /**
   * @return All 'FROM' relations or exception Object
   */
   public static Object PowerKernel_FindAllRelationsFrom( Integer leg, CustomerConnectionPanel ccp, String db, String relName) throws PowerSpaceException {
	   return "Ok";
   }

   /**
   * @return All 'FROM' relations or exception Object
   */
   public static Object PowerKernel_FindAllRelationsFrom( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Serializable tto) throws PowerSpaceException {
    return "Ok";
   }
   /**
   * @return All 'TO' relations or exception Object
   */
   public static Object PowerKernel_FindAllRelationsTo( Integer leg, CustomerConnectionPanel ccp, String db, String relName) throws PowerSpaceException {
    return "Ok";
   }
   /**
   * @return All 'TO' relations or exception Object
   */
   public static Object PowerKernel_FindAllRelationsTo( Integer leg, CustomerConnectionPanel ccp, String db, String relName, Serializable tfrom) throws PowerSpaceException {
    
        return "Ok";
   }

}

