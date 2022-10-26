package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MobEffect effect;
    int duration;
    private int amplifier;
    private boolean ambient;
    private boolean noCounter;
    private boolean visible;
    private boolean showIcon;
    @Nullable
    private MobEffectInstance hiddenEffect;
    private final Optional<MobEffectInstance.FactorData> factorData;

    public MobEffectInstance(MobEffect param0) {
        this(param0, 0, 0);
    }

    public MobEffectInstance(MobEffect param0, int param1) {
        this(param0, param1, 0);
    }

    public MobEffectInstance(MobEffect param0, int param1, int param2) {
        this(param0, param1, param2, false, true);
    }

    public MobEffectInstance(MobEffect param0, int param1, int param2, boolean param3, boolean param4) {
        this(param0, param1, param2, param3, param4, param4);
    }

    public MobEffectInstance(MobEffect param0, int param1, int param2, boolean param3, boolean param4, boolean param5) {
        this(param0, param1, param2, param3, param4, param5, null, param0.createFactorData());
    }

    public MobEffectInstance(
        MobEffect param0,
        int param1,
        int param2,
        boolean param3,
        boolean param4,
        boolean param5,
        @Nullable MobEffectInstance param6,
        Optional<MobEffectInstance.FactorData> param7
    ) {
        this.effect = param0;
        this.duration = param1;
        this.amplifier = param2;
        this.ambient = param3;
        this.visible = param4;
        this.showIcon = param5;
        this.hiddenEffect = param6;
        this.factorData = param7;
    }

    public MobEffectInstance(MobEffectInstance param0) {
        this.effect = param0.effect;
        this.factorData = this.effect.createFactorData();
        this.setDetailsFrom(param0);
    }

    public Optional<MobEffectInstance.FactorData> getFactorData() {
        return this.factorData;
    }

    void setDetailsFrom(MobEffectInstance param0) {
        this.duration = param0.duration;
        this.amplifier = param0.amplifier;
        this.ambient = param0.ambient;
        this.visible = param0.visible;
        this.showIcon = param0.showIcon;
    }

    public boolean update(MobEffectInstance param0) {
        if (this.effect != param0.effect) {
            LOGGER.warn("This method should only be called for matching effects!");
        }

        int var0 = this.duration;
        boolean var1 = false;
        if (param0.amplifier > this.amplifier) {
            if (param0.duration < this.duration) {
                MobEffectInstance var2 = this.hiddenEffect;
                this.hiddenEffect = new MobEffectInstance(this);
                this.hiddenEffect.hiddenEffect = var2;
            }

            this.amplifier = param0.amplifier;
            this.duration = param0.duration;
            var1 = true;
        } else if (param0.duration > this.duration) {
            if (param0.amplifier == this.amplifier) {
                this.duration = param0.duration;
                var1 = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffectInstance(param0);
            } else {
                this.hiddenEffect.update(param0);
            }
        }

        if (!param0.ambient && this.ambient || var1) {
            this.ambient = param0.ambient;
            var1 = true;
        }

        if (param0.visible != this.visible) {
            this.visible = param0.visible;
            var1 = true;
        }

        if (param0.showIcon != this.showIcon) {
            this.showIcon = param0.showIcon;
            var1 = true;
        }

        if (var0 != this.duration) {
            this.factorData.ifPresent(param1 -> param1.effectChangedTimestamp += this.duration - var0);
            var1 = true;
        }

        return var1;
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean tick(LivingEntity param0, Runnable param1) {
        if (this.duration > 0) {
            if (this.effect.isDurationEffectTick(this.duration, this.amplifier)) {
                this.applyEffect(param0);
            }

            this.tickDownDuration();
            if (this.duration == 0 && this.hiddenEffect != null) {
                this.setDetailsFrom(this.hiddenEffect);
                this.hiddenEffect = this.hiddenEffect.hiddenEffect;
                param1.run();
            }
        }

        this.factorData.ifPresent(param0x -> param0x.update(this));
        return this.duration > 0;
    }

    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDuration();
        }

        return --this.duration;
    }

    public void applyEffect(LivingEntity param0) {
        if (this.duration > 0) {
            this.effect.applyEffectTick(param0, this.amplifier);
        }

    }

    public String getDescriptionId() {
        return this.effect.getDescriptionId();
    }

    @Override
    public String toString() {
        String var0;
        if (this.amplifier > 0) {
            var0 = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
        } else {
            var0 = this.getDescriptionId() + ", Duration: " + this.duration;
        }

        if (!this.visible) {
            var0 = var0 + ", Particles: false";
        }

        if (!this.showIcon) {
            var0 = var0 + ", Show Icon: false";
        }

        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof MobEffectInstance)) {
            return false;
        } else {
            MobEffectInstance var0 = (MobEffectInstance)param0;
            return this.duration == var0.duration && this.amplifier == var0.amplifier && this.ambient == var0.ambient && this.effect.equals(var0.effect);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.effect.hashCode();
        var0 = 31 * var0 + this.duration;
        var0 = 31 * var0 + this.amplifier;
        return 31 * var0 + (this.ambient ? 1 : 0);
    }

    public CompoundTag save(CompoundTag param0) {
        param0.putInt("Id", MobEffect.getId(this.getEffect()));
        this.writeDetailsTo(param0);
        return param0;
    }

    private void writeDetailsTo(CompoundTag param0) {
        param0.putByte("Amplifier", (byte)this.getAmplifier());
        param0.putInt("Duration", this.getDuration());
        param0.putBoolean("Ambient", this.isAmbient());
        param0.putBoolean("ShowParticles", this.isVisible());
        param0.putBoolean("ShowIcon", this.showIcon());
        if (this.hiddenEffect != null) {
            CompoundTag var0 = new CompoundTag();
            this.hiddenEffect.save(var0);
            param0.put("HiddenEffect", var0);
        }

        this.factorData
            .ifPresent(
                param1 -> MobEffectInstance.FactorData.CODEC
                        .encodeStart(NbtOps.INSTANCE, param1)
                        .resultOrPartial(LOGGER::error)
                        .ifPresent(param1x -> param0.put("FactorCalculationData", param1x))
            );
    }

    @Nullable
    public static MobEffectInstance load(CompoundTag param0) {
        int var0 = param0.getInt("Id");
        MobEffect var1 = MobEffect.byId(var0);
        return var1 == null ? null : loadSpecifiedEffect(var1, param0);
    }

    private static MobEffectInstance loadSpecifiedEffect(MobEffect param0, CompoundTag param1) {
        int var0 = param1.getByte("Amplifier");
        int var1 = param1.getInt("Duration");
        boolean var2 = param1.getBoolean("Ambient");
        boolean var3 = true;
        if (param1.contains("ShowParticles", 1)) {
            var3 = param1.getBoolean("ShowParticles");
        }

        boolean var4 = var3;
        if (param1.contains("ShowIcon", 1)) {
            var4 = param1.getBoolean("ShowIcon");
        }

        MobEffectInstance var5 = null;
        if (param1.contains("HiddenEffect", 10)) {
            var5 = loadSpecifiedEffect(param0, param1.getCompound("HiddenEffect"));
        }

        Optional<MobEffectInstance.FactorData> var6;
        if (param1.contains("FactorCalculationData", 10)) {
            var6 = MobEffectInstance.FactorData.CODEC
                .parse(new Dynamic<>(NbtOps.INSTANCE, param1.getCompound("FactorCalculationData")))
                .resultOrPartial(LOGGER::error);
        } else {
            var6 = Optional.empty();
        }

        return new MobEffectInstance(param0, var1, Math.max(var0, 0), var2, var3, var4, var5, var6);
    }

    public void setNoCounter(boolean param0) {
        this.noCounter = param0;
    }

    public boolean isNoCounter() {
        return this.noCounter;
    }

    public int compareTo(MobEffectInstance param0) {
        int var0 = 32147;
        return (this.getDuration() <= 32147 || param0.getDuration() <= 32147) && (!this.isAmbient() || !param0.isAmbient())
            ? ComparisonChain.start()
                .compare(this.isAmbient(), param0.isAmbient())
                .compare(this.getDuration(), param0.getDuration())
                .compare(this.getEffect().getColor(), param0.getEffect().getColor())
                .result()
            : ComparisonChain.start()
                .compare(this.isAmbient(), param0.isAmbient())
                .compare(this.getEffect().getColor(), param0.getEffect().getColor())
                .result();
    }

    public static class FactorData {
        public static final Codec<MobEffectInstance.FactorData> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("padding_duration").forGetter(param0x -> param0x.paddingDuration),
                        Codec.FLOAT.fieldOf("factor_start").orElse(0.0F).forGetter(param0x -> param0x.factorStart),
                        Codec.FLOAT.fieldOf("factor_target").orElse(1.0F).forGetter(param0x -> param0x.factorTarget),
                        Codec.FLOAT.fieldOf("factor_current").orElse(0.0F).forGetter(param0x -> param0x.factorCurrent),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("effect_changed_timestamp").orElse(0).forGetter(param0x -> param0x.effectChangedTimestamp),
                        Codec.FLOAT.fieldOf("factor_previous_frame").orElse(0.0F).forGetter(param0x -> param0x.factorPreviousFrame),
                        Codec.BOOL.fieldOf("had_effect_last_tick").orElse(false).forGetter(param0x -> param0x.hadEffectLastTick)
                    )
                    .apply(param0, MobEffectInstance.FactorData::new)
        );
        private final int paddingDuration;
        private float factorStart;
        private float factorTarget;
        private float factorCurrent;
        int effectChangedTimestamp;
        private float factorPreviousFrame;
        private boolean hadEffectLastTick;

        public FactorData(int param0, float param1, float param2, float param3, int param4, float param5, boolean param6) {
            this.paddingDuration = param0;
            this.factorStart = param1;
            this.factorTarget = param2;
            this.factorCurrent = param3;
            this.effectChangedTimestamp = param4;
            this.factorPreviousFrame = param5;
            this.hadEffectLastTick = param6;
        }

        public FactorData(int param0) {
            this(param0, 0.0F, 1.0F, 0.0F, 0, 0.0F, false);
        }

        public void update(MobEffectInstance param0) {
            this.factorPreviousFrame = this.factorCurrent;
            boolean var0 = param0.duration > this.paddingDuration;
            if (this.hadEffectLastTick != var0) {
                this.hadEffectLastTick = var0;
                this.effectChangedTimestamp = param0.duration;
                this.factorStart = this.factorCurrent;
                this.factorTarget = var0 ? 1.0F : 0.0F;
            }

            float var1 = Mth.clamp(((float)this.effectChangedTimestamp - (float)param0.duration) / (float)this.paddingDuration, 0.0F, 1.0F);
            this.factorCurrent = Mth.lerp(var1, this.factorStart, this.factorTarget);
        }

        public float getFactor(LivingEntity param0, float param1) {
            if (param0.isRemoved()) {
                this.factorPreviousFrame = this.factorCurrent;
            }

            return Mth.lerp(param1, this.factorPreviousFrame, this.factorCurrent);
        }
    }
}
