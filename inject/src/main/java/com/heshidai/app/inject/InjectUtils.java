package com.heshidai.app.inject;

import org.gradle.api.Project;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Created by cool on 2018/3/30.
 */

public class InjectUtils {
    private final static ClassPool pool = ClassPool.getDefault();

    private final static HashMap injectMap = new HashMap<String, String>();

    private final static ArrayList injectClazz = new ArrayList<String>();

    static {
        //  injectMap.put("onClick", """com.heshidai.plugin.monitor.Monitor.onViewClick(\$1);""")
        //   injectMap.put("onLongClick", """com.heshidai.plugin.monitor.Monitor.onViewLongClick(\$1);""")
        //  injectMap.put("onCheckedChanged", """com.heshidai.plugin.monitor.Monitor.onCheckChanged(\$\$);""")

        //  injectClazz.add("android.view.View\$OnClickListener")
        //   injectClazz.add("android.view.View\$OnLongClickListener")
        //  injectClazz.add("android.widget.RadioGroup\$OnCheckedChangeListener")
    }

    public static void inject(String path, Project project) {
        System.out.println("inject~~~" + path);
        File dir = new File(path);
        try {
            eachFile(dir);
        } catch (Exception e) {
            System.out.println("EXCEPTION--->" + e.getMessage());
        }
    }

    private static void eachFile(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                eachFile(f);
            }
        } else {
            if (isFilterClazz(file.getAbsolutePath())) {
                System.out.println("insert code--->>" + file.getAbsolutePath());
//                        String pkg = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("debug\\") + 6);
//                        pkg = pkg.substring(0, pkg.lastIndexOf("."));
//                        String clazzName = pkg.replaceAll("\\\\", ".");
//                        CtClass ctClass = pool.getCtClass(clazzName);
//                        System.out.println("insert code--->>" + ctClass.getName());
//                        if (isInjectClazz(ctClass)) {
//                            CtMethod[] methods = ctClass.getDeclaredMethods();
//                            for (CtMethod cm : methods) {
//                                System.out.println("insert code--->>" + cm.getName());
//                            }
//                        }
//                        ctClass.detach();//release
            }
        }
    }

    private static boolean isInjectClazz(CtClass ctClass) throws NotFoundException {
        boolean isAbstract = Modifier.isAbstract(ctClass.getModifiers());
        boolean isInjectClazz = false;
        for (CtClass clazz : ctClass.getInterfaces()) {
            isInjectClazz |= injectClazz.contains(clazz.getName());
        }
        return !isAbstract && !ctClass.isInterface() && isInjectClazz;
    }

    private static boolean isFilterClazz(String filePath) {
        return filePath.endsWith(".class") && !filePath.contains("R$") &&
                !filePath.contains("R.class") && !filePath.contains("BuildConfig.class");
    }
}