package com.example.OnlineExecutor.compile;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringSourceCompiler {
    private static Map<String,JavaFileObject> fileObjectMap=new ConcurrentHashMap<>();

    public static byte[] compile(String source,DiagnosticCollector<JavaFileObject> collector){
        JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
        //DiagnosticListener<JavaFileObject> collector=new DiagnosticCollector<>();
        JavaFileManager javaFileManager=compiler.getStandardFileManager(collector,null,null);
        JavaFileManager manager=new TmpJavaFileManager(javaFileManager);

        Pattern pattern= Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
        Matcher matcher=pattern.matcher(source);
        String class_name;
        if(matcher.find()){
            class_name=matcher.group(1);
        }else{
            throw new IllegalArgumentException("No valid class");
        }
        JavaFileObject javaFileObject=new TmpJavaFileObject(class_name,source);
        Boolean result=compiler.getTask(null,manager,collector,null,
                null, Arrays.asList(javaFileObject)).call();
        JavaFileObject bytesJavaFileObject=fileObjectMap.get(class_name);
        if(result&&bytesJavaFileObject!=null){
            return ((TmpJavaFileObject)bytesJavaFileObject).getCompliedByte();
        }
        return null;
    }



    public static class TmpJavaFileObject extends SimpleJavaFileObject {
        private String source;
        private ByteArrayOutputStream outputStream;

        public TmpJavaFileObject(String name,String source){
            super(URI.create("String:///"+name+Kind.SOURCE.extension),Kind.SOURCE);
            this.source=source;
        }

        public TmpJavaFileObject(String name,Kind kind){
            super(URI.create("String:///"+name+kind.extension),kind);
            this.source=null;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            if(source==null){
                throw new IllegalArgumentException("source is null");
            }
            return source;
        }
        @Override
        public OutputStream openOutputStream(){
            outputStream=new ByteArrayOutputStream();
            return outputStream;
        }

        public byte[] getCompliedByte(){
            return outputStream.toByteArray();
        }
    }

    public static class TmpJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
//        private JavaFileManager javaFileManager;
        protected TmpJavaFileManager(JavaFileManager javaFileManager){
            super(javaFileManager);
        }
        @Override
        public JavaFileObject getJavaFileForOutput(Location location,
                                                   String className,
                                                   JavaFileObject.Kind kind,
                                                   FileObject sibling) throws IOException{
            JavaFileObject javaFileObject=new TmpJavaFileObject(className,kind);
            fileObjectMap.put(className,javaFileObject);
            return javaFileObject;
        }
        @Override
        public JavaFileObject getJavaFileForInput(Location location,
                                                  String className,
                                                  JavaFileObject.Kind kind) throws IOException{
            if(fileObjectMap.get(className)==null){
                return super.getJavaFileForInput(location,className,kind);
            }
            return fileObjectMap.get(className);
        }
    }
}
