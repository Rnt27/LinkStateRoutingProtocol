package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.PacketFactory;
import socs.network.message.SOSPFPacket;
import socs.network.util.Configuration;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Vector;

public class Router {
	
  protected LinkStateDatabase lsd;
  RouterDescription rd;

  Ports ports;
  Ports attachedPorts;

  PacketFactory packetFactory;
  Runnable server;
  volatile boolean running;

  Socket clientSocket = null;
      
  public Router(Configuration config) {
    rd = new RouterDescription();

    try {
      rd.simulatedIPAddress = config.getString("socs.network.router.ip");
      rd.processIPAddress = config.getString("socs.network.router.processIp");
      rd.processPortNumber = config.getShort("socs.network.router.processPort");
    } catch (Exception exception) {
      System.out.println("Cannot read the configuration file.");
      System.exit(200);
    }

    lsd = new LinkStateDatabase(rd);
    
    running = true;
    //assuming that all routers are with 4 ports
    ports = new Ports(4);
    attachedPorts = new Ports(4);

    packetFactory = new PacketFactory();
    server = new Server(this, this.lsd);
    Thread thread = new Thread(server);
    thread.start();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          Thread.sleep(200);
          processQuit();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          //e.printStackTrace();
        }
      }
    });
  }

  
  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {
	  
	  String path = lsd.getShortestPath(destinationIP);
		
	  if (path != null) {
			System.out.println(path);
		} else
			System.out.println("Shortest Path Not Found");
  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to identify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   * CHANGED: boolean to indicate to bind failed
   */
  private boolean processAttach(String processIP, short processPort, String simulatedIP, short weight) {
    try {
        RouterDescription router = new RouterDescription(processIP, processPort, simulatedIP);
        
        boolean check = ports.addLink(new Link(rd, router));
        
        
        if(check) {
        	
        	//LSA from here to neighbor
        	LSA thisLSA = new LSA();
        	thisLSA.linkStateID = rd.simulatedIPAddress;
        	        				
			synchronized (lsd._store) {
				//if LSA already exists, increase sequence number (it can be useful later on for flooding)
				if (lsd._store.containsKey(rd.simulatedIPAddress)) {
					
					thisLSA.lsaSeqNumber = lsd._store.get(rd.simulatedIPAddress).lsaSeqNumber + 1;
					thisLSA.links =  (LinkedList<LinkDescription>) lsd._store.get(rd.simulatedIPAddress).links.clone();
					
				} else
					//if first one in sequence
					thisLSA.lsaSeqNumber = 1;
				
				// insert link description to LSA and store it:
				LinkDescription ld1 = new LinkDescription();
				
				ld1.portNum = ports.find(router);
				ld1.linkID = simulatedIP;
				ld1.tosMetrics = weight;
				
				thisLSA.links.add(ld1);
				lsd._store.put(rd.simulatedIPAddress, thisLSA);
				
				/*
				//Print to check the link descriptions updated
				for (LSA l : lsd._store.values()) {
					  
					  if(l.lsaSeqNumber == -1) {
						  
						  lsd._store.remove(l);
						  
					  }
					  
					  System.out.println(l.linkStateID);
					  
					  for(LinkDescription a : l.links) {
					  
						  System.out.println(a.toString());
					  			  
					  }
				  }
				*/
			}
			
        }
        
     } catch (Exception exception) {
       System.out.println(exception.toString());
     }
     return true;
  }

  private boolean processAttachFile(String file, short weight) {

    try {
      Configuration config = new Configuration(file);
      String simulatedIP = config.getString("socs.network.router.ip");
      String processIP = config.getString("socs.network.router.processIp");
      short processPort = config.getShort("socs.network.router.processPort");

      return processAttach(processIP, processPort, simulatedIP, weight);
    } catch (Exception exception) {
      System.out.println("Cannot read the configuration file.");
      return false;
    }
  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    // start this router and initialize the database synchronization process.
    // After you establish the links by running attach you will run start command to send HELLO
    // messages and LSAUPDATE to all connected routers for the Link State Database
    // synchronization.

    for (Link link : ports) {
        if(link.router2.status != RouterStatus.TWO_WAY) {
          try {
            sendHello(link.router2);
            
			try {
				//send update 
				sendLSA(null);
			}catch(Exception e) {
	            System.out.println("Can't send update to " + link.router2.simulatedIPAddress);
	        }
			
			
            link.router2.status = RouterStatus.INIT;
            attachedPorts.addLink(link);

          } catch(Exception e) {
            System.out.println("Can't establish connection with router " + link.router2.simulatedIPAddress);
          }
        }
    }
  }

  public void sendConnRefused(RouterDescription router) {
    SOSPFPacket connRefused = packetFactory.getConnRefused(
            rd.processIPAddress, rd.processPortNumber,
            rd.simulatedIPAddress, router.simulatedIPAddress
    );

    sendMsg(router, connRefused);
  }

  public void sendHello(RouterDescription router) {
    SOSPFPacket hello = packetFactory.getHello(
            rd.processIPAddress, rd.processPortNumber,
            rd.simulatedIPAddress, router.simulatedIPAddress
    );
    
    
    
    for (LinkDescription ld : lsd._store.get(rd.simulatedIPAddress).links) {
		if (ld.linkID.equals(router.simulatedIPAddress)) {
			hello.weight = ld.tosMetrics;
			break;
		}
	}
    
    
    
    sendMsg(router, hello);
  }
  
  
  
  public void sendLSA(String from) {
	//Message to update the LSA
	  SOSPFPacket msg = new SOSPFPacket(rd.processIPAddress,(short)rd.processPortNumber,rd.simulatedIPAddress,rd.simulatedIPAddress,(short) 1);
	
	  msg.lsaArray = new Vector<LSA>();
	 
	  //Updating the array in the message
	  synchronized (lsd._store) {
		  
		  	for (LSA l : lsd._store.values()) {
		  		if (l != null)
		  			msg.lsaArray.add(l);
		  	}
		  
	  }
	  	  	
	  //For every connection, send an update
	  for (Link link : ports) {
		  if (link != null) {
			  //if from is not empty, send to everyone but this IP
			  if (from != null) {
				  if (!from.equals(link.router2.simulatedIPAddress)) {
					  
					  msg.dstIP = link.router2.simulatedIPAddress;
					  sendMsg(link.router2,msg);
	  
				  }
			  } else {
				  
				  msg.dstIP = link.router2.simulatedIPAddress;
				  sendMsg(link.router2,msg);
				  				  
			  }
		  }
	  }
	  
  }
  
  

  public void sendMsg(RouterDescription router, SOSPFPacket msg) {
    Socket socket;
    ObjectOutputStream out;

    try {
      socket = new Socket();
      socket.connect(new InetSocketAddress(router.processIPAddress, router.processPortNumber), 3000);

      out = new ObjectOutputStream(socket.getOutputStream());
      out.writeObject(msg);
      out.close();
      socket.close();
    } catch (Exception e) {
      throw new RuntimeException("Can't connect to the router " + router.simulatedIPAddress + " on IP/Port " + router.processIPAddress + "/" + router.processPortNumber, e);
    }
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort, String simulatedIP, short weight) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    //output all neighbor links' simulated IP Address
    for(String simulatedIP : ports.getNeighbors()) {
      System.out.println(simulatedIP);
	}
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {
    running = false;
    System.out.println("Shutting down ...");
  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
          break;
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          boolean result = false;
          if (cmdLine.length == 5) {
            result = processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                    cmdLine[3], Short.parseShort(cmdLine[4]));
          } else if (cmdLine.length == 3) {
            result = processAttachFile(cmdLine[1], Short.parseShort(cmdLine[2]));
          } else {
            System.out.println("Wrong number of arguments.");
          }
          if (!result) System.out.println("Attach command failed.");
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          System.out.println("Invalid command.");
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("Something went wrong... terminating.");
    }
  }
}