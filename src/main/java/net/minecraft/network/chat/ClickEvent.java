package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClickEvent {
    private final ClickEvent.Action action;
    private final String value;

    public ClickEvent(ClickEvent.Action param0, String param1) {
        this.action = param0;
        this.value = param1;
    }

    public ClickEvent.Action getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            ClickEvent var0 = (ClickEvent)param0;
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
        return "ClickEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
    }

    @Override
    public int hashCode() {
        int var0 = this.action.hashCode();
        return 31 * var0 + (this.value != null ? this.value.hashCode() : 0);
    }

    public static enum Action {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true);

        private static final Map<String, ClickEvent.Action> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toMap(ClickEvent.Action::getName, param0 -> param0));
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

        public static ClickEvent.Action getByName(String param0) {
            return LOOKUP.get(param0);
        }
    }
}
