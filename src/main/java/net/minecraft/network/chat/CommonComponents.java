package net.minecraft.network.chat;

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

    public static Component optionStatus(boolean param0) {
        return param0 ? OPTION_ON : OPTION_OFF;
    }

    public static MutableComponent optionStatus(Component param0, boolean param1) {
        return new TranslatableComponent(param1 ? "options.on.composed" : "options.off.composed", param0);
    }

    public static MutableComponent optionNameValue(Component param0, Component param1) {
        return new TranslatableComponent("options.generic_value", param0, param1);
    }
}
