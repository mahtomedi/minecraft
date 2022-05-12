package net.minecraft.world.level.timers;

import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionTagCallback implements TimerCallback<MinecraftServer> {
    final ResourceLocation tagId;

    public FunctionTagCallback(ResourceLocation param0) {
        this.tagId = param0;
    }

    public void handle(MinecraftServer param0, TimerQueue<MinecraftServer> param1, long param2) {
        ServerFunctionManager var0 = param0.getFunctions();

        for(CommandFunction var2 : var0.getTag(this.tagId)) {
            var0.execute(var2, var0.getGameLoopSender());
        }

    }

    public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionTagCallback> {
        public Serializer() {
            super(new ResourceLocation("function_tag"), FunctionTagCallback.class);
        }

        public void serialize(CompoundTag param0, FunctionTagCallback param1) {
            param0.putString("Name", param1.tagId.toString());
        }

        public FunctionTagCallback deserialize(CompoundTag param0) {
            ResourceLocation var0 = new ResourceLocation(param0.getString("Name"));
            return new FunctionTagCallback(var0);
        }
    }
}
