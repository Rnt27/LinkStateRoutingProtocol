package socs.network.message;

public class ErrorConnection extends Exception {
    public ErrorConnection(String errorMessage) {
        super(errorMessage);
    }
}