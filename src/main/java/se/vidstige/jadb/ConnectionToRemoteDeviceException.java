package se.vidstige.jadb;

import java.io.IOException;


public class ConnectionToRemoteDeviceException extends IOException {
    public ConnectionToRemoteDeviceException(String message) {
        super(message);
    }
}
