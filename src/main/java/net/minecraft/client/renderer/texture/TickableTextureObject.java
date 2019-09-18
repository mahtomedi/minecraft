package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TickableTextureObject extends TextureObject, Tickable {
}
