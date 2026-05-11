package com.visualengine.engine.healing;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HealingInvocationHandler implements InvocationHandler {
    private final WebDriver originalDriver;
    private final DeterministicHealer healer;

    public HealingInvocationHandler(WebDriver originalDriver) {
        this.originalDriver = originalDriver;
        this.healer = new DeterministicHealer();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(originalDriver, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            
            // Intercept findElement failure
            if (targetException instanceof NoSuchElementException && method.getName().equals("findElement")) {
                By brokenLocator = (By) args[0];
                By healedLocator = healer.healLocator(originalDriver, brokenLocator);
                
                if (healedLocator != null) {
                    // Try to find the element again using the healed locator
                    return originalDriver.findElement(healedLocator);
                }
            }
            throw targetException; // Re-throw if we couldn't heal it
        }
    }
}
