package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AmbientMoodSettings {
    public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    SoundEvent.CODEC.fieldOf("sound").forGetter(param0x -> param0x.soundEvent),
                    Codec.INT.fieldOf("tick_delay").forGetter(param0x -> param0x.tickDelay),
                    Codec.INT.fieldOf("block_search_extent").forGetter(param0x -> param0x.blockSearchExtent),
                    Codec.DOUBLE.fieldOf("offset").forGetter(param0x -> param0x.soundPositionOffset)
                )
                .apply(param0, AmbientMoodSettings::new)
    );
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
    private final SoundEvent soundEvent;
    private final int tickDelay;
    private final int blockSearchExtent;
    private final double soundPositionOffset;

    public AmbientMoodSettings(SoundEvent param0, int param1, int param2, double param3) {
        this.soundEvent = param0;
        this.tickDelay = param1;
        this.blockSearchExtent = param2;
        this.soundPositionOffset = param3;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public int getTickDelay() {
        return this.tickDelay;
    }

    public int getBlockSearchExtent() {
        return this.blockSearchExtent;
    }

    public double getSoundPositionOffset() {
        return this.soundPositionOffset;
    }
}
