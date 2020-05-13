package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;

public class CompassItem extends Item implements Vanishable {
    public CompassItem(Item.Properties param0) {
        super(param0);
    }

    public static boolean isLodestoneCompass(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null && (var0.contains("LodestoneDimension") || var0.contains("LodestonePos"));
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return isLodestoneCompass(param0) || super.isFoil(param0);
    }

    public static Optional<DimensionType> getLodestoneDimension(CompoundTag param0) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0.getString("LodestoneDimension"));
        return var0 != null ? Registry.DIMENSION_TYPE.getOptional(var0) : Optional.empty();
    }

    @Override
    public void inventoryTick(ItemStack param0, Level param1, Entity param2, int param3, boolean param4) {
        if (!param1.isClientSide) {
            if (isLodestoneCompass(param0)) {
                CompoundTag var0 = param0.getOrCreateTag();
                if (var0.contains("LodestoneTracked") && !var0.getBoolean("LodestoneTracked")) {
                    return;
                }

                Optional<DimensionType> var1 = getLodestoneDimension(var0);
                if (var1.isPresent()
                    && var1.get().equals(param1.dimensionType())
                    && var0.contains("LodestonePos")
                    && !((ServerLevel)param1).getPoiManager().existsAtPosition(PoiType.LODESTONE, NbtUtils.readBlockPos((CompoundTag)var0.get("LodestonePos")))
                    )
                 {
                    var0.remove("LodestonePos");
                }
            }

        }
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        BlockPos var0 = param0.hitResult.getBlockPos();
        if (!param0.level.getBlockState(var0).is(Blocks.LODESTONE)) {
            return super.useOn(param0);
        } else {
            param0.level.playSound(null, var0, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            boolean var1 = !param0.player.abilities.instabuild && param0.itemStack.getCount() == 1;
            if (var1) {
                this.addLodestoneTags(param0.level.getDimension(), var0, param0.itemStack.getOrCreateTag());
            } else {
                ItemStack var2 = new ItemStack(Items.COMPASS, 1);
                CompoundTag var3 = param0.itemStack.hasTag() ? param0.itemStack.getTag().copy() : new CompoundTag();
                var2.setTag(var3);
                if (!param0.player.abilities.instabuild) {
                    param0.itemStack.shrink(1);
                }

                this.addLodestoneTags(param0.level.getDimension(), var0, var3);
                if (!param0.player.inventory.add(var2)) {
                    param0.player.drop(var2, false);
                }
            }

            return InteractionResult.SUCCESS;
        }
    }

    private void addLodestoneTags(Dimension param0, BlockPos param1, CompoundTag param2) {
        param2.put("LodestonePos", NbtUtils.writeBlockPos(param1));
        param2.putString("LodestoneDimension", DimensionType.getName(param0.getType()).toString());
        param2.putBoolean("LodestoneTracked", true);
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return isLodestoneCompass(param0) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(param0);
    }
}
