package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockParticleOption implements ParticleOptions {
    public static final ParticleOptions.Deserializer<BlockParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<BlockParticleOption>() {
        public BlockParticleOption fromCommand(ParticleType<BlockParticleOption> param0, StringReader param1) throws CommandSyntaxException {
            param1.expect(' ');
            return new BlockParticleOption(param0, new BlockStateParser(param1, false).parse(false).getState());
        }

        public BlockParticleOption fromNetwork(ParticleType<BlockParticleOption> param0, FriendlyByteBuf param1) {
            return new BlockParticleOption(param0, Block.BLOCK_STATE_REGISTRY.byId(param1.readVarInt()));
        }
    };
    private final ParticleType<BlockParticleOption> type;
    private final BlockState state;

    public static Codec<BlockParticleOption> codec(ParticleType<BlockParticleOption> param0) {
        return BlockState.CODEC.xmap(param1 -> new BlockParticleOption(param0, param1), param0x -> param0x.state);
    }

    public BlockParticleOption(ParticleType<BlockParticleOption> param0, BlockState param1) {
        this.type = param0;
        this.state = param1;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf param0) {
        param0.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(this.state));
    }

    @Override
    public String writeToString() {
        return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.state);
    }

    @Override
    public ParticleType<BlockParticleOption> getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockState getState() {
        return this.state;
    }
}
