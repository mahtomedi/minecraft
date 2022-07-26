package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

public class CompassItem extends Item implements Vanishable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG_LODESTONE_POS = "LodestonePos";
    public static final String TAG_LODESTONE_DIMENSION = "LodestoneDimension";
    public static final String TAG_LODESTONE_TRACKED = "LodestoneTracked";

    public CompassItem(Item.Properties param0) {
        super(param0);
    }

    public static boolean isLodestoneCompass(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null && (var0.contains("LodestoneDimension") || var0.contains("LodestonePos"));
    }

    private static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag param0) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, param0.get("LodestoneDimension")).result();
    }

    @Nullable
    public static GlobalPos getLodestonePosition(CompoundTag param0) {
        boolean var0 = param0.contains("LodestonePos");
        boolean var1 = param0.contains("LodestoneDimension");
        if (var0 && var1) {
            Optional<ResourceKey<Level>> var2 = getLodestoneDimension(param0);
            if (var2.isPresent()) {
                BlockPos var3 = NbtUtils.readBlockPos(param0.getCompound("LodestonePos"));
                return GlobalPos.of(var2.get(), var3);
            }
        }

        return null;
    }

    @Nullable
    public static GlobalPos getSpawnPosition(Level param0) {
        return param0.dimensionType().natural() ? GlobalPos.of(param0.dimension(), param0.getSharedSpawnPos()) : null;
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return isLodestoneCompass(param0) || super.isFoil(param0);
    }

    @Override
    public void inventoryTick(ItemStack param0, Level param1, Entity param2, int param3, boolean param4) {
        if (!param1.isClientSide) {
            if (isLodestoneCompass(param0)) {
                CompoundTag var0 = param0.getOrCreateTag();
                if (var0.contains("LodestoneTracked") && !var0.getBoolean("LodestoneTracked")) {
                    return;
                }

                Optional<ResourceKey<Level>> var1 = getLodestoneDimension(var0);
                if (var1.isPresent() && var1.get() == param1.dimension() && var0.contains("LodestonePos")) {
                    BlockPos var2 = NbtUtils.readBlockPos(var0.getCompound("LodestonePos"));
                    if (!param1.isInWorldBounds(var2) || !((ServerLevel)param1).getPoiManager().existsAtPosition(PoiTypes.LODESTONE, var2)) {
                        var0.remove("LodestonePos");
                    }
                }
            }

        }
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        BlockPos var0 = param0.getClickedPos();
        Level var1 = param0.getLevel();
        if (!var1.getBlockState(var0).is(Blocks.LODESTONE)) {
            return super.useOn(param0);
        } else {
            var1.playSound(null, var0, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            Player var2 = param0.getPlayer();
            ItemStack var3 = param0.getItemInHand();
            boolean var4 = !var2.getAbilities().instabuild && var3.getCount() == 1;
            if (var4) {
                this.addLodestoneTags(var1.dimension(), var0, var3.getOrCreateTag());
            } else {
                ItemStack var5 = new ItemStack(Items.COMPASS, 1);
                CompoundTag var6 = var3.hasTag() ? var3.getTag().copy() : new CompoundTag();
                var5.setTag(var6);
                if (!var2.getAbilities().instabuild) {
                    var3.shrink(1);
                }

                this.addLodestoneTags(var1.dimension(), var0, var6);
                if (!var2.getInventory().add(var5)) {
                    var2.drop(var5, false);
                }
            }

            return InteractionResult.sidedSuccess(var1.isClientSide);
        }
    }

    private void addLodestoneTags(ResourceKey<Level> param0, BlockPos param1, CompoundTag param2) {
        param2.put("LodestonePos", NbtUtils.writeBlockPos(param1));
        Level.RESOURCE_KEY_CODEC
            .encodeStart(NbtOps.INSTANCE, param0)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1x -> param2.put("LodestoneDimension", param1x));
        param2.putBoolean("LodestoneTracked", true);
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return isLodestoneCompass(param0) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(param0);
    }
}
