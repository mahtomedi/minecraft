package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Music {
    public static final Codec<Music> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    SoundEvent.CODEC.fieldOf("sound").forGetter(param0x -> param0x.event),
                    Codec.INT.fieldOf("min_delay").forGetter(param0x -> param0x.minDelay),
                    Codec.INT.fieldOf("max_delay").forGetter(param0x -> param0x.maxDelay),
                    Codec.BOOL.fieldOf("replace_current_music").forGetter(param0x -> param0x.replaceCurrentMusic)
                )
                .apply(param0, Music::new)
    );
    private final SoundEvent event;
    private final int minDelay;
    private final int maxDelay;
    private final boolean replaceCurrentMusic;

    public Music(SoundEvent param0, int param1, int param2, boolean param3) {
        this.event = param0;
        this.minDelay = param1;
        this.maxDelay = param2;
        this.replaceCurrentMusic = param3;
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getEvent() {
        return this.event;
    }

    @OnlyIn(Dist.CLIENT)
    public int getMinDelay() {
        return this.minDelay;
    }

    @OnlyIn(Dist.CLIENT)
    public int getMaxDelay() {
        return this.maxDelay;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean replaceCurrentMusic() {
        return this.replaceCurrentMusic;
    }
}
