package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record ChatType(Optional<ChatType.TextDisplay> chat, Optional<ChatType.TextDisplay> overlay, Optional<ChatType.Narration> narration) {
    public static final Codec<ChatType> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ChatType.TextDisplay.CODEC.optionalFieldOf("chat").forGetter(ChatType::chat),
                    ChatType.TextDisplay.CODEC.optionalFieldOf("overlay").forGetter(ChatType::overlay),
                    ChatType.Narration.CODEC.optionalFieldOf("narration").forGetter(ChatType::narration)
                )
                .apply(param0, ChatType::new)
    );
    public static final ResourceKey<ChatType> CHAT = create("chat");
    public static final ResourceKey<ChatType> SYSTEM = create("system");
    public static final ResourceKey<ChatType> GAME_INFO = create("game_info");
    public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND = create("msg_command");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND = create("team_msg_command");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");
    public static final ResourceKey<ChatType> TELLRAW_COMMAND = create("tellraw_command");

    private static ResourceKey<ChatType> create(String param0) {
        return ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation(param0));
    }

    public static ChatType bootstrap(Registry<ChatType> param0) {
        ChatType var0 = Registry.register(
            param0,
            CHAT,
            new ChatType(
                Optional.of(ChatType.TextDisplay.decorated(ChatDecoration.withSender("chat.type.text"))),
                Optional.empty(),
                Optional.of(ChatType.Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
            )
        );
        Registry.register(
            param0,
            SYSTEM,
            new ChatType(
                Optional.of(ChatType.TextDisplay.undecorated()),
                Optional.empty(),
                Optional.of(ChatType.Narration.undecorated(ChatType.Narration.Priority.SYSTEM))
            )
        );
        Registry.register(param0, GAME_INFO, new ChatType(Optional.empty(), Optional.of(ChatType.TextDisplay.undecorated()), Optional.empty()));
        Registry.register(
            param0,
            SAY_COMMAND,
            new ChatType(
                Optional.of(ChatType.TextDisplay.decorated(ChatDecoration.withSender("chat.type.announcement"))),
                Optional.empty(),
                Optional.of(ChatType.Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
            )
        );
        Registry.register(
            param0,
            MSG_COMMAND,
            new ChatType(
                Optional.of(ChatType.TextDisplay.decorated(ChatDecoration.directMessage("commands.message.display.incoming"))),
                Optional.empty(),
                Optional.of(ChatType.Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
            )
        );
        Registry.register(
            param0,
            TEAM_MSG_COMMAND,
            new ChatType(
                Optional.of(ChatType.TextDisplay.decorated(ChatDecoration.teamMessage("chat.type.team.text"))),
                Optional.empty(),
                Optional.of(ChatType.Narration.decorated(ChatDecoration.withSender("chat.type.text.narrate"), ChatType.Narration.Priority.CHAT))
            )
        );
        Registry.register(
            param0,
            EMOTE_COMMAND,
            new ChatType(
                Optional.of(ChatType.TextDisplay.decorated(ChatDecoration.withSender("chat.type.emote"))),
                Optional.empty(),
                Optional.of(ChatType.Narration.decorated(ChatDecoration.withSender("chat.type.emote"), ChatType.Narration.Priority.CHAT))
            )
        );
        Registry.register(
            param0,
            TELLRAW_COMMAND,
            new ChatType(
                Optional.of(ChatType.TextDisplay.undecorated()),
                Optional.empty(),
                Optional.of(ChatType.Narration.undecorated(ChatType.Narration.Priority.CHAT))
            )
        );
        return var0;
    }

    public static record Narration(Optional<ChatDecoration> decoration, ChatType.Narration.Priority priority) {
        public static final Codec<ChatType.Narration> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ChatDecoration.CODEC.optionalFieldOf("decoration").forGetter(ChatType.Narration::decoration),
                        ChatType.Narration.Priority.CODEC.fieldOf("priority").forGetter(ChatType.Narration::priority)
                    )
                    .apply(param0, ChatType.Narration::new)
        );

        public static ChatType.Narration undecorated(ChatType.Narration.Priority param0) {
            return new ChatType.Narration(Optional.empty(), param0);
        }

        public static ChatType.Narration decorated(ChatDecoration param0, ChatType.Narration.Priority param1) {
            return new ChatType.Narration(Optional.of(param0), param1);
        }

        public Component decorate(Component param0, @Nullable ChatSender param1) {
            return this.decoration.<Component>map(param2 -> param2.decorate(param0, param1)).orElse(param0);
        }

        public static enum Priority implements StringRepresentable {
            CHAT("chat", false),
            SYSTEM("system", true);

            public static final Codec<ChatType.Narration.Priority> CODEC = StringRepresentable.fromEnum(ChatType.Narration.Priority::values);
            private final String name;
            private final boolean interrupts;

            private Priority(String param0, boolean param1) {
                this.name = param0;
                this.interrupts = param1;
            }

            public boolean interrupts() {
                return this.interrupts;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    public static record TextDisplay(Optional<ChatDecoration> decoration) {
        public static final Codec<ChatType.TextDisplay> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(ChatDecoration.CODEC.optionalFieldOf("decoration").forGetter(ChatType.TextDisplay::decoration))
                    .apply(param0, ChatType.TextDisplay::new)
        );

        public static ChatType.TextDisplay undecorated() {
            return new ChatType.TextDisplay(Optional.empty());
        }

        public static ChatType.TextDisplay decorated(ChatDecoration param0) {
            return new ChatType.TextDisplay(Optional.of(param0));
        }

        public Component decorate(Component param0, @Nullable ChatSender param1) {
            return this.decoration.<Component>map(param2 -> param2.decorate(param0, param1)).orElse(param0);
        }
    }
}
