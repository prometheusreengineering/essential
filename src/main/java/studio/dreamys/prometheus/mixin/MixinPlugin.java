//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package studio.dreamys.prometheus.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gg.essential.asm.EssentialTransformer;
import gg.essential.asm.GlErrorCheckingTransformer;
import gg.essential.asm.MixinTransformerWrapper;
import gg.essential.data.VersionInfo;
import gg.essential.lib.mixinextras.MixinExtrasBootstrap;
import gg.essential.mixins.injection.points.AfterInvokeInInit;
import gg.essential.mixins.injection.points.BeforeConstantInInit;
import gg.essential.mixins.injection.points.BeforeFieldAccessInInit;
import gg.essential.mixins.injection.points.BeforeInvokeInInit;
import gg.essential.util.MixinUtils;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.MixinService;

public class MixinPlugin implements IMixinConfigPlugin {
    private static final Logger logger;
    private final EssentialTransformer[] transformers = new EssentialTransformer[0];
    private final Multimap<String, EssentialTransformer> transformerMap = ArrayListMultimap.create();

    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
        MixinUtils.registerInjectionPoint(AfterInvokeInInit.class);
        MixinUtils.registerInjectionPoint(BeforeConstantInInit.class);
        MixinUtils.registerInjectionPoint(BeforeFieldAccessInInit.class);
        MixinUtils.registerInjectionPoint(BeforeInvokeInInit.class);

        for(EssentialTransformer transformer : this.transformers) {
            for(String target : transformer.getTargets()) {
                this.transformerMap.put(target, transformer);
            }
        }

    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for(EssentialTransformer transformer : this.transformerMap.get(targetClassName)) {
            transformer.preApply(targetClass);
        }

    }

    private void createDummyIfMissing(ClassNode targetClass, String name, String desc) {
        for(MethodNode method : targetClass.methods) {
            if (name.equals(method.name) && desc.equals(method.desc)) {
                return;
            }
        }

        MethodNode dummyMethod = new MethodNode(0, name, desc, (String)null, (String[])null);
        dummyMethod.instructions.add(new InsnNode(177));
        targetClass.methods.add(dummyMethod);
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for(EssentialTransformer transformer : this.transformerMap.get(targetClassName)) {
            transformer.postApply(targetClass);
        }

    }

    private static void registerGlobalTransformers(List<MixinTransformerWrapper.Transformer> extraTransformers) throws ReflectiveOperationException {
        ClassLoader classLoader = MixinPlugin.class.getClassLoader();
        Field delegateField = classLoader.getClass().getDeclaredField("delegate");
        delegateField.setAccessible(true);
        Object delegate = delegateField.get(classLoader);
        Field transformerField = delegate.getClass().getDeclaredField("mixinTransformer");
        transformerField.setAccessible(true);
        IMixinTransformer transformer = (IMixinTransformer)transformerField.get(delegate);
        IMixinTransformer var6 = new MixinTransformerWrapper(transformer, extraTransformers);
        transformerField.set(delegate, var6);
    }

    static boolean hasClass(String name) {
        return testClass(name, (cls) -> true);
    }

    static boolean testClass(String className, Predicate<ClassNode> test) {
        try {
            ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(className);
            return test.test(classNode);
        } catch (ClassNotFoundException var3) {
            return false;
        } catch (Exception e) {
            logger.error("Exception when testing class {}: ", className, e);
            return false;
        }
    }

    static {
        logger = LogManager.getLogger("Prometheus - Plugin");
        VersionInfo info = new VersionInfo();
        logger.info("Starting Essential v{} (#{}) [{}]", info.getEssentialVersion(), info.getEssentialCommit(), info.getEssentialBranch());
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        logger.info("Java: {} (v{}) by {} ({})", System.getProperty("java.vm.name"), System.getProperty("java.version"), System.getProperty("java.vm.vendor"), System.getProperty("java.vendor"));
        logger.info("Java Path: {}", System.getProperty("sun.boot.library.path"));
        logger.info("Java Info: {}", System.getProperty("java.vm.info"));
        logger.info("JVM Arguments: \n  - {}", String.join("\n  - ", arguments));
        logger.info("OS: {} (v{}) (Arch: {})", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        List<MixinTransformerWrapper.Transformer> globalTransformers = new ArrayList();
        if (Boolean.getBoolean("essential.gl_debug.asm")) {
            globalTransformers.add(new GlErrorCheckingTransformer());
        }

        if (!globalTransformers.isEmpty()) {
            try {
                registerGlobalTransformers(globalTransformers);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
