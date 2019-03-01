package com.a50647.hotfixdemo;

/**
 * 测试类(bug版,本应相加实际操作的是相减)
 *
 * @author wm
 * @date 2019/2/28
 */
public class Test {
    public int add(int a, int b) {
        return a - b;
    }
}
