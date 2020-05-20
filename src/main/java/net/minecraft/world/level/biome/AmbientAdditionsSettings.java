package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AmbientAdditionsSettings {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    SoundEvent.CODEC.fieldOf("sound").forGetter(param0x -> param0x.soundEvent),
                    Codec.DOUBLE.fieldOf("tick_chance").forGetter(param0x -> param0x.tickChance)
                )
                .apply(param0, AmbientAdditionsSettings::new)
    );
    private SoundEvent soundEvent;
    private double tickChance;

    public AmbientAdditionsSettings(SoundEvent param0, double param1) {
        this.soundEvent = param0;
        this.tickChance = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    @OnlyIn(Dist.CLIENT)
    public double getTickChance() {
        return this.tickChance;
    }
}
