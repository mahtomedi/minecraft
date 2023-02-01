package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
class SwitchGrid {
    private static final int DEFAULT_SWITCH_BUTTON_WIDTH = 44;
    private final List<SwitchGrid.LabeledSwitch> switches;

    SwitchGrid(List<SwitchGrid.LabeledSwitch> param0) {
        this.switches = param0;
    }

    public void refreshStates() {
        this.switches.forEach(SwitchGrid.LabeledSwitch::refreshState);
    }

    public static SwitchGrid.Builder builder(int param0) {
        return new SwitchGrid.Builder(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final int width;
        private final List<SwitchGrid.SwitchBuilder> switchBuilders = new ArrayList<>();
        int paddingLeft;

        public Builder(int param0) {
            this.width = param0;
        }

        public SwitchGrid.SwitchBuilder addSwitch(Component param0, BooleanSupplier param1, Consumer<Boolean> param2) {
            SwitchGrid.SwitchBuilder var0 = new SwitchGrid.SwitchBuilder(param0, param1, param2, 44);
            this.switchBuilders.add(var0);
            return var0;
        }

        public SwitchGrid.Builder withPaddingLeft(int param0) {
            this.paddingLeft = param0;
            return this;
        }

        public SwitchGrid build(Consumer<LayoutElement> param0) {
            GridLayout var0 = new GridLayout().rowSpacing(4);
            var0.addChild(SpacerElement.width(this.width - 44), 0, 0);
            var0.addChild(SpacerElement.width(44), 0, 1);
            List<SwitchGrid.LabeledSwitch> var1 = new ArrayList<>();
            int var2 = 0;

            for(SwitchGrid.SwitchBuilder var3 : this.switchBuilders) {
                var1.add(var3.build(this, var0, var2++, 0));
            }

            var0.arrangeElements();
            param0.accept(var0);
            return new SwitchGrid(var1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LabeledSwitch {
        private final CycleButton<Boolean> button;
        private final BooleanSupplier stateSupplier;
        @Nullable
        private final BooleanSupplier isActiveCondition;

        public LabeledSwitch(CycleButton<Boolean> param0, BooleanSupplier param1, @Nullable BooleanSupplier param2) {
            this.button = param0;
            this.stateSupplier = param1;
            this.isActiveCondition = param2;
        }

        public void refreshState() {
            this.button.setValue(this.stateSupplier.getAsBoolean());
            if (this.isActiveCondition != null) {
                this.button.active = this.isActiveCondition.getAsBoolean();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SwitchBuilder {
        private final Component label;
        private final BooleanSupplier stateSupplier;
        private final Consumer<Boolean> onClicked;
        @Nullable
        private Component info;
        @Nullable
        private BooleanSupplier isActiveCondition;
        private final int buttonWidth;

        SwitchBuilder(Component param0, BooleanSupplier param1, Consumer<Boolean> param2, int param3) {
            this.label = param0;
            this.stateSupplier = param1;
            this.onClicked = param2;
            this.buttonWidth = param3;
        }

        public SwitchGrid.SwitchBuilder withIsActiveCondition(BooleanSupplier param0) {
            this.isActiveCondition = param0;
            return this;
        }

        public SwitchGrid.SwitchBuilder withInfo(Component param0) {
            this.info = param0;
            return this;
        }

        SwitchGrid.LabeledSwitch build(SwitchGrid.Builder param0, GridLayout param1, int param2, int param3) {
            StringWidget var0 = new StringWidget(this.label, Minecraft.getInstance().font).alignLeft();
            param1.addChild(var0, param2, param3, param1.newCellSettings().align(0.0F, 0.5F).paddingLeft(param0.paddingLeft));
            CycleButton.Builder<Boolean> var1 = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
            var1.displayOnlyValue();
            var1.withCustomNarration(param0x -> CommonComponents.joinForNarration(this.label, param0x.createDefaultNarrationMessage()));
            if (this.info != null) {
                var1.withTooltip(param0x -> Tooltip.create(this.info));
            }

            CycleButton<Boolean> var2 = var1.create(0, 0, this.buttonWidth, 20, Component.empty(), (param0x, param1x) -> this.onClicked.accept(param1x));
            if (this.isActiveCondition != null) {
                var2.active = this.isActiveCondition.getAsBoolean();
            }

            param1.addChild(var2, param2, param3 + 1, param1.newCellSettings().alignHorizontallyRight());
            return new SwitchGrid.LabeledSwitch(var2, this.stateSupplier, this.isActiveCondition);
        }
    }
}
