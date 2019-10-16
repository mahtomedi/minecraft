package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullBlockRenderer extends BlockEntityRenderer<SkullBlockEntity> {
    private static final Map<SkullBlock.Type, SkullModel> MODEL_BY_TYPE = Util.make(Maps.newHashMap(), param0 -> {
        SkullModel var0 = new SkullModel(0, 0, 64, 32);
        SkullModel var1 = new HumanoidHeadModel();
        DragonHeadModel var2 = new DragonHeadModel(0.0F);
        param0.put(SkullBlock.Types.SKELETON, var0);
        param0.put(SkullBlock.Types.WITHER_SKELETON, var0);
        param0.put(SkullBlock.Types.PLAYER, var1);
        param0.put(SkullBlock.Types.ZOMBIE, var1);
        param0.put(SkullBlock.Types.CREEPER, var0);
        param0.put(SkullBlock.Types.DRAGON, var2);
    });
    private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
        param0.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
        param0.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
        param0.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
        param0.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
        param0.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
    });

    public SkullBlockRenderer(BlockEntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        SkullBlockEntity param0, double param1, double param2, double param3, float param4, PoseStack param5, MultiBufferSource param6, int param7, int param8
    ) {
        float var0 = param0.getMouthAnimation(param4);
        BlockState var1 = param0.getBlockState();
        boolean var2 = var1.getBlock() instanceof WallSkullBlock;
        Direction var3 = var2 ? var1.getValue(WallSkullBlock.FACING) : null;
        float var4 = 22.5F * (float)(var2 ? (2 + var3.get2DDataValue()) * 4 : var1.getValue(SkullBlock.ROTATION));
        renderSkull(var3, var4, ((AbstractSkullBlock)var1.getBlock()).getType(), param0.getOwnerProfile(), var0, param5, param6, param7);
    }

    public static void renderSkull(
        @Nullable Direction param0,
        float param1,
        SkullBlock.Type param2,
        @Nullable GameProfile param3,
        float param4,
        PoseStack param5,
        MultiBufferSource param6,
        int param7
    ) {
        SkullModel var0 = MODEL_BY_TYPE.get(param2);
        param5.pushPose();
        if (param0 == null) {
            param5.translate(0.5, 0.0, 0.5);
        } else {
            switch(param0) {
                case NORTH:
                    param5.translate(0.5, 0.25, 0.74F);
                    break;
                case SOUTH:
                    param5.translate(0.5, 0.25, 0.26F);
                    break;
                case WEST:
                    param5.translate(0.74F, 0.25, 0.5);
                    break;
                case EAST:
                default:
                    param5.translate(0.26F, 0.25, 0.5);
            }
        }

        param5.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer var1 = param6.getBuffer(getRenderType(param2, param3));
        var0.setupAnim(param4, param1, 0.0F);
        var0.renderToBuffer(param5, var1, param7, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
        param5.popPose();
    }

    private static RenderType getRenderType(SkullBlock.Type param0, @Nullable GameProfile param1) {
        ResourceLocation var0 = SKIN_BY_TYPE.get(param0);
        if (param0 == SkullBlock.Types.PLAYER && param1 != null) {
            Minecraft var1 = Minecraft.getInstance();
            Map<Type, MinecraftProfileTexture> var2 = var1.getSkinManager().getInsecureSkinInformation(param1);
            return var2.containsKey(Type.SKIN)
                ? RenderType.entityTranslucent(var1.getSkinManager().registerTexture(var2.get(Type.SKIN), Type.SKIN))
                : RenderType.entityCutout(DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(param1)));
        } else {
            return RenderType.entityCutout(var0);
        }
    }
}
