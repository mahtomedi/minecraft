package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Collection;

public class CommonComponents {
    public static final Component OPTION_ON = new TranslatableComponent("options.on");
    public static final Component OPTION_OFF = new TranslatableComponent("options.off");
    public static final Component GUI_DONE = new TranslatableComponent("gui.done");
    public static final Component GUI_CANCEL = new TranslatableComponent("gui.cancel");
    public static final Component GUI_YES = new TranslatableComponent("gui.yes");
    public static final Component GUI_NO = new TranslatableComponent("gui.no");
    public static final Component GUI_PROCEED = new TranslatableComponent("gui.proceed");
    public static final Component GUI_BACK = new TranslatableComponent("gui.back");
    public static final Component CONNECT_FAILED = new TranslatableComponent("connect.failed");
    public static final Component NEW_LINE = new TextComponent("\n");
    public static final Component NARRATION_SEPARATOR = new TextComponent(". ");

    public static Component optionStatus(boolean param0) {
        return param0 ? OPTION_ON : OPTION_OFF;
    }

    public static MutableComponent optionStatus(Component param0, boolean param1) {
        return new TranslatableComponent(param1 ? "options.on.composed" : "options.off.composed", param0);
    }

    public static MutableComponent optionNameValue(Component param0, Component param1) {
        return new TranslatableComponent("options.generic_value", param0, param1);
    }

    public static MutableComponent joinForNarration(Component param0, Component param1) {
        return new TextComponent("").append(param0).append(NARRATION_SEPARATOR).append(param1);
    }

    public static Component joinLines(Component... param0) {
        return joinLines(Arrays.asList(param0));
    }

    public static Component joinLines(Collection<? extends Component> param0) {
        return ComponentUtils.formatList(param0, NEW_LINE);
    }
}
