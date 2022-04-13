package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;

public interface CachedOutput {
    void writeIfNeeded(Path var1, String var2) throws IOException;

    void writeIfNeeded(Path var1, byte[] var2, String var3) throws IOException;
}
