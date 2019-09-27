package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTexture {
    protected int id = -1;
    protected boolean blur;
    protected boolean mipmap;
    protected boolean oldBlur;
    protected boolean oldMipmap;

    public void setFilter(boolean param0, boolean param1) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.blur = param0;
        this.mipmap = param1;
        int var0;
        int var1;
        if (param0) {
            var0 = param1 ? 9987 : 9729;
            var1 = 9729;
        } else {
            var0 = param1 ? 9986 : 9728;
            var1 = 9728;
        }

        GlStateManager._texParameter(3553, 10241, var0);
        GlStateManager._texParameter(3553, 10240, var1);
    }

    public void pushFilter(boolean param0, boolean param1) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._pushFilter(param0, param1));
        } else {
            this._pushFilter(param0, param1);
        }

    }

    private void _pushFilter(boolean param0, boolean param1) {
        this.oldBlur = this.blur;
        this.oldMipmap = this.mipmap;
        this.setFilter(param0, param1);
    }

    public void popFilter() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_popFilter);
        } else {
            this._popFilter();
        }

    }

    private void _popFilter() {
        this.setFilter(this.oldBlur, this.oldMipmap);
    }

    public int getId() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (this.id == -1) {
            this.id = TextureUtil.generateTextureId();
        }

        return this.id;
    }

    public void releaseId() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                if (this.id != -1) {
                    TextureUtil.releaseTextureId(this.id);
                    this.id = -1;
                }

            });
        } else if (this.id != -1) {
            TextureUtil.releaseTextureId(this.id);
            this.id = -1;
        }

    }

    public abstract void load(ResourceManager var1) throws IOException;

    public void bind() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> GlStateManager._bindTexture(this.getId()));
        } else {
            GlStateManager._bindTexture(this.getId());
        }

    }

    public void reset(TextureManager param0, ResourceManager param1, ResourceLocation param2, Executor param3) {
        param0.register(param2, this);
    }
}
