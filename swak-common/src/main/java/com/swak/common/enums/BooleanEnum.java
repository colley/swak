package com.swak.common.enums;

import java.util.Objects;

public enum BooleanEnum {
    //
    YES {
        @Override
        public Integer get() {
            return 1;
        }
    },
    NO {
        @Override
        public Integer get() {
            return 0;
        }
    };

    public static boolean yes(Integer type) {
        return Objects.equals(BooleanEnum.YES.get(), type);
    }

    public static boolean yes(String type) {
        return Objects.equals(BooleanEnum.YES.get().toString(), type);
    }

    public abstract Integer get();

    public boolean eq(String type) {
        return Objects.equals(get().toString(), type);
    }

    public boolean eq(Integer type) {
        return Objects.equals(get(), type);
    }

    public String getString() {
        return get().toString();
    }
}
