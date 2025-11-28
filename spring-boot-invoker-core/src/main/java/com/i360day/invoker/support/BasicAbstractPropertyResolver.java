package com.i360day.invoker.support;

import com.i360day.invoker.common.ObjectUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.AbstractPropertyResolver;
import org.springframework.core.env.Environment;

public abstract class BasicAbstractPropertyResolver extends AbstractPropertyResolver  implements EnvironmentAware {
    private Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected String getPropertyAsRawString(String key) {
        return environment.getProperty(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    /**
     * 根据占位符获取配置文件中的值
     *
     * @param pro
     * @return
     */
    protected String parsePlaceHolder(String pro) {
        return parsePlaceHolder(pro, () -> null);
    }

    /**
     * 根据占位符获取配置文件中的值
     *
     * @param pro
     * @param defaultValue
     * @return
     */
    protected String parsePlaceHolder(String pro, String defaultValue) {
        return parsePlaceHolder(pro, () -> defaultValue);
    }

    /**
     * 根据占位符获取配置文件中的值
     *
     * @param pro
     * @param defaultValueSupplier
     * @return
     */
    protected String parsePlaceHolder(String pro, java.util.function.Supplier<String> defaultValueSupplier) {
        if (ObjectUtils.isEmpty(pro)) return defaultValueSupplier.get();
        try {
            String value = resolveRequiredPlaceholders(pro);
            if (ObjectUtils.isNotEmpty(value)) {
                return value;
            }
        } catch (Exception ex) {
            //ignore
        }
        return defaultValueSupplier.get();
    }
}
