package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
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
        private final ItemRenderer itemRenderer;
        private final EntityRenderDispatcher entityRenderer;
        private final EntityModelSet modelSet;
        private final Font font;

        public Context(
            BlockEntityRenderDispatcher param0,
            BlockRenderDispatcher param1,
            ItemRenderer param2,
            EntityRenderDispatcher param3,
            EntityModelSet param4,
            Font param5
        ) {
            this.blockEntityRenderDispatcher = param0;
            this.blockRenderDispatcher = param1;
            this.itemRenderer = param2;
            this.entityRenderer = param3;
            this.modelSet = param4;
            this.font = param5;
        }

        public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
            return this.blockEntityRenderDispatcher;
        }

        public BlockRenderDispatcher getBlockRenderDispatcher() {
            return this.blockRenderDispatcher;
        }

        public EntityRenderDispatcher getEntityRenderer() {
            return this.entityRenderer;
        }

        public ItemRenderer getItemRenderer() {
            return this.itemRenderer;
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
