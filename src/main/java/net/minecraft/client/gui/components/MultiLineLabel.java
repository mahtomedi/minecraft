package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
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
        public void renderBackgroundCentered(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getWidth() {
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
        return param1.isEmpty()
            ? EMPTY
            : new MultiLineLabel() {
                private final int width = param1.stream().mapToInt(param0xx -> param0xx.width).max().orElse(0);
    
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
                public void renderBackgroundCentered(PoseStack param0x, int param1x, int param2, int param3, int param4, int param5) {
                    int var0 = param1.stream().mapToInt(param0xx -> param0xx.width).max().orElse(0);
                    if (var0 > 0) {
                        GuiComponent.fill(
                            param0, param1 - var0 / 2 - param4, param2 - param4, param1 + var0 / 2 + param4, param2 + param1.size() * param3 + param4, param5
                        );
                    }
    
                }
    
                @Override
                public int getLineCount() {
                    return param1.size();
                }
    
                @Override
                public int getWidth() {
                    return this.width;
                }
            };
    }

    int renderCentered(PoseStack var1, int var2, int var3);

    int renderCentered(PoseStack var1, int var2, int var3, int var4, int var5);

    int renderLeftAligned(PoseStack var1, int var2, int var3, int var4, int var5);

    int renderLeftAlignedNoShadow(PoseStack var1, int var2, int var3, int var4, int var5);

    void renderBackgroundCentered(PoseStack var1, int var2, int var3, int var4, int var5, int var6);

    int getLineCount();

    int getWidth();

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
