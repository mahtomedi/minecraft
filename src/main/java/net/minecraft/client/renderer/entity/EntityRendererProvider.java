package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface EntityRendererProvider<T extends Entity> {
    EntityRenderer<T> create(EntityRendererProvider.Context var1);

    @OnlyIn(Dist.CLIENT)
    public static class Context {
        private final EntityRenderDispatcher entityRenderDispatcher;
        private final ItemRenderer itemRenderer;
        private final BlockRenderDispatcher blockRenderDispatcher;
        private final ItemInHandRenderer itemInHandRenderer;
        private final ResourceManager resourceManager;
        private final EntityModelSet modelSet;
        private final Font font;

        public Context(
            EntityRenderDispatcher param0,
            ItemRenderer param1,
            BlockRenderDispatcher param2,
            ItemInHandRenderer param3,
            ResourceManager param4,
            EntityModelSet param5,
            Font param6
        ) {
            this.entityRenderDispatcher = param0;
            this.itemRenderer = param1;
            this.blockRenderDispatcher = param2;
            this.itemInHandRenderer = param3;
            this.resourceManager = param4;
            this.modelSet = param5;
            this.font = param6;
        }

        public EntityRenderDispatcher getEntityRenderDispatcher() {
            return this.entityRenderDispatcher;
        }

        public ItemRenderer getItemRenderer() {
            return this.itemRenderer;
        }

        public BlockRenderDispatcher getBlockRenderDispatcher() {
            return this.blockRenderDispatcher;
        }

        public ItemInHandRenderer getItemInHandRenderer() {
            return this.itemInHandRenderer;
        }

        public ResourceManager getResourceManager() {
            return this.resourceManager;
        }

        public EntityModelSet getModelSet() {
            return this.modelSet;
        }

        public ModelManager getModelManager() {
            return this.blockRenderDispatcher.getBlockModelShaper().getModelManager();
        }

        public ModelPart bakeLayer(ModelLayerLocation param0) {
            return this.modelSet.bakeLayer(param0);
        }

        public Font getFont() {
            return this.font;
        }
    }
}
