package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState extends BlockBehaviour.BlockStateBase {
    public static final Codec<BlockState> CODEC = codec(Registry.BLOCK.byNameCodec(), Block::defaultBlockState).stable();

    public BlockState(Block param0, ImmutableMap<Property<?>, Comparable<?>> param1, MapCodec<BlockState> param2) {
        super(param0, param1, param2);
    }

    @Override
    protected BlockState asState() {
        return this;
    }
}
