package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import org.slf4j.Logger;

public class Bootstrap {
    public static final PrintStream STDOUT = System.out;
    private static volatile boolean isBootstrapped;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final AtomicLong bootstrapDuration = new AtomicLong(-1L);

    public static void bootStrap() {
        if (!isBootstrapped) {
            isBootstrapped = true;
            Instant var0 = Instant.now();
            if (BuiltInRegistries.REGISTRY.keySet().isEmpty()) {
                throw new IllegalStateException("Unable to load registries");
            } else {
                FireBlock.bootStrap();
                ComposterBlock.bootStrap();
                if (EntityType.getKey(EntityType.PLAYER) == null) {
                    throw new IllegalStateException("Failed loading EntityTypes");
                } else {
                    PotionBrewing.bootStrap();
                    EntitySelectorOptions.bootStrap();
                    DispenseItemBehavior.bootStrap();
                    CauldronInteraction.bootStrap();
                    BuiltInRegistries.bootStrap();
                    CreativeModeTabs.validate();
                    wrapStreams();
                    bootstrapDuration.set(Duration.between(var0, Instant.now()).toMillis());
                }
            }
        }
    }

    private static <T> void checkTranslations(Iterable<T> param0, Function<T, String> param1, Set<String> param2) {
        Language var0 = Language.getInstance();
        param0.forEach(param3 -> {
            String var0x = param1.apply(param3);
            if (!var0.has(var0x)) {
                param2.add(var0x);
            }

        });
    }

    private static void checkGameruleTranslations(final Set<String> param0) {
        final Language var0 = Language.getInstance();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0x, GameRules.Type<T> param1) {
                if (!var0.has(param0.getDescriptionId())) {
                    param0.add(param0.getId());
                }

            }
        });
    }

    public static Set<String> getMissingTranslations() {
        Set<String> var0 = new TreeSet<>();
        checkTranslations(BuiltInRegistries.ATTRIBUTE, Attribute::getDescriptionId, var0);
        checkTranslations(BuiltInRegistries.ENTITY_TYPE, EntityType::getDescriptionId, var0);
        checkTranslations(BuiltInRegistries.MOB_EFFECT, MobEffect::getDescriptionId, var0);
        checkTranslations(BuiltInRegistries.ITEM, Item::getDescriptionId, var0);
        checkTranslations(BuiltInRegistries.ENCHANTMENT, Enchantment::getDescriptionId, var0);
        checkTranslations(BuiltInRegistries.BLOCK, Block::getDescriptionId, var0);
        checkTranslations(BuiltInRegistries.CUSTOM_STAT, param0 -> "stat." + param0.toString().replace(':', '.'), var0);
        checkGameruleTranslations(var0);
        return var0;
    }

    public static void checkBootstrapCalled(Supplier<String> param0) {
        if (!isBootstrapped) {
            throw createBootstrapException(param0);
        }
    }

    private static RuntimeException createBootstrapException(Supplier<String> param0) {
        try {
            String var0 = param0.get();
            return new IllegalArgumentException("Not bootstrapped (called from " + var0 + ")");
        } catch (Exception var3) {
            RuntimeException var2 = new IllegalArgumentException("Not bootstrapped (failed to resolve location)");
            var2.addSuppressed(var3);
            return var2;
        }
    }

    public static void validate() {
        checkBootstrapCalled(() -> "validate");
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            getMissingTranslations().forEach(param0 -> LOGGER.error("Missing translations: {}", param0));
            Commands.validate();
        }

        DefaultAttributes.validate();
    }

    private static void wrapStreams() {
        if (LOGGER.isDebugEnabled()) {
            System.setErr(new DebugLoggedPrintStream("STDERR", System.err));
            System.setOut(new DebugLoggedPrintStream("STDOUT", STDOUT));
        } else {
            System.setErr(new LoggedPrintStream("STDERR", System.err));
            System.setOut(new LoggedPrintStream("STDOUT", STDOUT));
        }

    }

    public static void realStdoutPrintln(String param0) {
        STDOUT.println(param0);
    }
}
