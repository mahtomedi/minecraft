package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
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
    private final ImmutableMap<String, VertexFormatElement> elementMapping;
    private final IntList offsets = new IntArrayList();
    private final int vertexSize;
    private int vertexArrayObject;
    private int vertexBufferObject;
    private int indexBufferObject;

    public VertexFormat(ImmutableMap<String, VertexFormatElement> param0) {
        this.elementMapping = param0;
        this.elements = param0.values().asList();
        int var0 = 0;

        for(VertexFormatElement var1 : param0.values()) {
            this.offsets.add(var0);
            var0 += var1.getByteSize();
        }

        this.vertexSize = var0;
    }

    @Override
    public String toString() {
        return "format: "
            + this.elementMapping.size()
            + " elements: "
            + (String)this.elementMapping.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
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

    public ImmutableList<String> getElementAttributeNames() {
        return this.elementMapping.keySet().asList();
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            VertexFormat var0 = (VertexFormat)param0;
            return this.vertexSize != var0.vertexSize ? false : this.elementMapping.equals(var0.elementMapping);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.elementMapping.hashCode();
    }

    public void setupBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_setupBufferState);
        } else {
            this._setupBufferState();
        }
    }

    private void _setupBufferState() {
        int var0 = this.getVertexSize();
        List<VertexFormatElement> var1 = this.getElements();

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            var1.get(var2).setupBufferState(var2, (long)this.offsets.getInt(var2), var0);
        }

    }

    public void clearBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_clearBufferState);
        } else {
            this._clearBufferState();
        }
    }

    private void _clearBufferState() {
        ImmutableList<VertexFormatElement> var0 = this.getElements();

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            VertexFormatElement var2 = var0.get(var1);
            var2.clearBufferState(var1);
        }

    }

    public int getOrCreateVertexArrayObject() {
        if (this.vertexArrayObject == 0) {
            this.vertexArrayObject = GlStateManager._glGenVertexArrays();
        }

        return this.vertexArrayObject;
    }

    public int getOrCreateVertexBufferObject() {
        if (this.vertexBufferObject == 0) {
            this.vertexBufferObject = GlStateManager._glGenBuffers();
        }

        return this.vertexBufferObject;
    }

    public int getOrCreateIndexBufferObject() {
        if (this.indexBufferObject == 0) {
            this.indexBufferObject = GlStateManager._glGenBuffers();
        }

        return this.indexBufferObject;
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
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(4, 4, 4, false);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;
        public final boolean connectedPrimitives;

        private Mode(int param0, int param1, int param2, boolean param3) {
            this.asGLMode = param0;
            this.primitiveLength = param1;
            this.primitiveStride = param2;
            this.connectedPrimitives = param3;
        }

        public int indexCount(int param0) {
            return switch(this) {
                case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> param0;
                case LINES, QUADS -> param0 / 4 * 6;
                default -> 0;
            };
        }
    }
}
