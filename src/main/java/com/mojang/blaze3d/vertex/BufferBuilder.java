package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
    private static final Logger LOGGER = LogManager.getLogger();
    private ByteBuffer buffer;
    private final List<BufferBuilder.DrawState> vertexCounts = Lists.newArrayList();
    private int lastRenderedCountIndex = 0;
    private int totalRenderedBytes = 0;
    private int nextElementByte = 0;
    private int totalUploadedBytes = 0;
    private int vertices;
    @Nullable
    private VertexFormatElement currentElement;
    private int elementIndex;
    private int mode;
    private VertexFormat format;
    private boolean fastFormat;
    private boolean fullFormat;
    private boolean building;

    public BufferBuilder(int param0) {
        this.buffer = MemoryTracker.createByteBuffer(param0 * 4);
    }

    protected void ensureVertexCapacity() {
        this.ensureCapacity(this.format.getVertexSize());
    }

    private void ensureCapacity(int param0) {
        if (this.nextElementByte + param0 > this.buffer.capacity()) {
            int var0 = this.buffer.capacity();
            int var1 = var0 + roundUp(param0);
            LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", var0, var1);
            ByteBuffer var2 = MemoryTracker.createByteBuffer(var1);
            ((Buffer)this.buffer).position(0);
            var2.put(this.buffer);
            ((Buffer)var2).rewind();
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

    public void sortQuads(float param0, float param1, float param2) {
        ((Buffer)this.buffer).clear();
        FloatBuffer var0 = this.buffer.asFloatBuffer();
        int var1 = this.vertices / 4;
        float[] var2 = new float[var1];

        for(int var3 = 0; var3 < var1; ++var3) {
            var2[var3] = getQuadDistanceFromPlayer(
                var0, param0, param1, param2, this.format.getIntegerSize(), this.totalRenderedBytes / 4 + var3 * this.format.getVertexSize()
            );
        }

        int[] var4 = new int[var1];
        int var5 = 0;

        while(var5 < var4.length) {
            var4[var5] = var5++;
        }

        IntArrays.mergeSort(var4, (param1x, param2x) -> Floats.compare(var2[param2x], var2[param1x]));
        BitSet var6 = new BitSet();
        FloatBuffer var7 = MemoryTracker.createFloatBuffer(this.format.getIntegerSize() * 4);

        for(int var8 = var6.nextClearBit(0); var8 < var4.length; var8 = var6.nextClearBit(var8 + 1)) {
            int var9 = var4[var8];
            if (var9 != var8) {
                this.limitToVertex(var0, var9);
                ((Buffer)var7).clear();
                var7.put(var0);
                int var10 = var9;

                for(int var11 = var4[var9]; var10 != var8; var11 = var4[var11]) {
                    this.limitToVertex(var0, var11);
                    FloatBuffer var12 = var0.slice();
                    this.limitToVertex(var0, var10);
                    var0.put(var12);
                    var6.set(var10);
                    var10 = var11;
                }

                this.limitToVertex(var0, var8);
                ((Buffer)var7).flip();
                var0.put(var7);
            }

            var6.set(var8);
        }

    }

    private void limitToVertex(FloatBuffer param0, int param1) {
        int var0 = this.format.getIntegerSize() * 4;
        ((Buffer)param0).limit(this.totalRenderedBytes / 4 + (param1 + 1) * var0);
        ((Buffer)param0).position(this.totalRenderedBytes / 4 + param1 * var0);
    }

    public BufferBuilder.State getState() {
        ((Buffer)this.buffer).limit(this.nextElementByte);
        ((Buffer)this.buffer).position(this.totalRenderedBytes);
        ByteBuffer var0 = ByteBuffer.allocate(this.vertices * this.format.getVertexSize());
        var0.put(this.buffer);
        ((Buffer)this.buffer).clear();
        return new BufferBuilder.State(var0, this.format);
    }

    private static float getQuadDistanceFromPlayer(FloatBuffer param0, float param1, float param2, float param3, int param4, int param5) {
        float var0 = param0.get(param5 + param4 * 0 + 0);
        float var1 = param0.get(param5 + param4 * 0 + 1);
        float var2 = param0.get(param5 + param4 * 0 + 2);
        float var3 = param0.get(param5 + param4 * 1 + 0);
        float var4 = param0.get(param5 + param4 * 1 + 1);
        float var5 = param0.get(param5 + param4 * 1 + 2);
        float var6 = param0.get(param5 + param4 * 2 + 0);
        float var7 = param0.get(param5 + param4 * 2 + 1);
        float var8 = param0.get(param5 + param4 * 2 + 2);
        float var9 = param0.get(param5 + param4 * 3 + 0);
        float var10 = param0.get(param5 + param4 * 3 + 1);
        float var11 = param0.get(param5 + param4 * 3 + 2);
        float var12 = (var0 + var3 + var6 + var9) * 0.25F - param1;
        float var13 = (var1 + var4 + var7 + var10) * 0.25F - param2;
        float var14 = (var2 + var5 + var8 + var11) * 0.25F - param3;
        return var12 * var12 + var13 * var13 + var14 * var14;
    }

    public void restoreState(BufferBuilder.State param0) {
        ((Buffer)param0.data).clear();
        int var0 = param0.data.capacity();
        this.ensureCapacity(var0);
        ((Buffer)this.buffer).limit(this.buffer.capacity());
        ((Buffer)this.buffer).position(this.totalRenderedBytes);
        this.buffer.put(param0.data);
        ((Buffer)this.buffer).clear();
        VertexFormat var1 = param0.format;
        this.switchFormat(var1);
        this.vertices = var0 / var1.getVertexSize();
        this.nextElementByte = this.totalRenderedBytes + this.vertices * var1.getVertexSize();
    }

    public void begin(int param0, VertexFormat param1) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        } else {
            this.building = true;
            this.mode = param0;
            this.switchFormat(param1);
            this.currentElement = param1.getElements().get(0);
            this.elementIndex = 0;
            ((Buffer)this.buffer).clear();
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

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        } else {
            this.building = false;
            this.vertexCounts.add(new BufferBuilder.DrawState(this.format, this.vertices, this.mode));
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
            this.vertices = 0;
            this.currentElement = null;
            this.elementIndex = 0;
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
        BufferBuilder.DrawState var0 = this.vertexCounts.get(this.lastRenderedCountIndex++);
        ((Buffer)this.buffer).position(this.totalUploadedBytes);
        this.totalUploadedBytes += var0.vertexCount() * var0.format().getVertexSize();
        ((Buffer)this.buffer).limit(this.totalUploadedBytes);
        if (this.lastRenderedCountIndex == this.vertexCounts.size() && this.vertices == 0) {
            this.clear();
        }

        ByteBuffer var1 = this.buffer.slice();
        ((Buffer)this.buffer).clear();
        return Pair.of(var0, var1);
    }

    public void clear() {
        if (this.totalRenderedBytes != this.totalUploadedBytes) {
            LOGGER.warn("Bytes mismatch " + this.totalRenderedBytes + " " + this.totalUploadedBytes);
        }

        this.discard();
    }

    public void discard() {
        this.totalRenderedBytes = 0;
        this.totalUploadedBytes = 0;
        this.nextElementByte = 0;
        this.vertexCounts.clear();
        this.lastRenderedCountIndex = 0;
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
        private final int mode;

        private DrawState(VertexFormat param0, int param1, int param2) {
            this.format = param0;
            this.vertexCount = param1;
            this.mode = param2;
        }

        public VertexFormat format() {
            return this.format;
        }

        public int vertexCount() {
            return this.vertexCount;
        }

        public int mode() {
            return this.mode;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class State {
        private final ByteBuffer data;
        private final VertexFormat format;

        private State(ByteBuffer param0, VertexFormat param1) {
            this.data = param0;
            this.format = param1;
        }
    }
}
