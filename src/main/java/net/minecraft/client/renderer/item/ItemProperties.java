package net.minecraft.client.renderer.item;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemProperties {
    private static final Map<ResourceLocation, ItemPropertyFunction> GENERIC_PROPERTIES = Maps.newHashMap();
    private static final ResourceLocation DAMAGED = new ResourceLocation("damaged");
    private static final ResourceLocation DAMAGE = new ResourceLocation("damage");
    private static final ItemPropertyFunction PROPERTY_DAMAGED = (param0, param1, param2) -> param0.isDamaged() ? 1.0F : 0.0F;
    private static final ItemPropertyFunction PROPERTY_DAMAGE = (param0, param1, param2) -> Mth.clamp(
            (float)param0.getDamageValue() / (float)param0.getMaxDamage(), 0.0F, 1.0F
        );
    private static final Map<Item, Map<ResourceLocation, ItemPropertyFunction>> PROPERTIES = Maps.newHashMap();

    private static ItemPropertyFunction registerGeneric(ResourceLocation param0, ItemPropertyFunction param1) {
        GENERIC_PROPERTIES.put(param0, param1);
        return param1;
    }

    private static void register(Item param0, ResourceLocation param1, ItemPropertyFunction param2) {
        PROPERTIES.computeIfAbsent(param0, param0x -> Maps.newHashMap()).put(param1, param2);
    }

    @Nullable
    public static ItemPropertyFunction getProperty(Item param0, ResourceLocation param1) {
        if (param0.getMaxDamage() > 0) {
            if (DAMAGE.equals(param1)) {
                return PROPERTY_DAMAGE;
            }

            if (DAMAGED.equals(param1)) {
                return PROPERTY_DAMAGED;
            }
        }

        ItemPropertyFunction var0 = GENERIC_PROPERTIES.get(param1);
        if (var0 != null) {
            return var0;
        } else {
            Map<ResourceLocation, ItemPropertyFunction> var1 = PROPERTIES.get(param0);
            return var1 == null ? null : var1.get(param1);
        }
    }

    static {
        registerGeneric(
            new ResourceLocation("lefthanded"), (param0, param1, param2) -> param2 != null && param2.getMainArm() != HumanoidArm.RIGHT ? 1.0F : 0.0F
        );
        registerGeneric(
            new ResourceLocation("cooldown"),
            (param0, param1, param2) -> param2 instanceof Player ? ((Player)param2).getCooldowns().getCooldownPercent(param0.getItem(), 0.0F) : 0.0F
        );
        registerGeneric(
            new ResourceLocation("custom_model_data"), (param0, param1, param2) -> param0.hasTag() ? (float)param0.getTag().getInt("CustomModelData") : 0.0F
        );
        register(Items.BOW, new ResourceLocation("pull"), (param0, param1, param2) -> {
            if (param2 == null) {
                return 0.0F;
            } else {
                return param2.getUseItem() != param0 ? 0.0F : (float)(param0.getUseDuration() - param2.getUseItemRemainingTicks()) / 20.0F;
            }
        });
        register(
            Items.BOW,
            new ResourceLocation("pulling"),
            (param0, param1, param2) -> param2 != null && param2.isUsingItem() && param2.getUseItem() == param0 ? 1.0F : 0.0F
        );
        register(Items.CLOCK, new ResourceLocation("time"), new ItemPropertyFunction() {
            private double rotation;
            private double rota;
            private long lastUpdateTick;

            @Override
            public float call(ItemStack param0, @Nullable ClientLevel param1, @Nullable LivingEntity param2) {
                boolean var0 = param2 != null;
                Entity var1 = (Entity)(var0 ? param2 : param0.getFrame());
                if (param1 == null && var1 != null && var1.level instanceof ClientLevel) {
                    param1 = (ClientLevel)var1.level;
                }

                if (param1 == null) {
                    return 0.0F;
                } else {
                    double var2;
                    if (param1.dimension.isNaturalDimension()) {
                        var2 = (double)param1.getTimeOfDay(1.0F);
                    } else {
                        var2 = Math.random();
                    }

                    var2 = this.wobble(param1, var2);
                    return (float)var2;
                }
            }

            private double wobble(Level param0, double param1) {
                if (param0.getGameTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = param0.getGameTime();
                    double var0 = param1 - this.rotation;
                    var0 = Mth.positiveModulo(var0 + 0.5, 1.0) - 0.5;
                    this.rota += var0 * 0.1;
                    this.rota *= 0.9;
                    this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0);
                }

                return this.rotation;
            }
        });
        register(
            Items.COMPASS,
            new ResourceLocation("angle"),
            new ItemPropertyFunction() {
                private final ItemProperties.CompassWobble wobble = new ItemProperties.CompassWobble();
                private final ItemProperties.CompassWobble wobbleRandom = new ItemProperties.CompassWobble();
    
                @Override
                public float call(ItemStack param0, @Nullable ClientLevel param1, @Nullable LivingEntity param2) {
                    Entity var0 = (Entity)(param2 != null ? param2 : param0.getEntityRepresentation());
                    if (var0 == null) {
                        return 0.0F;
                    } else {
                        if (param1 == null && var0.level instanceof ClientLevel) {
                            param1 = (ClientLevel)var0.level;
                        }
    
                        BlockPos var1 = CompassItem.isLodestoneCompass(param0)
                            ? this.getLodestonePosition(param1, param0.getOrCreateTag())
                            : this.getSpawnPosition(param1);
                        long var2 = param1.getGameTime();
                        if (var1 != null
                            && !(var0.position().distanceToSqr((double)var1.getX() + 0.5, var0.position().y(), (double)var1.getZ() + 0.5) < 1.0E-5F)) {
                            boolean var4 = param2 instanceof Player && ((Player)param2).isLocalPlayer();
                            double var5 = 0.0;
                            if (var4) {
                                var5 = (double)param2.yRot;
                            } else if (var0 instanceof ItemFrame) {
                                var5 = this.getFrameRotation((ItemFrame)var0);
                            } else if (var0 instanceof ItemEntity) {
                                var5 = (double)(180.0F - ((ItemEntity)var0).getSpin(0.5F) / (float) (Math.PI * 2) * 360.0F);
                            } else if (param2 != null) {
                                var5 = (double)param2.yBodyRot;
                            }
    
                            var5 = Mth.positiveModulo(var5 / 360.0, 1.0);
                            double var6 = this.getAngleTo(Vec3.atCenterOf(var1), var0) / (float) (Math.PI * 2);
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
    
                @Nullable
                private BlockPos getSpawnPosition(ClientLevel param0) {
                    return param0.dimension.isNaturalDimension() ? param0.getSharedSpawnPos() : null;
                }
    
                @Nullable
                private BlockPos getLodestonePosition(Level param0, CompoundTag param1) {
                    boolean var0 = param1.contains("LodestonePos");
                    boolean var1 = param1.contains("LodestoneDimension");
                    if (var0 && var1) {
                        Optional<DimensionType> var2 = CompassItem.getLodestoneDimension(param1);
                        if (var2.isPresent() && param0.dimension.getType().equals(var2.get())) {
                            return NbtUtils.readBlockPos((CompoundTag)param1.get("LodestonePos"));
                        }
                    }
    
                    return null;
                }
    
                private double getFrameRotation(ItemFrame param0) {
                    Direction var0 = param0.getDirection();
                    int var1 = var0.getAxis().isVertical() ? 90 * var0.getAxisDirection().getStep() : 0;
                    return (double)Mth.wrapDegrees(180 + var0.get2DDataValue() * 90 + param0.getRotation() * 45 + var1);
                }
    
                private double getAngleTo(Vec3 param0, Entity param1) {
                    return Math.atan2(param0.z() - param1.getZ(), param0.x() - param1.getX());
                }
            }
        );
        register(
            Items.CROSSBOW,
            new ResourceLocation("pull"),
            (param0, param1, param2) -> {
                if (param2 == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(param0)
                        ? 0.0F
                        : (float)(param0.getUseDuration() - param2.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(param0);
                }
            }
        );
        register(
            Items.CROSSBOW,
            new ResourceLocation("pulling"),
            (param0, param1, param2) -> param2 != null && param2.isUsingItem() && param2.getUseItem() == param0 && !CrossbowItem.isCharged(param0)
                    ? 1.0F
                    : 0.0F
        );
        register(Items.CROSSBOW, new ResourceLocation("charged"), (param0, param1, param2) -> param2 != null && CrossbowItem.isCharged(param0) ? 1.0F : 0.0F);
        register(
            Items.CROSSBOW,
            new ResourceLocation("firework"),
            (param0, param1, param2) -> param2 != null
                        && CrossbowItem.isCharged(param0)
                        && CrossbowItem.containsChargedProjectile(param0, Items.FIREWORK_ROCKET)
                    ? 1.0F
                    : 0.0F
        );
        register(Items.ELYTRA, new ResourceLocation("broken"), (param0, param1, param2) -> ElytraItem.isFlyEnabled(param0) ? 0.0F : 1.0F);
        register(Items.FISHING_ROD, new ResourceLocation("cast"), (param0, param1, param2) -> {
            if (param2 == null) {
                return 0.0F;
            } else {
                boolean var0 = param2.getMainHandItem() == param0;
                boolean var1 = param2.getOffhandItem() == param0;
                if (param2.getMainHandItem().getItem() instanceof FishingRodItem) {
                    var1 = false;
                }

                return (var0 || var1) && param2 instanceof Player && ((Player)param2).fishing != null ? 1.0F : 0.0F;
            }
        });
        register(
            Items.SHIELD,
            new ResourceLocation("blocking"),
            (param0, param1, param2) -> param2 != null && param2.isUsingItem() && param2.getUseItem() == param0 ? 1.0F : 0.0F
        );
        register(
            Items.TRIDENT,
            new ResourceLocation("throwing"),
            (param0, param1, param2) -> param2 != null && param2.isUsingItem() && param2.getUseItem() == param0 ? 1.0F : 0.0F
        );
    }

    @OnlyIn(Dist.CLIENT)
    static class CompassWobble {
        private double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        private CompassWobble() {
        }

        private boolean shouldUpdate(long param0) {
            return this.lastUpdateTick != param0;
        }

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
