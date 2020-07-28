package socs.network.node;

import socs.network.message.ErrorLink;

public class Link {

  RouterDescription router1;
  RouterDescription router2;
  short weight;

  public Link(RouterDescription r1, RouterDescription r2) throws ErrorLink {
    if(r1.equals(r2)) throw new ErrorLink("A port cannot attach to itself.");

    router1 = r1;
    router2 = r2;
  }
}