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

    /**
     * @param mode see "Encoding of the file mode." in
     *            <a href="https://android.googlesource.com/platform/prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.7-4.6/+/refs/heads/tools_r20/sysroot/usr/include/bits/stat.h">...</a>
     */
    public RemoteFileRecord(String path, int mode, int size, int lastModified) {
        super(path);
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
    public boolean isRegularFile() {
        return (mode & 0100000) == 0100000;
    }

    @Override
    public boolean isDirectory() {
        return (mode & 0040000) == 0040000;
    }

    @Override
    public boolean isSymbolicLink() {
        return (mode & 0120000) == 0120000;
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
