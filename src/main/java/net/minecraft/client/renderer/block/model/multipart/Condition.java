package net.minecraft.client.renderer.block.model.multipart;

import java.util.function.Predicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface Condition {
    Condition TRUE = param0 -> param0x -> true;
    Condition FALSE = param0 -> param0x -> false;

    Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> var1);
}
