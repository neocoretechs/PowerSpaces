package com.neocoretechs.powerspaces.server;
import java.net.*;
import java.io.*;
/**
* TCPServer is the superclass of all objects using ServerSockets.
* For each new connection, it clones itself
*/
public class TCPServer implements Cloneable, Runnable {
	Thread runner = null;
	ServerSocket server = null;
	Socket data = null;
	boolean shouldStop = false;
	public synchronized void startServer(int port) throws IOException {
		if( runner == null ) {
			server = new ServerSocket(port);
			runner = new Thread(this);
			runner.start();
		}
	}
	public synchronized void startServer(int port, InetAddress binder) throws IOException {
		if( runner == null ) {
                        System.out.println("TCPServer attempt local bind "+binder+" port "+port);
			server = new ServerSocket(port, 1000, binder);
			runner = new Thread(this);
			runner.start();
		}
	}
	public synchronized void stopServer() throws IOException {
		if( server != null ) {
			shouldStop = true;
			runner.interrupt();
			runner = null;
			server.close();
			server = null;
		}
	}
	public void run() {
		if(server != null ) {
			while(!shouldStop) {
				try {
					Socket datasocket = server.accept();
                                        // disable Nagles algoritm; do not combine small packets into larger ones
                                        datasocket.setTcpNoDelay(true);
                                        // wait 1 second before close; close blocks for 1 sec. and data can be sent
                                        datasocket.setSoLinger(true, 1);
					//
					TCPServer newSocket = (TCPServer)clone();
					newSocket.server = null;
					newSocket.data = datasocket;
					newSocket.runner = new Thread(newSocket);
					newSocket.runner.start();
				} catch(Exception e) {
                                        System.out.println("TCPServer socket accept exception");
                                        System.out.println(e.getMessage());
                                        e.printStackTrace();
                                }
			}
		} else {
			run(data);
		}
	}

	public void run(Socket data) {}

        public void reInit() throws IOException {
                   if( data != null ) data.close();
        }
}	

