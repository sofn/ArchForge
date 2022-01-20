package com.juneix.frame.context;

import org.junit.jupiter.api.Test;

/**
 * @author sofn
 * @version 1.0 Created at: 2015-07-08 15:21
 */
public class RequextContextTest {

    public static class MockContext {
        private long id;
        private String name;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MockContext context = (MockContext) o;

            return id == context.id;

        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    @Test
    public void testOneObject() {
        MockContext context1 = new MockContext();
        System.out.println(context1);
        context1.id = 1;
        context1.name = "context1";
        MockContext context2 = new MockContext();
        System.out.println(context2);
        context2.id = 1;
        context2.name = "context2";

        System.out.println(context1);
        System.out.println(context2);
        System.out.println(context1 == context2);
        System.out.println(context1.equals(context2));
        System.out.println(context1.name);
        System.out.println(context2.name);
    }
}
