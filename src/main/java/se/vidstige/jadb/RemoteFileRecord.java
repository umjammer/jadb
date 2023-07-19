package se.vidstige.jadb;

import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by vidstige on 2014-03-19.
 */
class RemoteFileRecord extends RemoteFile {
    public static final RemoteFileRecord DONE = new RemoteFileRecord(null, 0, 0, 0);

    private final int mode;
    private final int size;
    private final int lastModified;

    public RemoteFileRecord(String name, int mode, int size, int lastModified) {
        super(name);
        this.mode = mode;
        this.size = size;
        this.lastModified = lastModified;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getLastModified() {
        return lastModified;
    }

    @Override
    public boolean isDirectory() {
        return (mode & (1 << 14)) == (1 << 14);
    }

    @Override
    public int getMode() { return mode; }

    @Override
    public Set<PosixFilePermission> permissions() {
        int bits = mode & 0777;
        HashSet<PosixFilePermission> perms = new HashSet<>();

        if ((bits & 0400) > 0)
            perms.add(PosixFilePermission.OWNER_READ);
        if ((bits & 0200) > 0)
            perms.add(PosixFilePermission.OWNER_WRITE);
        if ((bits & 0100) > 0)
            perms.add(PosixFilePermission.OWNER_EXECUTE);

        if ((bits & 0040) > 0)
            perms.add(PosixFilePermission.GROUP_READ);
        if ((bits & 0020) > 0)
            perms.add(PosixFilePermission.GROUP_WRITE);
        if ((bits & 0010) > 0)
            perms.add(PosixFilePermission.GROUP_EXECUTE);

        if ((bits & 0004) > 0)
            perms.add(PosixFilePermission.OTHERS_READ);
        if ((bits & 0002) > 0)
            perms.add(PosixFilePermission.OTHERS_WRITE);
        if ((bits & 0001) > 0)
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

        return perms;
    }
}
