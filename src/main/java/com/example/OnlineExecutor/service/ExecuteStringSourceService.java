package com.example.OnlineExecutor.service;

import com.example.OnlineExecutor.compile.StringSourceCompiler;
import com.example.OnlineExecutor.controller.RunCodeController;
import com.example.OnlineExecutor.execute.JavaClassExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ExecuteStringSourceService {

    private static final int TIME_LIMIT= 15;

    /*N_THREAD=N_CPU+1,因为是CPU密集型的操作*/
    private static final int N_THREAD =5;

    //负责执行客户端代码的线程池
    private static final ExecutorService pool=new ThreadPoolExecutor(N_THREAD,N_THREAD,
    60L,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(N_THREAD));

    public String execute(String source){
        DiagnosticCollector<JavaFileObject> compileCollector=new DiagnosticCollector<>();

        byte[] bytes= StringSourceCompiler.compile(source,compileCollector);
        //编译出现问题
        if(bytes==null){
            List<Diagnostic<? extends JavaFileObject>> compilerError =compileCollector.getDiagnostics();
            StringBuilder errorRes=new StringBuilder();
            for(Diagnostic diagnostic:compilerError){
                errorRes.append("Compilation error at ");
                errorRes.append(diagnostic.getLineNumber());
                errorRes.append(".");
                errorRes.append(System.lineSeparator());
            }
            return errorRes.toString();
        }
        String res=ExecuteStringSourceService.limitRunTime(bytes,TIME_LIMIT);

        return res;
    }



    public static String limitRunTime(byte[] classBytes,int timeout)
     {

        Callable<String> runTask=new Callable<String>() {
            @Override
            public String call() throws Exception {
                return new JavaClassExecutor().execute(classBytes);
            }
        };
        Future<String> res=pool.submit(runTask);

        String runResult;
        try{
            runResult = res.get(timeout, TimeUnit.SECONDS);
        }catch(InterruptedException e){
            runResult = "Program interrupted.";

        }catch (ExecutionException e){
            runResult = e.getCause().getMessage();
        }catch (TimeoutException e){
           runResult = "Time Limit Exceeded";
        }
        /*finally {
            pool.shutdown();
        }每次运行共用的是一个线程池
        */
        return runResult;
    }
}
