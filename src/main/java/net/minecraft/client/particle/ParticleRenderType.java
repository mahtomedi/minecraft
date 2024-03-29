package net.minecraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ParticleRenderType {
    ParticleRenderType TERRAIN_SHEET = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder param0, TextureManager param1) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator param0) {
            param0.end();
        }

        @Override
        public String toString() {
            return "TERRAIN_SHEET";
        }
    };
    ParticleRenderType PARTICLE_SHEET_OPAQUE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder param0, TextureManager param1) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator param0) {
            param0.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_OPAQUE";
        }
    };
    ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder param0, TextureManager param1) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator param0) {
            param0.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT";
        }
    };
    ParticleRenderType PARTICLE_SHEET_LIT = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder param0, TextureManager param1) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator param0) {
            param0.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_LIT";
        }
    };
    ParticleRenderType CUSTOM = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder param0, TextureManager param1) {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }

        @Override
        public void end(Tesselator param0) {
        }

        @Override
        public String toString() {
            return "CUSTOM";
        }
    };
    ParticleRenderType NO_RENDER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder param0, TextureManager param1) {
        }

        @Override
        public void end(Tesselator param0) {
        }

        @Override
        public String toString() {
            return "NO_RENDER";
        }
    };

    void begin(BufferBuilder var1, TextureManager var2);

    void end(Tesselator var1);
}
