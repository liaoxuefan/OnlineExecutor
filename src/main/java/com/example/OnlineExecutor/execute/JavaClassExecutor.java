package com.example.OnlineExecutor.execute;


import java.lang.reflect.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.RuntimeErrorException;

/**
 * 执行外部传来的一个代表Java类的byte数组
 * 执行过程：
 * 1. 清空HackSystem中的缓存
 * 2. new ClassFileModifier，并传入需要被修改的字节数组
 * 3. 调用ClassFileModifier#modifyUTF8Constant修改：
 *      java/lang/System -> ……/HackSystem
 * 4. new一个类加载器，把字节数组加载为Class对象
 * 5. 通过反射调用Class对象的main方法
 * 6. 从HackSystem中获取返回结果
 */
public class JavaClassExecutor {


    public String execute(byte[] bytes) {
        //HackSystem.out.flush();
        byte[] modifyBytes=new ClassFileModifier(bytes).modifyUTF8Constant("java/lang/System","com/example/OnlineExecutor/execute/HackSystem");
        HotSwapClassLoader classLoader=new HotSwapClassLoader();
        Class clazz=classLoader.loadByte(modifyBytes);
        try {
            Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
            mainMethod.invoke(null, new String[] { null });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {

            e.getCause().printStackTrace(HackSystem.err);
        }

        // 6. 从HackSystem中获取返回结果
        String res = HackSystem.getBufferString();
        HackSystem.closeBuffer();
        return res;
    }


    //通过反射调用main方法
    public void runMain(Class cla) throws ReflectiveOperationException {
        Method mainMethod=cla.getMethod("main",new Class[]{String[].class});
        mainMethod.invoke(null,new String[]{null});
    }
}
