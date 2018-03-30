package com.heshidai.app.inject;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.ClassPool;

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
        if (dir.isDirectory()) {
            System.out.println("filePath-->" + dir.getAbsolutePath());
        }
    }
}