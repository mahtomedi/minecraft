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
}
