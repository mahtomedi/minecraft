package net.minecraft.world.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CompassItem extends Item implements Vanishable {
    public CompassItem(Item.Properties param0) {
        super(param0);
        this.addProperty(
            new ResourceLocation("angle"),
            new ItemPropertyFunction() {
                private final CompassItem.CompassWobble wobble = new CompassItem.CompassWobble();
                private final CompassItem.CompassWobble wobbleRandom = new CompassItem.CompassWobble();
    
                @OnlyIn(Dist.CLIENT)
                @Override
                public float call(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2) {
                    if (param2 == null && !param0.isFramed()) {
                        return 0.0F;
                    } else {
                        boolean var0 = param2 != null;
                        Entity var1 = (Entity)(var0 ? param2 : param0.getFrame());
                        if (param1 == null) {
                            param1 = var1.level;
                        }
    
                        CompoundTag var2 = param0.getOrCreateTag();
                        BlockPos var3 = CompassItem.hasLodestoneData(var2)
                            ? CompassItem.this.getLodestonePosition(param1, var2)
                            : CompassItem.this.getSpawnPosition(param1);
                        double var9;
                        if (var3 != null) {
                            double var4 = var0 ? (double)var1.yRot : CompassItem.getFrameRotation((ItemFrame)var1);
                            var4 = Mth.positiveModulo(var4 / 360.0, 1.0);
                            double var5 = 0.5 - (var4 - 0.25);
                            boolean var6 = !var0 && var1.getDirection().getAxis().isVertical();
                            boolean var7 = var6 && var1.getDirection() == Direction.UP;
                            double var8 = CompassItem.getAngleTo(Vec3.atCenterOf(var3), var1) / (float) (Math.PI * 2) * (double)(var7 ? -1 : 1);
                            var9 = 0.5 - (var4 - 0.25 - var8) * (double)(var6 ? -1 : 1);
                            if (var0) {
                                this.wobble.update(param1, var5);
                                var9 -= var5 - this.wobble.getRotation();
                            }
                        } else {
                            this.wobbleRandom.update(param1, Math.random());
                            var9 = this.wobbleRandom.getRotation();
                        }
    
                        return Mth.positiveModulo((float)var9, 1.0F);
                    }
                }
            }
        );
    }

    private static boolean hasLodestoneData(CompoundTag param0) {
        return param0.contains("LodestoneDimension") || param0.contains("LodestonePos");
    }

    private static boolean isLodestoneCompass(ItemStack param0) {
        return hasLodestoneData(param0.getOrCreateTag());
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return isLodestoneCompass(param0) || super.isFoil(param0);
    }

    private static Optional<DimensionType> getLodestoneDimension(CompoundTag param0) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0.getString("LodestoneDimension"));
        return var0 != null ? Registry.DIMENSION_TYPE.getOptional(var0) : Optional.empty();
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private BlockPos getSpawnPosition(Level param0) {
        return param0.dimension.isNaturalDimension() ? param0.getSharedSpawnPos() : null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private BlockPos getLodestonePosition(Level param0, CompoundTag param1) {
        boolean var0 = param1.contains("LodestonePos");
        boolean var1 = param1.contains("LodestoneDimension");
        if (var0 && var1) {
            Optional<DimensionType> var2 = getLodestoneDimension(param1);
            if (var2.isPresent() && param0.dimension.getType().equals(var2.get())) {
                return NbtUtils.readBlockPos((CompoundTag)param1.get("LodestonePos"));
            }
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static double getFrameRotation(ItemFrame param0) {
        Direction var0 = param0.getDirection();
        return var0.getAxis().isVertical()
            ? (double)Mth.wrapDegrees(param0.getRotation() * -45)
            : (double)Mth.wrapDegrees(180 + param0.getDirection().get2DDataValue() * 90 + param0.getRotation() * 45);
    }

    @OnlyIn(Dist.CLIENT)
    private static double getAngleTo(Vec3 param0, Entity param1) {
        return Math.atan2(param0.z() - param1.getZ(), param0.x() - param1.getX());
    }

    @Override
    public void inventoryTick(ItemStack param0, Level param1, Entity param2, int param3, boolean param4) {
        if (!param1.isClientSide) {
            CompoundTag var0 = param0.getOrCreateTag();
            if (hasLodestoneData(var0)) {
                if (var0.contains("LodestoneTracked") && !var0.getBoolean("LodestoneTracked")) {
                    return;
                }

                Optional<DimensionType> var1 = getLodestoneDimension(var0);
                if (var1.isPresent()
                    && var1.get().equals(param1.dimension.getType())
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
        if (param0.level.getBlockState(var0).getBlock() == Blocks.LODESTONE) {
            param0.level.playSound(null, var0, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            CompoundTag var1 = param0.itemStack.getOrCreateTag();
            var1.put("LodestonePos", NbtUtils.writeBlockPos(var0));
            var1.putString("LodestoneDimension", DimensionType.getName(param0.level.dimension.getType()).toString());
            var1.putBoolean("LodestoneTracked", true);
            return InteractionResult.SUCCESS;
        } else {
            return super.useOn(param0);
        }
    }

    @Override
    public String getDescriptionId(ItemStack param0) {
        return isLodestoneCompass(param0) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(param0);
    }

    static class CompassWobble {
        @OnlyIn(Dist.CLIENT)
        private double rotation;
        @OnlyIn(Dist.CLIENT)
        private double deltaRotation;
        @OnlyIn(Dist.CLIENT)
        private long lastUpdateTick;

        private CompassWobble() {
        }

        @OnlyIn(Dist.CLIENT)
        public double getRotation() {
            return this.rotation;
        }

        @OnlyIn(Dist.CLIENT)
        private void update(Level param0, double param1) {
            if (param0.getGameTime() != this.lastUpdateTick) {
                this.lastUpdateTick = param0.getGameTime();
                double var0 = param1 - this.rotation;
                var0 = Mth.positiveModulo(var0 + 0.5, 1.0) - 0.5;
                this.deltaRotation += var0 * 0.1;
                this.deltaRotation *= 0.8;
                this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
            }

        }
    }
}
