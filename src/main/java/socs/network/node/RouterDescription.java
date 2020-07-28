package socs.network.node;

public class RouterDescription {
  //used to socket communication
  String processIPAddress;
  // Ports 1024-49151 are the User Ports
  // 50000 and 50001 are outside of range
  short processPortNumber;
  //used to identify the router in the simulated network space
  String simulatedIPAddress;
  //status of the router
  volatile RouterStatus status;

  RouterDescription() {
  }

  RouterDescription(String processIP, short processPort, String simulatedIP) {
    processIPAddress = processIP;
    processPortNumber = processPort;
    simulatedIPAddress = simulatedIP;
    status = RouterStatus.DOWN;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null)
      return false;
    if (getClass() != o.getClass())
      return false;

    RouterDescription rd = (RouterDescription) o;
    return simulatedIPAddress.equals(rd.simulatedIPAddress) || (processIPAddress.equals(rd.processIPAddress) && processPortNumber == rd.processPortNumber);
  }
}