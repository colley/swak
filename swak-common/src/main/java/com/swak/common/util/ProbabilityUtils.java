
package com.swak.common.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机概率生产
 * @author colley.ma
 * @date 2022/07/13 16:33:59
 *
 */
@Slf4j
public class ProbabilityUtils {

	/**
	 * 根据传入的概率，判断当前这次是否命中 该方法是基于随机数的
	 * 
	 * @param probability
	 * @return
	 */
	private final static Random random = ThreadLocalRandom.current();

	public final static int MAX_RANDOM_POOL = 10000;

	/**
	 * 判断概率是否命中
	 * 
	 * @param @param  probability
	 * @param @param  maxPool
	 * @param @return
	 * @return boolean
	 */
	public static boolean hit(double probability, int maxBoundPool) {
		if (probability <= 0d) {
			return false;
		}
		if (probability >= 1d) {
			return true;
		}
		int a = BigDecimal.valueOf(probability).multiply(new BigDecimal(maxBoundPool)).intValue();
		int rnd = random.nextInt(maxBoundPool);
		log.info("probability:{}, num:{},randomNum:{}", probability, a, rnd);
		if (rnd <= a) {
			return true;
		}
		return false;
	}

	public static boolean hit(double probability) {
		return hit(probability, MAX_RANDOM_POOL);
	}

	public static <T extends Hit> T hit(List<T> hitlist, int rnd) {
		if (hitlist == null || hitlist.size() < 1) {
			return null;
		}

		if (hitlist.size() < 2) {
			return hitlist.get(0);
		}
		int length = hitlist.size(); // 总个数
		int totalWeight = 0; // 总权重
		boolean sameWeight = true; // 权重是否都一样
		for (int i = 0; i < length; i++) {
			int weight = hitlist.get(i).getWeight();
			totalWeight += weight; // 累计总权重
			if (sameWeight && i > 0 && weight != hitlist.get(i - 1).getWeight()) {
				sameWeight = false; // 计算所有权重是否一样
			}
		}
		if (totalWeight > 0 && !sameWeight) {
			// 如果权重不相同且权重大于0则按总权重数随机
			int offset = rnd;
			// 并确定随机值落在哪个片断上
			for (int i = 0; i < length; i++) {
				offset -= hitlist.get(i).getWeight();
				if (offset < 0) {
					T hitEle = hitlist.get(i);
					hitEle.setHitNum(rnd);
					return hitEle;
				}
			}
		}
		// 如果权重相同或权重为0则均等随机
		return hitlist.get(random.nextInt(length));
	}

	public static interface Hit {
		int getWeight();

		void setHitNum(int hit);
	}

	public static class RandomEle<T> implements Hit {
		private T hitEle;
		private int weight;
		private int rnd;

		public RandomEle(T hitEle, int weight) {
			this.hitEle = hitEle;
			this.weight = weight;
		}

		@Override
		public int getWeight() {
			return this.weight;
		}

		@Override
		public void setHitNum(int hit) {
			this.rnd = hit;
		}

		public T getHitEle() {
			return hitEle;
		}

		public int getRnd() {
			return rnd;
		}
	}
}
