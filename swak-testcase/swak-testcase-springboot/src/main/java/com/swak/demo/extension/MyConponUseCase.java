package com.swak.demo.extension;

import java.util.Objects;

/**
 * 我的优惠券扩展
 *
 * @ClassName: MyConponUseCase.java
 * @author: colley.matest
 * @date: 2022/02/14
 */
public enum MyConponUseCase {

    FESTIVAL_CASE("main", "主会场"), BUY_REBATE("rebate", "买返券"), MY("my", "我的专属");

    private final String useCase;
    private final String desc;

    MyConponUseCase(String useCase, String desc) {
        this.desc = desc;
        this.useCase = useCase;
    }

    public static MyConponUseCase getuseCase(String useCase) {
        for (MyConponUseCase useCases : MyConponUseCase.values()) {
            if (Objects.equals(useCases.useCase, useCase)) {
                return useCases;
            }
        }
        return null;
    }

    public String getUseCase() {
        return useCase;
    }

    public String getDesc() {
        return desc;
    }

    public boolean eq(String useCase) {
        return Objects.equals(useCase, this.getUseCase());
    }
}
