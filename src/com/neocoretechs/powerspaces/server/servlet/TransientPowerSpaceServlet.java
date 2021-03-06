package com.neocoretechs.powerspaces.server.servlet;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.neocoretechs.powerspaces.server.ConnectionPanel;


/**
* Main interface from Servlet engine to PowerSpace
* for known-to-be transient sessions
* @author Groff (C) NeoCoreTechs 2000
*/
public class TransientPowerSpaceServlet extends HttpServlet
{
    /**
     * Handle the GET and HEAD methods by building a simple web page.
     * HEAD is just like GET, except that the server returns only the
     * headers (including content length) not the body we write.
     */
    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
        try {
          String sSession = request.getSession(true).getId();
          ServletTransmissionLine stl = new ServletTransmissionLine(
                                sSession,
                                request, response);
          ConnectionPanel.CustomerConnectionPanelTable.remove(sSession);
        } catch(Exception e) {
                getServletContext().log(e, e.getMessage());
                System.out.println(e);
                return;
        }
    }

    public void doPost (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
                doGet ( request, response);
        }

}
 
