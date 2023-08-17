package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PlayerSkinWidget extends AbstractWidget {
    private static final float MODEL_OFFSET = 0.0625F;
    private static final float MODEL_HEIGHT = 2.125F;
    private static final float Z_OFFSET = 100.0F;
    private static final float ROTATION_SENSITIVITY = 2.5F;
    private static final float DEFAULT_ROTATION_X = -5.0F;
    private static final float DEFAULT_ROTATION_Y = 30.0F;
    private static final float ROTATION_X_LIMIT = 50.0F;
    private final PlayerSkinWidget.Model model;
    private final Supplier<PlayerSkin> skin;
    private float rotationX = -5.0F;
    private float rotationY = 30.0F;

    public PlayerSkinWidget(int param0, int param1, EntityModelSet param2, Supplier<PlayerSkin> param3) {
        super(0, 0, param0, param1, CommonComponents.EMPTY);
        this.model = PlayerSkinWidget.Model.bake(param2);
        this.skin = param3;
    }

    @Override
    protected void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        param0.pose().pushPose();
        param0.pose().translate((float)this.getX() + (float)this.getWidth() / 2.0F, (float)(this.getY() + this.getHeight()), 100.0F);
        float var0 = (float)this.getHeight() / 2.125F;
        param0.pose().scale(var0, var0, var0);
        param0.pose().translate(0.0F, -0.0625F, 0.0F);
        Matrix4f var1 = param0.pose().last().pose();
        var1.rotateAround(Axis.XP.rotationDegrees(this.rotationX), 0.0F, -1.0625F, 0.0F);
        param0.pose().mulPose(Axis.YP.rotationDegrees(this.rotationY));
        this.model.render(param0, this.skin.get());
        param0.pose().popPose();
    }

    @Override
    protected void onDrag(double param0, double param1, double param2, double param3) {
        this.rotationX = Mth.clamp(this.rotationX - (float)param3 * 2.5F, -50.0F, 50.0F);
        this.rotationY += (float)param2 * 2.5F;
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    static record Model(PlayerModel<?> wideModel, PlayerModel<?> slimModel) {
        public static PlayerSkinWidget.Model bake(EntityModelSet param0) {
            PlayerModel<?> var0 = new PlayerModel(param0.bakeLayer(ModelLayers.PLAYER), false);
            PlayerModel<?> var1 = new PlayerModel(param0.bakeLayer(ModelLayers.PLAYER_SLIM), true);
            var0.young = false;
            var1.young = false;
            return new PlayerSkinWidget.Model(var0, var1);
        }

        public void render(GuiGraphics param0, PlayerSkin param1) {
            param0.flush();
            Lighting.setupForEntityInInventory();
            param0.pose().pushPose();
            param0.pose().mulPoseMatrix(new Matrix4f().scaling(1.0F, 1.0F, -1.0F));
            param0.pose().translate(0.0F, -1.5F, 0.0F);
            PlayerModel<?> var0 = param1.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel;
            RenderType var1 = var0.renderType(param1.texture());
            var0.renderToBuffer(param0.pose(), param0.bufferSource().getBuffer(var1), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            param0.pose().popPose();
            param0.flush();
            Lighting.setupFor3DItems();
        }
    }
}
