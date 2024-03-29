package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SignEditScreen extends AbstractSignEditScreen {
    public static final float MAGIC_SCALE_NUMBER = 62.500004F;
    public static final float MAGIC_TEXT_SCALE = 0.9765628F;
    private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
    @Nullable
    private SignRenderer.SignModel signModel;

    public SignEditScreen(SignBlockEntity param0, boolean param1, boolean param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void init() {
        super.init();
        this.signModel = SignRenderer.createSignModel(this.minecraft.getEntityModels(), this.woodType);
    }

    @Override
    protected void offsetSign(GuiGraphics param0, BlockState param1) {
        super.offsetSign(param0, param1);
        boolean var0 = param1.getBlock() instanceof StandingSignBlock;
        if (!var0) {
            param0.pose().translate(0.0F, 35.0F, 0.0F);
        }

    }

    @Override
    protected void renderSignBackground(GuiGraphics param0, BlockState param1) {
        if (this.signModel != null) {
            boolean var0 = param1.getBlock() instanceof StandingSignBlock;
            param0.pose().translate(0.0F, 31.0F, 0.0F);
            param0.pose().scale(62.500004F, 62.500004F, -62.500004F);
            Material var1 = Sheets.getSignMaterial(this.woodType);
            VertexConsumer var2 = var1.buffer(param0.bufferSource(), this.signModel::renderType);
            this.signModel.stick.visible = var0;
            this.signModel.root.render(param0.pose(), var2, 15728880, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    protected Vector3f getSignTextScale() {
        return TEXT_SCALE;
    }
}
