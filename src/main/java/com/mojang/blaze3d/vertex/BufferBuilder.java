package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
    private ByteBuffer buffer;
    private IntBuffer intBuffer;
    private ShortBuffer shortBuffer;
    private FloatBuffer floatBuffer;
    private int vertices;
    private VertexFormatElement currentElement;
    private int elementIndex;
    private boolean noColor;
    private int mode;
    private double xo;
    private double yo;
    private double zo;
    private VertexFormat format;
    private boolean building;

    public BufferBuilder(int param0) {
        this.buffer = MemoryTracker.createByteBuffer(param0 * 4);
        this.intBuffer = this.buffer.asIntBuffer();
        this.shortBuffer = this.buffer.asShortBuffer();
        this.floatBuffer = this.buffer.asFloatBuffer();
    }

    private void ensureCapacity(int param0) {
        if (this.vertices * this.format.getVertexSize() + param0 > this.buffer.capacity()) {
            int var0 = this.buffer.capacity();
            int var1 = var0 + roundUp(param0);
            LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", var0, var1);
            int var2 = this.intBuffer.position();
            ByteBuffer var3 = MemoryTracker.createByteBuffer(var1);
            ((Buffer)this.buffer).position(0);
            var3.put(this.buffer);
            ((Buffer)var3).rewind();
            this.buffer = var3;
            this.floatBuffer = this.buffer.asFloatBuffer().asReadOnlyBuffer();
            this.intBuffer = this.buffer.asIntBuffer();
            ((Buffer)this.intBuffer).position(var2);
            this.shortBuffer = this.buffer.asShortBuffer();
            ((Buffer)this.shortBuffer).position(var2 << 1);
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
        int var0 = this.vertices / 4;
        float[] var1 = new float[var0];

        for(int var2 = 0; var2 < var0; ++var2) {
            var1[var2] = getQuadDistanceFromPlayer(
                this.floatBuffer,
                (float)((double)param0 + this.xo),
                (float)((double)param1 + this.yo),
                (float)((double)param2 + this.zo),
                this.format.getIntegerSize(),
                var2 * this.format.getVertexSize()
            );
        }

        Integer[] var3 = new Integer[var0];

        for(int var4 = 0; var4 < var3.length; ++var4) {
            var3[var4] = var4;
        }

        Arrays.sort(var3, (param1x, param2x) -> Floats.compare(var1[param2x], var1[param1x]));
        BitSet var5 = new BitSet();
        int var6 = this.format.getVertexSize();
        int[] var7 = new int[var6];

        for(int var8 = var5.nextClearBit(0); var8 < var3.length; var8 = var5.nextClearBit(var8 + 1)) {
            int var9 = var3[var8];
            if (var9 != var8) {
                ((Buffer)this.intBuffer).limit(var9 * var6 + var6);
                ((Buffer)this.intBuffer).position(var9 * var6);
                this.intBuffer.get(var7);
                int var10 = var9;

                for(int var11 = var3[var9]; var10 != var8; var11 = var3[var11]) {
                    ((Buffer)this.intBuffer).limit(var11 * var6 + var6);
                    ((Buffer)this.intBuffer).position(var11 * var6);
                    IntBuffer var12 = this.intBuffer.slice();
                    ((Buffer)this.intBuffer).limit(var10 * var6 + var6);
                    ((Buffer)this.intBuffer).position(var10 * var6);
                    this.intBuffer.put(var12);
                    var5.set(var10);
                    var10 = var11;
                }

                ((Buffer)this.intBuffer).limit(var8 * var6 + var6);
                ((Buffer)this.intBuffer).position(var8 * var6);
                this.intBuffer.put(var7);
            }

            var5.set(var8);
        }

    }

    public BufferBuilder.State getState() {
        ((Buffer)this.intBuffer).rewind();
        int var0 = this.getBufferIndex();
        ((Buffer)this.intBuffer).limit(var0);
        int[] var1 = new int[var0];
        this.intBuffer.get(var1);
        ((Buffer)this.intBuffer).limit(this.intBuffer.capacity());
        ((Buffer)this.intBuffer).position(var0);
        return new BufferBuilder.State(var1, new VertexFormat(this.format));
    }

    private int getBufferIndex() {
        return this.vertices * this.format.getIntegerSize();
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
        ((Buffer)this.intBuffer).clear();
        this.ensureCapacity(param0.array().length * 4);
        this.intBuffer.put(param0.array());
        this.vertices = param0.vertices();
        this.format = new VertexFormat(param0.getFormat());
    }

    public void clear() {
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
    }

    public void begin(int param0, VertexFormat param1) {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        } else {
            this.building = true;
            this.clear();
            this.mode = param0;
            this.format = param1;
            this.currentElement = param1.getElement(this.elementIndex);
            this.noColor = false;
            ((Buffer)this.buffer).limit(this.buffer.capacity());
        }
    }

    public BufferBuilder uv(double param0, double param1) {
        int var0 = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
        switch(this.currentElement.getType()) {
            case FLOAT:
                this.buffer.putFloat(var0, (float)param0);
                this.buffer.putFloat(var0 + 4, (float)param1);
                break;
            case UINT:
            case INT:
                this.buffer.putInt(var0, (int)param0);
                this.buffer.putInt(var0 + 4, (int)param1);
                break;
            case USHORT:
            case SHORT:
                this.buffer.putShort(var0, (short)((int)param1));
                this.buffer.putShort(var0 + 2, (short)((int)param0));
                break;
            case UBYTE:
            case BYTE:
                this.buffer.put(var0, (byte)((int)param1));
                this.buffer.put(var0 + 1, (byte)((int)param0));
        }

        this.nextElement();
        return this;
    }

    public BufferBuilder uv2(int param0, int param1) {
        int var0 = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
        switch(this.currentElement.getType()) {
            case FLOAT:
                this.buffer.putFloat(var0, (float)param0);
                this.buffer.putFloat(var0 + 4, (float)param1);
                break;
            case UINT:
            case INT:
                this.buffer.putInt(var0, param0);
                this.buffer.putInt(var0 + 4, param1);
                break;
            case USHORT:
            case SHORT:
                this.buffer.putShort(var0, (short)param1);
                this.buffer.putShort(var0 + 2, (short)param0);
                break;
            case UBYTE:
            case BYTE:
                this.buffer.put(var0, (byte)param1);
                this.buffer.put(var0 + 1, (byte)param0);
        }

        this.nextElement();
        return this;
    }

    public void faceTex2(int param0, int param1, int param2, int param3) {
        int var0 = (this.vertices - 4) * this.format.getIntegerSize() + this.format.getUvOffset(1) / 4;
        int var1 = this.format.getVertexSize() >> 2;
        this.intBuffer.put(var0, param0);
        this.intBuffer.put(var0 + var1, param1);
        this.intBuffer.put(var0 + var1 * 2, param2);
        this.intBuffer.put(var0 + var1 * 3, param3);
    }

    public void postProcessFacePosition(double param0, double param1, double param2) {
        int var0 = this.format.getIntegerSize();
        int var1 = (this.vertices - 4) * var0;

        for(int var2 = 0; var2 < 4; ++var2) {
            int var3 = var1 + var2 * var0;
            int var4 = var3 + 1;
            int var5 = var4 + 1;
            this.intBuffer.put(var3, Float.floatToRawIntBits((float)(param0 + this.xo) + Float.intBitsToFloat(this.intBuffer.get(var3))));
            this.intBuffer.put(var4, Float.floatToRawIntBits((float)(param1 + this.yo) + Float.intBitsToFloat(this.intBuffer.get(var4))));
            this.intBuffer.put(var5, Float.floatToRawIntBits((float)(param2 + this.zo) + Float.intBitsToFloat(this.intBuffer.get(var5))));
        }

    }

    private int getStartingColorIndex(int param0) {
        return ((this.vertices - param0) * this.format.getVertexSize() + this.format.getColorOffset()) / 4;
    }

    public void faceTint(float param0, float param1, float param2, int param3) {
        int var0 = this.getStartingColorIndex(param3);
        int var1 = -1;
        if (!this.noColor) {
            var1 = this.intBuffer.get(var0);
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int var2 = (int)((float)(var1 & 0xFF) * param0);
                int var3 = (int)((float)(var1 >> 8 & 0xFF) * param1);
                int var4 = (int)((float)(var1 >> 16 & 0xFF) * param2);
                var1 &= -16777216;
                var1 |= var4 << 16 | var3 << 8 | var2;
            } else {
                int var5 = (int)((float)(var1 >> 24 & 0xFF) * param0);
                int var6 = (int)((float)(var1 >> 16 & 0xFF) * param1);
                int var7 = (int)((float)(var1 >> 8 & 0xFF) * param2);
                var1 &= 255;
                var1 |= var5 << 24 | var6 << 16 | var7 << 8;
            }
        }

        this.intBuffer.put(var0, var1);
    }

    private void fixupVertexColor(int param0, int param1) {
        int var0 = this.getStartingColorIndex(param1);
        int var1 = param0 >> 16 & 0xFF;
        int var2 = param0 >> 8 & 0xFF;
        int var3 = param0 & 0xFF;
        this.putColor(var0, var1, var2, var3);
    }

    public void fixupVertexColor(float param0, float param1, float param2, int param3) {
        int var0 = this.getStartingColorIndex(param3);
        int var1 = clamp((int)(param0 * 255.0F), 0, 255);
        int var2 = clamp((int)(param1 * 255.0F), 0, 255);
        int var3 = clamp((int)(param2 * 255.0F), 0, 255);
        this.putColor(var0, var1, var2, var3);
    }

    private static int clamp(int param0, int param1, int param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    private void putColor(int param0, int param1, int param2, int param3) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.intBuffer.put(param0, 0xFF000000 | param3 << 16 | param2 << 8 | param1);
        } else {
            this.intBuffer.put(param0, param1 << 24 | param2 << 16 | param3 << 8 | 0xFF);
        }

    }

    public void noColor() {
        this.noColor = true;
    }

    public BufferBuilder color(float param0, float param1, float param2, float param3) {
        return this.color((int)(param0 * 255.0F), (int)(param1 * 255.0F), (int)(param2 * 255.0F), (int)(param3 * 255.0F));
    }

    public BufferBuilder color(int param0, int param1, int param2, int param3) {
        if (this.noColor) {
            return this;
        } else {
            int var0 = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
            switch(this.currentElement.getType()) {
                case FLOAT:
                    this.buffer.putFloat(var0, (float)param0 / 255.0F);
                    this.buffer.putFloat(var0 + 4, (float)param1 / 255.0F);
                    this.buffer.putFloat(var0 + 8, (float)param2 / 255.0F);
                    this.buffer.putFloat(var0 + 12, (float)param3 / 255.0F);
                    break;
                case UINT:
                case INT:
                    this.buffer.putFloat(var0, (float)param0);
                    this.buffer.putFloat(var0 + 4, (float)param1);
                    this.buffer.putFloat(var0 + 8, (float)param2);
                    this.buffer.putFloat(var0 + 12, (float)param3);
                    break;
                case USHORT:
                case SHORT:
                    this.buffer.putShort(var0, (short)param0);
                    this.buffer.putShort(var0 + 2, (short)param1);
                    this.buffer.putShort(var0 + 4, (short)param2);
                    this.buffer.putShort(var0 + 6, (short)param3);
                    break;
                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        this.buffer.put(var0, (byte)param0);
                        this.buffer.put(var0 + 1, (byte)param1);
                        this.buffer.put(var0 + 2, (byte)param2);
                        this.buffer.put(var0 + 3, (byte)param3);
                    } else {
                        this.buffer.put(var0, (byte)param3);
                        this.buffer.put(var0 + 1, (byte)param2);
                        this.buffer.put(var0 + 2, (byte)param1);
                        this.buffer.put(var0 + 3, (byte)param0);
                    }
            }

            this.nextElement();
            return this;
        }
    }

    public void putBulkData(int[] param0) {
        this.ensureCapacity(param0.length * 4 + this.format.getVertexSize());
        ((Buffer)this.intBuffer).position(this.getBufferIndex());
        this.intBuffer.put(param0);
        this.vertices += param0.length / this.format.getIntegerSize();
    }

    public void endVertex() {
        ++this.vertices;
        this.ensureCapacity(this.format.getVertexSize());
    }

    public BufferBuilder vertex(double param0, double param1, double param2) {
        int var0 = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
        switch(this.currentElement.getType()) {
            case FLOAT:
                this.buffer.putFloat(var0, (float)(param0 + this.xo));
                this.buffer.putFloat(var0 + 4, (float)(param1 + this.yo));
                this.buffer.putFloat(var0 + 8, (float)(param2 + this.zo));
                break;
            case UINT:
            case INT:
                this.buffer.putInt(var0, Float.floatToRawIntBits((float)(param0 + this.xo)));
                this.buffer.putInt(var0 + 4, Float.floatToRawIntBits((float)(param1 + this.yo)));
                this.buffer.putInt(var0 + 8, Float.floatToRawIntBits((float)(param2 + this.zo)));
                break;
            case USHORT:
            case SHORT:
                this.buffer.putShort(var0, (short)((int)(param0 + this.xo)));
                this.buffer.putShort(var0 + 2, (short)((int)(param1 + this.yo)));
                this.buffer.putShort(var0 + 4, (short)((int)(param2 + this.zo)));
                break;
            case UBYTE:
            case BYTE:
                this.buffer.put(var0, (byte)((int)(param0 + this.xo)));
                this.buffer.put(var0 + 1, (byte)((int)(param1 + this.yo)));
                this.buffer.put(var0 + 2, (byte)((int)(param2 + this.zo)));
        }

        this.nextElement();
        return this;
    }

    public void postNormal(float param0, float param1, float param2) {
        int var0 = (byte)((int)(param0 * 127.0F)) & 255;
        int var1 = (byte)((int)(param1 * 127.0F)) & 255;
        int var2 = (byte)((int)(param2 * 127.0F)) & 255;
        int var3 = var0 | var1 << 8 | var2 << 16;
        int var4 = this.format.getVertexSize() >> 2;
        int var5 = (this.vertices - 4) * var4 + this.format.getNormalOffset() / 4;
        this.intBuffer.put(var5, var3);
        this.intBuffer.put(var5 + var4, var3);
        this.intBuffer.put(var5 + var4 * 2, var3);
        this.intBuffer.put(var5 + var4 * 3, var3);
    }

    private void nextElement() {
        ++this.elementIndex;
        this.elementIndex %= this.format.getElementCount();
        this.currentElement = this.format.getElement(this.elementIndex);
        if (this.currentElement.getUsage() == VertexFormatElement.Usage.PADDING) {
            this.nextElement();
        }

    }

    public BufferBuilder normal(float param0, float param1, float param2) {
        int var0 = this.vertices * this.format.getVertexSize() + this.format.getOffset(this.elementIndex);
        switch(this.currentElement.getType()) {
            case FLOAT:
                this.buffer.putFloat(var0, param0);
                this.buffer.putFloat(var0 + 4, param1);
                this.buffer.putFloat(var0 + 8, param2);
                break;
            case UINT:
            case INT:
                this.buffer.putInt(var0, (int)param0);
                this.buffer.putInt(var0 + 4, (int)param1);
                this.buffer.putInt(var0 + 8, (int)param2);
                break;
            case USHORT:
            case SHORT:
                this.buffer.putShort(var0, (short)((int)param0 * 32767 & 65535));
                this.buffer.putShort(var0 + 2, (short)((int)param1 * 32767 & 65535));
                this.buffer.putShort(var0 + 4, (short)((int)param2 * 32767 & 65535));
                break;
            case UBYTE:
            case BYTE:
                this.buffer.put(var0, (byte)((int)param0 * 127 & 0xFF));
                this.buffer.put(var0 + 1, (byte)((int)param1 * 127 & 0xFF));
                this.buffer.put(var0 + 2, (byte)((int)param2 * 127 & 0xFF));
        }

        this.nextElement();
        return this;
    }

    public void offset(double param0, double param1, double param2) {
        this.xo = param0;
        this.yo = param1;
        this.zo = param2;
    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        } else {
            this.building = false;
            ((Buffer)this.buffer).position(0);
            ((Buffer)this.buffer).limit(this.getBufferIndex() * 4);
        }
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public VertexFormat getVertexFormat() {
        return this.format;
    }

    public int getVertexCount() {
        return this.vertices;
    }

    public int getDrawMode() {
        return this.mode;
    }

    public void fixupQuadColor(int param0) {
        for(int var0 = 0; var0 < 4; ++var0) {
            this.fixupVertexColor(param0, var0 + 1);
        }

    }

    public void fixupQuadColor(float param0, float param1, float param2) {
        for(int var0 = 0; var0 < 4; ++var0) {
            this.fixupVertexColor(param0, param1, param2, var0 + 1);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public class State {
        private final int[] array;
        private final VertexFormat format;

        public State(int[] param1, VertexFormat param2) {
            this.array = param1;
            this.format = param2;
        }

        public int[] array() {
            return this.array;
        }

        public int vertices() {
            return this.array.length / this.format.getIntegerSize();
        }

        public VertexFormat getFormat() {
            return this.format;
        }
    }
}
