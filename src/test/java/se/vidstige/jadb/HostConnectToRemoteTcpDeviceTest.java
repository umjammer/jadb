package se.vidstige.jadb;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HostConnectToRemoteTcpDeviceTest {

    @Test
    public void testNormalConnection() throws ConnectionToRemoteDeviceException, IOException, JadbException {
        // Prepare
        Transport transport = mock(Transport.class);
        when(transport.readString()).thenReturn("connected to somehost:1");

        InetSocketAddress inetSocketAddress = new InetSocketAddress("somehost", 1);

        // Do
        HostConnectToRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostConnectToRemoteTcpDevice(transport);
        InetSocketAddress resultTcpAddressEntity = hostConnectToRemoteTcpDevice.connect(inetSocketAddress);

        // Validate
        assertEquals(resultTcpAddressEntity, inetSocketAddress);

        ArgumentCaptor<String> argument = forClass(String.class);
        verify(transport, times(1)).send(argument.capture());
        assertEquals("host:connect:somehost:1", argument.getValue());
    }

    @Test
    public void testTransportLevelException() {
        assertThrows(JadbException.class, () -> {
            // Prepare
            Transport transport = mock(Transport.class);
            doThrow(new JadbException("Fake exception")).when(transport).verifyResponse();

            InetSocketAddress tcpAddressEntity = new InetSocketAddress("somehost", 1);

            // Do
            HostConnectToRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostConnectToRemoteTcpDevice(transport);
            hostConnectToRemoteTcpDevice.connect(tcpAddressEntity);

            // Validate
            verify(transport, times(1)).send(anyString());
            verify(transport, times(1)).verifyResponse();
        });
    }

    @Test
    public void testProtocolException() {
        assertThrows(ConnectionToRemoteDeviceException.class, () -> {
            // Prepare
            Transport transport = mock(Transport.class);
            when(transport.readString()).thenReturn("connected to somehost:1");
            HostConnectToRemoteTcpDevice.ResponseValidator responseValidator = mock(HostConnectionCommand.ResponseValidator.class);
            doThrow(new ConnectionToRemoteDeviceException("Fake exception")).when(responseValidator).validate(anyString());

            InetSocketAddress tcpAddressEntity = new InetSocketAddress("somehost", 1);

            // Do
            HostConnectToRemoteTcpDevice hostConnectToRemoteTcpDevice = new HostConnectToRemoteTcpDevice(transport, responseValidator);
            hostConnectToRemoteTcpDevice.connect(tcpAddressEntity);

            // Validate
            verify(transport, times(1)).send(anyString());
            verify(transport, times(1)).verifyResponse();
            verify(responseValidator, times(1)).validate(anyString());
        });
    }

    @Test
    public void testProtocolResponseValidatorSuccessfullyConnected() throws IOException {
       new HostConnectToRemoteTcpDevice.ResponseValidatorImp().validate("connected to somehost:1");
    }

    @Test
    public void testProtocolResponseValidatorAlreadyConnected() throws IOException {
        new HostConnectToRemoteTcpDevice.ResponseValidatorImp().validate("already connected to somehost:1");
    }

    @Test
    public void testProtocolResponseValidatorErrorInValidate() {
        assertThrows(ConnectionToRemoteDeviceException.class, () -> {
            new HostConnectToRemoteTcpDevice.ResponseValidatorImp().validate("some error occurred");
        });
    }
}