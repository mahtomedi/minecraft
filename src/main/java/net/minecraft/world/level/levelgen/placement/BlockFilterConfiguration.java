package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class BlockFilterConfiguration implements DecoratorConfiguration {
    public static final Codec<BlockFilterConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(param0x -> param0x.predicate)).apply(param0, BlockFilterConfiguration::new)
    );
    private final BlockPredicate predicate;

    public BlockFilterConfiguration(BlockPredicate param0) {
        this.predicate = param0;
    }

    public BlockPredicate predicate() {
        return this.predicate;
    }
}
