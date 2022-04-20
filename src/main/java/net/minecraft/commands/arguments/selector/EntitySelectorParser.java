package net.minecraft.commands.arguments.selector;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelectorParser {
    public static final char SYNTAX_SELECTOR_START = '@';
    private static final char SYNTAX_OPTIONS_START = '[';
    private static final char SYNTAX_OPTIONS_END = ']';
    public static final char SYNTAX_OPTIONS_KEY_VALUE_SEPARATOR = '=';
    private static final char SYNTAX_OPTIONS_SEPARATOR = ',';
    public static final char SYNTAX_NOT = '!';
    public static final char SYNTAX_TAG = '#';
    private static final char SELECTOR_NEAREST_PLAYER = 'p';
    private static final char SELECTOR_ALL_PLAYERS = 'a';
    private static final char SELECTOR_RANDOM_PLAYERS = 'r';
    private static final char SELECTOR_CURRENT_ENTITY = 's';
    private static final char SELECTOR_ALL_ENTITIES = 'e';
    public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(
        Component.translatable("argument.entity.invalid")
    );
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.entity.selector.unknown", param0)
    );
    public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(
        Component.translatable("argument.entity.selector.not_allowed")
    );
    public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = new SimpleCommandExceptionType(
        Component.translatable("argument.entity.selector.missing")
    );
    public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = new SimpleCommandExceptionType(
        Component.translatable("argument.entity.options.unterminated")
    );
    public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = new DynamicCommandExceptionType(
        param0 -> Component.translatable("argument.entity.options.valueless", param0)
    );
    public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_ARBITRARY = (param0, param1) -> {
    };
    public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_NEAREST = (param0, param1) -> param1.sort(
            (param1x, param2) -> Doubles.compare(param1x.distanceToSqr(param0), param2.distanceToSqr(param0))
        );
    public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_FURTHEST = (param0, param1) -> param1.sort(
            (param1x, param2) -> Doubles.compare(param2.distanceToSqr(param0), param1x.distanceToSqr(param0))
        );
    public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_RANDOM = (param0, param1) -> Collections.shuffle(param1);
    public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (param0, param1) -> param0.buildFuture(
            
        );
    private final StringReader reader;
    private final boolean allowSelectors;
    private int maxResults;
    private boolean includesEntities;
    private boolean worldLimited;
    private MinMaxBounds.Doubles distance = MinMaxBounds.Doubles.ANY;
    private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
    @Nullable
    private Double x;
    @Nullable
    private Double y;
    @Nullable
    private Double z;
    @Nullable
    private Double deltaX;
    @Nullable
    private Double deltaY;
    @Nullable
    private Double deltaZ;
    private WrappedMinMaxBounds rotX = WrappedMinMaxBounds.ANY;
    private WrappedMinMaxBounds rotY = WrappedMinMaxBounds.ANY;
    private Predicate<Entity> predicate = param0x -> true;
    private BiConsumer<Vec3, List<? extends Entity>> order = ORDER_ARBITRARY;
    private boolean currentEntity;
    @Nullable
    private String playerName;
    private int startPosition;
    @Nullable
    private UUID entityUUID;
    private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;
    private boolean hasNameEquals;
    private boolean hasNameNotEquals;
    private boolean isLimited;
    private boolean isSorted;
    private boolean hasGamemodeEquals;
    private boolean hasGamemodeNotEquals;
    private boolean hasTeamEquals;
    private boolean hasTeamNotEquals;
    @Nullable
    private EntityType<?> type;
    private boolean typeInverse;
    private boolean hasScores;
    private boolean hasAdvancements;
    private boolean usesSelectors;

    public EntitySelectorParser(StringReader param0) {
        this(param0, true);
    }

    public EntitySelectorParser(StringReader param0, boolean param1) {
        this.reader = param0;
        this.allowSelectors = param1;
    }

    public EntitySelector getSelector() {
        AABB var2;
        if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
            if (this.distance.getMax() != null) {
                double var1 = this.distance.getMax();
                var2 = new AABB(-var1, -var1, -var1, var1 + 1.0, var1 + 1.0, var1 + 1.0);
            } else {
                var2 = null;
            }
        } else {
            var2 = this.createAabb(this.deltaX == null ? 0.0 : this.deltaX, this.deltaY == null ? 0.0 : this.deltaY, this.deltaZ == null ? 0.0 : this.deltaZ);
        }

        Function<Vec3, Vec3> var4;
        if (this.x == null && this.y == null && this.z == null) {
            var4 = param0 -> param0;
        } else {
            var4 = param0 -> new Vec3(this.x == null ? param0.x : this.x, this.y == null ? param0.y : this.y, this.z == null ? param0.z : this.z);
        }

        return new EntitySelector(
            this.maxResults,
            this.includesEntities,
            this.worldLimited,
            this.predicate,
            this.distance,
            var4,
            var2,
            this.order,
            this.currentEntity,
            this.playerName,
            this.entityUUID,
            this.type,
            this.usesSelectors
        );
    }

    private AABB createAabb(double param0, double param1, double param2) {
        boolean var0 = param0 < 0.0;
        boolean var1 = param1 < 0.0;
        boolean var2 = param2 < 0.0;
        double var3 = var0 ? param0 : 0.0;
        double var4 = var1 ? param1 : 0.0;
        double var5 = var2 ? param2 : 0.0;
        double var6 = (var0 ? 0.0 : param0) + 1.0;
        double var7 = (var1 ? 0.0 : param1) + 1.0;
        double var8 = (var2 ? 0.0 : param2) + 1.0;
        return new AABB(var3, var4, var5, var6, var7, var8);
    }

    private void finalizePredicates() {
        if (this.rotX != WrappedMinMaxBounds.ANY) {
            this.predicate = this.predicate.and(this.createRotationPredicate(this.rotX, Entity::getXRot));
        }

        if (this.rotY != WrappedMinMaxBounds.ANY) {
            this.predicate = this.predicate.and(this.createRotationPredicate(this.rotY, Entity::getYRot));
        }

        if (!this.level.isAny()) {
            this.predicate = this.predicate
                .and(param0 -> !(param0 instanceof ServerPlayer) ? false : this.level.matches(((ServerPlayer)param0).experienceLevel));
        }

    }

    private Predicate<Entity> createRotationPredicate(WrappedMinMaxBounds param0, ToDoubleFunction<Entity> param1) {
        double var0 = (double)Mth.wrapDegrees(param0.getMin() == null ? 0.0F : param0.getMin());
        double var1 = (double)Mth.wrapDegrees(param0.getMax() == null ? 359.0F : param0.getMax());
        return param3 -> {
            double var0x = Mth.wrapDegrees(param1.applyAsDouble(param3));
            if (var0 > var1) {
                return var0x >= var0 || var0x <= var1;
            } else {
                return var0x >= var0 && var0x <= var1;
            }
        };
    }

    protected void parseSelector() throws CommandSyntaxException {
        this.usesSelectors = true;
        this.suggestions = this::suggestSelector;
        if (!this.reader.canRead()) {
            throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
        } else {
            int var0 = this.reader.getCursor();
            char var1 = this.reader.read();
            if (var1 == 'p') {
                this.maxResults = 1;
                this.includesEntities = false;
                this.order = ORDER_NEAREST;
                this.limitToType(EntityType.PLAYER);
            } else if (var1 == 'a') {
                this.maxResults = Integer.MAX_VALUE;
                this.includesEntities = false;
                this.order = ORDER_ARBITRARY;
                this.limitToType(EntityType.PLAYER);
            } else if (var1 == 'r') {
                this.maxResults = 1;
                this.includesEntities = false;
                this.order = ORDER_RANDOM;
                this.limitToType(EntityType.PLAYER);
            } else if (var1 == 's') {
                this.maxResults = 1;
                this.includesEntities = true;
                this.currentEntity = true;
            } else {
                if (var1 != 'e') {
                    this.reader.setCursor(var0);
                    throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, "@" + var1);
                }

                this.maxResults = Integer.MAX_VALUE;
                this.includesEntities = true;
                this.order = ORDER_ARBITRARY;
                this.predicate = Entity::isAlive;
            }

            this.suggestions = this::suggestOpenOptions;
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.reader.skip();
                this.suggestions = this::suggestOptionsKeyOrClose;
                this.parseOptions();
            }

        }
    }

    protected void parseNameOrUUID() throws CommandSyntaxException {
        if (this.reader.canRead()) {
            this.suggestions = this::suggestName;
        }

        int var0 = this.reader.getCursor();
        String var1 = this.reader.readString();

        try {
            this.entityUUID = UUID.fromString(var1);
            this.includesEntities = true;
        } catch (IllegalArgumentException var4) {
            if (var1.isEmpty() || var1.length() > 16) {
                this.reader.setCursor(var0);
                throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
            }

            this.includesEntities = false;
            this.playerName = var1;
        }

        this.maxResults = 1;
    }

    protected void parseOptions() throws CommandSyntaxException {
        this.suggestions = this::suggestOptionsKey;
        this.reader.skipWhitespace();

        while(this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int var0 = this.reader.getCursor();
            String var1 = this.reader.readString();
            EntitySelectorOptions.Modifier var2 = EntitySelectorOptions.get(this, var1, var0);
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                this.reader.setCursor(var0);
                throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, var1);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = SUGGEST_NOTHING;
            var2.handle(this);
            this.reader.skipWhitespace();
            this.suggestions = this::suggestOptionsNextOrClose;
            if (this.reader.canRead()) {
                if (this.reader.peek() != ',') {
                    if (this.reader.peek() != ']') {
                        throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
                    }
                    break;
                }

                this.reader.skip();
                this.suggestions = this::suggestOptionsKey;
            }
        }

        if (this.reader.canRead()) {
            this.reader.skip();
            this.suggestions = SUGGEST_NOTHING;
        } else {
            throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
        }
    }

    public boolean shouldInvertValue() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == '!') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        } else {
            return false;
        }
    }

    public boolean isTag() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        } else {
            return false;
        }
    }

    public StringReader getReader() {
        return this.reader;
    }

    public void addPredicate(Predicate<Entity> param0) {
        this.predicate = this.predicate.and(param0);
    }

    public void setWorldLimited() {
        this.worldLimited = true;
    }

    public MinMaxBounds.Doubles getDistance() {
        return this.distance;
    }

    public void setDistance(MinMaxBounds.Doubles param0) {
        this.distance = param0;
    }

    public MinMaxBounds.Ints getLevel() {
        return this.level;
    }

    public void setLevel(MinMaxBounds.Ints param0) {
        this.level = param0;
    }

    public WrappedMinMaxBounds getRotX() {
        return this.rotX;
    }

    public void setRotX(WrappedMinMaxBounds param0) {
        this.rotX = param0;
    }

    public WrappedMinMaxBounds getRotY() {
        return this.rotY;
    }

    public void setRotY(WrappedMinMaxBounds param0) {
        this.rotY = param0;
    }

    @Nullable
    public Double getX() {
        return this.x;
    }

    @Nullable
    public Double getY() {
        return this.y;
    }

    @Nullable
    public Double getZ() {
        return this.z;
    }

    public void setX(double param0) {
        this.x = param0;
    }

    public void setY(double param0) {
        this.y = param0;
    }

    public void setZ(double param0) {
        this.z = param0;
    }

    public void setDeltaX(double param0) {
        this.deltaX = param0;
    }

    public void setDeltaY(double param0) {
        this.deltaY = param0;
    }

    public void setDeltaZ(double param0) {
        this.deltaZ = param0;
    }

    @Nullable
    public Double getDeltaX() {
        return this.deltaX;
    }

    @Nullable
    public Double getDeltaY() {
        return this.deltaY;
    }

    @Nullable
    public Double getDeltaZ() {
        return this.deltaZ;
    }

    public void setMaxResults(int param0) {
        this.maxResults = param0;
    }

    public void setIncludesEntities(boolean param0) {
        this.includesEntities = param0;
    }

    public BiConsumer<Vec3, List<? extends Entity>> getOrder() {
        return this.order;
    }

    public void setOrder(BiConsumer<Vec3, List<? extends Entity>> param0) {
        this.order = param0;
    }

    public EntitySelector parse() throws CommandSyntaxException {
        this.startPosition = this.reader.getCursor();
        this.suggestions = this::suggestNameOrSelector;
        if (this.reader.canRead() && this.reader.peek() == '@') {
            if (!this.allowSelectors) {
                throw ERROR_SELECTORS_NOT_ALLOWED.createWithContext(this.reader);
            }

            this.reader.skip();
            this.parseSelector();
        } else {
            this.parseNameOrUUID();
        }

        this.finalizePredicates();
        return this.getSelector();
    }

    private static void fillSelectorSuggestions(SuggestionsBuilder param0) {
        param0.suggest("@p", Component.translatable("argument.entity.selector.nearestPlayer"));
        param0.suggest("@a", Component.translatable("argument.entity.selector.allPlayers"));
        param0.suggest("@r", Component.translatable("argument.entity.selector.randomPlayer"));
        param0.suggest("@s", Component.translatable("argument.entity.selector.self"));
        param0.suggest("@e", Component.translatable("argument.entity.selector.allEntities"));
    }

    private CompletableFuture<Suggestions> suggestNameOrSelector(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        param1.accept(param0);
        if (this.allowSelectors) {
            fillSelectorSuggestions(param0);
        }

        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestName(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        SuggestionsBuilder var0x = param0.createOffset(this.startPosition);
        param1.accept(var0x);
        return param0.add(var0x).buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        SuggestionsBuilder var0x = param0.createOffset(param0.getStart() - 1);
        fillSelectorSuggestions(var0x);
        param0.add(var0x);
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        param0.suggest(String.valueOf('['));
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        param0.suggest(String.valueOf(']'));
        EntitySelectorOptions.suggestNames(this, param0);
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        EntitySelectorOptions.suggestNames(this, param0);
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOptionsNextOrClose(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        param0.suggest(String.valueOf(','));
        param0.suggest(String.valueOf(']'));
        return param0.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestEquals(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        param0.suggest(String.valueOf('='));
        return param0.buildFuture();
    }

    public boolean isCurrentEntity() {
        return this.currentEntity;
    }

    public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> param0) {
        this.suggestions = param0;
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder param0, Consumer<SuggestionsBuilder> param1) {
        return this.suggestions.apply(param0.createOffset(this.reader.getCursor()), param1);
    }

    public boolean hasNameEquals() {
        return this.hasNameEquals;
    }

    public void setHasNameEquals(boolean param0) {
        this.hasNameEquals = param0;
    }

    public boolean hasNameNotEquals() {
        return this.hasNameNotEquals;
    }

    public void setHasNameNotEquals(boolean param0) {
        this.hasNameNotEquals = param0;
    }

    public boolean isLimited() {
        return this.isLimited;
    }

    public void setLimited(boolean param0) {
        this.isLimited = param0;
    }

    public boolean isSorted() {
        return this.isSorted;
    }

    public void setSorted(boolean param0) {
        this.isSorted = param0;
    }

    public boolean hasGamemodeEquals() {
        return this.hasGamemodeEquals;
    }

    public void setHasGamemodeEquals(boolean param0) {
        this.hasGamemodeEquals = param0;
    }

    public boolean hasGamemodeNotEquals() {
        return this.hasGamemodeNotEquals;
    }

    public void setHasGamemodeNotEquals(boolean param0) {
        this.hasGamemodeNotEquals = param0;
    }

    public boolean hasTeamEquals() {
        return this.hasTeamEquals;
    }

    public void setHasTeamEquals(boolean param0) {
        this.hasTeamEquals = param0;
    }

    public boolean hasTeamNotEquals() {
        return this.hasTeamNotEquals;
    }

    public void setHasTeamNotEquals(boolean param0) {
        this.hasTeamNotEquals = param0;
    }

    public void limitToType(EntityType<?> param0) {
        this.type = param0;
    }

    public void setTypeLimitedInversely() {
        this.typeInverse = true;
    }

    public boolean isTypeLimited() {
        return this.type != null;
    }

    public boolean isTypeLimitedInversely() {
        return this.typeInverse;
    }

    public boolean hasScores() {
        return this.hasScores;
    }

    public void setHasScores(boolean param0) {
        this.hasScores = param0;
    }

    public boolean hasAdvancements() {
        return this.hasAdvancements;
    }

    public void setHasAdvancements(boolean param0) {
        this.hasAdvancements = param0;
    }
}
