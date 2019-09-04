package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
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
    private final SkullBlockEntity skull = new SkullBlockEntity();
    private final ConduitBlockEntity conduit = new ConduitBlockEntity();
    private final ShieldModel shieldModel = new ShieldModel();
    private final TridentModel tridentModel = new TridentModel();

    public void renderByItem(ItemStack param0) {
        Item var0 = param0.getItem();
        if (var0 instanceof BannerItem) {
            this.banner.fromItem(param0, ((BannerItem)var0).getColor());
            BlockEntityRenderDispatcher.instance.renderItem(this.banner);
        } else if (var0 instanceof BlockItem && ((BlockItem)var0).getBlock() instanceof BedBlock) {
            this.bed.setColor(((BedBlock)((BlockItem)var0).getBlock()).getColor());
            BlockEntityRenderDispatcher.instance.renderItem(this.bed);
        } else if (var0 == Items.SHIELD) {
            if (param0.getTagElement("BlockEntityTag") != null) {
                this.banner.fromItem(param0, ShieldItem.getColor(param0));
                Minecraft.getInstance()
                    .getTextureManager()
                    .bind(BannerTextures.SHIELD_CACHE.getTextureLocation(this.banner.getTextureHashName(), this.banner.getPatterns(), this.banner.getColors()));
            } else {
                Minecraft.getInstance().getTextureManager().bind(BannerTextures.NO_PATTERN_SHIELD);
            }

            RenderSystem.pushMatrix();
            RenderSystem.scalef(1.0F, -1.0F, -1.0F);
            this.shieldModel.render();
            if (param0.hasFoil()) {
                this.renderFoil(this.shieldModel::render);
            }

            RenderSystem.popMatrix();
        } else if (var0 instanceof BlockItem && ((BlockItem)var0).getBlock() instanceof AbstractSkullBlock) {
            GameProfile var1 = null;
            if (param0.hasTag()) {
                CompoundTag var2 = param0.getTag();
                if (var2.contains("SkullOwner", 10)) {
                    var1 = NbtUtils.readGameProfile(var2.getCompound("SkullOwner"));
                } else if (var2.contains("SkullOwner", 8) && !StringUtils.isBlank(var2.getString("SkullOwner"))) {
                    GameProfile var6 = new GameProfile(null, var2.getString("SkullOwner"));
                    var1 = SkullBlockEntity.updateGameprofile(var6);
                    var2.remove("SkullOwner");
                    var2.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var1));
                }
            }

            if (SkullBlockRenderer.instance != null) {
                RenderSystem.pushMatrix();
                RenderSystem.disableCull();
                SkullBlockRenderer.instance
                    .renderSkull(0.0F, 0.0F, 0.0F, null, 180.0F, ((AbstractSkullBlock)((BlockItem)var0).getBlock()).getType(), var1, -1, 0.0F);
                RenderSystem.enableCull();
                RenderSystem.popMatrix();
            }
        } else if (var0 == Items.TRIDENT) {
            Minecraft.getInstance().getTextureManager().bind(TridentModel.TEXTURE);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(1.0F, -1.0F, -1.0F);
            this.tridentModel.render();
            if (param0.hasFoil()) {
                this.renderFoil(this.tridentModel::render);
            }

            RenderSystem.popMatrix();
        } else if (var0 instanceof BlockItem && ((BlockItem)var0).getBlock() == Blocks.CONDUIT) {
            BlockEntityRenderDispatcher.instance.renderItem(this.conduit);
        } else if (var0 == Blocks.ENDER_CHEST.asItem()) {
            BlockEntityRenderDispatcher.instance.renderItem(this.enderChest);
        } else if (var0 == Blocks.TRAPPED_CHEST.asItem()) {
            BlockEntityRenderDispatcher.instance.renderItem(this.trappedChest);
        } else if (Block.byItem(var0) instanceof ShulkerBoxBlock) {
            DyeColor var3 = ShulkerBoxBlock.getColorFromItem(var0);
            if (var3 == null) {
                BlockEntityRenderDispatcher.instance.renderItem(DEFAULT_SHULKER_BOX);
            } else {
                BlockEntityRenderDispatcher.instance.renderItem(SHULKER_BOXES[var3.getId()]);
            }
        } else {
            BlockEntityRenderDispatcher.instance.renderItem(this.chest);
        }

    }

    private void renderFoil(Runnable param0) {
        RenderSystem.color3f(0.5019608F, 0.2509804F, 0.8F);
        Minecraft.getInstance().getTextureManager().bind(ItemRenderer.ENCHANT_GLINT_LOCATION);
        ItemRenderer.renderFoilLayer(Minecraft.getInstance().getTextureManager(), param0, 1);
    }
}
