package com.lesofn.archforge.common.profile;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-14 20:58
 */
public enum Env {
    dev,
    test,
    prod;

    /**
     * Null-safe enum lookup — {@code Enum.valueOf} throws on unknown names, so a
     * {@code valueOf(name) != null} guard never worked as intended (found by Error Prone's
     * ImpossibleNullComparison).
     */
    public static Env fromName(String name) {
        if (name == null) {
            return null;
        }
        for (Env e : values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }
}
