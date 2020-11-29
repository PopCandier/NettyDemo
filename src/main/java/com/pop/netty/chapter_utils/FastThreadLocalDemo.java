package com.pop.netty.chapter_utils;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.InternalThreadLocalMap;

/**
 * @author Pop
 * @date 2020/11/29 13:13
 *
 * 它类似JDK的ThreadLocal，也是用于多线程条件下，保证统一线程的对象共享，Netty中的FastThreadLocal性能要高于
 * jdk的ThreadLocal
 */
public class FastThreadLocalDemo {

    final class FastThreadLocalTest extends FastThreadLocal<Object>{
        @Override
        protected Object initialValue() throws Exception {
            return new Object();
        }
    }

    private final FastThreadLocalTest fastThreadLocalTest;

    public FastThreadLocalDemo() {
        this.fastThreadLocalTest = new FastThreadLocalTest();
    }

    public static void main(String[] args) {

        final FastThreadLocalDemo fastThreadLocalDemo = new FastThreadLocalDemo();

        new Thread(()->{
            Object obj = fastThreadLocalDemo.fastThreadLocalTest.get();
            try{
                for(int i = 0;i<10;i++){
                    //这个线程里每个都会设置新的值
                    fastThreadLocalDemo.fastThreadLocalTest.set(new Object());
                    Thread.sleep(1000);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            try{
                Object obj = fastThreadLocalDemo.fastThreadLocalTest.get();
                //这个线程里判断每次取的是否相同
                for (int i = 0; i <10 ; i++) {
                    System.out.println(obj==fastThreadLocalDemo.fastThreadLocalTest.get());
                    Thread.sleep(1000);
                }
            }catch (Exception e){

            }
        }).start();
        /**
         *
         true
         true
         true
         true
         true
         true
         true
         true
         true
         true
         这里输出全是true 说明，虽然其他线程不断改变值，但是不影响当前线程共享的对象，这样就实现了线程共享对象的问题
         */
    }
}
