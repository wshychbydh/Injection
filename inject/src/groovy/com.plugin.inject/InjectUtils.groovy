package com.plugin.inject

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.lang.reflect.Modifier

public class InjectUtils {
//初始化类池
    private final static ClassPool pool = ClassPool.getDefault()

    private final static injectMap = new HashMap<String, String>() {}
    private final static injectClazz = new ArrayList<String>() {}

    static {
        injectMap.put("onClick", """com.heshidai.plugin.monitor.Monitor.onViewClick(\$1);""")
        injectMap.put("onLongClick", """com.heshidai.plugin.monitor.Monitor.onViewLongClick(\$1);""")
        injectMap.put("onCheckedChanged", """com.heshidai.plugin.monitor.Monitor.onCheckChanged(\$\$);""")

        injectClazz.add("android.view.View\$OnClickListener")
        injectClazz.add("android.view.View\$OnLongClickListener")
        injectClazz.add("android.widget.RadioGroup\$OnCheckedChangeListener")
    }

    public static void inject(String path, Project project) {
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
                    def pkg = file.absolutePath.substring(file.absolutePath.indexOf("debug\\") + 6)
                    pkg = pkg.substring(0, pkg.lastIndexOf("."))
                    def clazzName = pkg.replaceAll("\\\\", ".")
                    CtClass ctClass = pool.getCtClass(clazzName)
                    if (isInjectClazz(ctClass)) {
                        CtMethod[] methods = ctClass.getDeclaredMethods()
                        for (CtMethod cm : methods) {
                            if (injectMap.containsKey(cm.name)) {
                                if (ctClass.isFrozen()) {
                                    ctClass.defrost()//解冻
                                }
                                //CtClass的用法参考http://blog.csdn.net/u011425751/article/details/51917895
                                String insetBeforeStr = injectMap.get(cm.name)
                                println("inject-->" + clazzName + " : " + cm.name + " : injectStr-->" + insetBeforeStr)
                                //在方法开头插入代码
                                cm.insertBefore(insetBeforeStr)
                                ctClass.writeFile(path)
                            } else {
                                println("filter-->" + clazzName + " : " + cm.name)
                            }
                        }
                    }
                    ctClass.detach()//release
                }
            }
        }
    }

    private static boolean isInjectClazz(CtClass ctClass) {
        boolean isAbstract = Modifier.isAbstract(ctClass.getModifiers())
        boolean isInjectClazz = false
        ctClass.getInterfaces().each {
            isInjectClazz |= injectClazz.contains(it.name)
        }
        return !isAbstract && !ctClass.isInterface() && isInjectClazz
    }

    private static boolean isFilterClazz(String filePath) {
        return filePath.endsWith(".class") && !filePath.contains('R$') &&
                !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")
    }
}