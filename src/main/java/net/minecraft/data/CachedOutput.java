package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.FileUtil;

public interface CachedOutput {
    CachedOutput NO_CACHE = (param0, param1, param2) -> {
        FileUtil.createDirectoriesSafe(param0.getParent());
        Files.write(param0, param1);
    };

    void writeIfNeeded(Path var1, byte[] var2, HashCode var3) throws IOException;
}
