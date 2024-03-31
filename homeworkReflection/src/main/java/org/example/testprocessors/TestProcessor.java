package org.example.testprocessors;

import org.example.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class TestProcessor {
    private static List<Method> beforeMethod = new ArrayList<>();
    private static List<Method> afterMethod = new ArrayList<>();
    private static TreeMap<Integer, Method> testMethods = new TreeMap<>();
    private static int testCompleteCount;
    private static int testFailedCount;

    public static void process(Class<?> testclass) throws Exception{
        resetData();

        if (testclass.getAnnotation(Disabled.class) != null) {
            return;
        }

        sortMethodsByAnnotation(testclass.getDeclaredMethods());

        if (testMethods.entrySet().isEmpty()) {
            return;
        }

        if (beforeMethod.size() > 1) {
            throw new RuntimeException("More then one before method");
        }

        if (afterMethod.size() > 1) {
            throw new RuntimeException("More then one after method");
        }

        processTestMethods(testclass.newInstance());

        System.out.printf("Test complete %d, test failed %d \n", testCompleteCount, testFailedCount);
    }

    private static void resetData() {
        beforeMethod.clear();
        afterMethod.clear();
        testMethods.clear();
        testCompleteCount = 0;
        testFailedCount = 0;
    }

    private static void sortMethodsByAnnotation(Method[] methods) {
        for (Method m : methods) {
            if (( m.getAnnotation(Before.class)) != null) {
                beforeMethod.add(m);
            } else if (m.getAnnotation(After.class) != null) {
                afterMethod.add(m);
            } else if (m.getAnnotation(Test.class) != null) {
                Test annotation = m.getAnnotation(Test.class);

                if (m.getAnnotation(Disabled.class) != null
                    || annotation.priority() < 1
                    || annotation.priority() > 10) {
                    continue;
                }

                if (testMethods.containsKey(annotation.priority())) {
                    throw new IllegalArgumentException("Multiple methods with the same priority");
                }

                testMethods.put(annotation.priority(), m);
            }
        }
    }

    private static void processTestMethods(Object testObject) throws IllegalAccessException, InvocationTargetException {
        for (Integer tests : testMethods.keySet()) {
            if (!beforeMethod.isEmpty()) {
                beforeMethod.get(0).setAccessible(true);
                beforeMethod.get(0).invoke(testObject);
            }

            try {
                testMethods.get(tests).setAccessible(true);
                testMethods.get(tests).invoke(testObject);
            } catch (Exception e) {
                ThrowsException exceptionAnnotation = testMethods.get(tests).getAnnotation(ThrowsException.class);

                if (exceptionAnnotation == null
                    || !exceptionAnnotation.exception().equals(e.getCause().getClass())) {
                    testFailedCount++;

                    if (!afterMethod.isEmpty()) {
                        afterMethod.get(0).setAccessible(true);
                        afterMethod.get(0).invoke(testObject);
                    }

                    continue;
                }
            }

            testCompleteCount++;

            if (!afterMethod.isEmpty()) {
                afterMethod.get(0).setAccessible(true);
                afterMethod.get(0).invoke(testObject);
            }
        }
    }
}
