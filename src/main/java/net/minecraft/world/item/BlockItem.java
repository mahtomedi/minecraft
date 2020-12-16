package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockItem extends Item {
    @Deprecated
    private final Block block;

    public BlockItem(Block param0, Item.Properties param1) {
        super(param1);
        this.block = param0;
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        InteractionResult var0 = this.place(new BlockPlaceContext(param0));
        return !var0.consumesAction() && this.isEdible() ? this.use(param0.getLevel(), param0.getPlayer(), param0.getHand()).getResult() : var0;
    }

    public InteractionResult place(BlockPlaceContext param0) {
        if (!param0.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockPlaceContext var0 = this.updatePlacementContext(param0);
            if (var0 == null) {
                return InteractionResult.FAIL;
            } else {
                BlockState var1 = this.getPlacementState(var0);
                if (var1 == null) {
                    return InteractionResult.FAIL;
                } else if (!this.placeBlock(var0, var1)) {
                    return InteractionResult.FAIL;
                } else {
                    BlockPos var2 = var0.getClickedPos();
                    Level var3 = var0.getLevel();
                    Player var4 = var0.getPlayer();
                    ItemStack var5 = var0.getItemInHand();
                    BlockState var6 = var3.getBlockState(var2);
                    if (var6.is(var1.getBlock())) {
                        var6 = this.updateBlockStateFromTag(var2, var3, var5, var6);
                        this.updateCustomBlockEntityTag(var2, var3, var4, var5, var6);
                        var6.getBlock().setPlacedBy(var3, var2, var6, var4, var5);
                        if (var4 instanceof ServerPlayer) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)var4, var2, var5);
                        }
                    }

                    SoundType var7 = var6.getSoundType();
                    var3.playSound(var4, var2, this.getPlaceSound(var6), SoundSource.BLOCKS, (var7.getVolume() + 1.0F) / 2.0F, var7.getPitch() * 0.8F);
                    var3.gameEvent(var4, GameEvent.BLOCK_PLACE, var2);
                    if (var4 == null || !var4.getAbilities().instabuild) {
                        var5.shrink(1);
                    }

                    return InteractionResult.sidedSuccess(var3.isClientSide);
                }
            }
        }
    }

    protected SoundEvent getPlaceSound(BlockState param0) {
        return param0.getSoundType().getPlaceSound();
    }

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext param0) {
        return param0;
    }

    protected boolean updateCustomBlockEntityTag(BlockPos param0, Level param1, @Nullable Player param2, ItemStack param3, BlockState param4) {
        return updateCustomBlockEntityTag(param1, param2, param0, param3);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext param0) {
        BlockState var0 = this.getBlock().getStateForPlacement(param0);
        return var0 != null && this.canPlace(param0, var0) ? var0 : null;
    }

    private BlockState updateBlockStateFromTag(BlockPos param0, Level param1, ItemStack param2, BlockState param3) {
        BlockState var0 = param3;
        CompoundTag var1 = param2.getTag();
        if (var1 != null) {
            CompoundTag var2 = var1.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> var3 = param3.getBlock().getStateDefinition();

            for(String var4 : var2.getAllKeys()) {
                Property<?> var5 = var3.getProperty(var4);
                if (var5 != null) {
                    String var6 = var2.get(var4).getAsString();
                    var0 = updateState(var0, var5, var6);
                }
            }
        }

        if (var0 != param3) {
            param1.setBlock(param0, var0, 2);
        }

        return var0;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState param0, Property<T> param1, String param2) {
        return param1.getValue(param2).map(param2x -> param0.setValue(param1, param2x)).orElse(param0);
    }

    protected boolean canPlace(BlockPlaceContext param0, BlockState param1) {
        Player var0 = param0.getPlayer();
        CollisionContext var1 = var0 == null ? CollisionContext.empty() : CollisionContext.of(var0);
        return (!this.mustSurvive() || param1.canSurvive(param0.getLevel(), param0.getClickedPos()))
            && param0.getLevel().isUnobstructed(param1, param0.getClickedPos(), var1);
    }

    protected boolean mustSurvive() {
        return true;
    }

    protected boolean placeBlock(BlockPlaceContext param0, BlockState param1) {
        return param0.getLevel().setBlock(param0.getClickedPos(), param1, 11);
    }

    public static boolean updateCustomBlockEntityTag(Level param0, @Nullable Player param1, BlockPos param2, ItemStack param3) {
        MinecraftServer var0 = param0.getServer();
        if (var0 == null) {
            return false;
        } else {
            CompoundTag var1 = param3.getTagElement("BlockEntityTag");
            if (var1 != null) {
                BlockEntity var2 = param0.getBlockEntity(param2);
                if (var2 != null) {
                    if (!param0.isClientSide && var2.onlyOpCanSetNbt() && (param1 == null || !param1.canUseGameMasterBlocks())) {
                        return false;
                    }

                    CompoundTag var3 = var2.save(new CompoundTag());
                    CompoundTag var4 = var3.copy();
                    var3.merge(var1);
                    var3.putInt("x", param2.getX());
                    var3.putInt("y", param2.getY());
                    var3.putInt("z", param2.getZ());
                    if (!var3.equals(var4)) {
                        var2.load(var3);
                        var2.setChanged();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @Override
    public String getDescriptionId() {
        return this.getBlock().getDescriptionId();
    }

    @Override
    public void fillItemCategory(CreativeModeTab param0, NonNullList<ItemStack> param1) {
        if (this.allowdedIn(param0)) {
            this.getBlock().fillItemCategory(param0, param1);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack param0, @Nullable Level param1, List<Component> param2, TooltipFlag param3) {
        super.appendHoverText(param0, param1, param2, param3);
        this.getBlock().appendHoverText(param0, param1, param2, param3);
    }

    public Block getBlock() {
        return this.block;
    }

    public void registerBlocks(Map<Block, Item> param0, Item param1) {
        param0.put(this.getBlock(), param1);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(this.block instanceof ShulkerBoxBlock);
    }

    @Override
    public void onDestroyed(ItemEntity param0) {
        if (this.block instanceof ShulkerBoxBlock) {
            CompoundTag var0 = param0.getItem().getTag();
            if (var0 != null) {
                ListTag var1 = var0.getCompound("BlockEntityTag").getList("Items", 10);
                ItemUtils.onContainerDestroyed(param0, var1.stream().map(CompoundTag.class::cast).map(ItemStack::of));
            }
        }

    }
}
