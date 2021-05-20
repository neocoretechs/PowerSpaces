package com.neocoretechs.powerspaces.server.servlet;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.CustomerConnectionPanel;


/**
* This servlet handles requests for dynamic content and forwards them to
* the ContentServletTransmissionLine
* @author Groff (C) NeoCoreTechs 2000
*/
public class ContentServlet extends HttpServlet
{
    /**
     */
    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
        try {
          // set up session tracking
          String sSession = null;
          // check the cookies, if we have no session, make one
          // and cook it
          Cookie[] cookies = request.getCookies();
          if( cookies != null ) {
                for(int i = 0; i < cookies.length; i++) {
                        if( cookies[i].getName().equals("sessionid") ) {
                                sSession = cookies[i].getValue();
                                break;
                        }
                }
          }
          // no cookie, make one and session
          if( sSession == null ) {
                HttpSession session = request.getSession(true);
                sSession = session.getId();
                // set up session binding
                session.putValue("bindings.listener", new PKContentSessionBindingListener(getServletContext(), sSession));
                Cookie c = new Cookie("sessionid", sSession);
                response.addCookie(c);
          }
          // now look for panel, and ServletTransmissionLine in props
          CustomerConnectionPanel ccp = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(sSession));
          if( ccp == null ) {
               ContentServletTransmissionLine stl = new ContentServletTransmissionLine(
                                sSession,
                                request, response);
          } else {
                ContentServletTransmissionLine stl = (ContentServletTransmissionLine)(ccp.properties.get("ContentServlet"));
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

    class PKContentSessionBindingListener implements HttpSessionBindingListener {
        ServletContext context;
        String sSession;
        public PKContentSessionBindingListener(ServletContext context, String session) {
                this.context = context;
                this.sSession = session;
        }
        public void valueBound(HttpSessionBindingEvent event) {
//                String sSession = event.getSession().getId();
                context.log("ContentServlet session "+sSession+" BOUND");
        }
        public void valueUnbound(HttpSessionBindingEvent event) {
//                String sSession = event.getSession().getId();
                ConnectionPanel.CustomerConnectionPanelTable.remove(sSession);
                context.log("ContentServlet session "+sSession+" UNBOUND");
        }
    }

}
 
