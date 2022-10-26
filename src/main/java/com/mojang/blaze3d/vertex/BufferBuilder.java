package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
    private static final int GROWTH_SIZE = 2097152;
    private static final Logger LOGGER = LogUtils.getLogger();
    private ByteBuffer buffer;
    private int renderedBufferCount;
    private int renderedBufferPointer;
    private int nextElementByte;
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
        this.buffer.rewind();
        this.mode = param0.mode;
        this.vertices = param0.vertices;
        this.nextElementByte = this.renderedBufferPointer;
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
            this.buffer.rewind();
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

    private IntConsumer intConsumer(int param0, VertexFormat.IndexType param1) {
        MutableInt var0 = new MutableInt(param0);

        return switch(param1) {
            case BYTE -> param1x -> this.buffer.put(var0.getAndIncrement(), (byte)param1x);
            case SHORT -> param1x -> this.buffer.putShort(var0.getAndAdd(2), (short)param1x);
            case INT -> param1x -> this.buffer.putInt(var0.getAndAdd(4), param1x);
        };
    }

    private Vector3f[] makeQuadSortingPoints() {
        FloatBuffer var0 = this.buffer.asFloatBuffer();
        int var1 = this.renderedBufferPointer / 4;
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
        IntConsumer var6 = this.intConsumer(this.nextElementByte, param0);

        for(int var7 : var1) {
            var6.accept(var7 * this.mode.primitiveStride + 0);
            var6.accept(var7 * this.mode.primitiveStride + 1);
            var6.accept(var7 * this.mode.primitiveStride + 2);
            var6.accept(var7 * this.mode.primitiveStride + 2);
            var6.accept(var7 * this.mode.primitiveStride + 3);
            var6.accept(var7 * this.mode.primitiveStride + 0);
        }

    }

    public boolean isCurrentBatchEmpty() {
        return this.vertices == 0;
    }

    @Nullable
    public BufferBuilder.RenderedBuffer endOrDiscardIfEmpty() {
        this.ensureDrawing();
        if (this.isCurrentBatchEmpty()) {
            this.reset();
            return null;
        } else {
            BufferBuilder.RenderedBuffer var0 = this.storeRenderedBuffer();
            this.reset();
            return var0;
        }
    }

    public BufferBuilder.RenderedBuffer end() {
        this.ensureDrawing();
        BufferBuilder.RenderedBuffer var0 = this.storeRenderedBuffer();
        this.reset();
        return var0;
    }

    private void ensureDrawing() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    private BufferBuilder.RenderedBuffer storeRenderedBuffer() {
        int var0 = this.mode.indexCount(this.vertices);
        int var1 = !this.indexOnly ? this.vertices * this.format.getVertexSize() : 0;
        VertexFormat.IndexType var2 = VertexFormat.IndexType.least(var0);
        boolean var4;
        int var5;
        if (this.sortingPoints != null) {
            int var3 = Mth.roundToward(var0 * var2.bytes, 4);
            this.ensureCapacity(var3);
            this.putSortedQuadIndices(var2);
            var4 = false;
            this.nextElementByte += var3;
            var5 = var1 + var3;
        } else {
            var4 = true;
            var5 = var1;
        }

        int var8 = this.renderedBufferPointer;
        this.renderedBufferPointer += var5;
        ++this.renderedBufferCount;
        BufferBuilder.DrawState var9 = new BufferBuilder.DrawState(this.format, this.vertices, var0, this.mode, var2, this.indexOnly, var4);
        return new BufferBuilder.RenderedBuffer(var8, var9);
    }

    private void reset() {
        this.building = false;
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
        this.sortingPoints = null;
        this.sortX = Float.NaN;
        this.sortY = Float.NaN;
        this.sortZ = Float.NaN;
        this.indexOnly = false;
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
                this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - var0, var0);
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

    void releaseRenderedBuffer() {
        if (this.renderedBufferCount > 0 && --this.renderedBufferCount == 0) {
            this.clear();
        }

    }

    public void clear() {
        if (this.renderedBufferCount > 0) {
            LOGGER.warn("Clearing BufferBuilder with unused batches");
        }

        this.discard();
    }

    public void discard() {
        this.renderedBufferCount = 0;
        this.renderedBufferPointer = 0;
        this.nextElementByte = 0;
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

    ByteBuffer bufferSlice(int param0, int param1) {
        return MemoryUtil.memSlice(this.buffer, param0, param1 - param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static record DrawState(
        VertexFormat format,
        int vertexCount,
        int indexCount,
        VertexFormat.Mode mode,
        VertexFormat.IndexType indexType,
        boolean indexOnly,
        boolean sequentialIndex
    ) {
        public int vertexBufferSize() {
            return this.vertexCount * this.format.getVertexSize();
        }

        public int vertexBufferStart() {
            return 0;
        }

        public int vertexBufferEnd() {
            return this.vertexBufferSize();
        }

        public int indexBufferStart() {
            return this.indexOnly ? 0 : this.vertexBufferEnd();
        }

        public int indexBufferEnd() {
            return this.indexBufferStart() + this.indexBufferSize();
        }

        private int indexBufferSize() {
            return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
        }

        public int bufferSize() {
            return this.indexBufferEnd();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class RenderedBuffer {
        private final int pointer;
        private final BufferBuilder.DrawState drawState;
        private boolean released;

        RenderedBuffer(int param1, BufferBuilder.DrawState param2) {
            this.pointer = param1;
            this.drawState = param2;
        }

        public ByteBuffer vertexBuffer() {
            int var0 = this.pointer + this.drawState.vertexBufferStart();
            int var1 = this.pointer + this.drawState.vertexBufferEnd();
            return BufferBuilder.this.bufferSlice(var0, var1);
        }

        public ByteBuffer indexBuffer() {
            int var0 = this.pointer + this.drawState.indexBufferStart();
            int var1 = this.pointer + this.drawState.indexBufferEnd();
            return BufferBuilder.this.bufferSlice(var0, var1);
        }

        public BufferBuilder.DrawState drawState() {
            return this.drawState;
        }

        public boolean isEmpty() {
            return this.drawState.vertexCount == 0;
        }

        public void release() {
            if (this.released) {
                throw new IllegalStateException("Buffer has already been released!");
            } else {
                BufferBuilder.this.releaseRenderedBuffer();
                this.released = true;
            }
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
