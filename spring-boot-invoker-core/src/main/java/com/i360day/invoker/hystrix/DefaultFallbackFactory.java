package com.i360day.invoker.hystrix;

/**
 * @program: spring-cloud-invoker-parent
 * @description: 默认
 * @author: liju.z
 * @create: 2019-10-27 10:33
 **/
public class DefaultFallbackFactory implements FallbackFactory<Object> {

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 默认执行异常回调 <p>
     *
     * <p> @Date  13:24 <p>
     *
     * <p> @Param [throwable] <p>
     *
     * <p> @return [throwable] <p>
     *
     **/
    @Override
    public Object create(Throwable throwable) {
        return null;
    }
}
