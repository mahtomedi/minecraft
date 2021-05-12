package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockElementFace {
    public static final int NO_TINT = -1;
    public final Direction cullForDirection;
    public final int tintIndex;
    public final String texture;
    public final BlockFaceUV uv;

    public BlockElementFace(@Nullable Direction param0, int param1, String param2, BlockFaceUV param3) {
        this.cullForDirection = param0;
        this.tintIndex = param1;
        this.texture = param2;
        this.uv = param3;
    }

    @OnlyIn(Dist.CLIENT)
    protected static class Deserializer implements JsonDeserializer<BlockElementFace> {
        private static final int DEFAULT_TINT_INDEX = -1;

        public BlockElementFace deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            Direction var1 = this.getCullFacing(var0);
            int var2 = this.getTintIndex(var0);
            String var3 = this.getTexture(var0);
            BlockFaceUV var4 = param2.deserialize(var0, BlockFaceUV.class);
            return new BlockElementFace(var1, var2, var3, var4);
        }

        protected int getTintIndex(JsonObject param0) {
            return GsonHelper.getAsInt(param0, "tintindex", -1);
        }

        private String getTexture(JsonObject param0) {
            return GsonHelper.getAsString(param0, "texture");
        }

        @Nullable
        private Direction getCullFacing(JsonObject param0) {
            String var0 = GsonHelper.getAsString(param0, "cullface", "");
            return Direction.byName(var0);
        }
    }
}
