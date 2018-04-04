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

    private static final String INJECT_CLICK = "android.view.View\$OnClickListener"
    private static final String INJECT_LONG_CLICK = "android.view.View\$OnLongClickListener"
    // private static final String INJECT_TOUCH = "android.view.View\$OnTouchListener"
    private static final String INJECT_PATH = "com.plugin.inject.Injection"
    private static final String INJECT_GROUP = "android.widget.RadioGroup\$OnCheckedChangeListener"
    private static final String INJECT_BUTTON = "android.widget.CompoundButton\$OnCheckedChangeListener"

    private final static injectList = new ArrayList<String>()
    private static boolean injectClick = true
    private static boolean injectTouch = false
    private static boolean injectLongClick = false
    private static boolean injectRadioGroup = false
    private static boolean injectCompoundButton = false
    private static String injectPath = INJECT_PATH
    private static Class ignoreClazz = null

    static {
        injectMap.put("onClick", """onViewClick(\$1);""")
        injectMap.put("onLongClick", """onViewLongClick(\$1);""")
        injectMap.put("onCheckedChanged", """onCheckChanged(\$\$);""")
        injectMap.put("onTouch", """onTouch(\$\$);""")
    }

    public static void inject(String rootPath, Project project) {
        if (!isInjectFileExist(rootPath, project)) return
        //将根路径加入类池
        pool.appendClassPath(rootPath)
        //加入android相关类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        //   此处我是直接传递的方法参数，该参数在源码中已经引入了，此处无需再引入
        //   pool.importPackage("android.view.View")
        //   pool.importPackage("android.widget.RadioGroup")
        //   pool.importPackage("android.widget.CompoundButton")

        File dir = new File(rootPath)
        if (dir.isDirectory()) {
            config(project)
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                if (isInjectFile(file.absolutePath)) {
                    doInject(rootPath, file.absolutePath)
                }
            }
        }
    }

    private static void doInject(String rootPath, String filePath) {
        def clazzName = getClassNameFromPath(filePath)
        CtClass ctClass = pool.getCtClass(clazzName)
        if (isInjectClass(ctClass)) {
            CtMethod[] methods = ctClass.getDeclaredMethods()
            for (CtMethod cm : methods) {
                if (ignoreClazz != null && cm.hasAnnotation(ignoreClazz)) continue
                if (injectMap.containsKey(cm.name) && isInjectMethod(cm)) {
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()//解冻
                    }
                    //CtClass相关用法参考http://blog.csdn.net/u011425751/article/details/51917895
                    //FIXME Inject $0(class) Failed
                    String insetBeforeStr = injectPath + "." + injectMap.get(cm.name)
                    println("inject--> " + clazzName + "." + cm.name + " --> " + cm.parameterTypes[0].name)
                    cm.insertBefore(insetBeforeStr)
                    ctClass.writeFile(rootPath)
                }
            }
        }
        ctClass.detach()//release
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
        injectPath = INJECT_PATH
        if (project.hasProperty("INJECT_PATH")) {
            injectPath = project.INJECT_PATH
        }
        println("INJECT_PATH------------>" + injectPath)
        return injectPath.replace(".", "/")
    }

    private static boolean isInjectMethod(CtMethod cm) {
        def types = cm.parameterTypes
        if (types == null || types.length == 0) return false

        def isInjectMethod = false
        if ((cm.name == "onClick" && injectClick) || (cm.name == "onLongClick" && injectLongClick)) {
            isInjectMethod |= types.length == 1 &&
                    (types[0].name == "android.view.View" || types[0].name.startsWith("android.widget."))
        }
        if (cm.name == "onCheckedChanged" && injectRadioGroup) {
            isInjectMethod |= types.length == 2 &&
                    types[0].name == "android.widget.RadioGroup" && types[1].name == "int"
        }
        if (cm.name == "onCheckedChanged" && injectCompoundButton) {
            isInjectMethod |= types.length == 2 &&
                    types[0].name == "android.widget.CompoundButton" && types[1].name == "boolean"
        }
        if (cm.name == "onTouch" && injectTouch) {
            isInjectMethod |= types.length == 2 &&
                    types[0].name == "android.view.View" && types[1].name == "android.view.MotionEvent"
        }
        return isInjectMethod
    }

    /**
     * Rest all and load config info from app's build.xml
     * @param project
     */
    private static void config(Project project) {
        injectList.clear()
        injectClick = true
        injectTouch = false
        injectLongClick = false
        injectRadioGroup = false
        injectCompoundButton = false
        ignoreClazz = null

        if (project.hasProperty("INJECT_CLICK")) {
            injectClick = project.INJECT_CLICK
        }
        if (injectClick) {
            injectList.add(INJECT_CLICK)
        }
        if (project.hasProperty("INJECT_TOUCH")) {
            if (project.INJECT_TOUCH) {
                injectTouch = true
                //injectList.add(INJECT_TOUCH)
            }
        }
        if (project.hasProperty("INJECT_LONG_CLICK")) {
            if (project.INJECT_LONG_CLICK) {
                injectLongClick = true
                injectList.add(INJECT_LONG_CLICK)
            }
        }
        if (project.hasProperty("INJECT_RADIOGROUP")) {
            if (project.INJECT_RADIOGROUP) {
                injectRadioGroup = true
                injectList.add(INJECT_GROUP)
            }
        }
        if (project.hasProperty("INJECT_COMPOUNDBUTTON")) {
            if (project.INJECT_COMPOUNDBUTTON) {
                injectCompoundButton = true
                injectList.add(INJECT_BUTTON)
            }
        }
        if (project.hasProperty("INJECT_IGNORE")) {
            try {
                ignoreClazz = Class.forName(project.INJECT_IGNORE)
                //pool.makeClass(ignoreClazz) // FIXME Failed
            } catch (Exception ignored) {
                ignoreClazz = InjectIgnore.class
            }
        }
        println("INJECT_IGNORE---------->" + ignoreClazz.name)
    }

    private static boolean isInjectClass(CtClass ctClass) {
        boolean isAbstract = Modifier.isAbstract(ctClass.getModifiers())
        boolean isInjectClazz = false

        ctClass.getInterfaces().each {
            isInjectClazz |= injectList.contains(it.name)
        }
        return !isAbstract && !ctClass.isInterface() && isInjectClazz || injectTouch
    }

    private static boolean isInjectFile(String filePath) {
        def isInjectClass = filePath.endsWith(".class") && !filePath.contains('R$') &&
                !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")
        if (isInjectClass) {
            if (filePath.endsWith("Injection.class")) {
                isInjectClass &= getClassNameFromPath(filePath) != injectPath
            }
        }
        return isInjectClass
    }
}