package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class VertexFormat {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<VertexFormatElement> elements = Lists.newArrayList();
    private final IntList offsets = new IntArrayList();
    private int vertexSize;

    public VertexFormat(VertexFormat param0) {
        this();

        for(int var0 = 0; var0 < param0.getElementCount(); ++var0) {
            this.addElement(param0.getElement(var0));
        }

        this.vertexSize = param0.getVertexSize();
    }

    public VertexFormat() {
    }

    public void clear() {
        this.elements.clear();
        this.offsets.clear();
        this.vertexSize = 0;
    }

    public VertexFormat addElement(VertexFormatElement param0) {
        if (param0.isPosition() && this.hasPositionElement()) {
            LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
            return this;
        } else {
            this.elements.add(param0);
            this.offsets.add(this.vertexSize);
            this.vertexSize += param0.getByteSize();
            return this;
        }
    }

    @Override
    public String toString() {
        return "format: " + this.elements.size() + " elements: " + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    private boolean hasPositionElement() {
        return this.elements.stream().anyMatch(VertexFormatElement::isPosition);
    }

    public int getIntegerSize() {
        return this.getVertexSize() / 4;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public VertexFormatElement getElement(int param0) {
        return this.elements.get(param0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            VertexFormat var0 = (VertexFormat)param0;
            if (this.vertexSize != var0.vertexSize) {
                return false;
            } else {
                return !this.elements.equals(var0.elements) ? false : this.offsets.equals(var0.offsets);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.elements.hashCode();
        var0 = 31 * var0 + this.offsets.hashCode();
        return 31 * var0 + this.vertexSize;
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
