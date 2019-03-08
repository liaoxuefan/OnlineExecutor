package com.example.OnlineExecutor.execute;



public class HotSwapClassLoader extends ClassLoader {
    public HotSwapClassLoader(){
        super(HotSwapClassLoader.class.getClassLoader());//获取HotSwapClassLoader的类对象(模板对象)
    }

    public Class loadByte(byte[] bytes){
        return defineClass(null,bytes,0,bytes.length);
    }
}
