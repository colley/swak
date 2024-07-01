package com.swak.common.util;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShuffleCollector {
	public static <T> Collector<T, ?, Stream<T>> shuffle() {
		return shuffle(ThreadLocalRandom::current);
	}

	public static <T> Collector<T, ?, Stream<T>> shuffle(Supplier<? extends Random> random) {
		return Collectors.collectingAndThen(Collectors.toList(), ts -> {
			Collections.shuffle(ts, random.get());
			return ts.stream();
		});
	}
}
