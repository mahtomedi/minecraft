package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.HeadedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
    public CustomHeadLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        ItemStack var0 = param0.getItemBySlot(EquipmentSlot.HEAD);
        if (!var0.isEmpty()) {
            Item var1 = var0.getItem();
            GlStateManager.pushMatrix();
            if (param0.isVisuallySneaking()) {
                GlStateManager.translatef(0.0F, 0.2F, 0.0F);
            }

            boolean var2 = param0 instanceof Villager || param0 instanceof ZombieVillager;
            if (param0.isBaby() && !(param0 instanceof Villager)) {
                float var3 = 2.0F;
                float var4 = 1.4F;
                GlStateManager.translatef(0.0F, 0.5F * param7, 0.0F);
                GlStateManager.scalef(0.7F, 0.7F, 0.7F);
                GlStateManager.translatef(0.0F, 16.0F * param7, 0.0F);
            }

            this.getParentModel().translateToHead(0.0625F);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (var1 instanceof BlockItem && ((BlockItem)var1).getBlock() instanceof AbstractSkullBlock) {
                float var5 = 1.1875F;
                GlStateManager.scalef(1.1875F, -1.1875F, -1.1875F);
                if (var2) {
                    GlStateManager.translatef(0.0F, 0.0625F, 0.0F);
                }

                GameProfile var6 = null;
                if (var0.hasTag()) {
                    CompoundTag var7 = var0.getTag();
                    if (var7.contains("SkullOwner", 10)) {
                        var6 = NbtUtils.readGameProfile(var7.getCompound("SkullOwner"));
                    } else if (var7.contains("SkullOwner", 8)) {
                        String var8 = var7.getString("SkullOwner");
                        if (!StringUtils.isBlank(var8)) {
                            var6 = SkullBlockEntity.updateGameprofile(new GameProfile(null, var8));
                            var7.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var6));
                        }
                    }
                }

                SkullBlockRenderer.instance
                    .renderSkull(-0.5F, 0.0F, -0.5F, null, 180.0F, ((AbstractSkullBlock)((BlockItem)var1).getBlock()).getType(), var6, -1, param1);
            } else if (!(var1 instanceof ArmorItem) || ((ArmorItem)var1).getSlot() != EquipmentSlot.HEAD) {
                float var9 = 0.625F;
                GlStateManager.translatef(0.0F, -0.25F, 0.0F);
                GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scalef(0.625F, -0.625F, -0.625F);
                if (var2) {
                    GlStateManager.translatef(0.0F, 0.1875F, 0.0F);
                }

                Minecraft.getInstance().getItemInHandRenderer().renderItem(param0, var0, ItemTransforms.TransformType.HEAD);
            }

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
