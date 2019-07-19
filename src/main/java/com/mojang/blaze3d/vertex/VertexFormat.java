package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class VertexFormat {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<VertexFormatElement> elements = Lists.newArrayList();
    private final List<Integer> offsets = Lists.newArrayList();
    private int vertexSize;
    private int colorOffset = -1;
    private final List<Integer> texOffset = Lists.newArrayList();
    private int normalOffset = -1;

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
        this.colorOffset = -1;
        this.texOffset.clear();
        this.normalOffset = -1;
        this.vertexSize = 0;
    }

    public VertexFormat addElement(VertexFormatElement param0) {
        if (param0.isPosition() && this.hasPositionElement()) {
            LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
            return this;
        } else {
            this.elements.add(param0);
            this.offsets.add(this.vertexSize);
            switch(param0.getUsage()) {
                case NORMAL:
                    this.normalOffset = this.vertexSize;
                    break;
                case COLOR:
                    this.colorOffset = this.vertexSize;
                    break;
                case UV:
                    this.texOffset.add(param0.getIndex(), this.vertexSize);
            }

            this.vertexSize += param0.getByteSize();
            return this;
        }
    }

    public boolean hasNormal() {
        return this.normalOffset >= 0;
    }

    public int getNormalOffset() {
        return this.normalOffset;
    }

    public boolean hasColor() {
        return this.colorOffset >= 0;
    }

    public int getColorOffset() {
        return this.colorOffset;
    }

    public boolean hasUv(int param0) {
        return this.texOffset.size() - 1 >= param0;
    }

    public int getUvOffset(int param0) {
        return this.texOffset.get(param0);
    }

    @Override
    public String toString() {
        String var0 = "format: " + this.elements.size() + " elements: ";

        for(int var1 = 0; var1 < this.elements.size(); ++var1) {
            var0 = var0 + this.elements.get(var1).toString();
            if (var1 != this.elements.size() - 1) {
                var0 = var0 + " ";
            }
        }

        return var0;
    }

    private boolean hasPositionElement() {
        int var0 = 0;

        for(int var1 = this.elements.size(); var0 < var1; ++var0) {
            VertexFormatElement var2 = this.elements.get(var0);
            if (var2.isPosition()) {
                return true;
            }
        }

        return false;
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

    public int getOffset(int param0) {
        return this.offsets.get(param0);
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
}
