package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Path;

public interface CachedOutput {
    void writeIfNeeded(Path var1, byte[] var2, HashCode var3) throws IOException;
}
