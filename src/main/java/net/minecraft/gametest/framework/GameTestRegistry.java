package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestRegistry {
    private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
    private static final Set<String> TEST_CLASS_NAMES = Sets.newHashSet();
    private static final Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Collection<TestFunction> LAST_FAILED_TESTS = Sets.newHashSet();

    public static void register(Class<?> param0) {
        Arrays.stream(param0.getDeclaredMethods()).forEach(GameTestRegistry::register);
    }

    public static void register(Method param0x) {
        String var0 = param0x.getDeclaringClass().getSimpleName();
        GameTest var1 = param0x.getAnnotation(GameTest.class);
        if (var1 != null) {
            TEST_FUNCTIONS.add(turnMethodIntoTestFunction(param0x));
            TEST_CLASS_NAMES.add(var0);
        }

        GameTestGenerator var2 = param0x.getAnnotation(GameTestGenerator.class);
        if (var2 != null) {
            TEST_FUNCTIONS.addAll(useTestGeneratorMethod(param0x));
            TEST_CLASS_NAMES.add(var0);
        }

        registerBatchFunction(param0x, BeforeBatch.class, BeforeBatch::batch, BEFORE_BATCH_FUNCTIONS);
        registerBatchFunction(param0x, AfterBatch.class, AfterBatch::batch, AFTER_BATCH_FUNCTIONS);
    }

    private static <T extends Annotation> void registerBatchFunction(
        Method param0, Class<T> param1, Function<T, String> param2, Map<String, Consumer<ServerLevel>> param3
    ) {
        T var0 = param0.getAnnotation(param1);
        if (var0 != null) {
            String var1 = param2.apply(var0);
            Consumer<ServerLevel> var2 = param3.putIfAbsent(var1, turnMethodIntoConsumer(param0));
            if (var2 != null) {
                throw new RuntimeException("Hey, there should only be one " + param1 + " method per batch. Batch '" + var1 + "' has more than one!");
            }
        }

    }

    public static Collection<TestFunction> getTestFunctionsForClassName(String param0) {
        return TEST_FUNCTIONS.stream().filter(param1 -> isTestFunctionPartOfClass(param1, param0)).collect(Collectors.toList());
    }

    public static Collection<TestFunction> getAllTestFunctions() {
        return TEST_FUNCTIONS;
    }

    public static Collection<String> getAllTestClassNames() {
        return TEST_CLASS_NAMES;
    }

    public static boolean isTestClass(String param0) {
        return TEST_CLASS_NAMES.contains(param0);
    }

    @Nullable
    public static Consumer<ServerLevel> getBeforeBatchFunction(String param0) {
        return BEFORE_BATCH_FUNCTIONS.get(param0);
    }

    @Nullable
    public static Consumer<ServerLevel> getAfterBatchFunction(String param0) {
        return AFTER_BATCH_FUNCTIONS.get(param0);
    }

    public static Optional<TestFunction> findTestFunction(String param0) {
        return getAllTestFunctions().stream().filter(param1 -> param1.getTestName().equalsIgnoreCase(param0)).findFirst();
    }

    public static TestFunction getTestFunction(String param0) {
        Optional<TestFunction> var0 = findTestFunction(param0);
        if (!var0.isPresent()) {
            throw new IllegalArgumentException("Can't find the test function for " + param0);
        } else {
            return var0.get();
        }
    }

    private static Collection<TestFunction> useTestGeneratorMethod(Method param0) {
        try {
            Object var0 = param0.getDeclaringClass().newInstance();
            return (Collection<TestFunction>)param0.invoke(var0);
        } catch (ReflectiveOperationException var2) {
            throw new RuntimeException(var2);
        }
    }

    private static TestFunction turnMethodIntoTestFunction(Method param0) {
        GameTest var0 = param0.getAnnotation(GameTest.class);
        String var1 = param0.getDeclaringClass().getSimpleName();
        String var2 = var1.toLowerCase();
        String var3 = var2 + "." + param0.getName().toLowerCase();
        String var4 = var0.template().isEmpty() ? var3 : var2 + "." + var0.template();
        String var5 = var0.batch();
        Rotation var6 = StructureUtils.getRotationForRotationSteps(var0.rotationSteps());
        return new TestFunction(
            var5,
            var3,
            var4,
            var6,
            var0.timeoutTicks(),
            var0.setupTicks(),
            var0.required(),
            var0.requiredSuccesses(),
            var0.attempts(),
            turnMethodIntoConsumer(param0)
        );
    }

    private static Consumer<?> turnMethodIntoConsumer(Method param0) {
        return param1 -> {
            try {
                Object var0 = param0.getDeclaringClass().newInstance();
                param0.invoke(var0, param1);
            } catch (InvocationTargetException var3) {
                if (var3.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)var3.getCause();
                } else {
                    throw new RuntimeException(var3.getCause());
                }
            } catch (ReflectiveOperationException var4) {
                throw new RuntimeException(var4);
            }
        };
    }

    private static boolean isTestFunctionPartOfClass(TestFunction param0, String param1) {
        return param0.getTestName().toLowerCase().startsWith(param1.toLowerCase() + ".");
    }

    public static Collection<TestFunction> getLastFailedTests() {
        return LAST_FAILED_TESTS;
    }

    public static void rememberFailedTest(TestFunction param0) {
        LAST_FAILED_TESTS.add(param0);
    }

    public static void forgetFailedTests() {
        LAST_FAILED_TESTS.clear();
    }
}
