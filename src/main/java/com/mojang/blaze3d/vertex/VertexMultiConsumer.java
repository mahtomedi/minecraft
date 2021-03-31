package com.mojang.blaze3d.vertex;

import java.util.function.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexMultiConsumer {
    public static VertexConsumer create() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer create(VertexConsumer param0) {
        return param0;
    }

    public static VertexConsumer create(VertexConsumer param0, VertexConsumer param1) {
        return new VertexMultiConsumer.Double(param0, param1);
    }

    public static VertexConsumer create(VertexConsumer... param0) {
        return new VertexMultiConsumer.Multiple(param0);
    }

    @OnlyIn(Dist.CLIENT)
    static class Double implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Double(VertexConsumer param0, VertexConsumer param1) {
            if (param0 == param1) {
                throw new IllegalArgumentException("Duplicate delegates");
            } else {
                this.first = param0;
                this.second = param1;
            }
        }

        @Override
        public VertexConsumer vertex(double param0, double param1, double param2) {
            this.first.vertex(param0, param1, param2);
            this.second.vertex(param0, param1, param2);
            return this;
        }

        @Override
        public VertexConsumer color(int param0, int param1, int param2, int param3) {
            this.first.color(param0, param1, param2, param3);
            this.second.color(param0, param1, param2, param3);
            return this;
        }

        @Override
        public VertexConsumer uv(float param0, float param1) {
            this.first.uv(param0, param1);
            this.second.uv(param0, param1);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int param0, int param1) {
            this.first.overlayCoords(param0, param1);
            this.second.overlayCoords(param0, param1);
            return this;
        }

        @Override
        public VertexConsumer uv2(int param0, int param1) {
            this.first.uv2(param0, param1);
            this.second.uv2(param0, param1);
            return this;
        }

        @Override
        public VertexConsumer normal(float param0, float param1, float param2) {
            this.first.normal(param0, param1, param2);
            this.second.normal(param0, param1, param2);
            return this;
        }

        @Override
        public void vertex(
            float param0,
            float param1,
            float param2,
            float param3,
            float param4,
            float param5,
            float param6,
            float param7,
            float param8,
            int param9,
            int param10,
            float param11,
            float param12,
            float param13
        ) {
            this.first.vertex(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
            this.second.vertex(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
        }

        @Override
        public void endVertex() {
            this.first.endVertex();
            this.second.endVertex();
        }

        @Override
        public void defaultColor(int param0, int param1, int param2, int param3) {
            this.first.defaultColor(param0, param1, param2, param3);
            this.second.defaultColor(param0, param1, param2, param3);
        }

        @Override
        public void unsetDefaultColor() {
            this.first.unsetDefaultColor();
            this.second.unsetDefaultColor();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Multiple implements VertexConsumer {
        private final VertexConsumer[] delegates;

        public Multiple(VertexConsumer[] param0) {
            for(int var0 = 0; var0 < param0.length; ++var0) {
                for(int var1 = var0 + 1; var1 < param0.length; ++var1) {
                    if (param0[var0] == param0[var1]) {
                        throw new IllegalArgumentException("Duplicate delegates");
                    }
                }
            }

            this.delegates = param0;
        }

        private void forEach(Consumer<VertexConsumer> param0) {
            for(VertexConsumer var0 : this.delegates) {
                param0.accept(var0);
            }

        }

        @Override
        public VertexConsumer vertex(double param0, double param1, double param2) {
            this.forEach(param3 -> param3.vertex(param0, param1, param2));
            return this;
        }

        @Override
        public VertexConsumer color(int param0, int param1, int param2, int param3) {
            this.forEach(param4 -> param4.color(param0, param1, param2, param3));
            return this;
        }

        @Override
        public VertexConsumer uv(float param0, float param1) {
            this.forEach(param2 -> param2.uv(param0, param1));
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int param0, int param1) {
            this.forEach(param2 -> param2.overlayCoords(param0, param1));
            return this;
        }

        @Override
        public VertexConsumer uv2(int param0, int param1) {
            this.forEach(param2 -> param2.uv2(param0, param1));
            return this;
        }

        @Override
        public VertexConsumer normal(float param0, float param1, float param2) {
            this.forEach(param3 -> param3.normal(param0, param1, param2));
            return this;
        }

        @Override
        public void vertex(
            float param0,
            float param1,
            float param2,
            float param3,
            float param4,
            float param5,
            float param6,
            float param7,
            float param8,
            int param9,
            int param10,
            float param11,
            float param12,
            float param13
        ) {
            this.forEach(
                param14 -> param14.vertex(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13)
            );
        }

        @Override
        public void endVertex() {
            this.forEach(VertexConsumer::endVertex);
        }

        @Override
        public void defaultColor(int param0, int param1, int param2, int param3) {
            this.forEach(param4 -> param4.defaultColor(param0, param1, param2, param3));
        }

        @Override
        public void unsetDefaultColor() {
            this.forEach(VertexConsumer::unsetDefaultColor);
        }
    }
}
