package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class BlockEntityWithoutLevelRenderer {
    private static final ShulkerBoxBlockEntity[] SHULKER_BOXES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(ShulkerBoxBlockEntity::new)
        .toArray(param0 -> new ShulkerBoxBlockEntity[param0]);
    private static final ShulkerBoxBlockEntity DEFAULT_SHULKER_BOX = new ShulkerBoxBlockEntity(null);
    public static final BlockEntityWithoutLevelRenderer instance = new BlockEntityWithoutLevelRenderer();
    private final ChestBlockEntity chest = new ChestBlockEntity();
    private final ChestBlockEntity trappedChest = new TrappedChestBlockEntity();
    private final EnderChestBlockEntity enderChest = new EnderChestBlockEntity();
    private final BannerBlockEntity banner = new BannerBlockEntity();
    private final BedBlockEntity bed = new BedBlockEntity();
    private final ConduitBlockEntity conduit = new ConduitBlockEntity();
    private final ShieldModel shieldModel = new ShieldModel();
    private final TridentModel tridentModel = new TridentModel();

    public void renderByItem(ItemStack param0, ItemTransforms.TransformType param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Item var0 = param0.getItem();
        if (var0 instanceof BlockItem) {
            Block var1 = ((BlockItem)var0).getBlock();
            if (var1 instanceof AbstractSkullBlock) {
                GameProfile var2 = null;
                if (param0.hasTag()) {
                    CompoundTag var3 = param0.getTag();
                    if (var3.contains("SkullOwner", 10)) {
                        var2 = NbtUtils.readGameProfile(var3.getCompound("SkullOwner"));
                    } else if (var3.contains("SkullOwner", 8) && !StringUtils.isBlank(var3.getString("SkullOwner"))) {
                        GameProfile var161 = new GameProfile(null, var3.getString("SkullOwner"));
                        var2 = SkullBlockEntity.updateGameprofile(var161);
                        var3.remove("SkullOwner");
                        var3.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), var2));
                    }
                }

                SkullBlockRenderer.renderSkull(null, 180.0F, ((AbstractSkullBlock)var1).getType(), var2, 0.0F, param2, param3, param4);
            } else {
                BlockEntity var4;
                if (var1 instanceof AbstractBannerBlock) {
                    this.banner.fromItem(param0, ((AbstractBannerBlock)var1).getColor());
                    var4 = this.banner;
                } else if (var1 instanceof BedBlock) {
                    this.bed.setColor(((BedBlock)var1).getColor());
                    var4 = this.bed;
                } else if (var1 == Blocks.CONDUIT) {
                    var4 = this.conduit;
                } else if (var1 == Blocks.CHEST) {
                    var4 = this.chest;
                } else if (var1 == Blocks.ENDER_CHEST) {
                    var4 = this.enderChest;
                } else if (var1 == Blocks.TRAPPED_CHEST) {
                    var4 = this.trappedChest;
                } else {
                    if (!(var1 instanceof ShulkerBoxBlock)) {
                        return;
                    }

                    DyeColor var10 = ShulkerBoxBlock.getColorFromItem(var0);
                    if (var10 == null) {
                        var4 = DEFAULT_SHULKER_BOX;
                    } else {
                        var4 = SHULKER_BOXES[var10.getId()];
                    }
                }

                BlockEntityRenderDispatcher.instance.renderItem(var4, param2, param3, param4, param5);
            }
        } else {
            if (var0 == Items.SHIELD) {
                boolean var14 = param0.getTagElement("BlockEntityTag") != null;
                param2.pushPose();
                param2.scale(1.0F, -1.0F, -1.0F);
                Material var15 = var14 ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
                VertexConsumer var16 = var15.sprite()
                    .wrap(ItemRenderer.getFoilBufferDirect(param3, this.shieldModel.renderType(var15.atlasLocation()), true, param0.hasFoil()));
                this.shieldModel.handle().render(param2, var16, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
                if (var14) {
                    List<Pair<BannerPattern, DyeColor>> var17 = BannerBlockEntity.createPatterns(
                        ShieldItem.getColor(param0), BannerBlockEntity.getItemPatterns(param0)
                    );
                    BannerRenderer.renderPatterns(param2, param3, param4, param5, this.shieldModel.plate(), var15, false, var17, param0.hasFoil());
                } else {
                    this.shieldModel.plate().render(param2, var16, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
                }

                param2.popPose();
            } else if (var0 == Items.TRIDENT) {
                param2.pushPose();
                param2.scale(1.0F, -1.0F, -1.0F);
                VertexConsumer var18 = ItemRenderer.getFoilBufferDirect(param3, this.tridentModel.renderType(TridentModel.TEXTURE), false, param0.hasFoil());
                this.tridentModel.renderToBuffer(param2, var18, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
                param2.popPose();
            }

        }
    }
}
