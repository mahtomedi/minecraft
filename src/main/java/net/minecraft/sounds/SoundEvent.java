package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ResourceLocation.CODEC.fieldOf("sound_id").forGetter(SoundEvent::getLocation),
                    Codec.FLOAT.optionalFieldOf("range").forGetter(SoundEvent::fixedRange)
                )
                .apply(param0, SoundEvent::create)
    );
    public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
    private static final float DEFAULT_RANGE = 16.0F;
    private final ResourceLocation location;
    private final float range;
    private final boolean newSystem;

    private static SoundEvent create(ResourceLocation param0, Optional<Float> param1) {
        return param1.<SoundEvent>map(param1x -> createFixedRangeEvent(param0, param1x)).orElseGet(() -> createVariableRangeEvent(param0));
    }

    public static SoundEvent createVariableRangeEvent(ResourceLocation param0) {
        return new SoundEvent(param0, 16.0F, false);
    }

    public static SoundEvent createFixedRangeEvent(ResourceLocation param0, float param1) {
        return new SoundEvent(param0, param1, true);
    }

    private SoundEvent(ResourceLocation param0, float param1, boolean param2) {
        this.location = param0;
        this.range = param1;
        this.newSystem = param2;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public float getRange(float param0) {
        if (this.newSystem) {
            return this.range;
        } else {
            return param0 > 1.0F ? 16.0F * param0 : 16.0F;
        }
    }

    private Optional<Float> fixedRange() {
        return this.newSystem ? Optional.of(this.range) : Optional.empty();
    }

    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeResourceLocation(this.location);
        param0.writeOptional(this.fixedRange(), FriendlyByteBuf::writeFloat);
    }

    public static SoundEvent readFromNetwork(FriendlyByteBuf param0) {
        ResourceLocation var0 = param0.readResourceLocation();
        Optional<Float> var1 = param0.readOptional(FriendlyByteBuf::readFloat);
        return create(var0, var1);
    }
}
