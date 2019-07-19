package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractTexture implements TextureObject {
    protected int id = -1;
    protected boolean blur;
    protected boolean mipmap;
    protected boolean oldBlur;
    protected boolean oldMipmap;

    public void setFilter(boolean param0, boolean param1) {
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

        GlStateManager.texParameter(3553, 10241, var0);
        GlStateManager.texParameter(3553, 10240, var1);
    }

    @Override
    public void pushFilter(boolean param0, boolean param1) {
        this.oldBlur = this.blur;
        this.oldMipmap = this.mipmap;
        this.setFilter(param0, param1);
    }

    @Override
    public void popFilter() {
        this.setFilter(this.oldBlur, this.oldMipmap);
    }

    @Override
    public int getId() {
        if (this.id == -1) {
            this.id = TextureUtil.generateTextureId();
        }

        return this.id;
    }

    public void releaseId() {
        if (this.id != -1) {
            TextureUtil.releaseTextureId(this.id);
            this.id = -1;
        }

    }
}
