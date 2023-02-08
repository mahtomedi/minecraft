package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
    public static final Codec<DamageType> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.STRING.fieldOf("message_id").forGetter(DamageType::msgId),
                    DamageScaling.CODEC.fieldOf("scaling").forGetter(DamageType::scaling),
                    Codec.FLOAT.fieldOf("exhaustion").forGetter(DamageType::exhaustion),
                    DamageEffects.CODEC.optionalFieldOf("effects", DamageEffects.HURT).forGetter(DamageType::effects),
                    DeathMessageType.CODEC.optionalFieldOf("death_message_type", DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)
                )
                .apply(param0, DamageType::new)
    );

    public DamageType(String param0, DamageScaling param1, float param2) {
        this(param0, param1, param2, DamageEffects.HURT, DeathMessageType.DEFAULT);
    }

    public DamageType(String param0, DamageScaling param1, float param2, DamageEffects param3) {
        this(param0, param1, param2, param3, DeathMessageType.DEFAULT);
    }

    public DamageType(String param0, float param1, DamageEffects param2) {
        this(param0, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, param1, param2);
    }

    public DamageType(String param0, float param1) {
        this(param0, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, param1);
    }
}
