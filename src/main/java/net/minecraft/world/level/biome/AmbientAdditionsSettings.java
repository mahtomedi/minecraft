package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    SoundEvent.CODEC.fieldOf("sound").forGetter(param0x -> param0x.soundEvent),
                    Codec.DOUBLE.fieldOf("tick_chance").forGetter(param0x -> param0x.tickChance)
                )
                .apply(param0, AmbientAdditionsSettings::new)
    );
    private final Holder<SoundEvent> soundEvent;
    private final double tickChance;

    public AmbientAdditionsSettings(Holder<SoundEvent> param0, double param1) {
        this.soundEvent = param0;
        this.tickChance = param1;
    }

    public Holder<SoundEvent> getSoundEvent() {
        return this.soundEvent;
    }

    public double getTickChance() {
        return this.tickChance;
    }
}
