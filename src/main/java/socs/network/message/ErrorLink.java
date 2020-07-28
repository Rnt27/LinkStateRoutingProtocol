package socs.network.message;

public class ErrorLink extends Exception {
    public ErrorLink(String errorMessage) {
        super(errorMessage);
    }
}