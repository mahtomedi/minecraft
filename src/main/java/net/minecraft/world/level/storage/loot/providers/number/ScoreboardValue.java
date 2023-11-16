package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public record ScoreboardValue(ScoreboardNameProvider target, String score, float scale) implements NumberProvider {
    public static final Codec<ScoreboardValue> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ScoreboardNameProviders.CODEC.fieldOf("target").forGetter(ScoreboardValue::target),
                    Codec.STRING.fieldOf("score").forGetter(ScoreboardValue::score),
                    Codec.FLOAT.fieldOf("scale").orElse(1.0F).forGetter(ScoreboardValue::scale)
                )
                .apply(param0, ScoreboardValue::new)
    );

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.SCORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.target.getReferencedContextParams();
    }

    public static ScoreboardValue fromScoreboard(LootContext.EntityTarget param0, String param1) {
        return fromScoreboard(param0, param1, 1.0F);
    }

    public static ScoreboardValue fromScoreboard(LootContext.EntityTarget param0, String param1, float param2) {
        return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(param0), param1, param2);
    }

    @Override
    public float getFloat(LootContext param0) {
        ScoreHolder var0 = this.target.getScoreHolder(param0);
        if (var0 == null) {
            return 0.0F;
        } else {
            Scoreboard var1 = param0.getLevel().getScoreboard();
            Objective var2 = var1.getObjective(this.score);
            if (var2 == null) {
                return 0.0F;
            } else {
                ReadOnlyScoreInfo var3 = var1.getPlayerScoreInfo(var0, var2);
                return var3 == null ? 0.0F : (float)var3.value() * this.scale;
            }
        }
    }
}
