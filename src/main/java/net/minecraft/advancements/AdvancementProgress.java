package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
    private final Map<String, CriterionProgress> criteria;
    private String[][] requirements = new String[0][];

    private AdvancementProgress(Map<String, CriterionProgress> param0) {
        this.criteria = param0;
    }

    public AdvancementProgress() {
        this.criteria = Maps.newHashMap();
    }

    public void update(Map<String, Criterion> param0, String[][] param1) {
        Set<String> var0 = param0.keySet();
        this.criteria.entrySet().removeIf(param1x -> !var0.contains(param1x.getKey()));

        for(String var1 : var0) {
            if (!this.criteria.containsKey(var1)) {
                this.criteria.put(var1, new CriterionProgress());
            }
        }

        this.requirements = param1;
    }

    public boolean isDone() {
        if (this.requirements.length == 0) {
            return false;
        } else {
            for(String[] var0 : this.requirements) {
                boolean var1 = false;

                for(String var2 : var0) {
                    CriterionProgress var3 = this.getCriterion(var2);
                    if (var3 != null && var3.isDone()) {
                        var1 = true;
                        break;
                    }
                }

                if (!var1) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean hasProgress() {
        for(CriterionProgress var0 : this.criteria.values()) {
            if (var0.isDone()) {
                return true;
            }
        }

        return false;
    }

    public boolean grantProgress(String param0) {
        CriterionProgress var0 = this.criteria.get(param0);
        if (var0 != null && !var0.isDone()) {
            var0.grant();
            return true;
        } else {
            return false;
        }
    }

    public boolean revokeProgress(String param0) {
        CriterionProgress var0 = this.criteria.get(param0);
        if (var0 != null && var0.isDone()) {
            var0.revoke();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
    }

    public void serializeToNetwork(FriendlyByteBuf param0) {
        param0.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (param0x, param1) -> param1.serializeToNetwork(param0x));
    }

    public static AdvancementProgress fromNetwork(FriendlyByteBuf param0) {
        Map<String, CriterionProgress> var0 = param0.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        return new AdvancementProgress(var0);
    }

    @Nullable
    public CriterionProgress getCriterion(String param0) {
        return this.criteria.get(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0F;
        } else {
            float var0 = (float)this.requirements.length;
            float var1 = (float)this.countCompletedRequirements();
            return var1 / var0;
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public String getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        } else {
            int var0 = this.requirements.length;
            if (var0 <= 1) {
                return null;
            } else {
                int var1 = this.countCompletedRequirements();
                return var1 + "/" + var0;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private int countCompletedRequirements() {
        int var0 = 0;

        for(String[] var1 : this.requirements) {
            boolean var2 = false;

            for(String var3 : var1) {
                CriterionProgress var4 = this.getCriterion(var3);
                if (var4 != null && var4.isDone()) {
                    var2 = true;
                    break;
                }
            }

            if (var2) {
                ++var0;
            }
        }

        return var0;
    }

    public Iterable<String> getRemainingCriteria() {
        List<String> var0 = Lists.newArrayList();

        for(Entry<String, CriterionProgress> var1 : this.criteria.entrySet()) {
            if (!var1.getValue().isDone()) {
                var0.add(var1.getKey());
            }
        }

        return var0;
    }

    public Iterable<String> getCompletedCriteria() {
        List<String> var0 = Lists.newArrayList();

        for(Entry<String, CriterionProgress> var1 : this.criteria.entrySet()) {
            if (var1.getValue().isDone()) {
                var0.add(var1.getKey());
            }
        }

        return var0;
    }

    @Nullable
    public Date getFirstProgressDate() {
        Date var0 = null;

        for(CriterionProgress var1 : this.criteria.values()) {
            if (var1.isDone() && (var0 == null || var1.getObtained().before(var0))) {
                var0 = var1.getObtained();
            }
        }

        return var0;
    }

    public int compareTo(AdvancementProgress param0) {
        Date var0 = this.getFirstProgressDate();
        Date var1 = param0.getFirstProgressDate();
        if (var0 == null && var1 != null) {
            return 1;
        } else if (var0 != null && var1 == null) {
            return -1;
        } else {
            return var0 == null && var1 == null ? 0 : var0.compareTo(var1);
        }
    }

    public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
        public JsonElement serialize(AdvancementProgress param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            JsonObject var1 = new JsonObject();

            for(Entry<String, CriterionProgress> var2 : param0.criteria.entrySet()) {
                CriterionProgress var3 = var2.getValue();
                if (var3.isDone()) {
                    var1.add(var2.getKey(), var3.serializeToJson());
                }
            }

            if (!var1.entrySet().isEmpty()) {
                var0.add("criteria", var1);
            }

            var0.addProperty("done", param0.isDone());
            return var0;
        }

        public AdvancementProgress deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "advancement");
            JsonObject var1 = GsonHelper.getAsJsonObject(var0, "criteria", new JsonObject());
            AdvancementProgress var2 = new AdvancementProgress();

            for(Entry<String, JsonElement> var3 : var1.entrySet()) {
                String var4 = var3.getKey();
                var2.criteria.put(var4, CriterionProgress.fromJson(GsonHelper.convertToString(var3.getValue(), var4)));
            }

            return var2;
        }
    }
}
