package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;

public class RegionFile implements AutoCloseable {
    private static final byte[] EMPTY_SECTOR = new byte[4096];
    private final RandomAccessFile file;
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private final List<Boolean> sectorFree;

    public RegionFile(File param0) throws IOException {
        this.file = new RandomAccessFile(param0, "rw");
        if (this.file.length() < 4096L) {
            this.file.write(EMPTY_SECTOR);
            this.file.write(EMPTY_SECTOR);
        }

        if ((this.file.length() & 4095L) != 0L) {
            for(int var0 = 0; (long)var0 < (this.file.length() & 4095L); ++var0) {
                this.file.write(0);
            }
        }

        int var1 = (int)this.file.length() / 4096;
        this.sectorFree = Lists.newArrayListWithCapacity(var1);

        for(int var2 = 0; var2 < var1; ++var2) {
            this.sectorFree.add(true);
        }

        this.sectorFree.set(0, false);
        this.sectorFree.set(1, false);
        this.file.seek(0L);

        for(int var3 = 0; var3 < 1024; ++var3) {
            int var4 = this.file.readInt();
            this.offsets[var3] = var4;
            if (var4 != 0 && (var4 >> 8) + (var4 & 0xFF) <= this.sectorFree.size()) {
                for(int var5 = 0; var5 < (var4 & 0xFF); ++var5) {
                    this.sectorFree.set((var4 >> 8) + var5, false);
                }
            }
        }

        for(int var6 = 0; var6 < 1024; ++var6) {
            int var7 = this.file.readInt();
            this.chunkTimestamps[var6] = var7;
        }

    }

    @Nullable
    public synchronized DataInputStream getChunkDataInputStream(ChunkPos param0) throws IOException {
        int var0 = this.getOffset(param0);
        if (var0 == 0) {
            return null;
        } else {
            int var1 = var0 >> 8;
            int var2 = var0 & 0xFF;
            if (var1 + var2 > this.sectorFree.size()) {
                return null;
            } else {
                this.file.seek((long)(var1 * 4096));
                int var3 = this.file.readInt();
                if (var3 > 4096 * var2) {
                    return null;
                } else if (var3 <= 0) {
                    return null;
                } else {
                    byte var4 = this.file.readByte();
                    if (var4 == 1) {
                        byte[] var5 = new byte[var3 - 1];
                        this.file.read(var5);
                        return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(var5))));
                    } else if (var4 == 2) {
                        byte[] var6 = new byte[var3 - 1];
                        this.file.read(var6);
                        return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(var6))));
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    public boolean doesChunkExist(ChunkPos param0) {
        int var0 = this.getOffset(param0);
        if (var0 == 0) {
            return false;
        } else {
            int var1 = var0 >> 8;
            int var2 = var0 & 0xFF;
            if (var1 + var2 > this.sectorFree.size()) {
                return false;
            } else {
                try {
                    this.file.seek((long)(var1 * 4096));
                    int var3 = this.file.readInt();
                    if (var3 > 4096 * var2) {
                        return false;
                    } else {
                        return var3 > 0;
                    }
                } catch (IOException var6) {
                    return false;
                }
            }
        }
    }

    public DataOutputStream getChunkDataOutputStream(ChunkPos param0) {
        return new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new RegionFile.ChunkBuffer(param0))));
    }

    protected synchronized void write(ChunkPos param0, byte[] param1, int param2) throws IOException {
        int var0 = this.getOffset(param0);
        int var1 = var0 >> 8;
        int var2 = var0 & 0xFF;
        int var3 = (param2 + 5) / 4096 + 1;
        if (var3 >= 256) {
            throw new RuntimeException(String.format("Too big to save, %d > 1048576", param2));
        } else {
            if (var1 != 0 && var2 == var3) {
                this.write(var1, param1, param2);
            } else {
                for(int var4 = 0; var4 < var2; ++var4) {
                    this.sectorFree.set(var1 + var4, true);
                }

                int var5 = this.sectorFree.indexOf(true);
                int var6 = 0;
                if (var5 != -1) {
                    for(int var7 = var5; var7 < this.sectorFree.size(); ++var7) {
                        if (var6 != 0) {
                            if (this.sectorFree.get(var7)) {
                                ++var6;
                            } else {
                                var6 = 0;
                            }
                        } else if (this.sectorFree.get(var7)) {
                            var5 = var7;
                            var6 = 1;
                        }

                        if (var6 >= var3) {
                            break;
                        }
                    }
                }

                if (var6 >= var3) {
                    var1 = var5;
                    this.setOffset(param0, var5 << 8 | var3);

                    for(int var8 = 0; var8 < var3; ++var8) {
                        this.sectorFree.set(var1 + var8, false);
                    }

                    this.write(var1, param1, param2);
                } else {
                    this.file.seek(this.file.length());
                    var1 = this.sectorFree.size();

                    for(int var9 = 0; var9 < var3; ++var9) {
                        this.file.write(EMPTY_SECTOR);
                        this.sectorFree.add(false);
                    }

                    this.write(var1, param1, param2);
                    this.setOffset(param0, var1 << 8 | var3);
                }
            }

            this.setTimestamp(param0, (int)(Util.getEpochMillis() / 1000L));
        }
    }

    private void write(int param0, byte[] param1, int param2) throws IOException {
        this.file.seek((long)(param0 * 4096));
        this.file.writeInt(param2 + 1);
        this.file.writeByte(2);
        this.file.write(param1, 0, param2);
    }

    private int getOffset(ChunkPos param0) {
        return this.offsets[this.getOffsetIndex(param0)];
    }

    public boolean hasChunk(ChunkPos param0) {
        return this.getOffset(param0) != 0;
    }

    private void setOffset(ChunkPos param0, int param1) throws IOException {
        int var0 = this.getOffsetIndex(param0);
        this.offsets[var0] = param1;
        this.file.seek((long)(var0 * 4));
        this.file.writeInt(param1);
    }

    private int getOffsetIndex(ChunkPos param0) {
        return param0.getRegionLocalX() + param0.getRegionLocalZ() * 32;
    }

    private void setTimestamp(ChunkPos param0, int param1) throws IOException {
        int var0 = this.getOffsetIndex(param0);
        this.chunkTimestamps[var0] = param1;
        this.file.seek((long)(4096 + var0 * 4));
        this.file.writeInt(param1);
    }

    @Override
    public void close() throws IOException {
        this.file.close();
    }

    class ChunkBuffer extends ByteArrayOutputStream {
        private final ChunkPos pos;

        public ChunkBuffer(ChunkPos param0) {
            super(8096);
            this.pos = param0;
        }

        @Override
        public void close() throws IOException {
            RegionFile.this.write(this.pos, this.buf, this.count);
        }
    }
}
