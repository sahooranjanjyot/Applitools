package com.visualengine.engine.healing;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.JavascriptExecutor;
import java.lang.reflect.Proxy;

public class HealingDriverFactory {
    public static WebDriver create(WebDriver originalDriver) {
        return (WebDriver) Proxy.newProxyInstance(
            HealingDriverFactory.class.getClassLoader(),
            new Class<?>[] { WebDriver.class, TakesScreenshot.class, JavascriptExecutor.class },
            new HealingInvocationHandler(originalDriver)
        );
    }
}
