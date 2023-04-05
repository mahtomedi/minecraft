package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HangingSignEditScreen extends AbstractSignEditScreen {
    public static final float MAGIC_BACKGROUND_SCALE = 4.5F;
    private static final Vector3f TEXT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 16;
    private final ResourceLocation texture = new ResourceLocation("textures/gui/hanging_signs/" + this.woodType.name() + ".png");

    public HangingSignEditScreen(SignBlockEntity param0, boolean param1, boolean param2) {
        super(param0, param1, param2, Component.translatable("hanging_sign.edit"));
    }

    @Override
    protected void offsetSign(PoseStack param0, BlockState param1) {
        param0.translate((float)this.width / 2.0F, 125.0F, 50.0F);
    }

    @Override
    protected void renderSignBackground(PoseStack param0, MultiBufferSource.BufferSource param1, BlockState param2) {
        param0.translate(0.0F, -13.0F, 0.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        param0.scale(4.5F, 4.5F, 1.0F);
        blit(param0, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    @Override
    protected Vector3f getSignTextScale() {
        return TEXT_SCALE;
    }
}
