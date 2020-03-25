package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnEggItem extends Item {
    private static final Map<EntityType<?>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
    private final int color1;
    private final int color2;
    private final EntityType<?> defaultType;

    public SpawnEggItem(EntityType<?> param0, int param1, int param2, Item.Properties param3) {
        super(param3);
        this.defaultType = param0;
        this.color1 = param1;
        this.color2 = param2;
        BY_ID.put(param0, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        Level var0 = param0.getLevel();
        if (var0.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack var1 = param0.getItemInHand();
            BlockPos var2 = param0.getClickedPos();
            Direction var3 = param0.getClickedFace();
            BlockState var4 = var0.getBlockState(var2);
            Block var5 = var4.getBlock();
            if (var5 == Blocks.SPAWNER) {
                BlockEntity var6 = var0.getBlockEntity(var2);
                if (var6 instanceof SpawnerBlockEntity) {
                    BaseSpawner var7 = ((SpawnerBlockEntity)var6).getSpawner();
                    EntityType<?> var8 = this.getType(var1.getTag());
                    var7.setEntityId(var8);
                    var6.setChanged();
                    var0.sendBlockUpdated(var2, var4, var4, 3);
                    var1.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }

            BlockPos var9;
            if (var4.getCollisionShape(var0, var2).isEmpty()) {
                var9 = var2;
            } else {
                var9 = var2.relative(var3);
            }

            EntityType<?> var11 = this.getType(var1.getTag());
            if (var11.spawn(var0, var1, param0.getPlayer(), var9, MobSpawnType.SPAWN_EGG, true, !Objects.equals(var2, var9) && var3 == Direction.UP) != null) {
                var1.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        HitResult var1 = getPlayerPOVHitResult(param0, param1, ClipContext.Fluid.SOURCE_ONLY);
        if (var1.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(var0);
        } else if (param0.isClientSide) {
            return InteractionResultHolder.success(var0);
        } else {
            BlockHitResult var2 = (BlockHitResult)var1;
            BlockPos var3 = var2.getBlockPos();
            if (!(param0.getBlockState(var3).getBlock() instanceof LiquidBlock)) {
                return InteractionResultHolder.pass(var0);
            } else if (param0.mayInteract(param1, var3) && param1.mayUseItemAt(var3, var2.getDirection(), var0)) {
                EntityType<?> var4 = this.getType(var0.getTag());
                if (var4.spawn(param0, var0, param1, var3, MobSpawnType.SPAWN_EGG, false, false) == null) {
                    return InteractionResultHolder.pass(var0);
                } else {
                    if (!param1.abilities.instabuild) {
                        var0.shrink(1);
                    }

                    param1.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.success(var0);
                }
            } else {
                return InteractionResultHolder.fail(var0);
            }
        }
    }

    public boolean spawnsEntity(@Nullable CompoundTag param0, EntityType<?> param1) {
        return Objects.equals(this.getType(param0), param1);
    }

    @OnlyIn(Dist.CLIENT)
    public int getColor(int param0) {
        return param0 == 0 ? this.color1 : this.color2;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static SpawnEggItem byId(@Nullable EntityType<?> param0) {
        return BY_ID.get(param0);
    }

    public static Iterable<SpawnEggItem> eggs() {
        return Iterables.unmodifiableIterable(BY_ID.values());
    }

    public EntityType<?> getType(@Nullable CompoundTag param0) {
        if (param0 != null && param0.contains("EntityTag", 10)) {
            CompoundTag var0 = param0.getCompound("EntityTag");
            if (var0.contains("id", 8)) {
                return EntityType.byString(var0.getString("id")).orElse(this.defaultType);
            }
        }

        return this.defaultType;
    }

    public Optional<Mob> spawnOffspringFromSpawnEgg(Player param0, EntityType<? extends Mob> param1, Level param2, Vec3 param3, ItemStack param4) {
        if (!this.spawnsEntity(param4.getTag(), param1)) {
            return Optional.empty();
        } else {
            Mob var0 = param1.create(param2);
            if (var0 == null) {
                return Optional.empty();
            } else {
                var0.setBaby(true);
                if (!var0.isBaby()) {
                    return Optional.empty();
                } else {
                    var0.moveTo(param3.x(), param3.y(), param3.z(), 0.0F, 0.0F);
                    param2.addFreshEntity(var0);
                    if (param4.hasCustomHoverName()) {
                        var0.setCustomName(param4.getHoverName());
                    }

                    if (!param0.abilities.instabuild) {
                        param4.shrink(1);
                    }

                    return Optional.of(var0);
                }
            }
        }
    }
}
