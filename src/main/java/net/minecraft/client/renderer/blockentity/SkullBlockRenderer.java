package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.renderer.RenderType;
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
    public static SkullBlockRenderer instance;
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

    public void render(SkullBlockEntity param0, double param1, double param2, double param3, float param4, int param5, RenderType param6) {
        float var0 = param0.getMouthAnimation(param4);
        BlockState var1 = param0.getBlockState();
        boolean var2 = var1.getBlock() instanceof WallSkullBlock;
        Direction var3 = var2 ? var1.getValue(WallSkullBlock.FACING) : null;
        float var4 = 22.5F * (float)(var2 ? (2 + var3.get2DDataValue()) * 4 : var1.getValue(SkullBlock.ROTATION));
        this.renderSkull(
            (float)param1, (float)param2, (float)param3, var3, var4, ((AbstractSkullBlock)var1.getBlock()).getType(), param0.getOwnerProfile(), param5, var0
        );
    }

    @Override
    public void init(BlockEntityRenderDispatcher param0) {
        super.init(param0);
        instance = this;
    }

    public void renderSkull(
        float param0,
        float param1,
        float param2,
        @Nullable Direction param3,
        float param4,
        SkullBlock.Type param5,
        @Nullable GameProfile param6,
        int param7,
        float param8
    ) {
        SkullModel var0 = MODEL_BY_TYPE.get(param5);
        if (param7 >= 0) {
            this.bindTexture(BREAKING_LOCATIONS.get(param7));
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(4.0F, 2.0F, 1.0F);
            RenderSystem.translatef(0.0625F, 0.0625F, 0.0625F);
            RenderSystem.matrixMode(5888);
        } else {
            this.bindTexture(this.getLocation(param5, param6));
        }

        RenderSystem.pushMatrix();
        if (param3 == null) {
            RenderSystem.translatef(param0 + 0.5F, param1, param2 + 0.5F);
        } else {
            switch(param3) {
                case NORTH:
                    RenderSystem.translatef(param0 + 0.5F, param1 + 0.25F, param2 + 0.74F);
                    break;
                case SOUTH:
                    RenderSystem.translatef(param0 + 0.5F, param1 + 0.25F, param2 + 0.26F);
                    break;
                case WEST:
                    RenderSystem.translatef(param0 + 0.74F, param1 + 0.25F, param2 + 0.5F);
                    break;
                case EAST:
                default:
                    RenderSystem.translatef(param0 + 0.26F, param1 + 0.25F, param2 + 0.5F);
            }
        }

        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
        if (param5 == SkullBlock.Types.PLAYER) {
            RenderSystem.setProfile(RenderSystem.Profile.PLAYER_SKIN);
        }

        var0.render(param8, 0.0F, 0.0F, param4, 0.0F, 0.0625F);
        RenderSystem.popMatrix();
        if (param7 >= 0) {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }

    }

    private ResourceLocation getLocation(SkullBlock.Type param0, @Nullable GameProfile param1) {
        ResourceLocation var0 = SKIN_BY_TYPE.get(param0);
        if (param0 == SkullBlock.Types.PLAYER && param1 != null) {
            Minecraft var1 = Minecraft.getInstance();
            Map<Type, MinecraftProfileTexture> var2 = var1.getSkinManager().getInsecureSkinInformation(param1);
            if (var2.containsKey(Type.SKIN)) {
                var0 = var1.getSkinManager().registerTexture(var2.get(Type.SKIN), Type.SKIN);
            } else {
                var0 = DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(param1));
            }
        }

        return var0;
    }
}
