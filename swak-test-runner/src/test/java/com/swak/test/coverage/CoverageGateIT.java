package com.swak.test.coverage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 聚合覆盖率门禁。
 * <p>
 * JaCoCo 的 {@code check} goal 只能检查当前模块的 classes，无法对
 * {@code report-aggregate} 的结果做断言，所以这里直接读聚合报告里的 CSV 自己判定。
 * <p>
 * 用 failsafe 执行（类名以 {@code IT} 结尾），由 {@code -Pcoverage-gate} 激活：
 * <pre>
 * ./mvnw verify -Pcoverage-gate
 * ./mvnw verify -Pcoverage-gate -Dcoverage.minimum=0.30   # 阶段性目标
 * </pre>
 * 报告由 {@code jacoco:report-aggregate} 在 {@code prepare-package} 阶段生成，
 * 因此本用例一定读到的是本次构建的数据。
 */
class CoverageGateIT {

    private static final Path REPORT = Path.of("target", "site", "jacoco-aggregate", "jacoco.csv");

    private static final int INSTRUCTION_MISSED = 3;
    private static final int INSTRUCTION_COVERED = 4;

    @Test
    @DisplayName("聚合后的指令覆盖率不低于阈值")
    void aggregatedInstructionCoverageMeetsThreshold() throws IOException {
        assertThat(Files.exists(REPORT))
                .as("聚合报告不存在，先执行 mvn verify 生成 %s", REPORT)
                .isTrue();

        double minimum = Double.parseDouble(System.getProperty("coverage.minimum", "0.85"));

        long missed = 0;
        long covered = 0;
        Map<String, long[]> byModule = new TreeMap<>();

        List<String> lines = Files.readAllLines(REPORT);
        for (String line : lines.subList(1, lines.size())) {
            String[] cells = line.split(",");
            long m = Long.parseLong(cells[INSTRUCTION_MISSED]);
            long c = Long.parseLong(cells[INSTRUCTION_COVERED]);
            missed += m;
            covered += c;

            // GROUP 形如 swak-test-runner/swak-core，取模块名部分
            String group = cells[0];
            String module = group.substring(group.lastIndexOf('/') + 1);
            long[] acc = byModule.computeIfAbsent(module, key -> new long[2]);
            acc[0] += m;
            acc[1] += c;
        }

        long total = missed + covered;
        double ratio = total == 0 ? 0 : (double) covered / total;

        StringBuilder detail = new StringBuilder("各模块指令覆盖率：\n");
        byModule.forEach((module, acc) -> {
            long moduleTotal = acc[0] + acc[1];
            double moduleRatio = moduleTotal == 0 ? 0 : (double) acc[1] / moduleTotal;
            detail.append(String.format("  %-42s %6.1f%%  (%d/%d)%n",
                    module, moduleRatio * 100, acc[1], moduleTotal));
        });

        assertThat(ratio)
                .as("聚合指令覆盖率 %s，阈值 %.0f%%%n%s", String.format("%.1f%%", ratio * 100),
                        minimum * 100, detail)
                .isGreaterThanOrEqualTo(minimum);
    }
}
