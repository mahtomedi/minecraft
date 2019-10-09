package net.minecraft.realms;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsVertexFormat {
    private VertexFormat v;

    public RealmsVertexFormat(VertexFormat param0) {
        this.v = param0;
    }

    public VertexFormat getVertexFormat() {
        return this.v;
    }

    public List<RealmsVertexFormatElement> getElements() {
        List<RealmsVertexFormatElement> var0 = Lists.newArrayList();

        for(VertexFormatElement var1 : this.v.getElements()) {
            var0.add(new RealmsVertexFormatElement(var1));
        }

        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        return this.v.equals(param0);
    }

    @Override
    public int hashCode() {
        return this.v.hashCode();
    }

    @Override
    public String toString() {
        return this.v.toString();
    }
}
