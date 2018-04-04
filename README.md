# Injection 
### **通过代码注入实现无埋点方式采集用户点击行为**

### 注入条件：

1、项目源码，且不包含jar/aar等依赖包

2、非接口、非抽象类且拥有方法onClick，onLongClick，onCheckedChanged方法。

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