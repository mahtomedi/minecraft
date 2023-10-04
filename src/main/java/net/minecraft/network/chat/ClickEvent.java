package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public class ClickEvent {
    public static final Codec<ClickEvent> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ClickEvent.Action.CODEC.forGetter(param0x -> param0x.action), Codec.STRING.fieldOf("value").forGetter(param0x -> param0x.value))
                .apply(param0, ClickEvent::new)
    );
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
            return this.action == var0.action && this.value.equals(var0.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "ClickEvent{action=" + this.action + ", value='" + this.value + "'}";
    }

    @Override
    public int hashCode() {
        int var0 = this.action.hashCode();
        return 31 * var0 + this.value.hashCode();
    }

    public static enum Action implements StringRepresentable {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true),
        COPY_TO_CLIPBOARD("copy_to_clipboard", true);

        public static final MapCodec<ClickEvent.Action> UNSAFE_CODEC = StringRepresentable.fromEnum(ClickEvent.Action::values).fieldOf("action");
        public static final MapCodec<ClickEvent.Action> CODEC = ExtraCodecs.validate(UNSAFE_CODEC, ClickEvent.Action::filterForSerialization);
        private final boolean allowFromServer;
        private final String name;

        private Action(String param0, boolean param1) {
            this.name = param0;
            this.allowFromServer = param1;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static DataResult<ClickEvent.Action> filterForSerialization(ClickEvent.Action param0) {
            return !param0.isAllowedFromServer() ? DataResult.error(() -> "Action not allowed: " + param0) : DataResult.success(param0, Lifecycle.stable());
        }
    }
}
