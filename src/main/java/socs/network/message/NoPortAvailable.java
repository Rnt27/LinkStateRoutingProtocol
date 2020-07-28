package socs.network.message;

public class NoPortAvailable extends Exception {
    public NoPortAvailable(String errorMessage) {
        super(errorMessage);
    }
}