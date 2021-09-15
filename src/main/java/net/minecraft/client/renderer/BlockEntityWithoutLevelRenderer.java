package net.minecraft.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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
import net.minecraft.world.level.block.SkullBlock;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class BlockEntityWithoutLevelRenderer implements ResourceManagerReloadListener {
    private static final ShulkerBoxBlockEntity[] SHULKER_BOXES = Arrays.stream(DyeColor.values())
        .sorted(Comparator.comparingInt(DyeColor::getId))
        .map(param0 -> new ShulkerBoxBlockEntity(param0, BlockPos.ZERO, Blocks.SHULKER_BOX.defaultBlockState()))
        .toArray(param0 -> new ShulkerBoxBlockEntity[param0]);
    private static final ShulkerBoxBlockEntity DEFAULT_SHULKER_BOX = new ShulkerBoxBlockEntity(BlockPos.ZERO, Blocks.SHULKER_BOX.defaultBlockState());
    private final ChestBlockEntity chest = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
    private final ChestBlockEntity trappedChest = new TrappedChestBlockEntity(BlockPos.ZERO, Blocks.TRAPPED_CHEST.defaultBlockState());
    private final EnderChestBlockEntity enderChest = new EnderChestBlockEntity(BlockPos.ZERO, Blocks.ENDER_CHEST.defaultBlockState());
    private final BannerBlockEntity banner = new BannerBlockEntity(BlockPos.ZERO, Blocks.WHITE_BANNER.defaultBlockState());
    private final BedBlockEntity bed = new BedBlockEntity(BlockPos.ZERO, Blocks.RED_BED.defaultBlockState());
    private final ConduitBlockEntity conduit = new ConduitBlockEntity(BlockPos.ZERO, Blocks.CONDUIT.defaultBlockState());
    private ShieldModel shieldModel;
    private TridentModel tridentModel;
    private Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final EntityModelSet entityModelSet;

    public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher param0, EntityModelSet param1) {
        this.blockEntityRenderDispatcher = param0;
        this.entityModelSet = param1;
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.shieldModel = new ShieldModel(this.entityModelSet.bakeLayer(ModelLayers.SHIELD));
        this.tridentModel = new TridentModel(this.entityModelSet.bakeLayer(ModelLayers.TRIDENT));
        this.skullModels = SkullBlockRenderer.createSkullRenderers(this.entityModelSet);
    }

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
                        var2 = new GameProfile(null, var3.getString("SkullOwner"));
                        var3.remove("SkullOwner");
                        SkullBlockEntity.updateGameprofile(var2, param1x -> var3.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), param1x)));
                    }
                }

                SkullBlock.Type var4 = ((AbstractSkullBlock)var1).getType();
                SkullModelBase var5 = this.skullModels.get(var4);
                RenderType var6 = SkullBlockRenderer.getRenderType(var4, var2);
                SkullBlockRenderer.renderSkull(null, 180.0F, 0.0F, param2, param3, param4, var5, var6);
            } else {
                BlockState var7 = var1.defaultBlockState();
                BlockEntity var8;
                if (var1 instanceof AbstractBannerBlock) {
                    this.banner.fromItem(param0, ((AbstractBannerBlock)var1).getColor());
                    var8 = this.banner;
                } else if (var1 instanceof BedBlock) {
                    this.bed.setColor(((BedBlock)var1).getColor());
                    var8 = this.bed;
                } else if (var7.is(Blocks.CONDUIT)) {
                    var8 = this.conduit;
                } else if (var7.is(Blocks.CHEST)) {
                    var8 = this.chest;
                } else if (var7.is(Blocks.ENDER_CHEST)) {
                    var8 = this.enderChest;
                } else if (var7.is(Blocks.TRAPPED_CHEST)) {
                    var8 = this.trappedChest;
                } else {
                    if (!(var1 instanceof ShulkerBoxBlock)) {
                        return;
                    }

                    DyeColor var14 = ShulkerBoxBlock.getColorFromItem(var0);
                    if (var14 == null) {
                        var8 = DEFAULT_SHULKER_BOX;
                    } else {
                        var8 = SHULKER_BOXES[var14.getId()];
                    }
                }

                this.blockEntityRenderDispatcher.renderItem(var8, param2, param3, param4, param5);
            }
        } else {
            if (param0.is(Items.SHIELD)) {
                boolean var18 = BlockItem.getBlockEntityData(param0) != null;
                param2.pushPose();
                param2.scale(1.0F, -1.0F, -1.0F);
                Material var19 = var18 ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
                VertexConsumer var20 = var19.sprite()
                    .wrap(ItemRenderer.getFoilBufferDirect(param3, this.shieldModel.renderType(var19.atlasLocation()), true, param0.hasFoil()));
                this.shieldModel.handle().render(param2, var20, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
                if (var18) {
                    List<Pair<BannerPattern, DyeColor>> var21 = BannerBlockEntity.createPatterns(
                        ShieldItem.getColor(param0), BannerBlockEntity.getItemPatterns(param0)
                    );
                    BannerRenderer.renderPatterns(param2, param3, param4, param5, this.shieldModel.plate(), var19, false, var21, param0.hasFoil());
                } else {
                    this.shieldModel.plate().render(param2, var20, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
                }

                param2.popPose();
            } else if (param0.is(Items.TRIDENT)) {
                param2.pushPose();
                param2.scale(1.0F, -1.0F, -1.0F);
                VertexConsumer var22 = ItemRenderer.getFoilBufferDirect(param3, this.tridentModel.renderType(TridentModel.TEXTURE), false, param0.hasFoil());
                this.tridentModel.renderToBuffer(param2, var22, param4, param5, 1.0F, 1.0F, 1.0F, 1.0F);
                param2.popPose();
            }

        }
    }
}
