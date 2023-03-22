package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
    public static final Codec<ChatType> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat),
                    ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)
                )
                .apply(param0, ChatType::new)
    );
    public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatType> CHAT = create("chat");
    public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
    public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = create("msg_command_incoming");
    public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");
    public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");
    public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");

    private static ResourceKey<ChatType> create(String param0) {
        return ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation(param0));
    }

    public static void bootstrap(BootstapContext<ChatType> param0) {
        param0.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
        param0.register(
            SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
        );
        param0.register(
            MSG_COMMAND_INCOMING,
            new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
        );
        param0.register(
            MSG_COMMAND_OUTGOING,
            new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
        );
        param0.register(
            TEAM_MSG_COMMAND_INCOMING,
            new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
        );
        param0.register(
            TEAM_MSG_COMMAND_OUTGOING,
            new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate"))
        );
        param0.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
    }

    public static ChatType.Bound bind(ResourceKey<ChatType> param0, Entity param1) {
        return bind(param0, param1.level.registryAccess(), param1.getDisplayName());
    }

    public static ChatType.Bound bind(ResourceKey<ChatType> param0, CommandSourceStack param1) {
        return bind(param0, param1.registryAccess(), param1.getDisplayName());
    }

    public static ChatType.Bound bind(ResourceKey<ChatType> param0, RegistryAccess param1, Component param2) {
        Registry<ChatType> var0 = param1.registryOrThrow(Registries.CHAT_TYPE);
        return var0.getOrThrow(param0).bind(param2);
    }

    public ChatType.Bound bind(Component param0) {
        return new ChatType.Bound(this, param0);
    }

    public static record Bound(ChatType chatType, Component name, @Nullable Component targetName) {
        Bound(ChatType param0, Component param1) {
            this(param0, param1, null);
        }

        public Component decorate(Component param0) {
            return this.chatType.chat().decorate(param0, this);
        }

        public Component decorateNarration(Component param0) {
            return this.chatType.narration().decorate(param0, this);
        }

        public ChatType.Bound withTargetName(Component param0) {
            return new ChatType.Bound(this.chatType, this.name, param0);
        }

        public ChatType.BoundNetwork toNetwork(RegistryAccess param0) {
            Registry<ChatType> var0 = param0.registryOrThrow(Registries.CHAT_TYPE);
            return new ChatType.BoundNetwork(var0.getId(this.chatType), this.name, this.targetName);
        }
    }

    public static record BoundNetwork(int chatType, Component name, @Nullable Component targetName) {
        public BoundNetwork(FriendlyByteBuf param0) {
            this(param0.readVarInt(), param0.readComponent(), param0.readNullable(FriendlyByteBuf::readComponent));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeVarInt(this.chatType);
            param0.writeComponent(this.name);
            param0.writeNullable(this.targetName, FriendlyByteBuf::writeComponent);
        }

        public Optional<ChatType.Bound> resolve(RegistryAccess param0) {
            Registry<ChatType> var0 = param0.registryOrThrow(Registries.CHAT_TYPE);
            ChatType var1 = var0.byId(this.chatType);
            return Optional.ofNullable(var1).map(param0x -> new ChatType.Bound(param0x, this.name, this.targetName));
        }
    }
}
