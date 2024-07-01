package com.swak.common.dto.base;

import org.apache.commons.lang3.StringUtils;

/**
 * BizScenario（业务场景）= bizId + useCase + scenario, which can uniquely identify a
 * user scenario.
 */
public class BizScenario implements DTO {

	private static final long serialVersionUID = 844224702825061778L;
	public final static String DEFAULT_BIZ_ID = "#bizId#";
	public final static String DEFAULT_USE_CASE = "#useCase#";
	public final static String DEFAULT_SCENARIO = "#scenario#";
	private final static String DOT_SEPARATOR = ".";

	/**
	 * bizId is used to identify a business,
	 * is only one biz
	 */
	private String bizId = DEFAULT_BIZ_ID;

	/**
	 * useCase is used to identify a use case
	 */
	private String useCase = DEFAULT_USE_CASE;

	/**
	 * scenario is used to identify a use case, such as "88vip","normal", can not be
	 * null
	 */
	private String scenario = DEFAULT_SCENARIO;

	/**
	 * For above case, the BizScenario will be "swak.placeOrder.88vip", with this
	 * code, we can provide extension processing other than
	 * "tmall.placeOrder.normal" scenario.
	 *
	 */
	public String getUniqueIdentity() {
		return bizId + DOT_SEPARATOR + useCase + DOT_SEPARATOR + scenario;
	}

	public static BizScenario valueOf(String bizId, String useCase, String scenario) {
		BizScenario bizScenario = new BizScenario();
		bizScenario.bizId =bizId ;
		bizScenario.useCase = StringUtils.firstNonEmpty(useCase,DEFAULT_USE_CASE);
		bizScenario.scenario = StringUtils.firstNonEmpty(scenario,DEFAULT_SCENARIO);
		return bizScenario;
	}

	public static BizScenario valueOf(String bizId, String useCase) {
		return BizScenario.valueOf(bizId, useCase, DEFAULT_SCENARIO);
	}

	public static BizScenario valueOf(String bizId) {
		return BizScenario.valueOf(bizId, DEFAULT_USE_CASE, DEFAULT_SCENARIO);
	}

	public static BizScenario newDefault() {
		return BizScenario.valueOf(DEFAULT_BIZ_ID, DEFAULT_USE_CASE, DEFAULT_SCENARIO);
	}

	public String getIdentityWithDefaultScenario() {
		return bizId + DOT_SEPARATOR + useCase + DOT_SEPARATOR + DEFAULT_SCENARIO;
	}

	public String getIdentityWithDefaultUseCase() {
		return bizId + DOT_SEPARATOR + DEFAULT_USE_CASE + DOT_SEPARATOR + DEFAULT_SCENARIO;
	}

	public String getDefault() {
		return DEFAULT_BIZ_ID + DOT_SEPARATOR + DEFAULT_USE_CASE + DOT_SEPARATOR + DEFAULT_SCENARIO;
	}
}
