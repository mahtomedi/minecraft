package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegionFile implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ByteBuffer PADDING_BUFFER = ByteBuffer.allocateDirect(1);
    private final FileChannel file;
    private final Path externalFileDir;
    private final RegionFileVersion version;
    private final ByteBuffer header = ByteBuffer.allocateDirect(8192);
    private final IntBuffer offsets;
    private final IntBuffer timestamps;
    @VisibleForTesting
    protected final RegionBitmap usedSectors = new RegionBitmap();

    public RegionFile(File param0, File param1, boolean param2) throws IOException {
        this(param0.toPath(), param1.toPath(), RegionFileVersion.VERSION_DEFLATE, param2);
    }

    public RegionFile(Path param0, Path param1, RegionFileVersion param2, boolean param3) throws IOException {
        this.version = param2;
        if (!Files.isDirectory(param1)) {
            throw new IllegalArgumentException("Expected directory, got " + param1.toAbsolutePath());
        } else {
            this.externalFileDir = param1;
            this.offsets = this.header.asIntBuffer();
            ((Buffer)this.offsets).limit(1024);
            ((Buffer)this.header).position(4096);
            this.timestamps = this.header.asIntBuffer();
            if (param3) {
                this.file = FileChannel.open(param0, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
            } else {
                this.file = FileChannel.open(param0, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            }

            this.usedSectors.force(0, 2);
            ((Buffer)this.header).position(0);
            int var0 = this.file.read(this.header, 0L);
            if (var0 != -1) {
                if (var0 != 8192) {
                    LOGGER.warn("Region file {} has truncated header: {}", param0, var0);
                }

                long var1 = Files.size(param0);

                for(int var2 = 0; var2 < 1024; ++var2) {
                    int var3 = this.offsets.get(var2);
                    if (var3 != 0) {
                        int var4 = getSectorNumber(var3);
                        int var5 = getNumSectors(var3);
                        if (var4 < 2) {
                            LOGGER.warn("Region file {} has invalid sector at index: {}; sector {} overlaps with header", param0, var2, var4);
                            this.offsets.put(var2, 0);
                        } else if (var5 == 0) {
                            LOGGER.warn("Region file {} has an invalid sector at index: {}; size has to be > 0", param0, var2);
                            this.offsets.put(var2, 0);
                        } else if ((long)var4 * 4096L > var1) {
                            LOGGER.warn("Region file {} has an invalid sector at index: {}; sector {} is out of bounds", param0, var2, var4);
                            this.offsets.put(var2, 0);
                        } else {
                            this.usedSectors.force(var4, var5);
                        }
                    }
                }
            }

        }
    }

    private Path getExternalChunkPath(ChunkPos param0) {
        String var0 = "c." + param0.x + "." + param0.z + ".mcc";
        return this.externalFileDir.resolve(var0);
    }

    @Nullable
    public synchronized DataInputStream getChunkDataInputStream(ChunkPos param0) throws IOException {
        int var0 = this.getOffset(param0);
        if (var0 == 0) {
            return null;
        } else {
            int var1 = getSectorNumber(var0);
            int var2 = getNumSectors(var0);
            int var3 = var2 * 4096;
            ByteBuffer var4 = ByteBuffer.allocate(var3);
            this.file.read(var4, (long)(var1 * 4096));
            ((Buffer)var4).flip();
            if (var4.remaining() < 5) {
                LOGGER.error("Chunk {} header is truncated: expected {} but read {}", param0, var3, var4.remaining());
                return null;
            } else {
                int var5 = var4.getInt();
                byte var6 = var4.get();
                if (var5 == 0) {
                    LOGGER.warn("Chunk {} is allocated, but stream is missing", param0);
                    return null;
                } else {
                    int var7 = var5 - 1;
                    if (isExternalStreamChunk(var6)) {
                        if (var7 != 0) {
                            LOGGER.warn("Chunk has both internal and external streams");
                        }

                        return this.createExternalChunkInputStream(param0, getExternalChunkVersion(var6));
                    } else if (var7 > var4.remaining()) {
                        LOGGER.error("Chunk {} stream is truncated: expected {} but read {}", param0, var7, var4.remaining());
                        return null;
                    } else if (var7 < 0) {
                        LOGGER.error("Declared size {} of chunk {} is negative", var5, param0);
                        return null;
                    } else {
                        return this.createChunkInputStream(param0, var6, createStream(var4, var7));
                    }
                }
            }
        }
    }

    private static boolean isExternalStreamChunk(byte param0) {
        return (param0 & 128) != 0;
    }

    private static byte getExternalChunkVersion(byte param0) {
        return (byte)(param0 & -129);
    }

    @Nullable
    private DataInputStream createChunkInputStream(ChunkPos param0, byte param1, InputStream param2) throws IOException {
        RegionFileVersion var0 = RegionFileVersion.fromId(param1);
        if (var0 == null) {
            LOGGER.error("Chunk {} has invalid chunk stream version {}", param0, param1);
            return null;
        } else {
            return new DataInputStream(new BufferedInputStream(var0.wrap(param2)));
        }
    }

    @Nullable
    private DataInputStream createExternalChunkInputStream(ChunkPos param0, byte param1) throws IOException {
        Path var0 = this.getExternalChunkPath(param0);
        if (!Files.isRegularFile(var0)) {
            LOGGER.error("External chunk path {} is not file", var0);
            return null;
        } else {
            return this.createChunkInputStream(param0, param1, Files.newInputStream(var0));
        }
    }

    private static ByteArrayInputStream createStream(ByteBuffer param0, int param1) {
        return new ByteArrayInputStream(param0.array(), param0.position(), param1);
    }

    private int packSectorOffset(int param0, int param1) {
        return param0 << 8 | param1;
    }

    private static int getNumSectors(int param0) {
        return param0 & 0xFF;
    }

    private static int getSectorNumber(int param0) {
        return param0 >> 8 & 16777215;
    }

    private static int sizeToSectors(int param0) {
        return (param0 + 4096 - 1) / 4096;
    }

    public boolean doesChunkExist(ChunkPos param0) {
        int var0 = this.getOffset(param0);
        if (var0 == 0) {
            return false;
        } else {
            int var1 = getSectorNumber(var0);
            int var2 = getNumSectors(var0);
            ByteBuffer var3 = ByteBuffer.allocate(5);

            try {
                this.file.read(var3, (long)(var1 * 4096));
                ((Buffer)var3).flip();
                if (var3.remaining() != 5) {
                    return false;
                } else {
                    int var4 = var3.getInt();
                    byte var5 = var3.get();
                    if (isExternalStreamChunk(var5)) {
                        if (!RegionFileVersion.isValidVersion(getExternalChunkVersion(var5))) {
                            return false;
                        }

                        if (!Files.isRegularFile(this.getExternalChunkPath(param0))) {
                            return false;
                        }
                    } else {
                        if (!RegionFileVersion.isValidVersion(var5)) {
                            return false;
                        }

                        if (var4 == 0) {
                            return false;
                        }

                        int var6 = var4 - 1;
                        if (var6 < 0 || var6 > 4096 * var2) {
                            return false;
                        }
                    }

                    return true;
                }
            } catch (IOException var9) {
                return false;
            }
        }
    }

    public DataOutputStream getChunkDataOutputStream(ChunkPos param0) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(this.version.wrap(new RegionFile.ChunkBuffer(param0))));
    }

    public void flush() throws IOException {
        this.file.force(true);
    }

    protected synchronized void write(ChunkPos param0, ByteBuffer param1) throws IOException {
        int var0 = getOffsetIndex(param0);
        int var1 = this.offsets.get(var0);
        int var2 = getSectorNumber(var1);
        int var3 = getNumSectors(var1);
        int var4 = param1.remaining();
        int var5 = sizeToSectors(var4);
        int var7;
        RegionFile.CommitOp var8;
        if (var5 >= 256) {
            Path var6 = this.getExternalChunkPath(param0);
            LOGGER.warn("Saving oversized chunk {} ({} bytes} to external file {}", param0, var4, var6);
            var5 = 1;
            var7 = this.usedSectors.allocate(var5);
            var8 = this.writeToExternalFile(var6, param1);
            ByteBuffer var9 = this.createExternalStub();
            this.file.write(var9, (long)(var7 * 4096));
        } else {
            var7 = this.usedSectors.allocate(var5);
            var8 = () -> Files.deleteIfExists(this.getExternalChunkPath(param0));
            this.file.write(param1, (long)(var7 * 4096));
        }

        int var12 = (int)(Util.getEpochMillis() / 1000L);
        this.offsets.put(var0, this.packSectorOffset(var7, var5));
        this.timestamps.put(var0, var12);
        this.writeHeader();
        var8.run();
        if (var2 != 0) {
            this.usedSectors.free(var2, var3);
        }

    }

    private ByteBuffer createExternalStub() {
        ByteBuffer var0 = ByteBuffer.allocate(5);
        var0.putInt(1);
        var0.put((byte)(this.version.getId() | 128));
        ((Buffer)var0).flip();
        return var0;
    }

    private RegionFile.CommitOp writeToExternalFile(Path param0, ByteBuffer param1) throws IOException {
        Path var0 = Files.createTempFile(this.externalFileDir, "tmp", null);

        try (FileChannel var1 = FileChannel.open(var0, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ((Buffer)param1).position(5);
            var1.write(param1);
        }

        return () -> Files.move(var0, param0, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeHeader() throws IOException {
        ((Buffer)this.header).position(0);
        this.file.write(this.header, 0L);
    }

    private int getOffset(ChunkPos param0) {
        return this.offsets.get(getOffsetIndex(param0));
    }

    public boolean hasChunk(ChunkPos param0) {
        return this.getOffset(param0) != 0;
    }

    private static int getOffsetIndex(ChunkPos param0) {
        return param0.getRegionLocalX() + param0.getRegionLocalZ() * 32;
    }

    @Override
    public void close() throws IOException {
        try {
            this.padToFullSector();
        } finally {
            try {
                this.file.force(true);
            } finally {
                this.file.close();
            }
        }

    }

    private void padToFullSector() throws IOException {
        int var0 = (int)this.file.size();
        int var1 = sizeToSectors(var0) * 4096;
        if (var0 != var1) {
            ByteBuffer var2 = PADDING_BUFFER.duplicate();
            ((Buffer)var2).position(0);
            this.file.write(var2, (long)(var1 - 1));
        }

    }

    class ChunkBuffer extends ByteArrayOutputStream {
        private final ChunkPos pos;

        public ChunkBuffer(ChunkPos param0) {
            super(8096);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(RegionFile.this.version.getId());
            this.pos = param0;
        }

        @Override
        public void close() throws IOException {
            ByteBuffer var0 = ByteBuffer.wrap(this.buf, 0, this.count);
            var0.putInt(0, this.count - 5 + 1);
            RegionFile.this.write(this.pos, var0);
        }
    }

    interface CommitOp {
        void run() throws IOException;
    }
}
