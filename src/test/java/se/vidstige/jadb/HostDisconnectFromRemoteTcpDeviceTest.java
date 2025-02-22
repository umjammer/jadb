package se.vidstige.jadb;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HostDisconnectFromRemoteTcpDeviceTest {

    @Test
    public void testNormalConnection() throws IOException {
        // Prepare
        Transport transport = mock(Transport.class);
        when(transport.readString()).thenReturn("disconnected host:1");

        InetSocketAddress inetSocketAddress = new InetSocketAddress("host", 1);

        // Do
        HostDisconnectFromRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostDisconnectFromRemoteTcpDevice(transport);
        InetSocketAddress resultInetSocketAddress = hostConnectToRemoteTcpDevice.disconnect(inetSocketAddress);

        // Validate
        assertEquals(inetSocketAddress, resultInetSocketAddress);
    }

    @Test
    public void testTransportLevelException() {
        assertThrows(JadbException.class, () -> {
            // Prepare
            Transport transport = mock(Transport.class);
            doThrow(new JadbException("Fake exception")).when(transport).verifyResponse();

            InetSocketAddress inetSocketAddress = new InetSocketAddress("host", 1);

            // Do
            HostDisconnectFromRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostDisconnectFromRemoteTcpDevice(transport);
            hostConnectToRemoteTcpDevice.disconnect(inetSocketAddress);
        });
    }

    @Test
    public void testProtocolException() {
        assertThrows(ConnectionToRemoteDeviceException.class, () -> {
            // Prepare
            Transport transport = mock(Transport.class);
            when(transport.readString()).thenReturn("any string");
            HostDisconnectFromRemoteTcpDevice.ResponseValidator responseValidator = mock(HostConnectionCommand.ResponseValidator.class);
            doThrow(new ConnectionToRemoteDeviceException("Fake exception")).when(responseValidator).validate(anyString());

            InetSocketAddress inetSocketAddress = new InetSocketAddress("host", 1);

            // Do
            HostDisconnectFromRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostDisconnectFromRemoteTcpDevice(transport, responseValidator);
            hostConnectToRemoteTcpDevice.disconnect(inetSocketAddress);
        });
    }

    @Test
    public void testProtocolResponseValidatorSuccessfullyConnected() throws IOException {
        new HostDisconnectFromRemoteTcpDevice.ResponseValidatorImp().validate("disconnected 127.0.0.1:10001");
    }

    @Test
    public void testProtocolResponseValidatorAlreadyConnected() throws IOException {
        new HostDisconnectFromRemoteTcpDevice.ResponseValidatorImp().validate("error: no such device '127.0.0.1:10001'");
    }

    @Test
    public void testProtocolResponseValidatorErrorInValidate() {
        assertThrows(ConnectionToRemoteDeviceException.class, () -> {
            new HostDisconnectFromRemoteTcpDevice.ResponseValidatorImp().validate("some error occurred");
        });
    }
}
