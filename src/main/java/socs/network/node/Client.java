package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.NoPortAvailable;
import socs.network.message.SOSPFPacket;

import java.net.Socket;
import java.io.*;

public class Client implements Runnable {
    Socket clientSocket;
    Router router;
    ObjectInputStream in;
    
    int weight = 0;
    LinkStateDatabase linksd;
    //to check if everything is there
    boolean notthere = false;
    
    
    public Client() {
        super();
    }

    Client(Socket socket, Router router, LinkStateDatabase lsd) {
        clientSocket = socket;
        this.router = router;
        
        linksd = lsd;
    }

    @Override
    public void run() {
        // Obtain the input stream and the output stream for the socket
        // A good practice is to encapsulate them with a BufferedReader
        // and a PrintWriter as shown below.
        in = null;

        // Print out details of this connection
        //System.out.println("Accepted Client Address - " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getLocalPort());

        try {
            in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            // At this point, we can read for input and reply with appropriate output.

            // read incoming stream
            SOSPFPacket msg = (SOSPFPacket) in.readObject();
            String dstIP = msg.dstIP;

            
            if (dstIP.equals(router.rd.simulatedIPAddress)) {
                if (msg.sospfType == SOSPFPacket.CONNECT_REFUSED) {
                    System.out.println("Received CONNECTION REFUSED from " + msg.srcIP);
                                        
                } else if(msg.sospfType == 1) {
                	
                	
                	//checking if the current lsas and the msg lsas are the same
                	for(LSA lsa : msg.lsaArray) {
                		
                		for (LSA l : linksd._store.values()) {
          				            				  
          				  for(LinkDescription a : l.links) {
          				  
          					  	if(!lsa.links.contains(a)) {
          						  
          					  		notthere = true;
          					  		break;
          						  
          					  	}
          				  			  
          				  	}
                		} 
                		
                		notthere = false;
                		
                	}
                	
                	
                	for (LSA l : msg.lsaArray) {
						
                		//updating LSA and Link State Data accordingly
                		synchronized (linksd._store) {
                			if (linksd._store.get(l.linkStateID) == null) {
                				
                				// adding new lsa, if there were none from that router before
                				linksd._store.put(l.linkStateID, l); 
                				
                			}

                			else {
                				if (linksd._store.get(l.linkStateID).lsaSeqNumber < l.lsaSeqNumber) {
                					
                					//update the lsd
                					linksd._store.put(l.linkStateID, l);

                				}
                			}
                		}
					}
                	        
                	//send the update to everyone except the one who transmitted it
                	//only send if there's any new changes (avoids looping)
                	if(notthere)
                	router.sendLSA(msg.srcIP);
                	
                	
                	/*
					//printing checkup
                    for (LSA l : linksd._store.values()) {
        				  
        				  System.out.println(l.linkStateID);
        				  
        				  for(LinkDescription a : l.links) {
        				  
        					  System.out.println(a.toString());
        				  			  
        				  }
        			  }  
					*/
					
                } else if (msg.sospfType == SOSPFPacket.HELLO) {
                    System.out.println("Received HELLO from " + msg.srcIP + ";");

                    RouterDescription remoteRouter = router.ports.getRouterDescription(msg.srcIP);

                    if (remoteRouter == null) {
                        // Router not linked yet
                        remoteRouter = new RouterDescription(msg.srcProcessIP, msg.srcProcessPort, msg.srcIP);

                        try {
                            router.ports.addLink(new Link(router.rd, remoteRouter));
                        } catch(NoPortAvailable exception) {
                            System.out.println(exception.toString());
                            router.sendConnRefused(remoteRouter);
                            return;
                        } catch(Exception exception) {
                            System.out.println(exception.toString());
                            return;
                        }
                    }
                    // We have successfully linked the router
                    
                    // If HELLO is from a TWO_WAY router
                    if(remoteRouter.status == RouterStatus.TWO_WAY) {
                        System.out.println("Received a HELLO from a TWO_WAY router: " + remoteRouter.simulatedIPAddress + ". Ignoring." );
                        
                        return;
                    }

                    if(remoteRouter.status == RouterStatus.INIT) {
                        // We received a reply from an initial HELLO request
                        // Request to enter TWO_WAY state
                        remoteRouter.status = RouterStatus.TWO_WAY;

                        // We received an initial HELLO request from this router
                        // Confirm reception from this router
                        if(router.attachedPorts.find(remoteRouter) > -1)
                            router.sendHello(remoteRouter);
                    } else {
                        remoteRouter.status = RouterStatus.INIT;

                        
                        //Updating LSA with the link that connected to this
            	        LSA thisLSA = linksd._store.get(router.rd.simulatedIPAddress);
            	        thisLSA.lsaSeqNumber += 1;
            			LinkDescription ld1 = new LinkDescription();
            			
            			ld1.portNum = router.ports.find(remoteRouter);
            			ld1.linkID = msg.srcIP;
            			ld1.tosMetrics = msg.weight;
            			
            			thisLSA.links.add(ld1);
            			
            			//updating LSA for others
            			router.sendLSA(null);
            			
            			
                        // We received an initial HELLO request from this router
                        // Confirm reception from this router
                        router.sendHello(remoteRouter);
                    }

                    System.out.println("Set " + remoteRouter.simulatedIPAddress + " state to " + remoteRouter.status.toString() + ";");
                } else {
                    System.out.println("Received msg of unknown type " + msg.sospfType + " from " + msg.srcIP + ";");
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            // Clean up
            try {
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}