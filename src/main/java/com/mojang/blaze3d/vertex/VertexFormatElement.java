package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.function.IntConsumer;
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
        } else {
            LOGGER.warn("Multiple vertex elements of the same type other than UVs are not supported. Forcing type to UV.");
            this.usage = VertexFormatElement.Usage.UV;
        }

        this.type = param1;
        this.index = param0;
        this.count = param3;
        this.byteSize = param1.getSize() * this.count;
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

    public void setupBufferState(long param0, int param1) {
        this.usage.setupBufferState(this.count, this.type.getGlType(), param1, param0, this.index);
    }

    public void clearBufferState() {
        this.usage.clearBufferState(this.index);
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
        POSITION("Position", (param0, param1, param2, param3, param4) -> {
            GlStateManager._vertexPointer(param0, param1, param2, param3);
            GlStateManager._enableClientState(32884);
        }, param0 -> GlStateManager._disableClientState(32884)),
        NORMAL("Normal", (param0, param1, param2, param3, param4) -> {
            GlStateManager._normalPointer(param1, param2, param3);
            GlStateManager._enableClientState(32885);
        }, param0 -> GlStateManager._disableClientState(32885)),
        COLOR("Vertex Color", (param0, param1, param2, param3, param4) -> {
            GlStateManager._colorPointer(param0, param1, param2, param3);
            GlStateManager._enableClientState(32886);
        }, param0 -> {
            GlStateManager._disableClientState(32886);
            GlStateManager._clearCurrentColor();
        }),
        UV("UV", (param0, param1, param2, param3, param4) -> {
            GlStateManager._glClientActiveTexture(33984 + param4);
            GlStateManager._texCoordPointer(param0, param1, param2, param3);
            GlStateManager._enableClientState(32888);
            GlStateManager._glClientActiveTexture(33984);
        }, param0 -> {
            GlStateManager._glClientActiveTexture(33984 + param0);
            GlStateManager._disableClientState(32888);
            GlStateManager._glClientActiveTexture(33984);
        }),
        PADDING("Padding", (param0, param1, param2, param3, param4) -> {
        }, param0 -> {
        }),
        GENERIC("Generic", (param0, param1, param2, param3, param4) -> {
            GlStateManager._enableVertexAttribArray(param4);
            GlStateManager._vertexAttribPointer(param4, param0, param1, false, param2, param3);
        }, GlStateManager::_disableVertexAttribArray);

        private final String name;
        private final VertexFormatElement.Usage.SetupState setupState;
        private final IntConsumer clearState;

        private Usage(String param0, VertexFormatElement.Usage.SetupState param1, IntConsumer param2) {
            this.name = param0;
            this.setupState = param1;
            this.clearState = param2;
        }

        private void setupBufferState(int param0, int param1, int param2, long param3, int param4) {
            this.setupState.setupBufferState(param0, param1, param2, param3, param4);
        }

        public void clearBufferState(int param0) {
            this.clearState.accept(param0);
        }

        public String getName() {
            return this.name;
        }

        @OnlyIn(Dist.CLIENT)
        interface SetupState {
            void setupBufferState(int var1, int var2, int var3, long var4, int var6);
        }
    }
}
