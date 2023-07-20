package se.vidstige.jadb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;


/**
 * Created by vidstige on 2014-03-19.
 */
public class SyncTransport {

    private static final Logger logger = Logger.getLogger(SyncTransport.class.getName());

    private final DataOutput output;
    private final DataInput input;

    public SyncTransport(DataOutput outputStream, DataInput inputStream) {
        output = outputStream;
        input = inputStream;
    }

    public void send(String syncCommand, String name) throws IOException {
        if (syncCommand.length() != 4) throw new IllegalArgumentException("sync commands must have length 4");
        output.writeBytes(syncCommand);
        byte[] data = name.getBytes(StandardCharsets.UTF_8);
        output.writeInt(Integer.reverseBytes(data.length));
        output.write(data);
    }

    public void sendStatus(String statusCode, int length) throws IOException {
        output.writeBytes(statusCode);
        output.writeInt(Integer.reverseBytes(length));
    }

    public void verifyStatus() throws IOException {
        String status = readString(4);
        int length = readInt();
        if ("FAIL".equals(status)) {
            String error = readString(length);
            throw new JadbException(error);
        }
        if (!"OKAY".equals(status)) {
            throw new JadbException("Unknown error: " + status);
        }
    }

    private int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    private String readString(int length) throws IOException {
        byte[] buffer = new byte[length];
        input.readFully(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public void sendDirectoryEntry(RemoteFile file) throws IOException {
        output.writeBytes("DENT");
        output.writeInt(Integer.reverseBytes(0666 | (file.isDirectory() ? (1 << 14) : 0)));
        output.writeInt(Integer.reverseBytes(file.getSize()));
        output.writeInt(Integer.reverseBytes(file.getLastModified()));
        byte[] pathChars = file.getPath().getBytes(StandardCharsets.UTF_8);
        output.writeInt(Integer.reverseBytes(pathChars.length));
        output.write(pathChars);
    }

    public void sendDirectoryEntryDone() throws IOException {
        output.writeBytes("DONE");
        output.writeBytes("\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"); // equivalent to the length of a "normal" dent
    }

    public RemoteFileRecord readDirectoryEntry(String path) throws IOException {
        String id = readString(4);
        int mode = readInt();
        int size = readInt();
        int time = readInt();
        int nameLength = readInt();
        String name = readString(nameLength);

        if (!"DENT".equals(id)) return RemoteFileRecord.DONE;
        return new RemoteFileRecord(path + (path.length() > 0 && path.charAt(path.length() - 1) == '/' ? "" : "/") + name, mode, size, time);
    }

    private void sendChunk(byte[] buffer, int offset, int length) throws IOException {
        output.writeBytes("DATA");
        output.writeInt(Integer.reverseBytes(length));
        output.write(buffer, offset, length);
    }

    private int readChunk(byte[] buffer) throws IOException {
        String id = readString(4);
        int n = readInt();
        if ("FAIL".equals(id)) {
            throw new JadbException(readString(n));
        }
        if (!"DATA".equals(id)) return -1;
        input.readFully(buffer, 0, n);
        return n;
    }

    public void sendStream(InputStream in) throws IOException {
        byte[] buffer = new byte[1024 * 64];
        int n = in.read(buffer);
        while (n != -1) {
            sendChunk(buffer, 0, n);
            n = in.read(buffer);
        }
    }

    public OutputStream sendStream(Runnable onClose) {
        return new OutputStream() {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                sendChunk(b, off, len);
            }

            @Override
            public void write(int b) throws IOException {
                byte[] buf = new byte[] { (byte) b };
                write(buf, 0, 1);
            }

            @Override
            public void close() throws IOException {
                onClose.run();
            }
        };
    }

    public void readChunksTo(OutputStream stream) throws IOException {
        byte[] buffer = new byte[1024 * 64];
        int n = readChunk(buffer);
        while (n != -1) {
            stream.write(buffer, 0, n);
            n = readChunk(buffer);
        }
    }

    private int readChunk(Deque<Byte> deque) throws IOException {
        String id = readString(4);
        int n = readInt();
        if ("FAIL".equals(id)) {
            throw new JadbException(readString(n));
        }
        if (!"DATA".equals(id)) return -1;
        byte[] buffer = new byte[n];
        input.readFully(buffer, 0, n);
        for (int i = 0; i < n; i++) {
            deque.offer(buffer[i]);
        }
logger.finest("readChunk: " + n + "/" + deque.size());
        return n;
    }

    public InputStream readChunks(Runnable onClose) {
        return new InputStream() {
            Deque<Byte> deque = new LinkedList<>();
            boolean eof;
            void readInternal() throws IOException {
                if (!eof) {
                    int n = readChunk(deque);
                    if (n == -1) {
                        eof = true;
                    }
                }
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                readInternal();
                if (deque.size() == 0) {
                    return -1;
                }
                int i;
                for (i = 0; i < Math.min(len, deque.size()); i++) {
                    if (deque.peek() == null) {
                        break;
                    }
                    b[off + i] = deque.poll();
                }
logger.finest("read: " + i + "/" + deque.size());
                return i;
            }

            @Override
            public int read() throws IOException {
                readInternal();
                if (deque.size() == 0) {
                    return -1;
                }
                if (deque.peek() == null) {
                    return -1;
                }
logger.finest("read: 1/" + deque.size());
                return deque.poll();
            }

            @Override
            public void close() throws IOException {
                onClose.run();
            }
        };
    }
}
