package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemInHandRenderer itemInHandRenderer;

    public CustomHeadLayer(RenderLayerParent<T, M> param0, EntityModelSet param1, ItemInHandRenderer param2) {
        this(param0, param1, 1.0F, 1.0F, 1.0F, param2);
    }

    public CustomHeadLayer(RenderLayerParent<T, M> param0, EntityModelSet param1, float param2, float param3, float param4, ItemInHandRenderer param5) {
        super(param0);
        this.scaleX = param2;
        this.scaleY = param3;
        this.scaleZ = param4;
        this.skullModels = SkullBlockRenderer.createSkullRenderers(param1);
        this.itemInHandRenderer = param5;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        ItemStack var0 = param3.getItemBySlot(EquipmentSlot.HEAD);
        if (!var0.isEmpty()) {
            Item var1 = var0.getItem();
            param0.pushPose();
            param0.scale(this.scaleX, this.scaleY, this.scaleZ);
            boolean var2 = param3 instanceof Villager || param3 instanceof ZombieVillager;
            if (param3.isBaby() && !(param3 instanceof Villager)) {
                float var3 = 2.0F;
                float var4 = 1.4F;
                param0.translate(0.0F, 0.03125F, 0.0F);
                param0.scale(0.7F, 0.7F, 0.7F);
                param0.translate(0.0F, 1.0F, 0.0F);
            }

            this.getParentModel().getHead().translateAndRotate(param0);
            if (var1 instanceof BlockItem && ((BlockItem)var1).getBlock() instanceof AbstractSkullBlock) {
                float var5 = 1.1875F;
                param0.scale(1.1875F, -1.1875F, -1.1875F);
                if (var2) {
                    param0.translate(0.0F, 0.0625F, 0.0F);
                }

                GameProfile var6 = null;
                if (var0.hasTag()) {
                    CompoundTag var7 = var0.getTag();
                    if (var7.contains("SkullOwner", 10)) {
                        var6 = NbtUtils.readGameProfile(var7.getCompound("SkullOwner"));
                    }
                }

                param0.translate(-0.5, 0.0, -0.5);
                SkullBlock.Type var8 = ((AbstractSkullBlock)((BlockItem)var1).getBlock()).getType();
                SkullModelBase var9 = this.skullModels.get(var8);
                RenderType var10 = SkullBlockRenderer.getRenderType(var8, var6);
                Entity var22 = param3.getVehicle();
                WalkAnimationState var12;
                if (var22 instanceof LivingEntity var11) {
                    var12 = var11.walkAnimation;
                } else {
                    var12 = param3.walkAnimation;
                }

                float var14 = var12.position(param6);
                SkullBlockRenderer.renderSkull(null, 180.0F, var14, param0, param1, param2, var9, var10);
            } else if (!(var1 instanceof ArmorItem var15) || var15.getEquipmentSlot() != EquipmentSlot.HEAD) {
                translateToHead(param0, var2);
                this.itemInHandRenderer.renderItem(param3, var0, ItemDisplayContext.HEAD, false, param0, param1, param2);
            }

            param0.popPose();
        }
    }

    public static void translateToHead(PoseStack param0, boolean param1) {
        float var0 = 0.625F;
        param0.translate(0.0F, -0.25F, 0.0F);
        param0.mulPose(Axis.YP.rotationDegrees(180.0F));
        param0.scale(0.625F, -0.625F, -0.625F);
        if (param1) {
            param0.translate(0.0F, 0.1875F, 0.0F);
        }

    }
}
