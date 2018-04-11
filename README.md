# Injection 
### **通过代码注入实现无埋点方式采集用户点击行为**

### 注入条件：

1、项目源码，且不包含jar和aar等依赖包<br>
2、接口不会注入；抽象类默认不会注入，除非添加@Inject<br>
3、添加@Ignore的类，该类下所有方法都不会注入<br>
4、类必须有onClick/onTouch/onTouchEvent/onLongClick/onCheckedChanged方法并实现相应接口

### 使用规则：

#### 一、添加类

1、新建**Injection**类，代码如下：(方法按需添加)

    public class Injection {
          
          //默认需要添加该方法，否则可能会报错
          public static void onClick(View v) {
              Monitor.onClick(v);
          }
          
          public static void onTouch(View v, MotionEvent event) {
              Monitor.onTouch(v, event);
          }
          
          public static void onTouchEvent(Activity activity, MotionEvent event) {
              Monitor.onTouchEvent(activity, event);
          }
          
          public static void onTouchEvent(View view, MotionEvent event) {
              Monitor.onTouchEvent(view, event);
          }
      
          public static void onLongClick(View v) {
              Monitor.onLongClick(v);
          }
      
          public static void onCheckedChanged(RadioGroup group, int checkedId) {
              Monitor.onCheckedChanged(group, checkedId);
          }
          
          public static void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              Monitor.onCheckedChanged(buttonView, isChecked);
          }
     }
   注：Monitor类集中处理相应事件的逻辑，添加方法参考 https://github.com/wshychbydh/ActionDemo<br>


2、添加注解类：（必须添加）

   * 添加Ignore注解类：

    @Retention(CLASS)
    @Target({TYPE, METHOD})
    public @interface Ignore {
    }
   
   * 添加Inject注解类：

    @Retention(CLASS)
    @Target(TYPE)
    public @interface Inject {
    }
    
#### 二、添加配置     

1、配置Injection类，Inject注解，Ignore注解的存放路径，在app的build文件中添加：
    
    ext {
        INJECTION_PATH = "com.app.demo.inject.Injection" //Injection的全路径
        INJECT_PATH = "com.app.demo.inject.Inject" //Inject的全路径
        IGNORE_PATH = "com.app.demo.inject.Ignore" //Ignore的全路径
    }
    
2、在app的build文件中配置需要注入的方法：（按需添加）
 
    ext {
        INJECT_CLICK = true           //注入所有符合条件的onClick方法，默认为true 
        INJECT_TOUCH = false          //onTouch方法，默认为false
        INJECT_TOUCH_EVENT = false    //onTouchEvent方法，默认为false
        INJECT_LONG_CLICK = false     //onLongClick方法，默认为false
        INJECT_RADIOGROUP = false     //RadioGroup的onCheckedChanged方法，默认为false
        INJECT_COMPOUNDBUTTON = false //CompoundButton的onCheckedChanged方法，默认为false
        INJECT_LOG = false            //是否打印日志，默认false
    }

#### 三、使用注解 

1、如果有不需要注入的方法，在相应的方法上添加@Ignore注解，如下：
  
      @Ignore
      public void onClick(View v) {
          ```
      }
      
2、如果抽象类也需要注入，在类上添加@Inject注解，如下：
   
      @Inject
      public abstract class Xx implements View.OnClickListener {
          ```
      }
                   
#### 四、添加依赖
在app的build文件下添加如下代码
   
    buildscript {
        repositories {
            google()
            jcenter()
            maven {
                 url 'https://jitpack.io'
            }
            ```
        }
        dependencies {
            ```
            classpath 'com.github.wshychbydh:Injection:xxx'
        }
    }
    ```
    apply plugin: com.plugin.inject.InjectPlugin
    
#### Demo地址：https://github.com/wshychbydh/ActionDemo

[![](https://jitpack.io/v/wshychbydh/Injection.svg)](https://jitpack.io/#wshychbydh/Injection)