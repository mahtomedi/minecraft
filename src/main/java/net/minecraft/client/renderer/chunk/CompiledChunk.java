package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CompiledChunk {
    public static final CompiledChunk UNCOMPILED = new CompiledChunk() {
        @Override
        protected void setChanged(BlockLayer param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void layerIsPresent(BlockLayer param0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean facesCanSeeEachother(Direction param0, Direction param1) {
            return false;
        }
    };
    private final boolean[] hasBlocks = new boolean[BlockLayer.values().length];
    private final boolean[] hasLayer = new boolean[BlockLayer.values().length];
    private boolean isCompletelyEmpty = true;
    private final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
    private VisibilitySet visibilitySet = new VisibilitySet();
    private BufferBuilder.State transparencyState;

    public boolean hasNoRenderableLayers() {
        return this.isCompletelyEmpty;
    }

    protected void setChanged(BlockLayer param0) {
        this.isCompletelyEmpty = false;
        this.hasBlocks[param0.ordinal()] = true;
    }

    public boolean isEmpty(BlockLayer param0) {
        return !this.hasBlocks[param0.ordinal()];
    }

    public void layerIsPresent(BlockLayer param0) {
        this.hasLayer[param0.ordinal()] = true;
    }

    public boolean hasLayer(BlockLayer param0) {
        return this.hasLayer[param0.ordinal()];
    }

    public List<BlockEntity> getRenderableBlockEntities() {
        return this.renderableBlockEntities;
    }

    public void addRenderableBlockEntity(BlockEntity param0) {
        this.renderableBlockEntities.add(param0);
    }

    public boolean facesCanSeeEachother(Direction param0, Direction param1) {
        return this.visibilitySet.visibilityBetween(param0, param1);
    }

    public void setVisibilitySet(VisibilitySet param0) {
        this.visibilitySet = param0;
    }

    public BufferBuilder.State getTransparencyState() {
        return this.transparencyState;
    }

    public void setTransparencyState(BufferBuilder.State param0) {
        this.transparencyState = param0;
    }
}
