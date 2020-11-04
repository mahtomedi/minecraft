package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
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
        private final ResourceManager resourceManager;
        private final EntityModelSet modelSet;
        private final Font font;

        public Context(EntityRenderDispatcher param0, ItemRenderer param1, ResourceManager param2, EntityModelSet param3, Font param4) {
            this.entityRenderDispatcher = param0;
            this.itemRenderer = param1;
            this.resourceManager = param2;
            this.modelSet = param3;
            this.font = param4;
        }

        public EntityRenderDispatcher getEntityRenderDispatcher() {
            return this.entityRenderDispatcher;
        }

        public ItemRenderer getItemRenderer() {
            return this.itemRenderer;
        }

        public ResourceManager getResourceManager() {
            return this.resourceManager;
        }

        public EntityModelSet getModelSet() {
            return this.modelSet;
        }

        public ModelPart getLayer(ModelLayerLocation param0) {
            return this.modelSet.getLayer(param0);
        }

        public Font getFont() {
            return this.font;
        }
    }
}
