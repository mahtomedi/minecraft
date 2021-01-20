package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexFormat {
    private final ImmutableList<VertexFormatElement> elements;
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;

    public VertexFormat(ImmutableList<VertexFormatElement> param0) {
        this.elements = param0;
        int var0 = 0;

        for(VertexFormatElement var1 : param0) {
            this.offsets.add(var0);
            var0 += var1.getByteSize();
        }

        this.vertexSize = var0;
    }

    @Override
    public String toString() {
        return "format: " + this.elements.size() + " elements: " + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public ImmutableList<VertexFormatElement> getElements() {
        return this.elements;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            VertexFormat var0 = (VertexFormat)param0;
            return this.vertexSize != var0.vertexSize ? false : this.elements.equals(var0.elements);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }

    public void setupBufferState(long param0) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.setupBufferState(param0));
        } else {
            int var0 = this.getVertexSize();
            List<VertexFormatElement> var1 = this.getElements();

            for(int var2 = 0; var2 < var1.size(); ++var2) {
                var1.get(var2).setupBufferState(param0 + (long)this.offsets.getInt(var2), var0);
            }

        }
    }

    public void clearBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::clearBufferState);
        } else {
            for(VertexFormatElement var0 : this.getElements()) {
                var0.clearBufferState();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum IndexType {
        BYTE(5121, 1),
        SHORT(5123, 2),
        INT(5125, 4);

        public final int asGLType;
        public final int bytes;

        private IndexType(int param0, int param1) {
            this.asGLType = param0;
            this.bytes = param1;
        }

        public static VertexFormat.IndexType least(int param0) {
            if ((param0 & -65536) != 0) {
                return INT;
            } else {
                return (param0 & 0xFF00) != 0 ? SHORT : BYTE;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Mode {
        LINES(1, 2, 2),
        LINE_STRIP(3, 2, 1),
        TRIANGLES(4, 3, 3),
        TRIANGLE_STRIP(5, 3, 1),
        TRIANGLE_FAN(6, 3, 1),
        QUADS(4, 4, 4);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;

        private Mode(int param0, int param1, int param2) {
            this.asGLMode = param0;
            this.primitiveLength = param1;
            this.primitiveStride = param2;
        }

        public int indexCount(int param0) {
            int var0;
            switch(this) {
                case LINES:
                case LINE_STRIP:
                case TRIANGLES:
                case TRIANGLE_STRIP:
                case TRIANGLE_FAN:
                    var0 = param0;
                    break;
                case QUADS:
                    var0 = param0 / 4 * 6;
                    break;
                default:
                    var0 = 0;
            }

            return var0;
        }
    }
}
