package org.Archibald.witch.session;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public class Resource {
    /**
     * 通过文件名（相对路径）返回文件内容
     * @param resourceName 文件名
     * @return 文件内容
     */
    public static Reader getResourceAsReader(String resourceName) {
        return new InputStreamReader(Objects.requireNonNull(getInputStream(resourceName)));
    }

    private static InputStream getInputStream (String resourceName) {
        ClassLoader[] classLoaders = getClassLoader();
        for (ClassLoader classLoader: classLoaders) {
            InputStream inputStream = classLoader.getResourceAsStream(resourceName);
            if (inputStream != null)
                return inputStream;
        }
        return null;
    }

    private static ClassLoader[] getClassLoader () {
        return new ClassLoader[] {
            ClassLoader.getSystemClassLoader(),                     //***不知道这两个类加载器的作用和区别***
            Thread.currentThread().getContextClassLoader()
        };
    }
}
