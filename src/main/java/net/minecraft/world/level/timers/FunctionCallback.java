package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;

public class FunctionCallback implements TimerCallback<MinecraftServer> {
    final ResourceLocation functionId;

    public FunctionCallback(ResourceLocation param0) {
        this.functionId = param0;
    }

    public void handle(MinecraftServer param0, TimerQueue<MinecraftServer> param1, long param2) {
        ServerFunctionManager var0 = param0.getFunctions();
        var0.get(this.functionId).ifPresent(param1x -> var0.execute(param1x, var0.getGameLoopSender()));
    }

    public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionCallback> {
        public Serializer() {
            super(new ResourceLocation("function"), FunctionCallback.class);
        }

        public void serialize(CompoundTag param0, FunctionCallback param1) {
            param0.putString("Name", param1.functionId.toString());
        }

        public FunctionCallback deserialize(CompoundTag param0) {
            ResourceLocation var0 = new ResourceLocation(param0.getString("Name"));
            return new FunctionCallback(var0);
        }
    }
}
