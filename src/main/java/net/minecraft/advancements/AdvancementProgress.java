package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
    private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(OBTAINED_TIME_FORMAT)
        .xmap(Instant::from, param0 -> param0.atZone(ZoneId.systemDefault()));
    private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, OBTAINED_TIME_CODEC)
        .xmap(
            param0 -> param0.entrySet().stream().collect(Collectors.toMap(Entry::getKey, param0x -> new CriterionProgress((Instant)param0x.getValue()))),
            param0 -> param0.entrySet()
                    .stream()
                    .filter(param0x -> ((CriterionProgress)param0x.getValue()).isDone())
                    .collect(Collectors.toMap(Entry::getKey, param0x -> Objects.requireNonNull(((CriterionProgress)param0x.getValue()).getObtained())))
        );
    public static final Codec<AdvancementProgress> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(CRITERIA_CODEC, "criteria", Map.of()).forGetter(param0x -> param0x.criteria),
                    Codec.BOOL.fieldOf("done").orElse(true).forGetter(AdvancementProgress::isDone)
                )
                .apply(param0, (param0x, param1) -> new AdvancementProgress(new HashMap<>(param0x)))
    );
    private final Map<String, CriterionProgress> criteria;
    private AdvancementRequirements requirements = AdvancementRequirements.EMPTY;

    private AdvancementProgress(Map<String, CriterionProgress> param0) {
        this.criteria = param0;
    }

    public AdvancementProgress() {
        this.criteria = Maps.newHashMap();
    }

    public void update(AdvancementRequirements param0) {
        Set<String> var0 = param0.names();
        this.criteria.entrySet().removeIf(param1 -> !var0.contains(param1.getKey()));

        for(String var1 : var0) {
            this.criteria.putIfAbsent(var1, new CriterionProgress());
        }

        this.requirements = param0;
    }

    public boolean isDone() {
        return this.requirements.test(this::isCriterionDone);
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
        return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + this.requirements + "}";
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

    private boolean isCriterionDone(String param0) {
        CriterionProgress var0 = this.getCriterion(param0);
        return var0 != null && var0.isDone();
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0F;
        } else {
            float var0 = (float)this.requirements.size();
            float var1 = (float)this.countCompletedRequirements();
            return var1 / var0;
        }
    }

    @Nullable
    public Component getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        } else {
            int var0 = this.requirements.size();
            if (var0 <= 1) {
                return null;
            } else {
                int var1 = this.countCompletedRequirements();
                return Component.translatable("advancements.progress", var1, var0);
            }
        }
    }

    private int countCompletedRequirements() {
        return this.requirements.count(this::isCriterionDone);
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
    public Instant getFirstProgressDate() {
        return this.criteria.values().stream().map(CriterionProgress::getObtained).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }

    public int compareTo(AdvancementProgress param0) {
        Instant var0 = this.getFirstProgressDate();
        Instant var1 = param0.getFirstProgressDate();
        if (var0 == null && var1 != null) {
            return 1;
        } else if (var0 != null && var1 == null) {
            return -1;
        } else {
            return var0 == null && var1 == null ? 0 : var0.compareTo(var1);
        }
    }
}
