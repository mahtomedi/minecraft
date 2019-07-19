package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobEffectInstance implements Comparable<MobEffectInstance> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final MobEffect effect;
    private int duration;
    private int amplifier;
    private boolean splash;
    private boolean ambient;
    @OnlyIn(Dist.CLIENT)
    private boolean noCounter;
    private boolean visible;
    private boolean showIcon;

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
        this.effect = param0;
        this.duration = param1;
        this.amplifier = param2;
        this.ambient = param3;
        this.visible = param4;
        this.showIcon = param5;
    }

    public MobEffectInstance(MobEffectInstance param0) {
        this.effect = param0.effect;
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

        boolean var0 = false;
        if (param0.amplifier > this.amplifier) {
            this.amplifier = param0.amplifier;
            this.duration = param0.duration;
            var0 = true;
        } else if (param0.amplifier == this.amplifier && this.duration < param0.duration) {
            this.duration = param0.duration;
            var0 = true;
        }

        if (!param0.ambient && this.ambient || var0) {
            this.ambient = param0.ambient;
            var0 = true;
        }

        if (param0.visible != this.visible) {
            this.visible = param0.visible;
            var0 = true;
        }

        if (param0.showIcon != this.showIcon) {
            this.showIcon = param0.showIcon;
            var0 = true;
        }

        return var0;
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

    public boolean tick(LivingEntity param0) {
        if (this.duration > 0) {
            if (this.effect.isDurationEffectTick(this.duration, this.amplifier)) {
                this.applyEffect(param0);
            }

            this.tickDownDuration();
        }

        return this.duration > 0;
    }

    private int tickDownDuration() {
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

        if (this.splash) {
            var0 = var0 + ", Splash: true";
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
            return this.duration == var0.duration
                && this.amplifier == var0.amplifier
                && this.splash == var0.splash
                && this.ambient == var0.ambient
                && this.effect.equals(var0.effect);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.effect.hashCode();
        var0 = 31 * var0 + this.duration;
        var0 = 31 * var0 + this.amplifier;
        var0 = 31 * var0 + (this.splash ? 1 : 0);
        return 31 * var0 + (this.ambient ? 1 : 0);
    }

    public CompoundTag save(CompoundTag param0) {
        param0.putByte("Id", (byte)MobEffect.getId(this.getEffect()));
        param0.putByte("Amplifier", (byte)this.getAmplifier());
        param0.putInt("Duration", this.getDuration());
        param0.putBoolean("Ambient", this.isAmbient());
        param0.putBoolean("ShowParticles", this.isVisible());
        param0.putBoolean("ShowIcon", this.showIcon());
        return param0;
    }

    public static MobEffectInstance load(CompoundTag param0) {
        int var0 = param0.getByte("Id");
        MobEffect var1 = MobEffect.byId(var0);
        if (var1 == null) {
            return null;
        } else {
            int var2 = param0.getByte("Amplifier");
            int var3 = param0.getInt("Duration");
            boolean var4 = param0.getBoolean("Ambient");
            boolean var5 = true;
            if (param0.contains("ShowParticles", 1)) {
                var5 = param0.getBoolean("ShowParticles");
            }

            boolean var6 = var5;
            if (param0.contains("ShowIcon", 1)) {
                var6 = param0.getBoolean("ShowIcon");
            }

            return new MobEffectInstance(var1, var3, var2 < 0 ? 0 : var2, var4, var5, var6);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void setNoCounter(boolean param0) {
        this.noCounter = param0;
    }

    @OnlyIn(Dist.CLIENT)
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
}
