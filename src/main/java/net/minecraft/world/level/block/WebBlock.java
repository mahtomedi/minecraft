package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WebBlock extends Block {
    public static final MapCodec<WebBlock> CODEC = simpleCodec(WebBlock::new);

    @Override
    public MapCodec<WebBlock> codec() {
        return CODEC;
    }

    public WebBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        param3.makeStuckInBlock(param0, new Vec3(0.25, 0.05F, 0.25));
    }
}
