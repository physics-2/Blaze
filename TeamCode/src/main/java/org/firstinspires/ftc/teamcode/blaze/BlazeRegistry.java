package org.firstinspires.ftc.teamcode.blaze;

import org.firstinspires.ftc.teamcode.blaze.Anotations.BlazeModule;
import org.firstinspires.ftc.teamcode.blaze.Anotations.HardwareConfig;
import org.firstinspires.ftc.teamcode.blaze.Anotations.InitWithTelemetry;
import org.firstinspires.ftc.teamcode.blaze.AutoWire.ElectronicsConfig;
import org.firstinspires.ftc.teamcode.blaze.ModuleSubsystem.BlazingModule;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The central registry for the Blaze framework.
 * <p>
 * Holds the state of all discovered components (Modules, Telemetry Providers, Hardware Configs),
 * defines the scanning rules, and manages the lifecycle (instantiation, autowiring) of modules.
 * </p>
 */
public class BlazeRegistry {

    /** Internal data class holding module metadata and its instantiated object. */
    private static class ModuleInfo {
        final Class<? extends BlazingModule> type;
        final String name;
        BlazingModule instance;

        ModuleInfo(Class<? extends BlazingModule> type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    // --- State ---
    private static final Map<String, ModuleInfo> registry = new LinkedHashMap<>();
    private static Class<? extends TelemetryProvider> telemetryProviderClass = null;
    private static Class<? extends ElectronicsConfig> hardwareConfigClass = null;

    // --- Scan Rules Abstraction ---

    /**
     * Interface defining the contract for how a specific annotation type is scanned,
     * validated, cached, and registered.
     */
    public interface ScanRule {
        Class<? extends Annotation> getTargetAnnotation();
        String getCachePrefix();
        boolean isValid(Class<?> clazz);
        void onDexFound(Class<?> clazz);
        boolean onCacheFound(Class<?> clazz, ClassLoader classLoader);
        void writeCache(BufferedWriter writer) throws IOException;
        void reset();
    }


    /**
     * Extended scan rule specifically for modules, allowing retrieval of all found class names.
     */
    public interface ModuleScanRule extends ScanRule {
        List<String> getFoundClasses();
    }

    /** List of all active scanning rules applied during APK traversal. */
    public static final List<ScanRule> SCAN_RULES = new ArrayList<>();

    static {
        // 1. Rule for Modules
        SCAN_RULES.add(new ModuleScanRule() {
            private final List<String> foundClasses = new ArrayList<>();
            @Override public Class<? extends Annotation> getTargetAnnotation() { return BlazeModule.class; }
            @Override public String getCachePrefix() { return "MODULE:"; }
            @Override public boolean isValid(Class<?> clazz) { return BlazingModule.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()); }
            @Override public void onDexFound(Class<?> clazz) {
                BlazeLogger.addDefaultLog("BlazeRegistry","Found module during scan: " + clazz.getName());
                foundClasses.add(clazz.getName());
            }
            @Override public boolean onCacheFound(Class<?> clazz, ClassLoader cl) {
                BlazeLogger.addDefaultLog("BlazeRegistry","Found module in cache: " + clazz.getName());
                foundClasses.add(clazz.getName());
                return true;
            }
            @Override public void writeCache(BufferedWriter writer) throws IOException {

                for (String c : foundClasses) {
                    BlazeLogger.addDefaultLog("BlazeRegistry","Written module in cache: " + c);
                    writer.write(getCachePrefix() + c); writer.newLine(); }
            }
            @Override public void reset() { foundClasses.clear(); }
            public List<String> getFoundClasses() { return foundClasses; }
        });

        // 2. Rule for TelemetryProvider
        SCAN_RULES.add(createSingletonRule(
                org.firstinspires.ftc.teamcode.blaze.Anotations.TelemetryProvider.class, TelemetryProvider.class, "TELEMETRY_PROVIDER:",
                (clazz) -> telemetryProviderClass = (Class<? extends TelemetryProvider>) clazz,
                () -> telemetryProviderClass,
                () -> telemetryProviderClass = null
        ));

        // 3. Rule for HardwareConfig
        SCAN_RULES.add(createSingletonRule(
                HardwareConfig.class, ElectronicsConfig.class, "HARDWARE_CONFIG:",
                (clazz) -> hardwareConfigClass = (Class<? extends ElectronicsConfig>) clazz,
                () -> hardwareConfigClass,
                () -> hardwareConfigClass = null
        ));
    }

    /**
     * Factory method to create a scan rule for singleton components (e.g., TelemetryProvider, HardwareConfig).
     */
    private static ScanRule createSingletonRule(Class<? extends Annotation> annotation, Class<?> baseType, String prefix, Consumer<Class<?>> setter, Supplier<Class<?>> getter, Runnable resetter) {
        return new ScanRule() {
            @Override public Class<? extends Annotation> getTargetAnnotation() { return annotation; }
            @Override public String getCachePrefix() { return prefix; }
            @Override public boolean isValid(Class<?> clazz) { return baseType.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()); }
            @Override public void onDexFound(Class<?> clazz) {
                if (getter.get() != null) throw new IllegalStateException("Duplicate @" + annotation.getSimpleName() + ": " + clazz.getSimpleName());
                setter.accept(clazz);
                BlazeLogger.addDefaultLog("BlazeRegistry", "Found during scan" + annotation.getSimpleName() + ": " + clazz.getSimpleName());
            }
            @Override public boolean onCacheFound(Class<?> clazz, ClassLoader cl) {
                if (getter.get() != null) return false;
                setter.accept(clazz);
                BlazeLogger.addDefaultLog("BlazeRegistry", "Found cached " + annotation.getSimpleName() + ": " + clazz.getSimpleName());
                return true;
            }
            @Override public void writeCache(BufferedWriter writer) throws IOException {
                Class<?> clazz = getter.get();
                writer.write(getCachePrefix() + (clazz != null ? clazz.getName() : ""));
                writer.newLine();
                BlazeLogger.addDefaultLog("BlazeRegistry", "Written to cache " + annotation.getSimpleName() + ": " + clazz.getSimpleName());
            }
            @Override public void reset() { resetter.run(); }
        };
    }

    /** Private constructor to prevent instantiation. */
    private BlazeRegistry() {}

    // --- Methods for the Scanner to interact with the Registry ---

    /**
     * Resets all scanning rules, clearing their internal found-class lists.
     */
    public static void resetRules() {
        for (ScanRule rule : SCAN_RULES) rule.reset();
    }


    /**
     * Finalizes module registration by instantiating metadata for all valid classes found during the scan.
     *
     * @param classLoader The ClassLoader to use for loading the scanned class names.
     */
    public static void finalizeModuleRegistration(ClassLoader classLoader) {
        registry.clear();
        List<String> validClasses = ((ModuleScanRule) SCAN_RULES.get(0)).getFoundClasses();
        for (String className : validClasses) {
            try {
                Class<?> clazz = Class.forName(className, false, classLoader);
                BlazeModule annotation = clazz.getAnnotation(BlazeModule.class);
                String moduleName = annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name();

                if (registry.containsKey(moduleName)) {
                    throw new IllegalStateException("Duplicate module name: '" + moduleName + "'");
                }
                registry.put(moduleName, new ModuleInfo((Class<? extends BlazingModule>) clazz, moduleName));
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                // Ignore classes that disappear between scanning and registration
            }
        }
    }

    // --- Public Getters & Lifecycle ---

    /**
     * Instantiates and returns the global TelemetryProvider, if one was discovered.
     */
    public static TelemetryProvider getTelemetryProvider() {
        if (telemetryProviderClass == null) return null;
        try { return telemetryProviderClass.getDeclaredConstructor().newInstance(); }
        catch (Exception e) { throw new RuntimeException("Failed to instantiate TelemetryProvider: " + e.getMessage(), e); }
    }

    /**
     * Returns the class type of the discovered HardwareConfig.
     */
    public static Class<? extends ElectronicsConfig> getHardwareConfigClass() {
        return hardwareConfigClass;
    }

    /**
     * Instantiates and autowires all registered modules.
     *
     * @param telemetry The telemetry instance to pass to modules requiring it.
     */
    public static void createAll(MultiDashTelemetry telemetry) {
        for (ModuleInfo info : registry.values()) {
            if (info.instance != null) continue;
            try {
                Constructor<?> constructor = info.type.getDeclaredConstructor();
                constructor.setAccessible(true);
                info.instance = (BlazingModule) constructor.newInstance();
                long startTime = System.currentTimeMillis();
                if (info.type.getAnnotation(InitWithTelemetry.class) != null && telemetry != null) {
                    info.instance.autowireSelf(info.name, telemetry);
                } else {
                    info.instance.autowireSelf(info.name, null);
                }

                long timeToEnd = System.currentTimeMillis() - startTime;
                BlazeLogger.addDefaultLog(BlazeRegistry.class.getSimpleName(),"Took " + timeToEnd + " ms to autowire module " + info.name);
            } catch (Exception e) {
                Throwable realCause = (e instanceof InvocationTargetException) ? ((InvocationTargetException) e).getTargetException() : e;
                throw new RuntimeException("Failed to instantiate module '" + info.name + "': " + realCause.getMessage(), realCause);
            }
        }
    }

    /**
     * Manually registers an existing module instance into the registry.
     *
     * @param name     The registered name of the module.
     * @param instance The pre-instantiated module object.
     */
    public static void registerInstance(String name, BlazingModule instance) {
        ModuleInfo info = registry.get(name);
        if (info == null) throw new IllegalArgumentException("Module '" + name + "' not found in registry.");
        if (info.instance != null) throw new IllegalStateException("Module '" + name + "' already has an instance.");
        info.instance = instance;
    }

    /**
     * Retrieves a module instance by its registered string name.
     */
    public static BlazingModule get(String name) {
        ModuleInfo info = registry.get(name);
        if (info == null || info.instance == null) throw new IllegalStateException("Module not found or not initialized: '" + name + "'");
        return info.instance;
    }

    /**
     * Retrieves a module instance by its class type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlazingModule> T get(Class<T> type) {
        for (ModuleInfo info : registry.values()) {
            if (info.instance != null && type.isInstance(info.instance)) return (T) info.instance;
        }
        throw new IllegalStateException("Module of type '" + type.getSimpleName() + "' not found.");
    }

    /**
     * Returns a map of all currently instantiated modules, keyed by their registered name.
     */
    public static HashMap<String, BlazingModule> getAll() {
        HashMap<String, BlazingModule> modules = new HashMap<>();
        for (ModuleInfo info : registry.values()) {
            if (info.instance != null) modules.put(info.name, info.instance);
        }
        return modules;
    }

    /**
     * Clears all instantiated module references and empties the registry.
     */
    public static void clear() {
        for (ModuleInfo info : registry.values()) info.instance = null;
        registry.clear();
    }

    /**
     * Returns the total number of registered modules (instantiated or not).
     */
    public static int count() { return registry.size(); }
}