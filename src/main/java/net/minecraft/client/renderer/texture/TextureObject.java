package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TextureObject {
    void pushFilter(boolean var1, boolean var2);

    void popFilter();

    void load(ResourceManager var1) throws IOException;

    int getId();

    default void bind() {
        GlStateManager.bindTexture(this.getId());
    }

    default void reset(TextureManager param0, ResourceManager param1, ResourceLocation param2, Executor param3) {
        param0.register(param2, this);
    }
}
