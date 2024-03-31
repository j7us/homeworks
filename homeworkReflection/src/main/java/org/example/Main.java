package org.example;

import org.example.testclasses.*;
import org.example.testprocessors.TestProcessor;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Exception{
        TestProcessor.process(TestClassOne.class);
        TestProcessor.process(TestClassTwo.class);
        TestProcessor.process(TestClassThree.class);
        TestProcessor.process(TestClassFour.class);
        TestProcessor.process(TestClassFive.class);
    }
}