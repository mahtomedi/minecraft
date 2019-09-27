package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class EntityBlockRenderer {
    private static final ShulkerBoxBlockEntity[] SHULKER_BOXES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(ShulkerBoxBlockEntity::new)
        .toArray(param0 -> new ShulkerBoxBlockEntity[param0]);
    private static final ShulkerBoxBlockEntity DEFAULT_SHULKER_BOX = new ShulkerBoxBlockEntity(null);
    public static final EntityBlockRenderer instance = new EntityBlockRenderer();
    private final ChestBlockEntity chest = new ChestBlockEntity();
    private final ChestBlockEntity trappedChest = new TrappedChestBlockEntity();
    private final EnderChestBlockEntity enderChest = new EnderChestBlockEntity();
    private final BannerBlockEntity banner = new BannerBlockEntity();
    private final BedBlockEntity bed = new BedBlockEntity();
    private final ConduitBlockEntity conduit = new ConduitBlockEntity();
    private final ShieldModel shieldModel = new ShieldModel();
    private final TridentModel tridentModel = new TridentModel();

    public void renderByItem(ItemStack param0, PoseStack param1, MultiBufferSource param2, int param3) {
        Item var0 = param0.getItem();
        if (var0 instanceof BlockItem) {
            Block var1 = ((BlockItem)var0).getBlock();
            if (var1 instanceof AbstractBannerBlock) {
                this.banner.fromItem(param0, ((AbstractBannerBlock)var1).getColor());
                BlockEntityRenderDispatcher.instance.renderItem(this.banner, param1, param2, param3);
            } else if (var1 instanceof BedBlock) {
                this.bed.setColor(((BedBlock)var1).getColor());
                BlockEntityRenderDispatcher.instance.renderItem(this.bed, param1, param2, param3);
            } else if (var1 instanceof AbstractSkullBlock) {
                GameProfile var2 = null;
                if (param0.hasTag()) {
                    CompoundTag var3 = param0.getTag();
                    if (var3.contains("SkullOwner", 10)) {
                        var2 = NbtUtils.readGameProfile(var3.getCompound("SkullOwner"));
                    } else if (var3.contains("SkullOwner", 8) && !StringUtils.isBlank(var3.getString("SkullOwner"))) {
                        GameProfile var12 = new GameProfile(null, var3.getString("SkullOwner"));
                        var2 = SkullBlockEntity.updateGameprofile(var12);
                        var3.remove("SkullOwner");
                        var3.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var2));
                    }
                }

                SkullBlockRenderer.renderSkull(null, 180.0F, ((AbstractSkullBlock)var1).getType(), var2, 0.0F, param1, param2, param3);
            } else if (var1 == Blocks.CONDUIT) {
                BlockEntityRenderDispatcher.instance.renderItem(this.conduit, param1, param2, param3);
            } else if (var1 == Blocks.CHEST) {
                BlockEntityRenderDispatcher.instance.renderItem(this.chest, param1, param2, param3);
            } else if (var1 == Blocks.ENDER_CHEST) {
                BlockEntityRenderDispatcher.instance.renderItem(this.enderChest, param1, param2, param3);
            } else if (var1 == Blocks.TRAPPED_CHEST) {
                BlockEntityRenderDispatcher.instance.renderItem(this.trappedChest, param1, param2, param3);
            } else if (var1 instanceof ShulkerBoxBlock) {
                DyeColor var4 = ShulkerBoxBlock.getColorFromItem(var0);
                if (var4 == null) {
                    BlockEntityRenderDispatcher.instance.renderItem(DEFAULT_SHULKER_BOX, param1, param2, param3);
                } else {
                    BlockEntityRenderDispatcher.instance.renderItem(SHULKER_BOXES[var4.getId()], param1, param2, param3);
                }
            }

        } else {
            if (var0 == Items.SHIELD) {
                ResourceLocation var5;
                if (param0.getTagElement("BlockEntityTag") != null) {
                    this.banner.fromItem(param0, ShieldItem.getColor(param0));
                    var5 = BannerTextures.SHIELD_CACHE.getTextureLocation(this.banner.getTextureHashName(), this.banner.getPatterns(), this.banner.getColors());
                } else {
                    var5 = BannerTextures.NO_PATTERN_SHIELD;
                }

                param1.pushPose();
                param1.scale(1.0F, -1.0F, -1.0F);
                VertexConsumer var7 = ItemRenderer.getFoilBuffer(param2, var5, false, param0.hasFoil(), false);
                OverlayTexture.setDefault(var7);
                this.shieldModel.render(param1, var7, param3);
                var7.unsetDefaultOverlayCoords();
                param1.popPose();
            } else if (var0 == Items.TRIDENT) {
                Minecraft.getInstance().getTextureManager().bind(TridentModel.TEXTURE);
                param1.pushPose();
                param1.scale(1.0F, -1.0F, -1.0F);
                VertexConsumer var8 = ItemRenderer.getFoilBuffer(param2, TridentModel.TEXTURE, false, param0.hasFoil(), false);
                OverlayTexture.setDefault(var8);
                this.tridentModel.render(param1, var8, param3);
                var8.unsetDefaultOverlayCoords();
                param1.popPose();
            }

        }
    }
}
