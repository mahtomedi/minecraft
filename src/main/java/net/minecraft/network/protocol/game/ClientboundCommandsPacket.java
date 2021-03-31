package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final RootCommandNode<SharedSuggestionProvider> root;

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> param0) {
        this.root = param0;
    }

    public ClientboundCommandsPacket(FriendlyByteBuf param0) {
        List<ClientboundCommandsPacket.Entry> var0 = param0.readList(ClientboundCommandsPacket::readNode);
        resolveEntries(var0);
        int var1 = param0.readVarInt();
        this.root = (RootCommandNode)var0.get(var1).node;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> var0 = enumerateNodes(this.root);
        List<CommandNode<SharedSuggestionProvider>> var1 = getNodesInIdOrder(var0);
        param0.writeCollection(var1, (param1, param2) -> writeNode(param1, param2, var0));
        param0.writeVarInt(var0.get(this.root));
    }

    private static void resolveEntries(List<ClientboundCommandsPacket.Entry> param0) {
        List<ClientboundCommandsPacket.Entry> var0 = Lists.newArrayList(param0);

        while(!var0.isEmpty()) {
            boolean var1 = var0.removeIf(param1 -> param1.build(param0));
            if (!var1) {
                throw new IllegalStateException("Server sent an impossible command tree");
            }
        }

    }

    private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> param0) {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> var0 = new Object2IntOpenHashMap<>();
        Queue<CommandNode<SharedSuggestionProvider>> var1 = Queues.newArrayDeque();
        var1.add(param0);

        CommandNode<SharedSuggestionProvider> var2;
        while((var2 = var1.poll()) != null) {
            if (!var0.containsKey(var2)) {
                int var3 = var0.size();
                var0.put(var2, var3);
                var1.addAll(var2.getChildren());
                if (var2.getRedirect() != null) {
                    var1.add(var2.getRedirect());
                }
            }
        }

        return var0;
    }

    private static List<CommandNode<SharedSuggestionProvider>> getNodesInIdOrder(Object2IntMap<CommandNode<SharedSuggestionProvider>> param0) {
        ObjectArrayList<CommandNode<SharedSuggestionProvider>> var0 = new ObjectArrayList<>(param0.size());
        var0.size(param0.size());

        for(Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> var1 : Object2IntMaps.fastIterable(param0)) {
            var0.set(var1.getIntValue(), var1.getKey());
        }

        return var0;
    }

    private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf param0x) {
        byte var0x = param0x.readByte();
        int[] var1x = param0x.readVarIntArray();
        int var2 = (var0x & 8) != 0 ? param0x.readVarInt() : 0;
        ArgumentBuilder<SharedSuggestionProvider, ?> var3 = createBuilder(param0x, var0x);
        return new ClientboundCommandsPacket.Entry(var3, var0x, var2, var1x);
    }

    @Nullable
    private static ArgumentBuilder<SharedSuggestionProvider, ?> createBuilder(FriendlyByteBuf param0, byte param1) {
        int var0 = param1 & 3;
        if (var0 == 2) {
            String var1 = param0.readUtf();
            ArgumentType<?> var2 = ArgumentTypes.deserialize(param0);
            if (var2 == null) {
                return null;
            } else {
                RequiredArgumentBuilder<SharedSuggestionProvider, ?> var3 = RequiredArgumentBuilder.argument(var1, var2);
                if ((param1 & 16) != 0) {
                    var3.suggests(SuggestionProviders.getProvider(param0.readResourceLocation()));
                }

                return var3;
            }
        } else {
            return var0 == 1 ? LiteralArgumentBuilder.literal(param0.readUtf()) : null;
        }
    }

    private static void writeNode(
        FriendlyByteBuf param0, CommandNode<SharedSuggestionProvider> param1, Map<CommandNode<SharedSuggestionProvider>, Integer> param2
    ) {
        byte var0 = 0;
        if (param1.getRedirect() != null) {
            var0 = (byte)(var0 | 8);
        }

        if (param1.getCommand() != null) {
            var0 = (byte)(var0 | 4);
        }

        if (param1 instanceof RootCommandNode) {
            var0 = (byte)(var0 | 0);
        } else if (param1 instanceof ArgumentCommandNode) {
            var0 = (byte)(var0 | 2);
            if (((ArgumentCommandNode)param1).getCustomSuggestions() != null) {
                var0 = (byte)(var0 | 16);
            }
        } else {
            if (!(param1 instanceof LiteralCommandNode)) {
                throw new UnsupportedOperationException("Unknown node type " + param1);
            }

            var0 = (byte)(var0 | 1);
        }

        param0.writeByte(var0);
        param0.writeVarInt(param1.getChildren().size());

        for(CommandNode<SharedSuggestionProvider> var1 : param1.getChildren()) {
            param0.writeVarInt(param2.get(var1));
        }

        if (param1.getRedirect() != null) {
            param0.writeVarInt(param2.get(param1.getRedirect()));
        }

        if (param1 instanceof ArgumentCommandNode) {
            ArgumentCommandNode<SharedSuggestionProvider, ?> var2 = (ArgumentCommandNode)param1;
            param0.writeUtf(var2.getName());
            ArgumentTypes.serialize(param0, var2.getType());
            if (var2.getCustomSuggestions() != null) {
                param0.writeResourceLocation(SuggestionProviders.getName(var2.getCustomSuggestions()));
            }
        } else if (param1 instanceof LiteralCommandNode) {
            param0.writeUtf(((LiteralCommandNode)param1).getLiteral());
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleCommands(this);
    }

    public RootCommandNode<SharedSuggestionProvider> getRoot() {
        return this.root;
    }

    static class Entry {
        @Nullable
        private final ArgumentBuilder<SharedSuggestionProvider, ?> builder;
        private final byte flags;
        private final int redirect;
        private final int[] children;
        @Nullable
        private CommandNode<SharedSuggestionProvider> node;

        private Entry(@Nullable ArgumentBuilder<SharedSuggestionProvider, ?> param0, byte param1, int param2, int[] param3) {
            this.builder = param0;
            this.flags = param1;
            this.redirect = param2;
            this.children = param3;
        }

        public boolean build(List<ClientboundCommandsPacket.Entry> param0) {
            if (this.node == null) {
                if (this.builder == null) {
                    this.node = new RootCommandNode<>();
                } else {
                    if ((this.flags & 8) != 0) {
                        if (param0.get(this.redirect).node == null) {
                            return false;
                        }

                        this.builder.redirect(param0.get(this.redirect).node);
                    }

                    if ((this.flags & 4) != 0) {
                        this.builder.executes(param0x -> 0);
                    }

                    this.node = this.builder.build();
                }
            }

            for(int var0 : this.children) {
                if (param0.get(var0).node == null) {
                    return false;
                }
            }

            for(int var1 : this.children) {
                CommandNode<SharedSuggestionProvider> var2 = param0.get(var1).node;
                if (!(var2 instanceof RootCommandNode)) {
                    this.node.addChild(var2);
                }
            }

            return true;
        }
    }
}
