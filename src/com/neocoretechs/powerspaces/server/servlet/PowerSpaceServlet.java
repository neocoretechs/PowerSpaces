package com.neocoretechs.powerspaces.server.servlet;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.CustomerConnectionPanel;


/**
* Main interface from Servlet engine to PowerSpace
* @author Groff (C) NeoCoreTechs 2000
*/
public class PowerSpaceServlet extends HttpServlet
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
          // set up session tracking
          String sSession;
          sSession = request.getQueryString();
          // create one
          if( sSession == null) {
                HttpSession session = request.getSession(true);
                sSession = session.getId();
                // set up session binding
                if( session.getValue("bindings.listener") != null )
                        session.putValue("bindings.listener", new PKSessionBindingListener(getServletContext()));
          }
          // now look for panel, and ServletTransmissionLine in props
          CustomerConnectionPanel ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(sSession));
          if( ccp == null ) {
                ServletTransmissionLine stl = new ServletTransmissionLine(
                                sSession,
                                request, response);
          } else {
                ServletTransmissionLine stl = (ServletTransmissionLine)(ccp.properties.get("Servlet"));
                stl.setRequest(request);
                stl.setResponse(response);
                stl.startFlow(ccp.getSession(), ccp);
          }
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

    class PKSessionBindingListener implements HttpSessionBindingListener {
        ServletContext context;
        public PKSessionBindingListener(ServletContext context) {
                this.context = context;
        }
        public void valueBound(HttpSessionBindingEvent event) {
                String sSession = event.getSession().getId();
                context.log("PowerSpaceServlet session "+sSession+" BOUND");
        }
        public void valueUnbound(HttpSessionBindingEvent event) {
                String sSession = event.getSession().getId();
                ConnectionPanel.CustomerConnectionPanelTable.remove(sSession);
                context.log("PowerSpaceServlet session "+sSession+" UNBOUND");
        }
    }
}
 
