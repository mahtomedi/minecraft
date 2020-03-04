package net.minecraft.data.models.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class DelegatedModel implements Supplier<JsonElement> {
    private final ResourceLocation parent;

    public DelegatedModel(ResourceLocation param0) {
        this.parent = param0;
    }

    public JsonElement get() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("parent", this.parent.toString());
        return var0;
    }
}
