package net.minecraft.realms;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsDefaultVertexFormat {
    public static final RealmsVertexFormat POSITION_COLOR = new RealmsVertexFormat(DefaultVertexFormat.POSITION_COLOR);
    public static final RealmsVertexFormat POSITION_TEX_COLOR = new RealmsVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR);
}
