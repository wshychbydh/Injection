package com.plugin.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.lang.reflect.Modifier

/**
 * Created by cool on 2018/4/9.
 * CtClass reference http://blog.csdn.net/u011425751/article/details/51917895
 */
class InjectHelper {

    private final static String VIEW = "android.view.View"
    private final static String ACTIVITY = "android.app.Activity"
    private final static String GROUP = "android.widget.RadioGroup"
    private final static String BUTTON = "android.widget.CompoundButton"
    private final static String EVENT = "android.view.MotionEvent"

    private final static String CLICK = "onClick"
    private final static String LONG_CLICK = "onLongClick"
    private final static String TOUCH = "onTouch"
    private final static String TOUCH_EVENT = "onTouchEvent"
    private final static String CHECK_CHANGE = "onCheckedChanged"

    private static final String INJECT_CLICK = VIEW + "\$OnClickListener"
    private static final String INJECT_LONG_CLICK = VIEW + "\$OnLongClickListener"
    private static final String INJECT_TOUCH = VIEW + "\$OnTouchListener"
    private static final String INJECT_GROUP = GROUP + "\$OnCheckedChangeListener"
    private static final String INJECT_BUTTON = BUTTON + "\$OnCheckedChangeListener"

    private static final String INJECT_PATH = "com.plugin.inject.Injection"

    private final ClassPool pool = ClassPool.getDefault()
    private final injectMap = new HashMap<String, String>()
    private String injectPath = INJECT_PATH

    private final injectList = new ArrayList<CtClass>()
    private boolean injectClick = true
    private boolean injectTouch = false
    private boolean injectTouchEvent = false
    private boolean injectLongClick = false
    private boolean injectRadioGroup = false
    private boolean injectCompoundButton = false
    private boolean injectLog = false

    InjectHelper() {
        injectMap.put(CLICK, """onClick(\$1);""")
        injectMap.put(LONG_CLICK, """onLongClick(\$1);""")
        injectMap.put(CHECK_CHANGE, """onCheckedChanged(\$1,\$2);""")
        injectMap.put(TOUCH, """onTouch(\$1,\$2);""")
        injectMap.put(TOUCH_EVENT, """onTouchEvent(\$0,\$1);""")
    }

    void inject(String rootPath, Project project) {
        if (!isInjectFileExist(rootPath, project)) return
        //add path to pool
        pool.appendClassPath(rootPath)
        //import android
        pool.appendClassPath(project.android.bootClasspath[0].toString())

        File dir = new File(rootPath)
        if (dir.isDirectory()) {
            config(project)
            dir.eachFileRecurse { File file ->
                if (isInjectFile(file.absolutePath, injectPath)) {
                    doInject(rootPath, file.absolutePath)
                }
            }
        }
    }

    private void doInject(String rootPath, String filePath) {
        def clazzName = getClassNameFromPath(filePath)
        CtClass ctClass = pool.getCtClass(clazzName)
        if (isInjectClass(ctClass)) {
            CtMethod[] methods = ctClass.getDeclaredMethods()
            for (CtMethod cm : methods) {
                if (Modifier.isAbstract(cm.getModifiers())) continue
                if (cm.hasAnnotation(Ignore.class)) {
                    Ignore ignore = cm.getAnnotation(Ignore.class)
                    if (ignore.value()) continue
                }

                if ((injectMap.containsKey(cm.name) && isInjectMethod(cm))) {
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }
                    if (injectLog) {
                        println("inject ----> " + clazzName + "." + cm.name)
                    }
                    cm.insertBefore(injectPath + "." + injectMap.get(cm.name))
                    ctClass.writeFile(rootPath)
                }
            }
        }
        ctClass.detach()//release
    }

    private boolean isInjectFileExist(String path, Project project) {
        String injectPath = obtainInjectPath(project)
        File file = new File(path)
        if (file.exists() && file.isDirectory()) {
            file = new File(file.absolutePath, injectPath + ".class")
            return file.exists()
        }
        return false
    }

    private String obtainInjectPath(Project project) {
        injectPath = INJECT_PATH
        if (project.hasProperty("INJECT_PATH")) {
            injectPath = project.INJECT_PATH
        }
        return injectPath.replace(".", "/")
    }

    private boolean isInjectMethod(CtMethod cm) {
        def types = cm.parameterTypes
        if (types == null || types.length == 0) return false

        def isInjectMethod = false
        if ((injectClick && cm.name == CLICK) || (injectLongClick && cm.name == LONG_CLICK)) {
            isInjectMethod |= types.length == 1 &&
                    (types[0].name == VIEW || types[0].name.startsWith("android.widget."))
        }
        if (injectRadioGroup && cm.name == CHECK_CHANGE) {
            isInjectMethod |= types.length == 2 && types[0].name == GROUP && types[1].name == "int"
        }
        if (injectCompoundButton && cm.name == CHECK_CHANGE) {
            isInjectMethod |= types.length == 2 && types[0].name == BUTTON && types[1].name == "boolean"
        }
        if (injectTouch && cm.name == TOUCH) {
            isInjectMethod |= types.length == 2 && types[0].name == VIEW && types[1].name == EVENT
        }
        if (injectTouchEvent && cm.name == TOUCH_EVENT) {
            isInjectMethod |= types.length == 1 && types[0].name == EVENT
        }
        return isInjectMethod
    }

    /**
     * Rest all and load config info from app's build.xml
     * @param project
     */
    private void config(Project project) {
        if (project.hasProperty("INJECT_CLICK")) {
            injectClick = project.INJECT_CLICK
        }
        if (injectClick) {
            injectList.add(pool.get(INJECT_CLICK))
        }
        if (project.hasProperty("INJECT_TOUCH_EVENT")) {
            if (project.INJECT_TOUCH_EVENT) {
                injectTouchEvent = true
                injectList.add(pool.get(VIEW))
                injectList.add(pool.get(ACTIVITY))
            }
        }
        if (project.hasProperty("INJECT_TOUCH")) {
            if (project.INJECT_TOUCH) {
                injectTouch = true
                injectList.add(pool.get(INJECT_TOUCH))
            }
        }
        if (project.hasProperty("INJECT_LONG_CLICK")) {
            if (project.INJECT_LONG_CLICK) {
                injectLongClick = true
                injectList.add(pool.get(INJECT_LONG_CLICK))
            }
        }
        if (project.hasProperty("INJECT_RADIOGROUP")) {
            if (project.INJECT_RADIOGROUP) {
                injectRadioGroup = true
                injectList.add(pool.get(INJECT_GROUP))
            }
        }
        if (project.hasProperty("INJECT_COMPOUNDBUTTON")) {
            if (project.INJECT_COMPOUNDBUTTON) {
                injectCompoundButton = true
                injectList.add(pool.get(INJECT_BUTTON))
            }
        }
        if (project.hasProperty("INJECT_LOG")) {
            injectLog = project.INJECT_LOG
        }
        if (injectLog) {
            println("INJECT_PATH------------>" + injectPath)
        }
    }

    private boolean isInjectClass(CtClass ctClass) {

        if (ctClass.isInterface()) return false

        if (ctClass.hasAnnotation(Ignore.class)) {
            Ignore ignore = ctClass.getAnnotation(Ignore.class)
            //Abstract class can use @Ignore(false) to inject action
            return !ignore.value()
        }

        if (Modifier.isAbstract(ctClass.getModifiers())) {
            return false
        }

        //Check anonymous inner class is in @Ignored class.
        if (ctClass.name.contains("\$")) {
            def superClass = pool.get(ctClass.name.substring(0, ctClass.name.indexOf("\$")))
            if (superClass.hasAnnotation(Ignore.class)) {
                Ignore ignore = superClass.getAnnotation(Ignore.class)
                return !ignore.value()
            }
        }

        //check class is need inject
        int size = injectList.size()
        for (int i = 0; i < size; i++) {
            if (ctClass.subtypeOf(injectList.get(i))) {
                return true
            }
        }

        return false
    }

    /**
     *
     * @param filePath
     * @param filterPath This file will be filtered
     * @return
     */
    private static boolean isInjectFile(String filePath, String filterPath) {
        def isInjectClass = filePath.endsWith(".class") && !filePath.contains('R$') &&
                !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")
        if (isInjectClass) {
            if (filePath.endsWith("Injection.class")) {
                isInjectClass &= getClassNameFromPath(filePath) != filterPath
            }
        }
        return isInjectClass
    }

    private static String getClassNameFromPath(String path) {
        def pkg = path.substring(path.indexOf("debug\\") + 6)
        pkg = pkg.substring(0, pkg.lastIndexOf("."))
        return pkg.replaceAll("\\\\", ".")
    }
}