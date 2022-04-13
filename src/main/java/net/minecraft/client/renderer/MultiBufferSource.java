package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiBufferSource {
    static MultiBufferSource.BufferSource immediate(BufferBuilder param0) {
        return immediateWithBuffers(ImmutableMap.of(), param0);
    }

    static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> param0, BufferBuilder param1) {
        return new MultiBufferSource.BufferSource(param1, param0);
    }

    VertexConsumer getBuffer(RenderType var1);

    @OnlyIn(Dist.CLIENT)
    public static class BufferSource implements MultiBufferSource {
        protected final BufferBuilder builder;
        protected final Map<RenderType, BufferBuilder> fixedBuffers;
        protected Optional<RenderType> lastState = Optional.empty();
        protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();

        protected BufferSource(BufferBuilder param0, Map<RenderType, BufferBuilder> param1) {
            this.builder = param0;
            this.fixedBuffers = param1;
        }

        @Override
        public VertexConsumer getBuffer(RenderType param0) {
            Optional<RenderType> var0 = param0.asOptional();
            BufferBuilder var1 = this.getBuilderRaw(param0);
            if (!Objects.equals(this.lastState, var0) || !param0.canConsolidateConsecutiveGeometry()) {
                if (this.lastState.isPresent()) {
                    RenderType var2 = this.lastState.get();
                    if (!this.fixedBuffers.containsKey(var2)) {
                        this.endBatch(var2);
                    }
                }

                if (this.startedBuffers.add(var1)) {
                    var1.begin(param0.mode(), param0.format());
                }

                this.lastState = var0;
            }

            return var1;
        }

        private BufferBuilder getBuilderRaw(RenderType param0) {
            return this.fixedBuffers.getOrDefault(param0, this.builder);
        }

        public void endLastBatch() {
            if (this.lastState.isPresent()) {
                RenderType var0 = this.lastState.get();
                if (!this.fixedBuffers.containsKey(var0)) {
                    this.endBatch(var0);
                }

                this.lastState = Optional.empty();
            }

        }

        public void endBatch() {
            this.lastState.ifPresent(param0 -> {
                VertexConsumer var0x = this.getBuffer(param0);
                if (var0x == this.builder) {
                    this.endBatch(param0);
                }

            });

            for(RenderType var0 : this.fixedBuffers.keySet()) {
                this.endBatch(var0);
            }

        }

        public void endBatch(RenderType param0) {
            BufferBuilder var0 = this.getBuilderRaw(param0);
            boolean var1 = Objects.equals(this.lastState, param0.asOptional());
            if (var1 || var0 != this.builder) {
                if (this.startedBuffers.remove(var0)) {
                    param0.end(var0, 0, 0, 0);
                    if (var1) {
                        this.lastState = Optional.empty();
                    }

                }
            }
        }
    }
}
