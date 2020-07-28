package socs.network.message;

import java.io.*;
import java.util.Vector;

public class SOSPFPacket implements Serializable {

  //for inter-process communication
  public String srcProcessIP;
  public short srcProcessPort;

  //simulated IP address
  public String srcIP;
  public String dstIP;

  //common header
  public short sospfType; //0 - HELLO, 1 - LinkState Update
  public String routerID;

  //used by HELLO message to identify the sender of the message
  //e.g. when router A sends HELLO to its neighbor, it has to fill this field with its own
  //simulated IP address
  public String neighborID; //neighbor's simulated IP address
  
  //keeping track of weight
  public int weight;
  
  //used by LSAUPDATE
  public Vector<LSA> lsaArray = null;

  public static final short CONNECT_REFUSED = -1;
  public static final short HELLO = 0;
  public static final short LSAUPDATE = 1;
  //public static final short LINKSTATE_CONNECT_UPDATE = 2;
  //public static final short LINKSTATE_DISCONNECT = 3;

  public SOSPFPacket(String srcProcessIP, short srcProcessPort, String srcIP, String dstIP, short type) {
    // Src process address to open the socket connection
    this.srcProcessIP = srcProcessIP;
    this.srcProcessPort = srcProcessPort;

    //simulated IP address
    this.srcIP = srcIP;
    this.dstIP = dstIP;

    this.routerID = srcIP;
    this.sospfType = type;
  }
}