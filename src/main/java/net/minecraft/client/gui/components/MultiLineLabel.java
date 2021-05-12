package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiLineLabel {
    MultiLineLabel EMPTY = new MultiLineLabel() {
        @Override
        public int renderCentered(PoseStack param0, int param1, int param2) {
            return param2;
        }

        @Override
        public int renderCentered(PoseStack param0, int param1, int param2, int param3, int param4) {
            return param2;
        }

        @Override
        public int renderLeftAligned(PoseStack param0, int param1, int param2, int param3, int param4) {
            return param2;
        }

        @Override
        public int renderLeftAlignedNoShadow(PoseStack param0, int param1, int param2, int param3, int param4) {
            return param2;
        }

        @Override
        public int getLineCount() {
            return 0;
        }
    };

    static MultiLineLabel create(Font param0, FormattedText param1, int param2) {
        return createFixed(
            param0,
            param0.split(param1, param2)
                .stream()
                .map(param1x -> new MultiLineLabel.TextWithWidth(param1x, param0.width(param1x)))
                .collect(ImmutableList.toImmutableList())
        );
    }

    static MultiLineLabel create(Font param0, FormattedText param1, int param2, int param3) {
        return createFixed(
            param0,
            param0.split(param1, param2)
                .stream()
                .limit((long)param3)
                .map(param1x -> new MultiLineLabel.TextWithWidth(param1x, param0.width(param1x)))
                .collect(ImmutableList.toImmutableList())
        );
    }

    static MultiLineLabel create(Font param0, Component... param1) {
        return createFixed(
            param0,
            Arrays.stream(param1)
                .map(Component::getVisualOrderText)
                .map(param1x -> new MultiLineLabel.TextWithWidth(param1x, param0.width(param1x)))
                .collect(ImmutableList.toImmutableList())
        );
    }

    static MultiLineLabel create(Font param0, List<Component> param1) {
        return createFixed(
            param0,
            param1.stream()
                .map(Component::getVisualOrderText)
                .map(param1x -> new MultiLineLabel.TextWithWidth(param1x, param0.width(param1x)))
                .collect(ImmutableList.toImmutableList())
        );
    }

    static MultiLineLabel createFixed(final Font param0, final List<MultiLineLabel.TextWithWidth> param1) {
        return param1.isEmpty() ? EMPTY : new MultiLineLabel() {
            @Override
            public int renderCentered(PoseStack param0x, int param1x, int param2) {
                return this.renderCentered(param0, param1, param2, 9, 16777215);
            }

            @Override
            public int renderCentered(PoseStack param0x, int param1x, int param2, int param3, int param4) {
                int var0 = param2;

                for(MultiLineLabel.TextWithWidth var1 : param1) {
                    param0.drawShadow(param0, var1.text, (float)(param1 - var1.width / 2), (float)var0, param4);
                    var0 += param3;
                }

                return var0;
            }

            @Override
            public int renderLeftAligned(PoseStack param0x, int param1x, int param2, int param3, int param4) {
                int var0 = param2;

                for(MultiLineLabel.TextWithWidth var1 : param1) {
                    param0.drawShadow(param0, var1.text, (float)param1, (float)var0, param4);
                    var0 += param3;
                }

                return var0;
            }

            @Override
            public int renderLeftAlignedNoShadow(PoseStack param0x, int param1x, int param2, int param3, int param4) {
                int var0 = param2;

                for(MultiLineLabel.TextWithWidth var1 : param1) {
                    param0.draw(param0, var1.text, (float)param1, (float)var0, param4);
                    var0 += param3;
                }

                return var0;
            }

            @Override
            public int getLineCount() {
                return param1.size();
            }
        };
    }

    int renderCentered(PoseStack var1, int var2, int var3);

    int renderCentered(PoseStack var1, int var2, int var3, int var4, int var5);

    int renderLeftAligned(PoseStack var1, int var2, int var3, int var4, int var5);

    int renderLeftAlignedNoShadow(PoseStack var1, int var2, int var3, int var4, int var5);

    int getLineCount();

    @OnlyIn(Dist.CLIENT)
    public static class TextWithWidth {
        final FormattedCharSequence text;
        final int width;

        TextWithWidth(FormattedCharSequence param0, int param1) {
            this.text = param0;
            this.width = param1;
        }
    }
}
