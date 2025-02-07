package com.flechazo.util;

import net.minecraft.registry.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 替代
 */
public class RegistryUtils {

    // 通用获取方法（1.19+）
    public static <T> Collection<T> getValues(RegistryKey<? extends Registry<T>> registryKey) {
        // 获取注册表实例
        Registry<?> registry = Registries.REGISTRIES.get(registryKey.getValue());

        if (registry == null) {
            return Collections.emptyList();
        }

        // 使用泛型擦除的方式访问具体的 Registry<T>
        if (registry.getKey() == registryKey) {
            @SuppressWarnings("unchecked")  // 强制转换为正确类型
            Registry<T> typedRegistry = (Registry<T>) registry;

            // 获取注册表的条目并将其转换为集合
            return typedRegistry.getEntrySet().stream()  // 获取注册表条目的集合
                    .map(Map.Entry::getValue)        // 获取每个条目的值
                    .collect(Collectors.toList());   // 收集为 List 或 Collection
        }

        // 如果类型不匹配，则返回空集合
        return Collections.emptyList();
    }
}
