package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
    private static final int GROWTH_SIZE = 2097152;
    private static final Logger LOGGER = LogUtils.getLogger();
    private ByteBuffer buffer;
    private final List<BufferBuilder.DrawState> drawStates = Lists.newArrayList();
    private int lastPoppedStateIndex;
    private int totalRenderedBytes;
    private int nextElementByte;
    private int totalUploadedBytes;
    private int vertices;
    @Nullable
    private VertexFormatElement currentElement;
    private int elementIndex;
    private VertexFormat format;
    private VertexFormat.Mode mode;
    private boolean fastFormat;
    private boolean fullFormat;
    private boolean building;
    @Nullable
    private Vector3f[] sortingPoints;
    private float sortX = Float.NaN;
    private float sortY = Float.NaN;
    private float sortZ = Float.NaN;
    private boolean indexOnly;

    public BufferBuilder(int param0) {
        this.buffer = MemoryTracker.create(param0 * 6);
    }

    private void ensureVertexCapacity() {
        this.ensureCapacity(this.format.getVertexSize());
    }

    private void ensureCapacity(int param0) {
        if (this.nextElementByte + param0 > this.buffer.capacity()) {
            int var0 = this.buffer.capacity();
            int var1 = var0 + roundUp(param0);
            LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", var0, var1);
            ByteBuffer var2 = MemoryTracker.resize(this.buffer, var1);
            var2.rewind();
            this.buffer = var2;
        }
    }

    private static int roundUp(int param0) {
        int var0 = 2097152;
        if (param0 == 0) {
            return var0;
        } else {
            if (param0 < 0) {
                var0 *= -1;
            }

            int var1 = param0 % var0;
            return var1 == 0 ? param0 : param0 + var0 - var1;
        }
    }

    public void setQuadSortOrigin(float param0, float param1, float param2) {
        if (this.mode == VertexFormat.Mode.QUADS) {
            if (this.sortX != param0 || this.sortY != param1 || this.sortZ != param2) {
                this.sortX = param0;
                this.sortY = param1;
                this.sortZ = param2;
                if (this.sortingPoints == null) {
                    this.sortingPoints = this.makeQuadSortingPoints();
                }
            }

        }
    }

    public BufferBuilder.SortState getSortState() {
        return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY, this.sortZ);
    }

    public void restoreSortState(BufferBuilder.SortState param0) {
        this.buffer.clear();
        this.mode = param0.mode;
        this.vertices = param0.vertices;
        this.nextElementByte = this.totalRenderedBytes;
        this.sortingPoints = param0.sortingPoints;
        this.sortX = param0.sortX;
        this.sortY = param0.sortY;
        this.sortZ = param0.sortZ;
        this.indexOnly = true;
    }

    public void begin(VertexFormat.Mode param0, VertexFormat param1) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        } else {
            this.building = true;
            this.mode = param0;
            this.switchFormat(param1);
            this.currentElement = param1.getElements().get(0);
            this.elementIndex = 0;
            this.buffer.clear();
        }
    }

    private void switchFormat(VertexFormat param0) {
        if (this.format != param0) {
            this.format = param0;
            boolean var0 = param0 == DefaultVertexFormat.NEW_ENTITY;
            boolean var1 = param0 == DefaultVertexFormat.BLOCK;
            this.fastFormat = var0 || var1;
            this.fullFormat = var0;
        }
    }

    private IntConsumer intConsumer(VertexFormat.IndexType param0) {
        switch(param0) {
            case BYTE:
                return param0x -> this.buffer.put((byte)param0x);
            case SHORT:
                return param0x -> this.buffer.putShort((short)param0x);
            case INT:
            default:
                return param0x -> this.buffer.putInt(param0x);
        }
    }

    private Vector3f[] makeQuadSortingPoints() {
        FloatBuffer var0 = this.buffer.asFloatBuffer();
        int var1 = this.totalRenderedBytes / 4;
        int var2 = this.format.getIntegerSize();
        int var3 = var2 * this.mode.primitiveStride;
        int var4 = this.vertices / this.mode.primitiveStride;
        Vector3f[] var5 = new Vector3f[var4];

        for(int var6 = 0; var6 < var4; ++var6) {
            float var7 = var0.get(var1 + var6 * var3 + 0);
            float var8 = var0.get(var1 + var6 * var3 + 1);
            float var9 = var0.get(var1 + var6 * var3 + 2);
            float var10 = var0.get(var1 + var6 * var3 + var2 * 2 + 0);
            float var11 = var0.get(var1 + var6 * var3 + var2 * 2 + 1);
            float var12 = var0.get(var1 + var6 * var3 + var2 * 2 + 2);
            float var13 = (var7 + var10) / 2.0F;
            float var14 = (var8 + var11) / 2.0F;
            float var15 = (var9 + var12) / 2.0F;
            var5[var6] = new Vector3f(var13, var14, var15);
        }

        return var5;
    }

    private void putSortedQuadIndices(VertexFormat.IndexType param0) {
        float[] var0 = new float[this.sortingPoints.length];
        int[] var1 = new int[this.sortingPoints.length];

        for(int var2 = 0; var2 < this.sortingPoints.length; var1[var2] = var2++) {
            float var3 = this.sortingPoints[var2].x() - this.sortX;
            float var4 = this.sortingPoints[var2].y() - this.sortY;
            float var5 = this.sortingPoints[var2].z() - this.sortZ;
            var0[var2] = var3 * var3 + var4 * var4 + var5 * var5;
        }

        IntArrays.mergeSort(var1, (param1, param2) -> Floats.compare(var0[param2], var0[param1]));
        IntConsumer var6 = this.intConsumer(param0);
        this.buffer.position(this.nextElementByte);

        for(int var7 : var1) {
            var6.accept(var7 * this.mode.primitiveStride + 0);
            var6.accept(var7 * this.mode.primitiveStride + 1);
            var6.accept(var7 * this.mode.primitiveStride + 2);
            var6.accept(var7 * this.mode.primitiveStride + 2);
            var6.accept(var7 * this.mode.primitiveStride + 3);
            var6.accept(var7 * this.mode.primitiveStride + 0);
        }

    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        } else {
            int var0 = this.mode.indexCount(this.vertices);
            VertexFormat.IndexType var1 = VertexFormat.IndexType.least(var0);
            boolean var3;
            if (this.sortingPoints != null) {
                int var2 = Mth.roundToward(var0 * var1.bytes, 4);
                this.ensureCapacity(var2);
                this.putSortedQuadIndices(var1);
                var3 = false;
                this.nextElementByte += var2;
                this.totalRenderedBytes += this.vertices * this.format.getVertexSize() + var2;
            } else {
                var3 = true;
                this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
            }

            this.building = false;
            this.drawStates.add(new BufferBuilder.DrawState(this.format, this.vertices, var0, this.mode, var1, this.indexOnly, var3));
            this.vertices = 0;
            this.currentElement = null;
            this.elementIndex = 0;
            this.sortingPoints = null;
            this.sortX = Float.NaN;
            this.sortY = Float.NaN;
            this.sortZ = Float.NaN;
            this.indexOnly = false;
        }
    }

    @Override
    public void putByte(int param0, byte param1) {
        this.buffer.put(this.nextElementByte + param0, param1);
    }

    @Override
    public void putShort(int param0, short param1) {
        this.buffer.putShort(this.nextElementByte + param0, param1);
    }

    @Override
    public void putFloat(int param0, float param1) {
        this.buffer.putFloat(this.nextElementByte + param0, param1);
    }

    @Override
    public void endVertex() {
        if (this.elementIndex != 0) {
            throw new IllegalStateException("Not filled all elements of the vertex");
        } else {
            ++this.vertices;
            this.ensureVertexCapacity();
            if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
                int var0 = this.format.getVertexSize();
                this.buffer.position(this.nextElementByte);
                ByteBuffer var1 = this.buffer.duplicate();
                var1.position(this.nextElementByte - var0).limit(this.nextElementByte);
                this.buffer.put(var1);
                this.nextElementByte += var0;
                ++this.vertices;
                this.ensureVertexCapacity();
            }

        }
    }

    @Override
    public void nextElement() {
        ImmutableList<VertexFormatElement> var0 = this.format.getElements();
        this.elementIndex = (this.elementIndex + 1) % var0.size();
        this.nextElementByte += this.currentElement.getByteSize();
        VertexFormatElement var1 = var0.get(this.elementIndex);
        this.currentElement = var1;
        if (var1.getUsage() == VertexFormatElement.Usage.PADDING) {
            this.nextElement();
        }

        if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
            BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
        }

    }

    @Override
    public VertexConsumer color(int param0, int param1, int param2, int param3) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        } else {
            return BufferVertexConsumer.super.color(param0, param1, param2, param3);
        }
    }

    @Override
    public void vertex(
        float param0,
        float param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        int param9,
        int param10,
        float param11,
        float param12,
        float param13
    ) {
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        } else if (this.fastFormat) {
            this.putFloat(0, param0);
            this.putFloat(4, param1);
            this.putFloat(8, param2);
            this.putByte(12, (byte)((int)(param3 * 255.0F)));
            this.putByte(13, (byte)((int)(param4 * 255.0F)));
            this.putByte(14, (byte)((int)(param5 * 255.0F)));
            this.putByte(15, (byte)((int)(param6 * 255.0F)));
            this.putFloat(16, param7);
            this.putFloat(20, param8);
            int var0;
            if (this.fullFormat) {
                this.putShort(24, (short)(param9 & 65535));
                this.putShort(26, (short)(param9 >> 16 & 65535));
                var0 = 28;
            } else {
                var0 = 24;
            }

            this.putShort(var0 + 0, (short)(param10 & 65535));
            this.putShort(var0 + 2, (short)(param10 >> 16 & 65535));
            this.putByte(var0 + 4, BufferVertexConsumer.normalIntValue(param11));
            this.putByte(var0 + 5, BufferVertexConsumer.normalIntValue(param12));
            this.putByte(var0 + 6, BufferVertexConsumer.normalIntValue(param13));
            this.nextElementByte += var0 + 8;
            this.endVertex();
        } else {
            super.vertex(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
        }
    }

    public Pair<BufferBuilder.DrawState, ByteBuffer> popNextBuffer() {
        BufferBuilder.DrawState var0 = this.drawStates.get(this.lastPoppedStateIndex++);
        this.buffer.position(this.totalUploadedBytes);
        this.totalUploadedBytes += Mth.roundToward(var0.bufferSize(), 4);
        this.buffer.limit(this.totalUploadedBytes);
        if (this.lastPoppedStateIndex == this.drawStates.size() && this.vertices == 0) {
            this.clear();
        }

        ByteBuffer var1 = this.buffer.slice();
        this.buffer.clear();
        return Pair.of(var0, var1);
    }

    public void clear() {
        if (this.totalRenderedBytes != this.totalUploadedBytes) {
            LOGGER.warn("Bytes mismatch {} {}", this.totalRenderedBytes, this.totalUploadedBytes);
        }

        this.discard();
    }

    public void discard() {
        this.totalRenderedBytes = 0;
        this.totalUploadedBytes = 0;
        this.nextElementByte = 0;
        this.drawStates.clear();
        this.lastPoppedStateIndex = 0;
    }

    @Override
    public VertexFormatElement currentElement() {
        if (this.currentElement == null) {
            throw new IllegalStateException("BufferBuilder not started");
        } else {
            return this.currentElement;
        }
    }

    public boolean building() {
        return this.building;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class DrawState {
        private final VertexFormat format;
        private final int vertexCount;
        private final int indexCount;
        private final VertexFormat.Mode mode;
        private final VertexFormat.IndexType indexType;
        private final boolean indexOnly;
        private final boolean sequentialIndex;

        DrawState(VertexFormat param0, int param1, int param2, VertexFormat.Mode param3, VertexFormat.IndexType param4, boolean param5, boolean param6) {
            this.format = param0;
            this.vertexCount = param1;
            this.indexCount = param2;
            this.mode = param3;
            this.indexType = param4;
            this.indexOnly = param5;
            this.sequentialIndex = param6;
        }

        public VertexFormat format() {
            return this.format;
        }

        public int vertexCount() {
            return this.vertexCount;
        }

        public int indexCount() {
            return this.indexCount;
        }

        public VertexFormat.Mode mode() {
            return this.mode;
        }

        public VertexFormat.IndexType indexType() {
            return this.indexType;
        }

        public int vertexBufferSize() {
            return this.vertexCount * this.format.getVertexSize();
        }

        private int indexBufferSize() {
            return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
        }

        public int bufferSize() {
            return this.vertexBufferSize() + this.indexBufferSize();
        }

        public boolean indexOnly() {
            return this.indexOnly;
        }

        public boolean sequentialIndex() {
            return this.sequentialIndex;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SortState {
        final VertexFormat.Mode mode;
        final int vertices;
        @Nullable
        final Vector3f[] sortingPoints;
        final float sortX;
        final float sortY;
        final float sortZ;

        SortState(VertexFormat.Mode param0, int param1, @Nullable Vector3f[] param2, float param3, float param4, float param5) {
            this.mode = param0;
            this.vertices = param1;
            this.sortingPoints = param2;
            this.sortX = param3;
            this.sortY = param4;
            this.sortZ = param5;
        }
    }
}
