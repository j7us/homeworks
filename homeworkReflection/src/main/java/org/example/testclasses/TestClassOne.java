package org.example.testclasses;

import org.example.annotation.After;
import org.example.annotation.Before;
import org.example.annotation.Test;

public class TestClassOne {

    @Before
    void beforeTest() {
        System.out.println("Before");
    }

    @After
    void afterTest() {
        System.out.println("After");
    }

    @Test(priority = 1)
    void testOne() {
        System.out.println("test 1");
    }

    @Test(priority = 2)
    void testTwo() {
        System.out.println("test 2");
    }

    @Test(priority = 3)
    void testThree() {
        throw new RuntimeException("oops");
    }

    @Test(priority = 4)
    void testFour() {
        System.out.println("test 4");
    }
}
