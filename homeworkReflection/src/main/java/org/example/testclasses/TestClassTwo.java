package org.example.testclasses;

import org.example.annotation.After;
import org.example.annotation.Before;
import org.example.annotation.Test;

public class TestClassTwo {

    @Before
    void beforeTest() {
        System.out.println("Before");
    }

    @After
    void afterTest() {
        System.out.println("After");
    }

    @Test(priority = 10)
    void testOne() {
        System.out.println("test 1");
    }

    @Test(priority = 2)
    void testTwo() {
        System.out.println("test 2");
    }

    @Test(priority = 1)
    void testThree() {
        throw new RuntimeException("oops");
    }

    @Test(priority = 5)
    void testFour() {
        System.out.println("test 4");
    }
}
