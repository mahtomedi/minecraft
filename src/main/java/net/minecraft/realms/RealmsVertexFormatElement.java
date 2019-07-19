package net.minecraft.realms;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsVertexFormatElement {
    private final VertexFormatElement v;

    public RealmsVertexFormatElement(VertexFormatElement param0) {
        this.v = param0;
    }

    public VertexFormatElement getVertexFormatElement() {
        return this.v;
    }

    public boolean isPosition() {
        return this.v.isPosition();
    }

    public int getIndex() {
        return this.v.getIndex();
    }

    public int getByteSize() {
        return this.v.getByteSize();
    }

    public int getCount() {
        return this.v.getCount();
    }

    @Override
    public int hashCode() {
        return this.v.hashCode();
    }

    @Override
    public boolean equals(Object param0) {
        return this.v.equals(param0);
    }

    @Override
    public String toString() {
        return this.v.toString();
    }
}
