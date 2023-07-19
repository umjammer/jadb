package se.vidstige.jadb;

import java.io.IOException;


public class JadbException extends IOException {

    public JadbException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -3879283786835654165L;
}
