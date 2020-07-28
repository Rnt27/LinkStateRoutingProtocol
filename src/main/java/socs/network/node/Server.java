package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;

public class Server implements Runnable {
    ServerSocket serverSocket;
    ThreadPoolExecutor tp;
    Router router;
    
    
    LinkStateDatabase linksd;
    //SOSPFPacket message = null;
    
    
    public Server() {
        super();
    }

    Server(Router router, LinkStateDatabase lsd) {
        // Create Server Socket
        try {
            serverSocket = new ServerSocket(router.rd.processPortNumber);
        } catch (IOException ioe) {
            System.out.println("Can't bind the router on port " + router.rd.processPortNumber);
            System.exit(20);
        }

        this.router = router;
        this.linksd = lsd;
        
        tp = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        // Wait for connections.
        while (router.running) {
            try {
                // Accept incoming connections.
                Socket clientSocket = serverSocket.accept();

                // accept() will block until a client connects to the server.
                // If execution reaches this point, then it means that a client
                // socket has been accepted.

                // For each client, we will start a service thread to
                // service the client requests. This is to demonstrate a
                // Multi-Threaded server. Starting a thread also lets our
                // MTServerSocket accept multiple connections simultaneously.

                // Start a Service thread
                                				
                try {
                	//tp.execute(new Client(clientSocket, router));
                	
                	Client c = null;
                	//Client c = new Client(clientSocket, router);
                	tp.execute(c = new Client(clientSocket, router, linksd));
                	
                	
                	
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("Couldn't connect to client.");
                }

            } catch (IOException e) {
                System.out.println("Exception encountered on accept. Ignoring.");
                System.exit(21);
            }
        }
        
        
        
        
		
		
		
        try {
            serverSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception e) {
            System.out.println("Problem stopping server socket");
            System.exit(22);
        }
    }
}