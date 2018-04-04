# Injection 
### **通过代码注入实现无埋点方式采集用户点击行为**

### 注入条件：

1、项目源码，且不包含jar/aar等依赖包

2、非接口、非抽象类且拥有方法onClick，onLongClick，onCheckedChanged方法。

### 使用规则：

#### 一、添加类

1、在main目录下新建目录**com.plugin.inject** (InjectIgnore不会失效，推荐使用)

2、在新建的目录下新建**Injection**类，代码如下：

    public class Injection {
          
          //默认需要添加该方法，否则可能会报错
          public static void onViewClick(View v) {
              Monitor.onViewClick(v);
          }
          
          public static void onTouch(View v, MotionEvent event) {
              Monitor.onTouch(v, event);
          }
      
          public static void onViewLongClick(View v) {
              Monitor.onViewLongClick(v);
          }
      
          public static void onCheckChanged(RadioGroup group, int checkedId) {
              Monitor.onCheckChanged(group, checkedId);
          }
          
          public static void onCheckChanged(CompoundButton buttonView, boolean isChecked) {
              Monitor.onCheckChanged(buttonView, isChecked);
          }
     }
   **说明:** 
   
     * 类中的方法按需加载，如果不需要注入则可不加。
     * Monitor在数据采集依赖包com.github.wshychbydh:ActionMonitor:xx中。 
     
3、如果有不需要注入的方法，需新建一个**InjectIgnore**注解类，如下：

    public @interface InjectIgnore {}
    
   然后在不需要标记的方法上面添加@InjectIgnore
    
4、如果不想创建新的目录，也可以将Injection类存放于任何目录下，然后在app的build文件中添加代码：
    
    ext {
        INJECT_PATH = "com.plugin.inject.Injection" //Injection的完整地址
        INJECT_IGNORE = "com.plugin.inject.InjectIgnore"  //InjectIgnore的完整地址
    }
   **InjectIgnore注意事项：**
   
    * 如果按照‘规则1’设置，以上参数可不设置。
    * 该方式InjectIgnore可能会失效，如果失效，请参考“规则1”。
    * 检查InjectIgnore是否失效的方式：在编译时查看‘Inject_IGNORE’的打印地址是否与设置一致，不一致则说明失效。  
        
 5、在app的build文件中配置需要过滤的方法：
 
    ext {
        INJECT_CLICK = true          //注入所有符合条件的onClick方法，默认为true 
        INJECT_TOUCH = true          //onTouch方法，默认为false
        INJECT_LONG_CLICK = true     //onLongClick方法，默认为false
        INJECT_RADIOGROUP = true     //RadioGroup的onCheckedChanged方法，默认为false
        INJECT_COMPOUNDBUTTON = true //CompoundButton的onCheckedChanged方法，默认为false
    }

#### 二、添加依赖
在app的build文件下添加如下代码
   
    buildscript {
        repositories {
            google()
            jcenter()
            maven {
                 url 'https://jitpack.io'
            }
        }
        dependencies {
            classpath 'com.github.wshychbydh:Injection:xxx'
        }
    }
    ```
    apply plugin: com.plugin.inject.InjectPlugin
    
#### Demo地址：https://github.com/wshychbydh/ActionDemo

[![](https://jitpack.io/v/wshychbydh/Injection.svg)](https://jitpack.io/#wshychbydh/Injection)