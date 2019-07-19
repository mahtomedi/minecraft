package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HoverEvent {
    private final HoverEvent.Action action;
    private final Component value;

    public HoverEvent(HoverEvent.Action param0, Component param1) {
        this.action = param0;
        this.value = param1;
    }

    public HoverEvent.Action getAction() {
        return this.action;
    }

    public Component getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            HoverEvent var0 = (HoverEvent)param0;
            if (this.action != var0.action) {
                return false;
            } else {
                if (this.value != null) {
                    if (!this.value.equals(var0.value)) {
                        return false;
                    }
                } else if (var0.value != null) {
                    return false;
                }

                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
    }

    @Override
    public int hashCode() {
        int var0 = this.action.hashCode();
        return 31 * var0 + (this.value != null ? this.value.hashCode() : 0);
    }

    public static enum Action {
        SHOW_TEXT("show_text", true),
        SHOW_ITEM("show_item", true),
        SHOW_ENTITY("show_entity", true);

        private static final Map<String, HoverEvent.Action> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toMap(HoverEvent.Action::getName, param0 -> param0));
        private final boolean allowFromServer;
        private final String name;

        private Action(String param0, boolean param1) {
            this.name = param0;
            this.allowFromServer = param1;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        public String getName() {
            return this.name;
        }

        public static HoverEvent.Action getByName(String param0) {
            return LOOKUP.get(param0);
        }
    }
}
