package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Collection;

public class CommonComponents {
    public static final Component EMPTY = Component.empty();
    public static final Component OPTION_ON = Component.translatable("options.on");
    public static final Component OPTION_OFF = Component.translatable("options.off");
    public static final Component GUI_DONE = Component.translatable("gui.done");
    public static final Component GUI_CANCEL = Component.translatable("gui.cancel");
    public static final Component GUI_YES = Component.translatable("gui.yes");
    public static final Component GUI_NO = Component.translatable("gui.no");
    public static final Component GUI_PROCEED = Component.translatable("gui.proceed");
    public static final Component GUI_CONTINUE = Component.translatable("gui.continue");
    public static final Component GUI_BACK = Component.translatable("gui.back");
    public static final Component GUI_ACKNOWLEDGE = Component.translatable("gui.acknowledge");
    public static final Component CONNECT_FAILED = Component.translatable("connect.failed");
    public static final Component NEW_LINE = Component.literal("\n");
    public static final Component NARRATION_SEPARATOR = Component.literal(". ");
    public static final Component ELLIPSIS = Component.literal("...");
    public static final Component SPACE = space();

    public static MutableComponent space() {
        return Component.literal(" ");
    }

    public static MutableComponent days(long param0) {
        return Component.translatable("gui.days", param0);
    }

    public static MutableComponent hours(long param0) {
        return Component.translatable("gui.hours", param0);
    }

    public static MutableComponent minutes(long param0) {
        return Component.translatable("gui.minutes", param0);
    }

    public static Component optionStatus(boolean param0) {
        return param0 ? OPTION_ON : OPTION_OFF;
    }

    public static MutableComponent optionStatus(Component param0, boolean param1) {
        return Component.translatable(param1 ? "options.on.composed" : "options.off.composed", param0);
    }

    public static MutableComponent optionNameValue(Component param0, Component param1) {
        return Component.translatable("options.generic_value", param0, param1);
    }

    public static MutableComponent joinForNarration(Component param0, Component param1) {
        return Component.empty().append(param0).append(NARRATION_SEPARATOR).append(param1);
    }

    public static Component joinLines(Component... param0) {
        return joinLines(Arrays.asList(param0));
    }

    public static Component joinLines(Collection<? extends Component> param0) {
        return ComponentUtils.formatList(param0, NEW_LINE);
    }
}
