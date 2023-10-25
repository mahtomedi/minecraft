package net.minecraft.client.gui.screens.telemetry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleConsumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TelemetryEventWidget extends AbstractScrollWidget {
    private static final int HEADER_HORIZONTAL_PADDING = 32;
    private static final String TELEMETRY_REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
    private static final String TELEMETRY_OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
    private static final String TELEMETRY_OPTIONAL_DISABLED_TRANSLATION_KEY = "telemetry.event.optional.disabled";
    private static final Component PROPERTY_TITLE = Component.translatable("telemetry_info.property_title").withStyle(ChatFormatting.UNDERLINE);
    private final Font font;
    private TelemetryEventWidget.Content content;
    @Nullable
    private DoubleConsumer onScrolledListener;

    public TelemetryEventWidget(int param0, int param1, int param2, int param3, Font param4) {
        super(param0, param1, param2, param3, Component.empty());
        this.font = param4;
        this.content = this.buildContent(Minecraft.getInstance().telemetryOptInExtra());
    }

    public void onOptInChanged(boolean param0) {
        this.content = this.buildContent(param0);
        this.setScrollAmount(this.scrollAmount());
    }

    private TelemetryEventWidget.Content buildContent(boolean param0) {
        TelemetryEventWidget.ContentBuilder var0 = new TelemetryEventWidget.ContentBuilder(this.containerWidth());
        List<TelemetryEventType> var1 = new ArrayList<>(TelemetryEventType.values());
        var1.sort(Comparator.comparing(TelemetryEventType::isOptIn));

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            TelemetryEventType var3 = var1.get(var2);
            boolean var4 = var3.isOptIn() && !param0;
            this.addEventType(var0, var3, var4);
            if (var2 < var1.size() - 1) {
                var0.addSpacer(9);
            }
        }

        return var0.build();
    }

    public void setOnScrolledListener(@Nullable DoubleConsumer param0) {
        this.onScrolledListener = param0;
    }

    @Override
    protected void setScrollAmount(double param0) {
        super.setScrollAmount(param0);
        if (this.onScrolledListener != null) {
            this.onScrolledListener.accept(this.scrollAmount());
        }

    }

    @Override
    protected int getInnerHeight() {
        return this.content.container().getHeight();
    }

    @Override
    protected double scrollRate() {
        return 9.0;
    }

    @Override
    protected void renderContents(GuiGraphics param0, int param1, int param2, float param3) {
        int var0 = this.getY() + this.innerPadding();
        int var1 = this.getX() + this.innerPadding();
        param0.pose().pushPose();
        param0.pose().translate((double)var1, (double)var0, 0.0);
        this.content.container().visitWidgets(param4 -> param4.render(param0, param1, param2, param3));
        param0.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, this.content.narration());
    }

    private Component grayOutIfDisabled(Component param0, boolean param1) {
        return (Component)(param1 ? param0.copy().withStyle(ChatFormatting.GRAY) : param0);
    }

    private void addEventType(TelemetryEventWidget.ContentBuilder param0, TelemetryEventType param1, boolean param2) {
        String var0 = param1.isOptIn() ? (param2 ? "telemetry.event.optional.disabled" : "telemetry.event.optional") : "telemetry.event.required";
        param0.addHeader(this.font, this.grayOutIfDisabled(Component.translatable(var0, param1.title()), param2));
        param0.addHeader(this.font, param1.description().withStyle(ChatFormatting.GRAY));
        param0.addSpacer(9 / 2);
        param0.addLine(this.font, this.grayOutIfDisabled(PROPERTY_TITLE, param2), 2);
        this.addEventTypeProperties(param1, param0, param2);
    }

    private void addEventTypeProperties(TelemetryEventType param0, TelemetryEventWidget.ContentBuilder param1, boolean param2) {
        for(TelemetryProperty<?> var0 : param0.properties()) {
            param1.addLine(this.font, this.grayOutIfDisabled(var0.title(), param2));
        }

    }

    private int containerWidth() {
        return this.width - this.totalInnerPadding();
    }

    @OnlyIn(Dist.CLIENT)
    static record Content(Layout container, Component narration) {
    }

    @OnlyIn(Dist.CLIENT)
    static class ContentBuilder {
        private final int width;
        private final LinearLayout layout;
        private final MutableComponent narration = Component.empty();

        public ContentBuilder(int param0) {
            this.width = param0;
            this.layout = LinearLayout.vertical();
            this.layout.defaultCellSetting().alignHorizontallyLeft();
            this.layout.addChild(SpacerElement.width(param0));
        }

        public void addLine(Font param0, Component param1) {
            this.addLine(param0, param1, 0);
        }

        public void addLine(Font param0, Component param1, int param2) {
            this.layout.addChild(new MultiLineTextWidget(param1, param0).setMaxWidth(this.width), param1x -> param1x.paddingBottom(param2));
            this.narration.append(param1).append("\n");
        }

        public void addHeader(Font param0, Component param1) {
            this.layout
                .addChild(
                    new MultiLineTextWidget(param1, param0).setMaxWidth(this.width - 64).setCentered(true),
                    param0x -> param0x.alignHorizontallyCenter().paddingHorizontal(32)
                );
            this.narration.append(param1).append("\n");
        }

        public void addSpacer(int param0) {
            this.layout.addChild(SpacerElement.height(param0));
        }

        public TelemetryEventWidget.Content build() {
            this.layout.arrangeElements();
            return new TelemetryEventWidget.Content(this.layout, this.narration);
        }
    }
}
