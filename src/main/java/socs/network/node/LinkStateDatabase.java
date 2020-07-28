package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.ArrayList;
import java.util.HashMap;

public class LinkStateDatabase {

  //linkID => LSAInstance
  HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  String getShortestPath(String destinationIP) {
    //TODO: fill the implementation here
	  
	  //to hold different link IDs
	  ArrayList<String> linkIDs = new ArrayList<String>();
	  //To hold distance with link IDs
	  HashMap<String, Integer> distance = new HashMap<String, Integer>();
	  //To hold link IDs and the previous
	  HashMap<String, String> prev = new HashMap<String, String>();

	  
	  synchronized(_store) {
	    
	
		  for (LSA l : _store.values()) {
			  for (LinkDescription link : _store.get(l.linkStateID).links) {
			  
				  //if it there's no linkID in this arrayList, add a link ID
				  if (!linkIDs.contains(link.linkID)) linkIDs.add(link.linkID);
				  //if distance is not there, add distance as infinite, or at least highest integer
				  if (!distance.containsKey(link.linkID)) distance.put(link.linkID, Integer.MAX_VALUE);
				  //if previous is not there, then initiate it as null
				  if (!prev.containsKey(link.linkID)) prev.put(link.linkID, null);
			  
			  }
			  if (!linkIDs.contains(l.linkStateID)) linkIDs.add(l.linkStateID);
		  }

		  //set up distance and previous to itself for the source router
		  distance.put(rd.simulatedIPAddress, 0);
		  prev.put(rd.simulatedIPAddress, null);

		  //keep looping while not empty
		  while (!linkIDs.isEmpty()) {
	      
			  //known shortest distance, starts as high number
			  Integer minKnown = Integer.MAX_VALUE;
			  //Node min which is supposed to have the minimum distance to the source
			  String min = null;
	      
			  //finding out minimum node. 
			  //At the beginning, distances are "infinite" except rd
			  for (int i = 0; i < linkIDs.size(); i++) {
				  if (distance.get(linkIDs.get(i)) <= minKnown) {
	    		  
					  min = linkIDs.get(i);
					  minKnown = distance.get(min);
	    		  
				  }
			  }

			  //if minimum distance if not null, creating the string
			  if (min != null) {
	    	  
				  //Where we want to go - destination IP
				  String goal = destinationIP;
				  //what we will print
				  String print = "";
	    	  
				  //if the minimum node is the goal
				  if (min.equals(goal)) {
	    		  	    		
					  while (!goal.equals(rd.simulatedIPAddress)) {
	    			  
						  //If distance of goal and previous of goal are not null, then a path could be found
						  if (distance.get(goal) != null && distance.get(prev.get(goal)) != null) {
	    				  
							  print = "-> (" + (distance.get(goal) - distance.get(prev.get(goal))) + ") " + goal + " " + print;
	    				  
						  } else {
	    				  
							  return "Shortest Path could not be found";
	    				  
						  }
						  
						  //turn goal into the previous node
						  goal = prev.get(goal);
					  }
					  
					  print = goal + " " + print;

					  return print;

				  }
			  
	      
				  //removing minimum node from array
				  linkIDs.remove(min);

	      
				  LSA minLSA = _store.get(min);
	      
				  //update distance and previous for min, whichever gives smaller distance
				  for (LinkDescription link : minLSA.links) {
	    	
					  int other = distance.get(min) + link.tosMetrics;
	      	
					 if (other < distance.get(link.linkID)) {
	      		
						 distance.put(link.linkID, other);
						 prev.put(link.linkID, min);
	      		
					 }
				  }
			  }
	      
		  }
	  
	  }
	  
	  return null;
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa: _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                append(ld.tosMetrics).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
