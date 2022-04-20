package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ScoreContents implements ComponentContents {
    private static final String SCORER_PLACEHOLDER = "*";
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;

    @Nullable
    private static EntitySelector parseSelector(String param0) {
        try {
            return new EntitySelectorParser(new StringReader(param0)).parse();
        } catch (CommandSyntaxException var2) {
            return null;
        }
    }

    public ScoreContents(String param0, String param1) {
        this.name = param0;
        this.selector = parseSelector(param0);
        this.objective = param1;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public String getObjective() {
        return this.objective;
    }

    private String findTargetName(CommandSourceStack param0) throws CommandSyntaxException {
        if (this.selector != null) {
            List<? extends Entity> var0 = this.selector.findEntities(param0);
            if (!var0.isEmpty()) {
                if (var0.size() != 1) {
                    throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
                }

                return var0.get(0).getScoreboardName();
            }
        }

        return this.name;
    }

    private String getScore(String param0, CommandSourceStack param1) {
        MinecraftServer var0 = param1.getServer();
        if (var0 != null) {
            Scoreboard var1 = var0.getScoreboard();
            Objective var2 = var1.getObjective(this.objective);
            if (var1.hasPlayerScore(param0, var2)) {
                Score var3 = var1.getOrCreatePlayerScore(param0, var2);
                return Integer.toString(var3.getScore());
            }
        }

        return "";
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack param0, @Nullable Entity param1, int param2) throws CommandSyntaxException {
        if (param0 == null) {
            return Component.empty();
        } else {
            String var0 = this.findTargetName(param0);
            String var1 = param1 != null && var0.equals("*") ? param1.getScoreboardName() : var0;
            return Component.literal(this.getScore(var1, param0));
        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof ScoreContents var0 && this.name.equals(var0.name) && this.objective.equals(var0.objective)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.name.hashCode();
        return 31 * var0 + this.objective.hashCode();
    }

    @Override
    public String toString() {
        return "score{name='" + this.name + "', objective='" + this.objective + "'}";
    }
}
