package net.minecraft.network.protocol.game;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<ClientboundCommandsPacket.Entry> entries;

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> param0) {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> var0 = enumerateNodes(param0);
        this.entries = createEntries(var0);
        this.rootIndex = var0.getInt(param0);
    }

    public ClientboundCommandsPacket(FriendlyByteBuf param0) {
        this.entries = param0.readList(ClientboundCommandsPacket::readNode);
        this.rootIndex = param0.readVarInt();
        validateEntries(this.entries);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.entries, (param0x, param1) -> param1.write(param0x));
        param0.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<ClientboundCommandsPacket.Entry> param0, BiPredicate<ClientboundCommandsPacket.Entry, IntSet> param1) {
        IntSet var0 = new IntOpenHashSet(IntSets.fromTo(0, param0.size()));

        while(!var0.isEmpty()) {
            boolean var1 = var0.removeIf(param3 -> param1.test(param0.get(param3), var0));
            if (!var1) {
                throw new IllegalStateException("Server sent an impossible command tree");
            }
        }

    }

    private static void validateEntries(List<ClientboundCommandsPacket.Entry> param0) {
        validateEntries(param0, ClientboundCommandsPacket.Entry::canBuild);
        validateEntries(param0, ClientboundCommandsPacket.Entry::canResolve);
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

    private static List<ClientboundCommandsPacket.Entry> createEntries(Object2IntMap<CommandNode<SharedSuggestionProvider>> param0) {
        ObjectArrayList<ClientboundCommandsPacket.Entry> var0 = new ObjectArrayList<>(param0.size());
        var0.size(param0.size());

        for(Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> var1 : Object2IntMaps.fastIterable(param0)) {
            var0.set(var1.getIntValue(), createEntry(var1.getKey(), param0));
        }

        return var0;
    }

    private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf param0x) {
        byte var0 = param0x.readByte();
        int[] var1 = param0x.readVarIntArray();
        int var2 = (var0 & 8) != 0 ? param0x.readVarInt() : 0;
        ClientboundCommandsPacket.NodeStub var3 = read(param0x, var0);
        return new ClientboundCommandsPacket.Entry(var3, var0, var2, var1);
    }

    @Nullable
    private static ClientboundCommandsPacket.NodeStub read(FriendlyByteBuf param0, byte param1) {
        int var0 = param1 & 3;
        if (var0 == 2) {
            String var1 = param0.readUtf();
            int var2 = param0.readVarInt();
            ArgumentTypeInfo<?, ?> var3 = Registry.COMMAND_ARGUMENT_TYPE.byId(var2);
            if (var3 == null) {
                return null;
            } else {
                ArgumentTypeInfo.Template<?> var4 = var3.deserializeFromNetwork(param0);
                ResourceLocation var5 = (param1 & 16) != 0 ? param0.readResourceLocation() : null;
                return new ClientboundCommandsPacket.ArgumentNodeStub(var1, var4, var5);
            }
        } else if (var0 == 1) {
            String var6 = param0.readUtf();
            return new ClientboundCommandsPacket.LiteralNodeStub(var6);
        } else {
            return null;
        }
    }

    private static ClientboundCommandsPacket.Entry createEntry(
        CommandNode<SharedSuggestionProvider> param0, Object2IntMap<CommandNode<SharedSuggestionProvider>> param1
    ) {
        int var0 = 0;
        int var1;
        if (param0.getRedirect() != null) {
            var0 |= 8;
            var1 = param1.getInt(param0.getRedirect());
        } else {
            var1 = 0;
        }

        if (param0.getCommand() != null) {
            var0 |= 4;
        }

        ClientboundCommandsPacket.NodeStub var3;
        if (param0 instanceof RootCommandNode) {
            var0 |= 0;
            var3 = null;
        } else if (param0 instanceof ArgumentCommandNode var4) {
            var3 = new ClientboundCommandsPacket.ArgumentNodeStub(var4);
            var0 |= 2;
            if (var4.getCustomSuggestions() != null) {
                var0 |= 16;
            }
        } else {
            if (!(param0 instanceof LiteralCommandNode)) {
                throw new UnsupportedOperationException("Unknown node type " + param0);
            }

            LiteralCommandNode var6 = (LiteralCommandNode)param0;
            var3 = new ClientboundCommandsPacket.LiteralNodeStub(var6.getLiteral());
            var0 |= 1;
        }

        int[] var9 = param0.getChildren().stream().mapToInt(param1::getInt).toArray();
        return new ClientboundCommandsPacket.Entry(var3, var0, var1, var9);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleCommands(this);
    }

    public RootCommandNode<SharedSuggestionProvider> getRoot(CommandBuildContext param0) {
        return (RootCommandNode<SharedSuggestionProvider>)new ClientboundCommandsPacket.NodeResolver(param0, this.entries).resolve(this.rootIndex);
    }

    static class ArgumentNodeStub implements ClientboundCommandsPacket.NodeStub {
        private final String id;
        private final ArgumentTypeInfo.Template<?> argumentType;
        @Nullable
        private final ResourceLocation suggestionId;

        @Nullable
        private static ResourceLocation getSuggestionId(@Nullable SuggestionProvider<SharedSuggestionProvider> param0) {
            return param0 != null ? SuggestionProviders.getName(param0) : null;
        }

        ArgumentNodeStub(String param0, ArgumentTypeInfo.Template<?> param1, @Nullable ResourceLocation param2) {
            this.id = param0;
            this.argumentType = param1;
            this.suggestionId = param2;
        }

        public ArgumentNodeStub(ArgumentCommandNode<SharedSuggestionProvider, ?> param0) {
            this(param0.getName(), ArgumentTypeInfos.unpack(param0.getType()), getSuggestionId(param0.getCustomSuggestions()));
        }

        @Override
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext param0) {
            ArgumentType<?> var0 = this.argumentType.instantiate(param0);
            RequiredArgumentBuilder<SharedSuggestionProvider, ?> var1 = RequiredArgumentBuilder.argument(this.id, var0);
            if (this.suggestionId != null) {
                var1.suggests(SuggestionProviders.getProvider(this.suggestionId));
            }

            return var1;
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeUtf(this.id);
            serializeCap(param0, this.argumentType);
            if (this.suggestionId != null) {
                param0.writeResourceLocation(this.suggestionId);
            }

        }

        private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf param0, ArgumentTypeInfo.Template<A> param1) {
            serializeCap(param0, param1.type(), param1);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(
            FriendlyByteBuf param0, ArgumentTypeInfo<A, T> param1, ArgumentTypeInfo.Template<A> param2
        ) {
            param0.writeVarInt(Registry.COMMAND_ARGUMENT_TYPE.getId(param1));
            param1.serializeToNetwork((T)param2, param0);
        }
    }

    static class Entry {
        @Nullable
        final ClientboundCommandsPacket.NodeStub stub;
        final int flags;
        final int redirect;
        final int[] children;

        Entry(@Nullable ClientboundCommandsPacket.NodeStub param0, int param1, int param2, int[] param3) {
            this.stub = param0;
            this.flags = param1;
            this.redirect = param2;
            this.children = param3;
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeByte(this.flags);
            param0.writeVarIntArray(this.children);
            if ((this.flags & 8) != 0) {
                param0.writeVarInt(this.redirect);
            }

            if (this.stub != null) {
                this.stub.write(param0);
            }

        }

        public boolean canBuild(IntSet param0) {
            if ((this.flags & 8) != 0) {
                return !param0.contains(this.redirect);
            } else {
                return true;
            }
        }

        public boolean canResolve(IntSet param0) {
            for(int var0 : this.children) {
                if (param0.contains(var0)) {
                    return false;
                }
            }

            return true;
        }
    }

    static class LiteralNodeStub implements ClientboundCommandsPacket.NodeStub {
        private final String id;

        LiteralNodeStub(String param0) {
            this.id = param0;
        }

        @Override
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext param0) {
            return LiteralArgumentBuilder.literal(this.id);
        }

        @Override
        public void write(FriendlyByteBuf param0) {
            param0.writeUtf(this.id);
        }
    }

    static class NodeResolver {
        private final CommandBuildContext context;
        private final List<ClientboundCommandsPacket.Entry> entries;
        private final List<CommandNode<SharedSuggestionProvider>> nodes;

        NodeResolver(CommandBuildContext param0, List<ClientboundCommandsPacket.Entry> param1) {
            this.context = param0;
            this.entries = param1;
            ObjectArrayList<CommandNode<SharedSuggestionProvider>> var0 = new ObjectArrayList<>();
            var0.size(param1.size());
            this.nodes = var0;
        }

        public CommandNode<SharedSuggestionProvider> resolve(int param0) {
            CommandNode<SharedSuggestionProvider> var0 = this.nodes.get(param0);
            if (var0 != null) {
                return var0;
            } else {
                ClientboundCommandsPacket.Entry var1 = this.entries.get(param0);
                CommandNode<SharedSuggestionProvider> var2;
                if (var1.stub == null) {
                    var2 = new RootCommandNode<>();
                } else {
                    ArgumentBuilder<SharedSuggestionProvider, ?> var3 = var1.stub.build(this.context);
                    if ((var1.flags & 8) != 0) {
                        var3.redirect(this.resolve(var1.redirect));
                    }

                    if ((var1.flags & 4) != 0) {
                        var3.executes(param0x -> 0);
                    }

                    var2 = var3.build();
                }

                this.nodes.set(param0, var2);

                for(int var5 : var1.children) {
                    CommandNode<SharedSuggestionProvider> var6 = this.resolve(var5);
                    if (!(var6 instanceof RootCommandNode)) {
                        var2.addChild(var6);
                    }
                }

                return var2;
            }
        }
    }

    interface NodeStub {
        ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext var1);

        void write(FriendlyByteBuf var1);
    }
}
