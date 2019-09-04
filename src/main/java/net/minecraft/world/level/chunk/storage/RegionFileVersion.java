package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;

public class RegionFileVersion {
    private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap<>();
    public static final RegionFileVersion VERSION_GZIP = register(new RegionFileVersion(1, GZIPInputStream::new, GZIPOutputStream::new));
    public static final RegionFileVersion VERSION_DEFLATE = register(new RegionFileVersion(2, InflaterInputStream::new, DeflaterOutputStream::new));
    public static final RegionFileVersion VERSION_NONE = register(new RegionFileVersion(3, param0 -> param0, param0 -> param0));
    private final int id;
    private final RegionFileVersion.StreamWrapper<InputStream> inputWrapper;
    private final RegionFileVersion.StreamWrapper<OutputStream> outputWrapper;

    private RegionFileVersion(int param0, RegionFileVersion.StreamWrapper<InputStream> param1, RegionFileVersion.StreamWrapper<OutputStream> param2) {
        this.id = param0;
        this.inputWrapper = param1;
        this.outputWrapper = param2;
    }

    private static RegionFileVersion register(RegionFileVersion param0) {
        VERSIONS.put(param0.id, param0);
        return param0;
    }

    @Nullable
    public static RegionFileVersion fromId(int param0) {
        return VERSIONS.get(param0);
    }

    public static boolean isValidVersion(int param0) {
        return VERSIONS.containsKey(param0);
    }

    public int getId() {
        return this.id;
    }

    public OutputStream wrap(OutputStream param0) throws IOException {
        return this.outputWrapper.wrap(param0);
    }

    public InputStream wrap(InputStream param0) throws IOException {
        return this.inputWrapper.wrap(param0);
    }

    @FunctionalInterface
    interface StreamWrapper<O> {
        O wrap(O var1) throws IOException;
    }
}
