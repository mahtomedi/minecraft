package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AndCondition implements Condition {
    public static final String TOKEN = "AND";
    private final Iterable<? extends Condition> conditions;

    public AndCondition(Iterable<? extends Condition> param0) {
        this.conditions = param0;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> param0) {
        List<Predicate<BlockState>> var0 = Streams.stream(this.conditions).map(param1 -> param1.getPredicate(param0)).collect(Collectors.toList());
        return param1 -> var0.stream().allMatch(param1x -> param1x.test(param1));
    }
}
