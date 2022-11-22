package net.minecraft.world.entity.decoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity implements VariantHolder<Holder<PaintingVariant>> {
    private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(
        Painting.class, EntityDataSerializers.PAINTING_VARIANT
    );
    private static final ResourceKey<PaintingVariant> DEFAULT_VARIANT = PaintingVariants.KEBAB;

    private static Holder<PaintingVariant> getDefaultVariant() {
        return BuiltInRegistries.PAINTING_VARIANT.getHolderOrThrow(DEFAULT_VARIANT);
    }

    public Painting(EntityType<? extends Painting> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_PAINTING_VARIANT_ID, getDefaultVariant());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_PAINTING_VARIANT_ID.equals(param0)) {
            this.recalculateBoundingBox();
        }

    }

    public void setVariant(Holder<PaintingVariant> param0) {
        this.entityData.set(DATA_PAINTING_VARIANT_ID, param0);
    }

    public Holder<PaintingVariant> getVariant() {
        return this.entityData.get(DATA_PAINTING_VARIANT_ID);
    }

    public static Optional<Painting> create(Level param0, BlockPos param1, Direction param2) {
        Painting var0 = new Painting(param0, param1);
        List<Holder<PaintingVariant>> var1 = new ArrayList<>();
        BuiltInRegistries.PAINTING_VARIANT.getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(var1::add);
        if (var1.isEmpty()) {
            return Optional.empty();
        } else {
            var0.setDirection(param2);
            var1.removeIf(param1x -> {
                var0.setVariant(param1x);
                return !var0.survives();
            });
            if (var1.isEmpty()) {
                return Optional.empty();
            } else {
                int var2 = var1.stream().mapToInt(Painting::variantArea).max().orElse(0);
                var1.removeIf(param1x -> variantArea(param1x) < var2);
                Optional<Holder<PaintingVariant>> var3 = Util.getRandomSafe(var1, var0.random);
                if (var3.isEmpty()) {
                    return Optional.empty();
                } else {
                    var0.setVariant(var3.get());
                    var0.setDirection(param2);
                    return Optional.of(var0);
                }
            }
        }
    }

    private static int variantArea(Holder<PaintingVariant> param0x) {
        return param0x.value().getWidth() * param0x.value().getHeight();
    }

    private Painting(Level param0, BlockPos param1) {
        super(EntityType.PAINTING, param0, param1);
    }

    public Painting(Level param0, BlockPos param1, Direction param2, Holder<PaintingVariant> param3) {
        this(param0, param1);
        this.setVariant(param3);
        this.setDirection(param2);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putString("variant", this.getVariant().unwrapKey().orElse(DEFAULT_VARIANT).location().toString());
        param0.putByte("facing", (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        Holder<PaintingVariant> var0 = Optional.ofNullable(ResourceLocation.tryParse(param0.getString("variant")))
            .map(param0x -> ResourceKey.create(Registries.PAINTING_VARIANT, param0x))
            .flatMap(BuiltInRegistries.PAINTING_VARIANT::getHolder)
            .map((Function<? super Holder.Reference<PaintingVariant>, ? extends Holder.Reference<PaintingVariant>>)(param0x -> param0x))
            .orElseGet(Painting::getDefaultVariant);
        this.setVariant(var0);
        this.direction = Direction.from2DDataValue(param0.getByte("facing"));
        super.readAdditionalSaveData(param0);
        this.setDirection(this.direction);
    }

    @Override
    public int getWidth() {
        return this.getVariant().value().getWidth();
    }

    @Override
    public int getHeight() {
        return this.getVariant().value().getHeight();
    }

    @Override
    public void dropItem(@Nullable Entity param0) {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (param0 instanceof Player var0 && var0.getAbilities().instabuild) {
                return;
            }

            this.spawnAtLocation(Items.PAINTING);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double param0, double param1, double param2, float param3, float param4) {
        this.setPos(param0, param1, param2);
    }

    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        this.setPos(param0, param1, param2);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket param0) {
        super.recreateFromPacket(param0);
        this.setDirection(Direction.from3DDataValue(param0.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}
