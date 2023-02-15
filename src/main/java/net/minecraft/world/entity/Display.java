package net.minecraft.world.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class Display extends Entity {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long NO_LAST_UPDATE = -1000L;
    public static final int NO_BRIGHTNESS_OVERRIDE = -1;
    private static final EntityDataAccessor<Long> DATA_INTERPOLATION_START_TICKS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DATA_INTERPOLATION_DURATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> DATA_TRANSLATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> DATA_SCALE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Quaternionf> DATA_LEFT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Quaternionf> DATA_RIGHT_ROTATION_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.QUATERNION);
    private static final EntityDataAccessor<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VIEW_RANGE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_RADIUS_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SHADOW_STRENGTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = SynchedEntityData.defineId(Display.class, EntityDataSerializers.INT);
    private static final float INITIAL_SHADOW_RADIUS = 0.0F;
    private static final float INITIAL_SHADOW_STRENGTH = 1.0F;
    private static final int NO_GLOW_COLOR_OVERRIDE = -1;
    public static final String TAG_INTERPOLATION_DURATION = "interpolation_duration";
    public static final String TAG_INTERPOLATION_START = "interpolation_start";
    public static final String TAG_TRANSFORMATION = "transformation";
    public static final String TAG_BILLBOARD = "billboard";
    public static final String TAG_BRIGHTNESS = "brightness";
    public static final String TAG_VIEW_RANGE = "view_range";
    public static final String TAG_SHADOW_RADIUS = "shadow_radius";
    public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
    public static final String TAG_WIDTH = "width";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
    private final Display.GenericInterpolator<Transformation> transformation = new Display.GenericInterpolator<Transformation>(Transformation.identity()) {
        protected Transformation interpolate(float param0, Transformation param1, Transformation param2) {
            return param1.slerp(param2, param0);
        }
    };
    private final Display.FloatInterpolator shadowRadius = new Display.FloatInterpolator(0.0F);
    private final Display.FloatInterpolator shadowStrength = new Display.FloatInterpolator(1.0F);
    private final Quaternionf orientation = new Quaternionf();
    protected final Display.InterpolatorSet interpolators = new Display.InterpolatorSet();
    private long interpolationStartClientTick;
    private AABB cullingBoundingBox;

    public Display(EntityType<?> param0, Level param1) {
        super(param0, param1);
        this.noPhysics = true;
        this.noCulling = true;
        this.cullingBoundingBox = this.getBoundingBox();
        this.interpolators
            .addEntry(
                Set.of(DATA_TRANSLATION_ID, DATA_LEFT_ROTATION_ID, DATA_SCALE_ID, DATA_RIGHT_ROTATION_ID),
                param0x -> this.transformation.updateValue(createTransformation(param0x))
            );
        this.interpolators.addEntry(DATA_SHADOW_STRENGTH_ID, this.shadowStrength);
        this.interpolators.addEntry(DATA_SHADOW_RADIUS_ID, this.shadowRadius);
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> param0) {
        super.onSyncedDataUpdated(param0);
        boolean var0 = false;

        for(SynchedEntityData.DataValue<?> var1 : param0) {
            var0 |= this.interpolators.shouldTriggerUpdate(var1.id());
        }

        if (var0) {
            this.interpolators.updateValues(this.entityData);
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        super.onSyncedDataUpdated(param0);
        if (DATA_HEIGHT_ID.equals(param0) || DATA_WIDTH_ID.equals(param0)) {
            this.updateCulling();
        }

        if (DATA_INTERPOLATION_START_TICKS_ID.equals(param0)) {
            long var0 = this.entityData.get(DATA_INTERPOLATION_START_TICKS_ID) - this.level.getGameTime();
            this.interpolationStartClientTick = (long)this.tickCount + var0;
        }

    }

    private static Transformation createTransformation(SynchedEntityData param0) {
        Vector3f var0 = param0.get(DATA_TRANSLATION_ID);
        Quaternionf var1 = param0.get(DATA_LEFT_ROTATION_ID);
        Vector3f var2 = param0.get(DATA_SCALE_ID);
        Quaternionf var3 = param0.get(DATA_RIGHT_ROTATION_ID);
        return new Transformation(var0, var1, var2, var3);
    }

    @Override
    public void tick() {
        Entity var0 = this.getVehicle();
        if (var0 != null && var0.isRemoved()) {
            this.stopRiding();
        }

    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_INTERPOLATION_START_TICKS_ID, -1000L);
        this.entityData.define(DATA_INTERPOLATION_DURATION_ID, 0);
        this.entityData.define(DATA_TRANSLATION_ID, new Vector3f());
        this.entityData.define(DATA_SCALE_ID, new Vector3f(1.0F, 1.0F, 1.0F));
        this.entityData.define(DATA_RIGHT_ROTATION_ID, new Quaternionf());
        this.entityData.define(DATA_LEFT_ROTATION_ID, new Quaternionf());
        this.entityData.define(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, Display.BillboardConstraints.FIXED.getId());
        this.entityData.define(DATA_BRIGHTNESS_OVERRIDE_ID, -1);
        this.entityData.define(DATA_VIEW_RANGE_ID, 1.0F);
        this.entityData.define(DATA_SHADOW_RADIUS_ID, 0.0F);
        this.entityData.define(DATA_SHADOW_STRENGTH_ID, 1.0F);
        this.entityData.define(DATA_WIDTH_ID, 0.0F);
        this.entityData.define(DATA_HEIGHT_ID, 0.0F);
        this.entityData.define(DATA_GLOW_COLOR_OVERRIDE_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        if (param0.contains("transformation")) {
            Transformation.EXTENDED_CODEC
                .decode(NbtOps.INSTANCE, param0.get("transformation"))
                .resultOrPartial(Util.prefix("Display entity", LOGGER::error))
                .ifPresent(param0x -> this.setTransformation(param0x.getFirst()));
        }

        if (param0.contains("interpolation_duration", 99)) {
            int var0 = param0.getInt("interpolation_duration");
            this.setInterpolationDuration(var0);
        }

        if (param0.contains("interpolation_start", 99)) {
            long var1 = param0.getLong("interpolation_start");
            if (var1 < 0L) {
                long var2 = -var1 - 1L;
                this.setInterpolationStartTick(this.level.getGameTime() + var2);
            } else {
                this.setInterpolationStartTick(var1);
            }
        }

        if (param0.contains("billboard", 8)) {
            Display.BillboardConstraints.CODEC
                .decode(NbtOps.INSTANCE, param0.get("billboard"))
                .resultOrPartial(Util.prefix("Display entity", LOGGER::error))
                .ifPresent(param0x -> this.setBillboardConstraints(param0x.getFirst()));
        }

        if (param0.contains("view_range", 99)) {
            this.setViewRange(param0.getFloat("view_range"));
        }

        if (param0.contains("shadow_radius", 99)) {
            this.setShadowRadius(param0.getFloat("shadow_radius"));
        }

        if (param0.contains("shadow_strength", 99)) {
            this.setShadowStrength(param0.getFloat("shadow_strength"));
        }

        if (param0.contains("width", 99)) {
            this.setWidth(param0.getFloat("width"));
        }

        if (param0.contains("height", 99)) {
            this.setHeight(param0.getFloat("height"));
        }

        if (param0.contains("glow_color_override", 99)) {
            this.setGlowColorOverride(param0.getInt("glow_color_override"));
        }

        if (param0.contains("brightness", 10)) {
            Brightness.CODEC
                .decode(NbtOps.INSTANCE, param0.get("brightness"))
                .resultOrPartial(Util.prefix("Display entity", LOGGER::error))
                .ifPresent(param0x -> this.setBrightnessOverride((Brightness)param0x.getFirst()));
        } else {
            this.setBrightnessOverride(null);
        }

    }

    private void setTransformation(Transformation param0) {
        this.entityData.set(DATA_TRANSLATION_ID, param0.getTranslation());
        this.entityData.set(DATA_LEFT_ROTATION_ID, param0.getLeftRotation());
        this.entityData.set(DATA_SCALE_ID, param0.getScale());
        this.entityData.set(DATA_RIGHT_ROTATION_ID, param0.getRightRotation());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        Transformation.EXTENDED_CODEC
            .encodeStart(NbtOps.INSTANCE, createTransformation(this.entityData))
            .result()
            .ifPresent(param1 -> param0.put("transformation", param1));
        Display.BillboardConstraints.CODEC
            .encodeStart(NbtOps.INSTANCE, this.getBillboardConstraints())
            .result()
            .ifPresent(param1 -> param0.put("billboard", param1));
        param0.putInt("interpolation_duration", this.getInterpolationDuration());
        param0.putFloat("view_range", this.getViewRange());
        param0.putFloat("shadow_radius", this.getShadowRadius());
        param0.putFloat("shadow_strength", this.getShadowStrength());
        param0.putFloat("width", this.getWidth());
        param0.putFloat("height", this.getHeight());
        param0.putLong("interpolation_start", this.getInterpolationStartTick());
        param0.putInt("glow_color_override", this.getGlowColorOverride());
        Brightness var0 = this.getBrightnessOverride();
        if (var0 != null) {
            Brightness.CODEC.encodeStart(NbtOps.INSTANCE, var0).result().ifPresent(param1 -> param0.put("brightness", param1));
        }

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.cullingBoundingBox;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public Quaternionf orientation() {
        return this.orientation;
    }

    public Transformation transformation(float param0) {
        return this.transformation.get(param0);
    }

    private void setInterpolationDuration(int param0) {
        this.entityData.set(DATA_INTERPOLATION_DURATION_ID, param0);
    }

    private int getInterpolationDuration() {
        return this.entityData.get(DATA_INTERPOLATION_DURATION_ID);
    }

    private void setInterpolationStartTick(long param0) {
        this.entityData.set(DATA_INTERPOLATION_START_TICKS_ID, param0);
    }

    private long getInterpolationStartTick() {
        return this.entityData.get(DATA_INTERPOLATION_START_TICKS_ID);
    }

    private void setBillboardConstraints(Display.BillboardConstraints param0) {
        this.entityData.set(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, param0.getId());
    }

    public Display.BillboardConstraints getBillboardConstraints() {
        return Display.BillboardConstraints.BY_ID.apply(this.entityData.get(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID));
    }

    private void setBrightnessOverride(@Nullable Brightness param0) {
        this.entityData.set(DATA_BRIGHTNESS_OVERRIDE_ID, param0 != null ? param0.pack() : -1);
    }

    @Nullable
    private Brightness getBrightnessOverride() {
        int var0 = this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
        return var0 != -1 ? Brightness.unpack(var0) : null;
    }

    public int getPackedBrightnessOverride() {
        return this.entityData.get(DATA_BRIGHTNESS_OVERRIDE_ID);
    }

    private void setViewRange(float param0) {
        this.entityData.set(DATA_VIEW_RANGE_ID, param0);
    }

    private float getViewRange() {
        return this.entityData.get(DATA_VIEW_RANGE_ID);
    }

    private void setShadowRadius(float param0) {
        this.entityData.set(DATA_SHADOW_RADIUS_ID, param0);
    }

    private float getShadowRadius() {
        return this.entityData.get(DATA_SHADOW_RADIUS_ID);
    }

    public float getShadowRadius(float param0) {
        return this.shadowRadius.get(param0);
    }

    private void setShadowStrength(float param0) {
        this.entityData.set(DATA_SHADOW_STRENGTH_ID, param0);
    }

    private float getShadowStrength() {
        return this.entityData.get(DATA_SHADOW_STRENGTH_ID);
    }

    public float getShadowStrength(float param0) {
        return this.shadowStrength.get(param0);
    }

    private void setWidth(float param0) {
        this.entityData.set(DATA_WIDTH_ID, param0);
    }

    private float getWidth() {
        return this.entityData.get(DATA_WIDTH_ID);
    }

    private void setHeight(float param0) {
        this.entityData.set(DATA_HEIGHT_ID, param0);
    }

    private int getGlowColorOverride() {
        return this.entityData.get(DATA_GLOW_COLOR_OVERRIDE_ID);
    }

    private void setGlowColorOverride(int param0) {
        this.entityData.set(DATA_GLOW_COLOR_OVERRIDE_ID, param0);
    }

    public float calculateInterpolationProgress(float param0) {
        int var0 = this.getInterpolationDuration();
        if (var0 <= 0) {
            return 1.0F;
        } else {
            float var1 = (float)((long)this.tickCount - this.interpolationStartClientTick);
            float var2 = var1 + param0;
            return Mth.clamp(Mth.inverseLerp(var2, 0.0F, (float)var0), 0.0F, 1.0F);
        }
    }

    private float getHeight() {
        return this.entityData.get(DATA_HEIGHT_ID);
    }

    @Override
    public void setPos(double param0, double param1, double param2) {
        super.setPos(param0, param1, param2);
        this.updateCulling();
    }

    private void updateCulling() {
        float var0 = this.getWidth();
        float var1 = this.getHeight();
        if (var0 != 0.0F && var1 != 0.0F) {
            this.noCulling = false;
            float var2 = var0 / 2.0F;
            double var3 = this.getX();
            double var4 = this.getY();
            double var5 = this.getZ();
            this.cullingBoundingBox = new AABB(var3 - (double)var2, var4, var5 - (double)var2, var3 + (double)var2, var4 + (double)var1, var5 + (double)var2);
        } else {
            this.noCulling = true;
        }

    }

    @Override
    public void setXRot(float param0) {
        super.setXRot(param0);
        this.updateOrientation();
    }

    @Override
    public void setYRot(float param0) {
        super.setYRot(param0);
        this.updateOrientation();
    }

    private void updateOrientation() {
        this.orientation.rotationYXZ((float) (-Math.PI / 180.0) * this.getYRot(), (float) (Math.PI / 180.0) * this.getXRot(), 0.0F);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        return param0 < Mth.square((double)this.getViewRange() * 64.0 * getViewScale());
    }

    @Override
    public int getTeamColor() {
        int var0 = this.getGlowColorOverride();
        return var0 != -1 ? var0 : super.getTeamColor();
    }

    public static enum BillboardConstraints implements StringRepresentable {
        FIXED((byte)0, "fixed"),
        VERTICAL((byte)1, "vertical"),
        HORIZONTAL((byte)2, "horizontal"),
        CENTER((byte)3, "center");

        public static final Codec<Display.BillboardConstraints> CODEC = StringRepresentable.fromEnum(Display.BillboardConstraints::values);
        public static final IntFunction<Display.BillboardConstraints> BY_ID = ByIdMap.continuous(
            Display.BillboardConstraints::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        private final byte id;
        private final String name;

        private BillboardConstraints(byte param0, String param1) {
            this.name = param1;
            this.id = param0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        byte getId() {
            return this.id;
        }
    }

    public static class BlockDisplay extends Display {
        public static final String TAG_BLOCK_STATE = "block_state";
        private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(
            Display.BlockDisplay.class, EntityDataSerializers.BLOCK_STATE
        );

        public BlockDisplay(EntityType<?> param0, Level param1) {
            super(param0, param1);
        }

        @Override
        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
        }

        public BlockState getBlockState() {
            return this.entityData.get(DATA_BLOCK_STATE_ID);
        }

        public void setBlockState(BlockState param0) {
            this.entityData.set(DATA_BLOCK_STATE_ID, param0);
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag param0) {
            super.readAdditionalSaveData(param0);
            this.setBlockState(NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), param0.getCompound("block_state")));
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
        }
    }

    static class ColorInterpolator extends Display.IntInterpolator {
        protected ColorInterpolator(int param0) {
            super(param0);
        }

        @Override
        protected int interpolate(float param0, int param1, int param2) {
            return FastColor.ARGB32.lerp(param0, param1, param2);
        }
    }

    static class FloatInterpolator extends Display.Interpolator<Float> {
        protected FloatInterpolator(float param0) {
            super(param0);
        }

        protected float interpolate(float param0, float param1, float param2) {
            return Mth.lerp(param0, param1, param2);
        }

        public float get(float param0) {
            return !((double)param0 >= 1.0) && this.lastValue != null ? this.interpolate(param0, this.lastValue, this.currentValue) : this.currentValue;
        }
    }

    abstract static class GenericInterpolator<T> extends Display.Interpolator<T> {
        protected GenericInterpolator(T param0) {
            super(param0);
        }

        protected abstract T interpolate(float var1, T var2, T var3);

        public T get(float param0) {
            return (T)(!((double)param0 >= 1.0) && this.lastValue != null ? this.interpolate(param0, this.lastValue, this.currentValue) : this.currentValue);
        }
    }

    static class IntInterpolator extends Display.Interpolator<Integer> {
        protected IntInterpolator(int param0) {
            super(param0);
        }

        protected int interpolate(float param0, int param1, int param2) {
            return Mth.lerpInt(param0, param1, param2);
        }

        public int get(float param0) {
            return !((double)param0 >= 1.0) && this.lastValue != null ? this.interpolate(param0, this.lastValue, this.currentValue) : this.currentValue;
        }
    }

    abstract static class Interpolator<T> {
        @Nullable
        protected T lastValue;
        protected T currentValue;

        protected Interpolator(T param0) {
            this.currentValue = param0;
        }

        public void updateValue(T param0) {
            this.lastValue = this.currentValue;
            this.currentValue = param0;
        }
    }

    static class InterpolatorSet {
        private final IntSet interpolatedData = new IntOpenHashSet();
        private final List<Consumer<SynchedEntityData>> updaters = new ArrayList<>();

        protected <T> void addEntry(EntityDataAccessor<T> param0, Display.Interpolator<T> param1) {
            this.interpolatedData.add(param0.getId());
            this.updaters.add(param2 -> param1.updateValue(param2.get(param0)));
        }

        protected void addEntry(Set<EntityDataAccessor<?>> param0, Consumer<SynchedEntityData> param1) {
            for(EntityDataAccessor<?> var0 : param0) {
                this.interpolatedData.add(var0.getId());
            }

            this.updaters.add(param1);
        }

        public boolean shouldTriggerUpdate(int param0) {
            return this.interpolatedData.contains(param0);
        }

        public void updateValues(SynchedEntityData param0) {
            for(Consumer<SynchedEntityData> var0 : this.updaters) {
                var0.accept(param0);
            }

        }
    }

    public static class ItemDisplay extends Display {
        private static final String TAG_ITEM = "item";
        private static final String TAG_ITEM_DISPLAY = "item_display";
        private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID = SynchedEntityData.defineId(
            Display.ItemDisplay.class, EntityDataSerializers.ITEM_STACK
        );
        private static final EntityDataAccessor<Byte> DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(Display.ItemDisplay.class, EntityDataSerializers.BYTE);
        private final SlotAccess slot = new SlotAccess() {
            @Override
            public ItemStack get() {
                return ItemDisplay.this.getItemStack();
            }

            @Override
            public boolean set(ItemStack param0) {
                ItemDisplay.this.setItemStack(param0);
                return true;
            }
        };

        public ItemDisplay(EntityType<?> param0, Level param1) {
            super(param0, param1);
        }

        @Override
        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
            this.entityData.define(DATA_ITEM_DISPLAY_ID, ItemDisplayContext.FIXED.getId());
        }

        public ItemStack getItemStack() {
            return this.entityData.get(DATA_ITEM_STACK_ID);
        }

        void setItemStack(ItemStack param0) {
            this.entityData.set(DATA_ITEM_STACK_ID, param0);
        }

        private void setItemTransform(ItemDisplayContext param0) {
            this.entityData.set(DATA_ITEM_DISPLAY_ID, param0.getId());
        }

        public ItemDisplayContext getItemTransform() {
            return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID));
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag param0) {
            super.readAdditionalSaveData(param0);
            this.setItemStack(ItemStack.of(param0.getCompound("item")));
            if (param0.contains("item_display", 8)) {
                ItemDisplayContext.CODEC
                    .decode(NbtOps.INSTANCE, param0.get("item_display"))
                    .resultOrPartial(Util.prefix("Display entity", Display.LOGGER::error))
                    .ifPresent(param0x -> this.setItemTransform(param0x.getFirst()));
            }

        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.put("item", this.getItemStack().save(new CompoundTag()));
            ItemDisplayContext.CODEC.encodeStart(NbtOps.INSTANCE, this.getItemTransform()).result().ifPresent(param1 -> param0.put("item_display", param1));
        }

        @Override
        public SlotAccess getSlot(int param0) {
            return param0 == 0 ? this.slot : SlotAccess.NULL;
        }
    }

    public static class TextDisplay extends Display {
        public static final String TAG_TEXT = "text";
        private static final String TAG_LINE_WIDTH = "line_width";
        private static final String TAG_TEXT_OPACITY = "text_opacity";
        private static final String TAG_BACKGROUND_COLOR = "background";
        private static final String TAG_SHADOW = "shadow";
        private static final String TAG_SEE_THROUGH = "see_through";
        private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
        private static final String TAG_ALIGNMENT = "alignment";
        public static final byte FLAG_SHADOW = 1;
        public static final byte FLAG_SEE_THROUGH = 2;
        public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
        public static final byte FLAG_ALIGN_LEFT = 8;
        public static final byte FLAG_ALIGN_RIGHT = 16;
        private static final byte INITIAL_TEXT_OPACITY = -1;
        public static final int INITIAL_BACKGROUND = 1073741824;
        private static final EntityDataAccessor<Component> DATA_TEXT_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.COMPONENT);
        private static final EntityDataAccessor<Integer> DATA_LINE_WIDTH_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.INT);
        private static final EntityDataAccessor<Integer> DATA_BACKGROUND_COLOR_ID = SynchedEntityData.defineId(
            Display.TextDisplay.class, EntityDataSerializers.INT
        );
        private static final EntityDataAccessor<Byte> DATA_TEXT_OPACITY_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.BYTE);
        private static final EntityDataAccessor<Byte> DATA_STYLE_FLAGS_ID = SynchedEntityData.defineId(Display.TextDisplay.class, EntityDataSerializers.BYTE);
        private final Display.IntInterpolator textOpacity = new Display.IntInterpolator(-1);
        private final Display.IntInterpolator backgroundColor = new Display.ColorInterpolator(1073741824);
        @Nullable
        private Display.TextDisplay.CachedInfo clientDisplayCache;

        public TextDisplay(EntityType<?> param0, Level param1) {
            super(param0, param1);
            this.interpolators.addEntry(DATA_BACKGROUND_COLOR_ID, this.backgroundColor);
            this.interpolators
                .addEntry(Set.of(DATA_TEXT_OPACITY_ID), param0x -> this.textOpacity.updateValue(Integer.valueOf(param0x.get(DATA_TEXT_OPACITY_ID) & 255)));
        }

        @Override
        protected void defineSynchedData() {
            super.defineSynchedData();
            this.entityData.define(DATA_TEXT_ID, Component.empty());
            this.entityData.define(DATA_LINE_WIDTH_ID, 200);
            this.entityData.define(DATA_BACKGROUND_COLOR_ID, 1073741824);
            this.entityData.define(DATA_TEXT_OPACITY_ID, (byte)-1);
            this.entityData.define(DATA_STYLE_FLAGS_ID, (byte)0);
        }

        @Override
        public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
            super.onSyncedDataUpdated(param0);
            this.clientDisplayCache = null;
        }

        public Component getText() {
            return this.entityData.get(DATA_TEXT_ID);
        }

        private void setText(Component param0) {
            this.entityData.set(DATA_TEXT_ID, param0);
        }

        public int getLineWidth() {
            return this.entityData.get(DATA_LINE_WIDTH_ID);
        }

        private void setLineWidth(int param0) {
            this.entityData.set(DATA_LINE_WIDTH_ID, param0);
        }

        public byte getTextOpacity(float param0) {
            return (byte)this.textOpacity.get(param0);
        }

        private byte getTextOpacity() {
            return this.entityData.get(DATA_TEXT_OPACITY_ID);
        }

        private void setTextOpacity(byte param0) {
            this.entityData.set(DATA_TEXT_OPACITY_ID, param0);
        }

        public int getBackgroundColor(float param0) {
            return this.backgroundColor.get(param0);
        }

        private int getBackgroundColor() {
            return this.entityData.get(DATA_BACKGROUND_COLOR_ID);
        }

        private void setBackgroundColor(int param0) {
            this.entityData.set(DATA_BACKGROUND_COLOR_ID, param0);
        }

        public byte getFlags() {
            return this.entityData.get(DATA_STYLE_FLAGS_ID);
        }

        private void setFlags(byte param0) {
            this.entityData.set(DATA_STYLE_FLAGS_ID, param0);
        }

        private static byte loadFlag(byte param0, CompoundTag param1, String param2, byte param3) {
            return param1.getBoolean(param2) ? (byte)(param0 | param3) : param0;
        }

        @Override
        protected void readAdditionalSaveData(CompoundTag param0) {
            super.readAdditionalSaveData(param0);
            if (param0.contains("line_width", 99)) {
                this.setLineWidth(param0.getInt("line_width"));
            }

            if (param0.contains("text_opacity", 99)) {
                this.setTextOpacity(param0.getByte("text_opacity"));
            }

            if (param0.contains("background", 99)) {
                this.setBackgroundColor(param0.getInt("background"));
            }

            byte var0 = loadFlag((byte)0, param0, "shadow", (byte)1);
            var0 = loadFlag(var0, param0, "see_through", (byte)2);
            var0 = loadFlag(var0, param0, "default_background", (byte)4);
            Optional<Display.TextDisplay.Align> var1 = Display.TextDisplay.Align.CODEC
                .decode(NbtOps.INSTANCE, param0.get("alignment"))
                .resultOrPartial(Util.prefix("Display entity", Display.LOGGER::error))
                .map(Pair::getFirst);
            if (var1.isPresent()) {
                var0 = switch((Display.TextDisplay.Align)var1.get()) {
                    case CENTER -> var0;
                    case LEFT -> (byte)(var0 | 8);
                    case RIGHT -> (byte)(var0 | 16);
                };
            }

            this.setFlags(var0);
            if (param0.contains("text", 8)) {
                String var2 = param0.getString("text");

                try {
                    Component var3 = Component.Serializer.fromJson(var2);
                    if (var3 != null) {
                        CommandSourceStack var4 = this.createCommandSourceStack().withPermission(2);
                        Component var5 = ComponentUtils.updateForEntity(var4, var3, this, 0);
                        this.setText(var5);
                    } else {
                        this.setText(Component.empty());
                    }
                } catch (Exception var8) {
                    Display.LOGGER.warn("Failed to parse display entity text {}", var2, var8);
                }
            }

        }

        private static void storeFlag(byte param0, CompoundTag param1, String param2, byte param3) {
            param1.putBoolean(param2, (param0 & param3) != 0);
        }

        @Override
        protected void addAdditionalSaveData(CompoundTag param0) {
            super.addAdditionalSaveData(param0);
            param0.putString("text", Component.Serializer.toJson(this.getText()));
            param0.putInt("line_width", this.getLineWidth());
            param0.putInt("background", this.getBackgroundColor());
            param0.putByte("text_opacity", this.getTextOpacity());
            byte var0 = this.getFlags();
            storeFlag(var0, param0, "shadow", (byte)1);
            storeFlag(var0, param0, "see_through", (byte)2);
            storeFlag(var0, param0, "default_background", (byte)4);
            Display.TextDisplay.Align.CODEC.encodeStart(NbtOps.INSTANCE, getAlign(var0)).result().ifPresent(param1 -> param0.put("alignment", param1));
        }

        public Display.TextDisplay.CachedInfo cacheDisplay(Display.TextDisplay.LineSplitter param0) {
            if (this.clientDisplayCache == null) {
                int var0 = this.getLineWidth();
                this.clientDisplayCache = param0.split(this.getText(), var0);
            }

            return this.clientDisplayCache;
        }

        public static Display.TextDisplay.Align getAlign(byte param0) {
            if ((param0 & 8) != 0) {
                return Display.TextDisplay.Align.LEFT;
            } else {
                return (param0 & 16) != 0 ? Display.TextDisplay.Align.RIGHT : Display.TextDisplay.Align.CENTER;
            }
        }

        public static enum Align implements StringRepresentable {
            CENTER("center"),
            LEFT("left"),
            RIGHT("right");

            public static final Codec<Display.TextDisplay.Align> CODEC = StringRepresentable.fromEnum(Display.TextDisplay.Align::values);
            private final String name;

            private Align(String param0) {
                this.name = param0;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }

        public static record CachedInfo(List<Display.TextDisplay.CachedLine> lines, int width) {
        }

        public static record CachedLine(FormattedCharSequence contents, int width) {
        }

        @FunctionalInterface
        public interface LineSplitter {
            Display.TextDisplay.CachedInfo split(Component var1, int var2);
        }
    }
}
