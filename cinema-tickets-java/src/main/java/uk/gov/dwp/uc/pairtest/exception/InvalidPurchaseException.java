package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {

    public InvalidPurchaseException(String s) {
        super(s); // Passes the message to the RuntimeException class
    }
}
