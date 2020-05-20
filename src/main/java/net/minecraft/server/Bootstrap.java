package net.minecraft.server;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.locale.Language;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootstrap {
    public static final PrintStream STDOUT = System.out;
    private static boolean isBootstrapped;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void bootStrap() {
        if (!isBootstrapped) {
            isBootstrapped = true;
            if (Registry.REGISTRY.keySet().isEmpty()) {
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
                    ArgumentTypes.bootStrap();
                    wrapStreams();
                }
            }
        }
    }

    private static <T> void checkTranslations(Iterable<T> param0, Function<T, String> param1, Set<String> param2) {
        Language var0 = Language.getInstance();
        param0.forEach(param3 -> {
            String var0x = param1.apply(param3);
            if (!var0.exists(var0x)) {
                param2.add(var0x);
            }

        });
    }

    private static void checkGameruleTranslations(final Set<String> param0) {
        final Language var0 = Language.getInstance();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0x, GameRules.Type<T> param1) {
                if (!var0.exists(param0.getDescriptionId())) {
                    param0.add(param0.getId());
                }

            }
        });
    }

    public static Set<String> getMissingTranslations() {
        Set<String> var0 = new TreeSet<>();
        checkTranslations(Registry.ATTRIBUTES, Attribute::getDescriptionId, var0);
        checkTranslations(Registry.ENTITY_TYPE, EntityType::getDescriptionId, var0);
        checkTranslations(Registry.MOB_EFFECT, MobEffect::getDescriptionId, var0);
        checkTranslations(Registry.ITEM, Item::getDescriptionId, var0);
        checkTranslations(Registry.ENCHANTMENT, Enchantment::getDescriptionId, var0);
        checkTranslations(Registry.BIOME, Biome::getDescriptionId, var0);
        checkTranslations(Registry.BLOCK, Block::getDescriptionId, var0);
        checkTranslations(Registry.CUSTOM_STAT, param0 -> "stat." + param0.toString().replace(':', '.'), var0);
        checkGameruleTranslations(var0);
        return var0;
    }

    public static void validate() {
        if (!isBootstrapped) {
            throw new IllegalArgumentException("Not bootstrapped");
        } else {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                getMissingTranslations().forEach(param0 -> LOGGER.error("Missing translations: " + param0));
            }

            DefaultAttributes.validate();
        }
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
