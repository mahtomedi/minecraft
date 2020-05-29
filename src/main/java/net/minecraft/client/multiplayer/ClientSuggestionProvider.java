package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSuggestionProvider implements SharedSuggestionProvider {
    private final ClientPacketListener connection;
    private final Minecraft minecraft;
    private int pendingSuggestionsId = -1;
    private CompletableFuture<Suggestions> pendingSuggestionsFuture;

    public ClientSuggestionProvider(ClientPacketListener param0, Minecraft param1) {
        this.connection = param0;
        this.minecraft = param1;
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        List<String> var0 = Lists.newArrayList();

        for(PlayerInfo var1 : this.connection.getOnlinePlayers()) {
            var0.add(var1.getProfile().getName());
        }

        return var0;
    }

    @Override
    public Collection<String> getSelectedEntities() {
        return (Collection<String>)(this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.ENTITY
            ? Collections.singleton(((EntityHitResult)this.minecraft.hitResult).getEntity().getStringUUID())
            : Collections.emptyList());
    }

    @Override
    public Collection<String> getAllTeams() {
        return this.connection.getLevel().getScoreboard().getTeamNames();
    }

    @Override
    public Collection<ResourceLocation> getAvailableSoundEvents() {
        return this.minecraft.getSoundManager().getAvailableSounds();
    }

    @Override
    public Stream<ResourceLocation> getRecipeNames() {
        return this.connection.getRecipeManager().getRecipeIds();
    }

    @Override
    public boolean hasPermission(int param0) {
        LocalPlayer var0 = this.minecraft.player;
        return var0 != null ? var0.hasPermissions(param0) : param0 == 0;
    }

    @Override
    public CompletableFuture<Suggestions> customSuggestion(CommandContext<SharedSuggestionProvider> param0, SuggestionsBuilder param1) {
        if (this.pendingSuggestionsFuture != null) {
            this.pendingSuggestionsFuture.cancel(false);
        }

        this.pendingSuggestionsFuture = new CompletableFuture<>();
        int var0 = ++this.pendingSuggestionsId;
        this.connection.send(new ServerboundCommandSuggestionPacket(var0, param0.getInput()));
        return this.pendingSuggestionsFuture;
    }

    private static String prettyPrint(double param0) {
        return String.format(Locale.ROOT, "%.2f", param0);
    }

    private static String prettyPrint(int param0) {
        return Integer.toString(param0);
    }

    @Override
    public Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
        HitResult var0 = this.minecraft.hitResult;
        if (var0 != null && var0.getType() == HitResult.Type.BLOCK) {
            BlockPos var1 = ((BlockHitResult)var0).getBlockPos();
            return Collections.singleton(
                new SharedSuggestionProvider.TextCoordinates(prettyPrint(var1.getX()), prettyPrint(var1.getY()), prettyPrint(var1.getZ()))
            );
        } else {
            return SharedSuggestionProvider.super.getRelevantCoordinates();
        }
    }

    @Override
    public Collection<SharedSuggestionProvider.TextCoordinates> getAbsoluteCoordinates() {
        HitResult var0 = this.minecraft.hitResult;
        if (var0 != null && var0.getType() == HitResult.Type.BLOCK) {
            Vec3 var1 = var0.getLocation();
            return Collections.singleton(new SharedSuggestionProvider.TextCoordinates(prettyPrint(var1.x), prettyPrint(var1.y), prettyPrint(var1.z)));
        } else {
            return SharedSuggestionProvider.super.getAbsoluteCoordinates();
        }
    }

    @Override
    public Set<ResourceKey<Level>> levels() {
        return this.connection.levels();
    }

    public void completeCustomSuggestions(int param0, Suggestions param1) {
        if (param0 == this.pendingSuggestionsId) {
            this.pendingSuggestionsFuture.complete(param1);
            this.pendingSuggestionsFuture = null;
            this.pendingSuggestionsId = -1;
        }

    }
}
