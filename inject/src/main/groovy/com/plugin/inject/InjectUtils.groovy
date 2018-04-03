package com.plugin.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.lang.reflect.Modifier

public class InjectUtils {
//初始化类池
    private final static ClassPool pool = ClassPool.getDefault()
    private final static injectMap = new HashMap<String, String>()
    private final static injectList = new ArrayList<String>()
    private static String injectPath = "com.plugin.inject.Injection"
    private static Class ignoreClazz = null

    static {
        injectMap.put("onClick", """onViewClick(\$1);""")
        injectMap.put("onLongClick", """onViewLongClick(\$1);""")
        injectMap.put("onCheckedChanged", """onCheckChanged(\$\$);""")

        injectList.add("android.view.View\$OnClickListener")
        injectList.add("android.view.View\$OnLongClickListener")
        injectList.add("android.widget.RadioGroup\$OnCheckedChangeListener")
    }

    public static void inject(String path, Project project) {
        if (!isInjectFileExist(path, project)) return
        //将当前路径加入类池,不然找不到这个类
        pool.appendClassPath(path)
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        //引入android.view.View包，因为onClick方法参数有View
        pool.importPackage("android.view.View")
        //引入android.view.View包，因为onCheckedChanged方法参数有RadioGroup
        pool.importPackage("android.widget.RadioGroup")

        File dir = new File(path)
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                if (isFilterClazz(file.absolutePath)) {
                    def clazzName = getClassNameFromPath(file.absolutePath)
                    CtClass ctClass = pool.getCtClass(clazzName)
                    if (isInjectClazz(ctClass)) {
                        CtMethod[] methods = ctClass.getDeclaredMethods()
                        for (CtMethod cm : methods) {
                            if (ignoreClazz != null && cm.hasAnnotation(ignoreClazz)) continue
                            if (injectMap.containsKey(cm.name)) {
                                if (ctClass.isFrozen()) {
                                    ctClass.defrost()//解冻
                                }
                                //http://blog.csdn.net/u011425751/article/details/51917895
                                //TODO Inject $0(class) Failed
                                String insetBeforeStr = injectPath + "." + injectMap.get(cm.name)
                                println("inject--> " + clazzName + "." + cm.name + " --> " + insetBeforeStr)

                                //FIXME Inject class's path for get more method info
                                cm.insertBefore(insetBeforeStr)
                                ctClass.writeFile(path)
                            }
                        }
                    }
                    ctClass.detach()//release
                }
            }
        }
    }

    private static String getClassNameFromPath(String path) {
        def pkg = path.substring(path.indexOf("debug\\") + 6)
        pkg = pkg.substring(0, pkg.lastIndexOf("."))
        return pkg.replaceAll("\\\\", ".")
    }

    private static boolean isInjectFileExist(String path, Project project) {
        String injectPath = obtainInjectPath(project)
        File file = new File(path)
        if (file.exists() && file.isDirectory()) {
            file = new File(file.absolutePath, injectPath + ".class")
            return file.exists()
        }
        return false
    }

    private static String obtainInjectPath(Project project) {
        if (project.hasProperty("INJECT_IGNORE")) {
            try {
                ignoreClazz = Class.forName(project.INJECT_IGNORE)
                //pool.makeClass(ignoreClazz) // TODO Failed
            } catch (Exception ignored) {
                ignoreClazz = InjectIgnore.class
            }
        }

        if (project.hasProperty("INJECT_PATH")) {
            injectPath = project.INJECT_PATH
        }
        println("INJECT_IGNORE---------->" + ignoreClazz.name)
        println("INJECT_PATH------------>" + injectPath)

        return injectPath.replace(".", "/")
    }

    private static boolean isInjectClazz(CtClass ctClass) {
        boolean isAbstract = Modifier.isAbstract(ctClass.getModifiers())
        boolean isInjectClazz = false
        ctClass.getInterfaces().each {
            isInjectClazz |= injectList.contains(it.name)
        }
        return !isAbstract && !ctClass.isInterface() && isInjectClazz
    }

    private static boolean isFilterClazz(String filePath) {
        return filePath.endsWith(".class") && !filePath.contains('R$') &&
                !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")
    }
}