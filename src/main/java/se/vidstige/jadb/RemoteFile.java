package se.vidstige.jadb;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;


/**
 * Created by vidstige on 2014-03-20
 */
public class RemoteFile {
    private final String path;

    public RemoteFile(String path) { this.path = path; }

    public String getName() { throw new UnsupportedOperationException(); }
    public int getSize() { throw new UnsupportedOperationException(); }
    public int getLastModified() { throw new UnsupportedOperationException(); }
    public boolean isDirectory() { throw new UnsupportedOperationException(); }

    public int getMode() { throw new UnsupportedOperationException(); }

    public Set<PosixFilePermission> permissions() { throw new UnsupportedOperationException(); }

    public String getPath() { return path;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteFile that = (RemoteFile) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
