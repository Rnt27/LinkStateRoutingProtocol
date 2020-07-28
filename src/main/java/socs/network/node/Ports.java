package socs.network.node;

import socs.network.message.ErrorConnection;
import socs.network.message.NoPortAvailable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ports implements Iterable<Link> {

    private Link[] ports;
    private int count;
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    public Ports(int num) {
         ports = new Link[num];
         count = 0;
    }

    public int find(RouterDescription router) {
        rwlock.readLock().lock();
        try {
            PortsIterator iter = new PortsIterator();
            Link l;

            while (iter.hasNext()) {
                l = iter.next();

                if(l.router2.equals(router)) {
                    return iter.nextIndex();
                }
            }
            return -1;
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public boolean addLink(Link link) throws ErrorConnection, NoPortAvailable {
        rwlock.writeLock().lock();
        try {
            if(find(link.router2) > -1) throw new ErrorConnection("You are already attached to that router.");

            for (int i = 0; i < ports.length; i++) {
                //if there are an empty port
                if (ports[i] == null) {
                    ports[i] = link;
                    count++;
                    return true;
                }
            }

            throw new NoPortAvailable("No ports available.");
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    public void removeLink(Link link) {
        rwlock.writeLock().lock();
        try {
            int position = find(link.router2);
            if(position > 0) {
                ports[position] = null;
                count--;
            }
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    public RouterDescription getRouterDescription(String IPAddress) {
        rwlock.readLock().lock();
        try {
            PortsIterator iter = new PortsIterator();
            Link l;
            while (iter.hasNext()) {
                l = iter.next();
                if (l.router2.simulatedIPAddress.equals(IPAddress)) {
                    return l.router2;
                }
            }
            return null;
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public List<String> getNeighbors() {
        rwlock.readLock().lock();
        try {
            List<String> neighbors = new ArrayList<String>();
            PortsIterator iter = new PortsIterator();
            Link link;
            while (iter.hasNext()) {
                link = iter.next();
                if (link.router2.status == RouterStatus.TWO_WAY) neighbors.add(link.router2.simulatedIPAddress);
            }

            return neighbors;
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public boolean isNeighbor(String IPAddress) {
        for (String neighbor : getNeighbors()) {
            if (neighbor.equals(IPAddress)) return true;
        }
        return false;
    }

    @Override
    public Iterator<Link> iterator() {
        return new PortsIterator();
    }

    private class PortsIterator implements Iterator {
        private int position = 0;

        public boolean hasNext() {
            Link link = null;
            while(position < ports.length && ports[position] == null) position++;
            if(position < ports.length)
                return true;
            else
                return false;
        }

        public Link next() {
            if (hasNext())
                return ports[position++];
            else
                return null;
        }

        public int nextIndex() {
            return position;
        }

        @Override
        public void remove() {
        }
    }
}