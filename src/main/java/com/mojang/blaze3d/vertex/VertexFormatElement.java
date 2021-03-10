package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class VertexFormatElement {
    private static final Logger LOGGER = LogManager.getLogger();
    private final VertexFormatElement.Type type;
    private final VertexFormatElement.Usage usage;
    private final int index;
    private final int count;
    private final int byteSize;

    public VertexFormatElement(int param0, VertexFormatElement.Type param1, VertexFormatElement.Usage param2, int param3) {
        if (this.supportsUsage(param0, param2)) {
            this.usage = param2;
            this.type = param1;
            this.index = param0;
            this.count = param3;
            this.byteSize = param1.getSize() * this.count;
        } else {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        }
    }

    private boolean supportsUsage(int param0, VertexFormatElement.Usage param1) {
        return param0 == 0 || param1 == VertexFormatElement.Usage.UV;
    }

    public final VertexFormatElement.Type getType() {
        return this.type;
    }

    public final VertexFormatElement.Usage getUsage() {
        return this.usage;
    }

    public final int getCount() {
        return this.count;
    }

    public final int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return this.count + "," + this.usage.getName() + "," + this.type.getName();
    }

    public final int getByteSize() {
        return this.byteSize;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            VertexFormatElement var0 = (VertexFormatElement)param0;
            if (this.count != var0.count) {
                return false;
            } else if (this.index != var0.index) {
                return false;
            } else if (this.type != var0.type) {
                return false;
            } else {
                return this.usage == var0.usage;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.type.hashCode();
        var0 = 31 * var0 + this.usage.hashCode();
        var0 = 31 * var0 + this.index;
        return 31 * var0 + this.count;
    }

    public void setupBufferState(int param0, long param1, int param2) {
        this.usage.setupBufferState(this.count, this.type.getGlType(), param2, param1, this.index, param0);
    }

    public void clearBufferState(int param0) {
        this.usage.clearBufferState(this.index, param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        FLOAT(4, "Float", 5126),
        UBYTE(1, "Unsigned Byte", 5121),
        BYTE(1, "Byte", 5120),
        USHORT(2, "Unsigned Short", 5123),
        SHORT(2, "Short", 5122),
        UINT(4, "Unsigned Int", 5125),
        INT(4, "Int", 5124);

        private final int size;
        private final String name;
        private final int glType;

        private Type(int param0, String param1, int param2) {
            this.size = param0;
            this.name = param1;
            this.glType = param2;
        }

        public int getSize() {
            return this.size;
        }

        public String getName() {
            return this.name;
        }

        public int getGlType() {
            return this.glType;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Usage {
        POSITION("Position", (param0, param1, param2, param3, param4, param5) -> {
            GlStateManager._enableVertexAttribArray(param5);
            GlStateManager._vertexAttribPointer(param5, param0, param1, false, param2, param3);
        }, (param0, param1) -> GlStateManager._disableVertexAttribArray(param1)),
        NORMAL("Normal", (param0, param1, param2, param3, param4, param5) -> {
            GlStateManager._enableVertexAttribArray(param5);
            GlStateManager._vertexAttribPointer(param5, param0, param1, true, param2, param3);
        }, (param0, param1) -> GlStateManager._disableVertexAttribArray(param1)),
        COLOR("Vertex Color", (param0, param1, param2, param3, param4, param5) -> {
            GlStateManager._enableVertexAttribArray(param5);
            GlStateManager._vertexAttribPointer(param5, param0, param1, true, param2, param3);
        }, (param0, param1) -> GlStateManager._disableVertexAttribArray(param1)),
        UV("UV", (param0, param1, param2, param3, param4, param5) -> {
            GlStateManager._enableVertexAttribArray(param5);
            if (param1 == 5126) {
                GlStateManager._vertexAttribPointer(param5, param0, param1, false, param2, param3);
            } else {
                GlStateManager._vertexAttribIPointer(param5, param0, param1, param2, param3);
            }

        }, (param0, param1) -> GlStateManager._disableVertexAttribArray(param1)),
        PADDING("Padding", (param0, param1, param2, param3, param4, param5) -> {
        }, (param0, param1) -> {
        }),
        GENERIC("Generic", (param0, param1, param2, param3, param4, param5) -> {
            GlStateManager._enableVertexAttribArray(param5);
            GlStateManager._vertexAttribPointer(param5, param0, param1, false, param2, param3);
        }, (param0, param1) -> GlStateManager._disableVertexAttribArray(param1));

        private final String name;
        private final VertexFormatElement.Usage.SetupState setupState;
        private final VertexFormatElement.Usage.ClearState clearState;

        private Usage(String param0, VertexFormatElement.Usage.SetupState param1, VertexFormatElement.Usage.ClearState param2) {
            this.name = param0;
            this.setupState = param1;
            this.clearState = param2;
        }

        private void setupBufferState(int param0, int param1, int param2, long param3, int param4, int param5) {
            this.setupState.setupBufferState(param0, param1, param2, param3, param4, param5);
        }

        public void clearBufferState(int param0, int param1) {
            this.clearState.clearBufferState(param0, param1);
        }

        public String getName() {
            return this.name;
        }

        @FunctionalInterface
        @OnlyIn(Dist.CLIENT)
        interface ClearState {
            void clearBufferState(int var1, int var2);
        }

        @FunctionalInterface
        @OnlyIn(Dist.CLIENT)
        interface SetupState {
            void setupBufferState(int var1, int var2, int var3, long var4, int var6, int var7);
        }
    }
}
