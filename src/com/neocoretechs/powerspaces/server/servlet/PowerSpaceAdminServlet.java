package com.neocoretechs.powerspaces.server.servlet;
import java.util.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.PowerPlant;


/**
 */
public class PowerSpaceAdminServlet extends HttpServlet
{ 
    /**
     */
    public void doPost (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
        }
    /**
    */
    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
            PrintWriter out;
            String title = "PowerSpace Admin";

            // set content type and other response header fields first
            response.setContentType("text/html");

            // then write the data of the response
            out = response.getWriter();
            
            out.println("<HTML><HEAD><TITLE>");
            out.println(title);
            out.println("</TITLE></HEAD><BODY bgcolor=\"#FFFFFF\">");
            out.println("<H1>" + title + "</H1>");
            //
            // parse the params
            //
            Enumeration cname = request.getParameterNames();
            // parse the params
            //
            try {
                if( cname.hasMoreElements() ) {
                        String cname0 = (String)cname.nextElement();
                        if( cname0.equals("database") ) {
                                // db off/ on line
//                                String dbname = request.getParameter("T1");
//                                String onoff = request.getParameter("R1");
                        } else {
                                if(cname0.equals("remove") ) {
                                        out.println("<H2> We are removing session "+request.getParameterValues(cname0)[0]+"<br></br>");
                                        out.println("<H2> Hit 'back' then 'reload' buttons for next<br>");

                                }
                        }
                }

                out.println("<H2> PowerPlant port "+PowerPlant.PowerPlantPort+"</H2>");
                for(int i = 0; i < PowerPlant.numberLegs ; i++ ) {
                        out.println("<H3>Locally bound ports for leg "+i+":"+PowerPlant.powerPlantLegConnections[i].getLocalBindIn()+","+PowerPlant.powerPlantLegConnections[i].getLocalBindOut()+"<br>");
                        if( PowerPlant.powerPlantLegConnections[i].isLinked() )
                                out.println("<H3>Leg "+i+" is linked to cluster ID "+PowerPlant.powerPlantLegConnections[i].getLinkedClusterID()+"</H3><br></br>");
                        else
                                out.println("<H3>Leg "+i+" is not linked</H3><br></br>");
                }
                out.println("<p>Sessions:</p><br></br>");
                Enumeration e = ConnectionPanel.CustomerConnectionPanelTable.keys();
                while(e.hasMoreElements()) {
                        out.println(e.nextElement()+"<br></br>");
                }
                out.println("<p></p>");
                out.println("Loaded handlers:<br></br>");
                Enumeration ek = PowerPlant.handlerClasses.keys();
                while(ek.hasMoreElements())
                        out.println("<li></li>"+ek.nextElement()+"<br></br>");
            } catch(Exception e2) {
                out.println("<H2> Admin Access Exception "+e2+"</H2><br></br>");
                e2.printStackTrace(out);
            }
            out.println("</BODY></HTML>");
            out.close();
       }
}
 
