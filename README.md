# Injection 
### **通过代码注入实现无埋点方式采集用户点击行为**

### 注入条件：

1、项目源码，且不包含jar和aar等依赖包<br>
2、接口不会注入;抽象类默认不会注入，除非添加@Ignore(false)<br>
3、添加@Ignore的类，该类下所有方法都不会注入<br>
4、类必须有onClick/onTouch/onTouchEvent/onLongClick/onCheckedChanged方法并实现相应接口

### 使用规则：

#### 一、添加类

1、在引入该插件之前，需要先添加另外一个依赖：

   在根目录的build文件中添加：
    
    allprojects {
        repositories {
            google()
            jcenter()
            maven { url 'https://jitpack.io' }
            ```
        }
    }
    
   在app的build文件中添加：

     dependencies {
         ```
         implementation 'com.github.wshychbydh:ActionMonitor:xxx'
     }

2、新建**Injection**类，代码如下：(方法按需添加)

    public class Injection {
          
          //默认需要添加该方法，否则可能会报错
          public static void onClick(View v) {
              Monitor.onViewClick(v);
          }
          
          public static void onTouch(View v, MotionEvent event) {
              Monitor.onTouch(v, event);
          }
          
          public static void onTouchEvent(Object obj, MotionEvent event) {
              if (obj instanceof View) {
                  Monitor.onTouchEvent((View)obj, event);
              } else if (obj instanceof Activity) {
                  Monitor.onTouchEvent((Activity)obj, event);
              }
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
     
3、如果有不需要注入的方法，在相应的方法上添加@Ignore注解，如下：

    @Ignore
    public void onClick(View v) {
        ```
    }
    
    
4、配置Injection类的存放目录，在app的build文件中添加：
    
    ext {
        INJECT_PATH = "com.plugin.inject.Injection" //Injection的完整地址
    }
        
 5、在app的build文件中配置需要注入的方法：（按需添加）
 
    ext {
        INJECT_CLICK = true          //注入所有符合条件的onClick方法，默认为true 
        INJECT_TOUCH = true          //onTouch方法，默认为false
        INJECT_LONG_CLICK = true     //onLongClick方法，默认为false
        INJECT_RADIOGROUP = true     //RadioGroup的onCheckedChanged方法，默认为false
        INJECT_COMPOUNDBUTTON = true //CompoundButton的onCheckedChanged方法，默认为false
        INJECT_LOG = true            //是否打印日志，默认false
    }

#### 二、添加插件依赖
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