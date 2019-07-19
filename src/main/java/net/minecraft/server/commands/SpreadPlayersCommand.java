package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType(
        (param0, param1, param2, param3) -> new TranslatableComponent("commands.spreadplayers.failed.teams", param0, param1, param2, param3)
    );
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType(
        (param0, param1, param2, param3) -> new TranslatableComponent("commands.spreadplayers.failed.entities", param0, param1, param2, param3)
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("spreadplayers")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("center", Vec2Argument.vec2())
                        .then(
                            Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F))
                                .then(
                                    Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F))
                                        .then(
                                            Commands.argument("respectTeams", BoolArgumentType.bool())
                                                .then(
                                                    Commands.argument("targets", EntityArgument.entities())
                                                        .executes(
                                                            param0x -> spreadPlayers(
                                                                    param0x.getSource(),
                                                                    Vec2Argument.getVec2(param0x, "center"),
                                                                    FloatArgumentType.getFloat(param0x, "spreadDistance"),
                                                                    FloatArgumentType.getFloat(param0x, "maxRange"),
                                                                    BoolArgumentType.getBool(param0x, "respectTeams"),
                                                                    EntityArgument.getEntities(param0x, "targets")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int spreadPlayers(CommandSourceStack param0, Vec2 param1, float param2, float param3, boolean param4, Collection<? extends Entity> param5) throws CommandSyntaxException {
        Random var0 = new Random();
        double var1 = (double)(param1.x - param3);
        double var2 = (double)(param1.y - param3);
        double var3 = (double)(param1.x + param3);
        double var4 = (double)(param1.y + param3);
        SpreadPlayersCommand.Position[] var5 = createInitialPositions(var0, param4 ? getNumberOfTeams(param5) : param5.size(), var1, var2, var3, var4);
        spreadPositions(param1, (double)param2, param0.getLevel(), var0, var1, var2, var3, var4, var5, param4);
        double var6 = setPlayerPositions(param5, param0.getLevel(), var5, param4);
        param0.sendSuccess(
            new TranslatableComponent(
                "commands.spreadplayers.success." + (param4 ? "teams" : "entities"), var5.length, param1.x, param1.y, String.format(Locale.ROOT, "%.2f", var6)
            ),
            true
        );
        return var5.length;
    }

    private static int getNumberOfTeams(Collection<? extends Entity> param0) {
        Set<Team> var0 = Sets.newHashSet();

        for(Entity var1 : param0) {
            if (var1 instanceof Player) {
                var0.add(var1.getTeam());
            } else {
                var0.add(null);
            }
        }

        return var0.size();
    }

    private static void spreadPositions(
        Vec2 param0,
        double param1,
        ServerLevel param2,
        Random param3,
        double param4,
        double param5,
        double param6,
        double param7,
        SpreadPlayersCommand.Position[] param8,
        boolean param9
    ) throws CommandSyntaxException {
        boolean var0 = true;
        double var1 = Float.MAX_VALUE;

        int var2;
        for(var2 = 0; var2 < 10000 && var0; ++var2) {
            var0 = false;
            var1 = Float.MAX_VALUE;

            for(int var3 = 0; var3 < param8.length; ++var3) {
                SpreadPlayersCommand.Position var4 = param8[var3];
                int var5 = 0;
                SpreadPlayersCommand.Position var6 = new SpreadPlayersCommand.Position();

                for(int var7 = 0; var7 < param8.length; ++var7) {
                    if (var3 != var7) {
                        SpreadPlayersCommand.Position var8 = param8[var7];
                        double var9 = var4.dist(var8);
                        var1 = Math.min(var9, var1);
                        if (var9 < param1) {
                            ++var5;
                            var6.x = var6.x + (var8.x - var4.x);
                            var6.z = var6.z + (var8.z - var4.z);
                        }
                    }
                }

                if (var5 > 0) {
                    var6.x = var6.x / (double)var5;
                    var6.z = var6.z / (double)var5;
                    double var10 = (double)var6.getLength();
                    if (var10 > 0.0) {
                        var6.normalize();
                        var4.moveAway(var6);
                    } else {
                        var4.randomize(param3, param4, param5, param6, param7);
                    }

                    var0 = true;
                }

                if (var4.clamp(param4, param5, param6, param7)) {
                    var0 = true;
                }
            }

            if (!var0) {
                for(SpreadPlayersCommand.Position var11 : param8) {
                    if (!var11.isSafe(param2)) {
                        var11.randomize(param3, param4, param5, param6, param7);
                        var0 = true;
                    }
                }
            }
        }

        if (var1 == Float.MAX_VALUE) {
            var1 = 0.0;
        }

        if (var2 >= 10000) {
            if (param9) {
                throw ERROR_FAILED_TO_SPREAD_TEAMS.create(param8.length, param0.x, param0.y, String.format(Locale.ROOT, "%.2f", var1));
            } else {
                throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(param8.length, param0.x, param0.y, String.format(Locale.ROOT, "%.2f", var1));
            }
        }
    }

    private static double setPlayerPositions(Collection<? extends Entity> param0, ServerLevel param1, SpreadPlayersCommand.Position[] param2, boolean param3) {
        double var0 = 0.0;
        int var1 = 0;
        Map<Team, SpreadPlayersCommand.Position> var2 = Maps.newHashMap();

        for(Entity var3 : param0) {
            SpreadPlayersCommand.Position var5;
            if (param3) {
                Team var4 = var3 instanceof Player ? var3.getTeam() : null;
                if (!var2.containsKey(var4)) {
                    var2.put(var4, param2[var1++]);
                }

                var5 = var2.get(var4);
            } else {
                var5 = param2[var1++];
            }

            var3.teleportToWithTicket((double)((float)Mth.floor(var5.x) + 0.5F), (double)var5.getSpawnY(param1), (double)Mth.floor(var5.z) + 0.5);
            double var7 = Double.MAX_VALUE;

            for(SpreadPlayersCommand.Position var8 : param2) {
                if (var5 != var8) {
                    double var9 = var5.dist(var8);
                    var7 = Math.min(var9, var7);
                }
            }

            var0 += var7;
        }

        return param0.size() < 2 ? 0.0 : var0 / (double)param0.size();
    }

    private static SpreadPlayersCommand.Position[] createInitialPositions(Random param0, int param1, double param2, double param3, double param4, double param5) {
        SpreadPlayersCommand.Position[] var0 = new SpreadPlayersCommand.Position[param1];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            SpreadPlayersCommand.Position var2 = new SpreadPlayersCommand.Position();
            var2.randomize(param0, param2, param3, param4, param5);
            var0[var1] = var2;
        }

        return var0;
    }

    static class Position {
        private double x;
        private double z;

        double dist(SpreadPlayersCommand.Position param0) {
            double var0 = this.x - param0.x;
            double var1 = this.z - param0.z;
            return Math.sqrt(var0 * var0 + var1 * var1);
        }

        void normalize() {
            double var0 = (double)this.getLength();
            this.x /= var0;
            this.z /= var0;
        }

        float getLength() {
            return Mth.sqrt(this.x * this.x + this.z * this.z);
        }

        public void moveAway(SpreadPlayersCommand.Position param0) {
            this.x -= param0.x;
            this.z -= param0.z;
        }

        public boolean clamp(double param0, double param1, double param2, double param3) {
            boolean var0 = false;
            if (this.x < param0) {
                this.x = param0;
                var0 = true;
            } else if (this.x > param2) {
                this.x = param2;
                var0 = true;
            }

            if (this.z < param1) {
                this.z = param1;
                var0 = true;
            } else if (this.z > param3) {
                this.z = param3;
                var0 = true;
            }

            return var0;
        }

        public int getSpawnY(BlockGetter param0) {
            BlockPos var0 = new BlockPos(this.x, 256.0, this.z);

            while(var0.getY() > 0) {
                var0 = var0.below();
                if (!param0.getBlockState(var0).isAir()) {
                    return var0.getY() + 1;
                }
            }

            return 257;
        }

        public boolean isSafe(BlockGetter param0) {
            BlockPos var0 = new BlockPos(this.x, 256.0, this.z);

            while(var0.getY() > 0) {
                var0 = var0.below();
                BlockState var1 = param0.getBlockState(var0);
                if (!var1.isAir()) {
                    Material var2 = var1.getMaterial();
                    return !var2.isLiquid() && var2 != Material.FIRE;
                }
            }

            return false;
        }

        public void randomize(Random param0, double param1, double param2, double param3, double param4) {
            this.x = Mth.nextDouble(param0, param1, param3);
            this.z = Mth.nextDouble(param0, param2, param4);
        }
    }
}
