package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface BlockEntityRendererProvider<T extends BlockEntity> {
    BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context var1);

    @OnlyIn(Dist.CLIENT)
    public static class Context {
        private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
        private final BlockRenderDispatcher blockRenderDispatcher;
        private final EntityModelSet modelSet;
        private final Font font;

        public Context(BlockEntityRenderDispatcher param0, BlockRenderDispatcher param1, EntityModelSet param2, Font param3) {
            this.blockEntityRenderDispatcher = param0;
            this.blockRenderDispatcher = param1;
            this.modelSet = param2;
            this.font = param3;
        }

        public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
            return this.blockEntityRenderDispatcher;
        }

        public BlockRenderDispatcher getBlockRenderDispatcher() {
            return this.blockRenderDispatcher;
        }

        public EntityModelSet getModelSet() {
            return this.modelSet;
        }

        public ModelPart bakeLayer(ModelLayerLocation param0) {
            return this.modelSet.bakeLayer(param0);
        }

        public Font getFont() {
            return this.font;
        }
    }
}
