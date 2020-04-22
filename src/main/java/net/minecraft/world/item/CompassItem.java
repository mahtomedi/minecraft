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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.Dimension;
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
                    Entity var0 = (Entity)(param2 != null ? param2 : param0.getEntityRepresentation());
                    if (var0 == null) {
                        return 0.0F;
                    } else {
                        if (param1 == null) {
                            param1 = var0.level;
                        }
    
                        BlockPos var1 = CompassItem.isLodestoneCompass(param0)
                            ? CompassItem.this.getLodestonePosition(param1, param0.getOrCreateTag())
                            : CompassItem.this.getSpawnPosition(param1);
                        long var2 = param1.getGameTime();
                        if (var1 != null
                            && !(var0.position().distanceToSqr((double)var1.getX() + 0.5, var0.position().y(), (double)var1.getZ() + 0.5) < 1.0E-5F)) {
                            boolean var4 = param2 instanceof Player && ((Player)param2).isLocalPlayer();
                            double var5 = 0.0;
                            if (var4) {
                                var5 = (double)param2.yRot;
                            } else if (var0 instanceof ItemFrame) {
                                var5 = CompassItem.getFrameRotation((ItemFrame)var0);
                            } else if (var0 instanceof ItemEntity) {
                                var5 = (double)(180.0F - ((ItemEntity)var0).getSpin(0.5F) / (float) (Math.PI * 2) * 360.0F);
                            } else if (param2 != null) {
                                var5 = (double)param2.yBodyRot;
                            }
    
                            var5 = Mth.positiveModulo(var5 / 360.0, 1.0);
                            double var6 = CompassItem.getAngleTo(Vec3.atCenterOf(var1), var0) / (float) (Math.PI * 2);
                            double var7;
                            if (var4) {
                                if (this.wobble.shouldUpdate(var2)) {
                                    this.wobble.update(var2, 0.5 - (var5 - 0.25));
                                }
    
                                var7 = var6 + this.wobble.rotation;
                            } else {
                                var7 = 0.5 - (var5 - 0.25 - var6);
                            }
    
                            return Mth.positiveModulo((float)var7, 1.0F);
                        } else {
                            if (this.wobbleRandom.shouldUpdate(var2)) {
                                this.wobbleRandom.update(var2, Math.random());
                            }
    
                            double var3 = this.wobbleRandom.rotation + (double)((float)param0.hashCode() / 2.14748365E9F);
                            return Mth.positiveModulo((float)var3, 1.0F);
                        }
                    }
                }
            }
        );
    }

    private static boolean isLodestoneCompass(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null && (var0.contains("LodestoneDimension") || var0.contains("LodestonePos"));
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
        if (param0.level.getBlockState(var0).getBlock() != Blocks.LODESTONE) {
            return super.useOn(param0);
        } else {
            param0.level.playSound(null, var0, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            boolean var1 = !param0.player.abilities.instabuild && param0.itemStack.getCount() == 1;
            if (var1) {
                this.addLodestoneTags(param0.level.dimension, var0, param0.itemStack.getOrCreateTag());
            } else {
                ItemStack var2 = new ItemStack(Items.COMPASS, 1);
                CompoundTag var3 = param0.itemStack.hasTag() ? param0.itemStack.getTag().copy() : new CompoundTag();
                var2.setTag(var3);
                if (!param0.player.abilities.instabuild) {
                    param0.itemStack.shrink(1);
                }

                this.addLodestoneTags(param0.level.dimension, var0, var3);
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

    @OnlyIn(Dist.CLIENT)
    private static double getFrameRotation(ItemFrame param0) {
        Direction var0 = param0.getDirection();
        int var1 = var0.getAxis().isVertical() ? 90 * var0.getAxisDirection().getStep() : 0;
        return (double)Mth.wrapDegrees(180 + var0.get2DDataValue() * 90 + param0.getRotation() * 45 + var1);
    }

    @OnlyIn(Dist.CLIENT)
    private static double getAngleTo(Vec3 param0, Entity param1) {
        return Math.atan2(param0.z() - param1.getZ(), param0.x() - param1.getX());
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
        private boolean shouldUpdate(long param0) {
            return this.lastUpdateTick != param0;
        }

        @OnlyIn(Dist.CLIENT)
        private void update(long param0, double param1) {
            this.lastUpdateTick = param0;
            double var0 = param1 - this.rotation;
            var0 = Mth.positiveModulo(var0 + 0.5, 1.0) - 0.5;
            this.deltaRotation += var0 * 0.1;
            this.deltaRotation *= 0.8;
            this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
        }
    }
}
