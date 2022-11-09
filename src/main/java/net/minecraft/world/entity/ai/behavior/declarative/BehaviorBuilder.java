package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorBuilder<E extends LivingEntity, M> implements App<BehaviorBuilder.Mu<E>, M> {
    private final BehaviorBuilder.TriggerWithResult<E, M> trigger;

    public static <E extends LivingEntity, M> BehaviorBuilder<E, M> unbox(App<BehaviorBuilder.Mu<E>, M> param0) {
        return (BehaviorBuilder<E, M>)param0;
    }

    public static <E extends LivingEntity> BehaviorBuilder.Instance<E> instance() {
        return new BehaviorBuilder.Instance<>();
    }

    public static <E extends LivingEntity> OneShot<E> create(Function<BehaviorBuilder.Instance<E>, ? extends App<BehaviorBuilder.Mu<E>, Trigger<E>>> param0) {
        final BehaviorBuilder.TriggerWithResult<E, Trigger<E>> var0 = get(param0.apply(instance()));
        return new OneShot<E>() {
            @Override
            public boolean trigger(ServerLevel param0, E param1, long param2) {
                Trigger<E> var0 = var0.tryTrigger(param0, param1, param2);
                return var0 == null ? false : var0.trigger(param0, param1, param2);
            }

            @Override
            public String debugString() {
                return "OneShot[" + var0.debugString() + "]";
            }

            @Override
            public String toString() {
                return this.debugString();
            }
        };
    }

    public static <E extends LivingEntity> OneShot<E> sequence(Trigger<? super E> param0, Trigger<? super E> param1) {
        return create(param2 -> param2.<Unit>group(param2.ifTriggered(param0)).apply(param2, param1x -> param1::trigger));
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> param0, OneShot<? super E> param1) {
        return sequence(triggerIf(param0), param1);
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> param0) {
        return create(param1 -> param1.point((param1x, param2, param3) -> param0.test(param2)));
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(BiPredicate<ServerLevel, E> param0) {
        return create(param1 -> param1.point((param1x, param2, param3) -> param0.test(param1x, param2)));
    }

    static <E extends LivingEntity, M> BehaviorBuilder.TriggerWithResult<E, M> get(App<BehaviorBuilder.Mu<E>, M> param0) {
        return unbox(param0).trigger;
    }

    BehaviorBuilder(BehaviorBuilder.TriggerWithResult<E, M> param0) {
        this.trigger = param0;
    }

    static <E extends LivingEntity, M> BehaviorBuilder<E, M> create(BehaviorBuilder.TriggerWithResult<E, M> param0) {
        return new BehaviorBuilder<>(param0);
    }

    static final class Constant<E extends LivingEntity, A> extends BehaviorBuilder<E, A> {
        Constant(A param0) {
            this(param0, () -> "C[" + param0 + "]");
        }

        Constant(final A param0, final Supplier<String> param1) {
            super(new BehaviorBuilder.TriggerWithResult<E, A>() {
                @Override
                public A tryTrigger(ServerLevel param0x, E param1x, long param2) {
                    return param0;
                }

                @Override
                public String debugString() {
                    return param1.get();
                }

                @Override
                public String toString() {
                    return this.debugString();
                }
            });
        }
    }

    public static final class Instance<E extends LivingEntity> implements Applicative<BehaviorBuilder.Mu<E>, BehaviorBuilder.Instance.Mu<E>> {
        public <Value> Optional<Value> tryGet(MemoryAccessor<OptionalBox.Mu, Value> param0) {
            return OptionalBox.unbox(param0.value());
        }

        public <Value> Value get(MemoryAccessor<IdF.Mu, Value> param0) {
            return IdF.get(param0.value());
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<OptionalBox.Mu, Value>> registered(MemoryModuleType<Value> param0) {
            return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Registered<>(param0));
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<IdF.Mu, Value>> present(MemoryModuleType<Value> param0) {
            return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Present<>(param0));
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<Const.Mu<Unit>, Value>> absent(MemoryModuleType<Value> param0) {
            return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Absent<>(param0));
        }

        public BehaviorBuilder<E, Unit> ifTriggered(Trigger<? super E> param0) {
            return new BehaviorBuilder.TriggerWrapper<>(param0);
        }

        public <A> BehaviorBuilder<E, A> point(A param0) {
            return new BehaviorBuilder.Constant<>(param0);
        }

        public <A> BehaviorBuilder<E, A> point(Supplier<String> param0, A param1) {
            return new BehaviorBuilder.Constant<>(param1, param0);
        }

        @Override
        public <A, R> Function<App<BehaviorBuilder.Mu<E>, A>, App<BehaviorBuilder.Mu<E>, R>> lift1(App<BehaviorBuilder.Mu<E>, Function<A, R>> param0) {
            return param1 -> {
                final BehaviorBuilder.TriggerWithResult<E, A> var0 = BehaviorBuilder.get(param1);
                final BehaviorBuilder.TriggerWithResult<E, Function<A, R>> var1x = BehaviorBuilder.get(param0);
                return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
                    @Override
                    public R tryTrigger(ServerLevel param0, E param1, long param2) {
                        A var0 = (A)var0.tryTrigger(param0, param1, param2);
                        if (var0 == null) {
                            return null;
                        } else {
                            Function<A, R> var1 = (Function)var1.tryTrigger(param0, param1, param2);
                            return (R)(var1 == null ? null : var1.apply(var0));
                        }
                    }

                    @Override
                    public String debugString() {
                        return var1.debugString() + " * " + var0.debugString();
                    }

                    @Override
                    public String toString() {
                        return this.debugString();
                    }
                });
            };
        }

        public <T, R> BehaviorBuilder<E, R> map(final Function<? super T, ? extends R> param0, App<BehaviorBuilder.Mu<E>, T> param1) {
            final BehaviorBuilder.TriggerWithResult<E, T> var0 = BehaviorBuilder.get(param1);
            return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
                @Override
                public R tryTrigger(ServerLevel param0x, E param1, long param2) {
                    T var0 = var0.tryTrigger(param0, param1, param2);
                    return var0 == null ? null : param0.apply(var0);
                }

                @Override
                public String debugString() {
                    return var0.debugString() + ".map[" + param0 + "]";
                }

                @Override
                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <A, B, R> BehaviorBuilder<E, R> ap2(
            App<BehaviorBuilder.Mu<E>, BiFunction<A, B, R>> param0, App<BehaviorBuilder.Mu<E>, A> param1, App<BehaviorBuilder.Mu<E>, B> param2
        ) {
            final BehaviorBuilder.TriggerWithResult<E, A> var0 = BehaviorBuilder.get(param1);
            final BehaviorBuilder.TriggerWithResult<E, B> var1 = BehaviorBuilder.get(param2);
            final BehaviorBuilder.TriggerWithResult<E, BiFunction<A, B, R>> var2 = BehaviorBuilder.get(param0);
            return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
                @Override
                public R tryTrigger(ServerLevel param0, E param1, long param2) {
                    A var0 = var0.tryTrigger(param0, param1, param2);
                    if (var0 == null) {
                        return null;
                    } else {
                        B var1 = var1.tryTrigger(param0, param1, param2);
                        if (var1 == null) {
                            return null;
                        } else {
                            BiFunction<A, B, R> var2 = var2.tryTrigger(param0, param1, param2);
                            return var2 == null ? null : var2.apply(var0, var1);
                        }
                    }
                }

                @Override
                public String debugString() {
                    return var2.debugString() + " * " + var0.debugString() + " * " + var1.debugString();
                }

                @Override
                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <T1, T2, T3, R> BehaviorBuilder<E, R> ap3(
            App<BehaviorBuilder.Mu<E>, Function3<T1, T2, T3, R>> param0,
            App<BehaviorBuilder.Mu<E>, T1> param1,
            App<BehaviorBuilder.Mu<E>, T2> param2,
            App<BehaviorBuilder.Mu<E>, T3> param3
        ) {
            final BehaviorBuilder.TriggerWithResult<E, T1> var0 = BehaviorBuilder.get(param1);
            final BehaviorBuilder.TriggerWithResult<E, T2> var1 = BehaviorBuilder.get(param2);
            final BehaviorBuilder.TriggerWithResult<E, T3> var2 = BehaviorBuilder.get(param3);
            final BehaviorBuilder.TriggerWithResult<E, Function3<T1, T2, T3, R>> var3 = BehaviorBuilder.get(param0);
            return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
                @Override
                public R tryTrigger(ServerLevel param0, E param1, long param2) {
                    T1 var0 = var0.tryTrigger(param0, param1, param2);
                    if (var0 == null) {
                        return null;
                    } else {
                        T2 var1 = var1.tryTrigger(param0, param1, param2);
                        if (var1 == null) {
                            return null;
                        } else {
                            T3 var2 = var2.tryTrigger(param0, param1, param2);
                            if (var2 == null) {
                                return null;
                            } else {
                                Function3<T1, T2, T3, R> var3 = var3.tryTrigger(param0, param1, param2);
                                return var3 == null ? null : var3.apply(var0, var1, var2);
                            }
                        }
                    }
                }

                @Override
                public String debugString() {
                    return var3.debugString() + " * " + var0.debugString() + " * " + var1.debugString() + " * " + var2.debugString();
                }

                @Override
                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <T1, T2, T3, T4, R> BehaviorBuilder<E, R> ap4(
            App<BehaviorBuilder.Mu<E>, Function4<T1, T2, T3, T4, R>> param0,
            App<BehaviorBuilder.Mu<E>, T1> param1,
            App<BehaviorBuilder.Mu<E>, T2> param2,
            App<BehaviorBuilder.Mu<E>, T3> param3,
            App<BehaviorBuilder.Mu<E>, T4> param4
        ) {
            final BehaviorBuilder.TriggerWithResult<E, T1> var0 = BehaviorBuilder.get(param1);
            final BehaviorBuilder.TriggerWithResult<E, T2> var1 = BehaviorBuilder.get(param2);
            final BehaviorBuilder.TriggerWithResult<E, T3> var2 = BehaviorBuilder.get(param3);
            final BehaviorBuilder.TriggerWithResult<E, T4> var3 = BehaviorBuilder.get(param4);
            final BehaviorBuilder.TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> var4 = BehaviorBuilder.get(param0);
            return BehaviorBuilder.create(
                new BehaviorBuilder.TriggerWithResult<E, R>() {
                    @Override
                    public R tryTrigger(ServerLevel param0, E param1, long param2) {
                        T1 var0 = var0.tryTrigger(param0, param1, param2);
                        if (var0 == null) {
                            return null;
                        } else {
                            T2 var1 = var1.tryTrigger(param0, param1, param2);
                            if (var1 == null) {
                                return null;
                            } else {
                                T3 var2 = var2.tryTrigger(param0, param1, param2);
                                if (var2 == null) {
                                    return null;
                                } else {
                                    T4 var3 = var3.tryTrigger(param0, param1, param2);
                                    if (var3 == null) {
                                        return null;
                                    } else {
                                        Function4<T1, T2, T3, T4, R> var4 = var4.tryTrigger(param0, param1, param2);
                                        return var4 == null ? null : var4.apply(var0, var1, var2, var3);
                                    }
                                }
                            }
                        }
                    }
    
                    @Override
                    public String debugString() {
                        return var4.debugString()
                            + " * "
                            + var0.debugString()
                            + " * "
                            + var1.debugString()
                            + " * "
                            + var2.debugString()
                            + " * "
                            + var3.debugString();
                    }
    
                    @Override
                    public String toString() {
                        return this.debugString();
                    }
                }
            );
        }

        static final class Mu<E extends LivingEntity> implements Applicative.Mu {
            private Mu() {
            }
        }
    }

    public static final class Mu<E extends LivingEntity> implements K1 {
    }

    static final class PureMemory<E extends LivingEntity, F extends K1, Value> extends BehaviorBuilder<E, MemoryAccessor<F, Value>> {
        PureMemory(final MemoryCondition<F, Value> param0) {
            super(new BehaviorBuilder.TriggerWithResult<E, MemoryAccessor<F, Value>>() {
                public MemoryAccessor<F, Value> tryTrigger(ServerLevel param0x, E param1, long param2) {
                    Brain<?> var0 = param1.getBrain();
                    Optional<Value> var1 = var0.getMemoryInternal(param0.memory());
                    return var1 == null ? null : param0.createAccessor(var0, var1);
                }

                @Override
                public String debugString() {
                    return "M[" + param0 + "]";
                }

                @Override
                public String toString() {
                    return this.debugString();
                }
            });
        }
    }

    interface TriggerWithResult<E extends LivingEntity, R> {
        @Nullable
        R tryTrigger(ServerLevel var1, E var2, long var3);

        String debugString();
    }

    static final class TriggerWrapper<E extends LivingEntity> extends BehaviorBuilder<E, Unit> {
        TriggerWrapper(final Trigger<? super E> param0) {
            super(new BehaviorBuilder.TriggerWithResult<E, Unit>() {
                @Nullable
                public Unit tryTrigger(ServerLevel param0x, E param1, long param2) {
                    return param0.trigger(param0, param1, param2) ? Unit.INSTANCE : null;
                }

                @Override
                public String debugString() {
                    return "T[" + param0 + "]";
                }
            });
        }
    }
}
