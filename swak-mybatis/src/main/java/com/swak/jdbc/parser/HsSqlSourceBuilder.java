package com.swak.jdbc.parser;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

public class HsSqlSourceBuilder {

    public HsSqlSource parse(String originalSql) {
        SwakTokenHandler handler = new SwakTokenHandler();
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql = parser.parse(removeExtraWhitespaces(originalSql));
        return new SwakStaticSqlSource(sql, handler.getParameterMappings());
    }

    public HsSqlSource parseProviderSql(String originalSql) {
        ProviderTokenHandler handler = new ProviderTokenHandler();
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql = parser.parse(removeExtraWhitespaces(originalSql));
        return new SwakProviderSqlSource(sql, handler.getParameterMappings());
    }

    public static String removeExtraWhitespaces(String original) {
        StringTokenizer tokenizer = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        boolean hasMoreTokens = tokenizer.hasMoreTokens();
        while (hasMoreTokens) {
            builder.append(tokenizer.nextToken());
            hasMoreTokens = tokenizer.hasMoreTokens();
            if (hasMoreTokens) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    private static class SwakTokenHandler implements TokenHandler {
        private final List<ParameterMapping> parameterMappings = new ArrayList<>();
        private AtomicInteger atomicIndex = new AtomicInteger(0);

        @Override
        public String handleToken(String content) {
            ParameterMapping parameterMapping = buildParameterMapping(content);
            parameterMappings.add(parameterMapping);
            return "?";
        }

        private ParameterMapping buildParameterMapping(String content) {
            return new ParameterMapping(content, atomicIndex.getAndIncrement());
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }
    }

    private static class ProviderTokenHandler implements TokenHandler {
        private final List<ParameterMapping> parameterMappings = new ArrayList<>();
        private final AtomicInteger atomicIndex = new AtomicInteger(0);

        @Override
        public String handleToken(String content) {
            ParameterMapping parameterMapping = buildParameterMapping(content);
            parameterMappings.add(parameterMapping);
            return "{" + parameterMapping.getIndex() + "}";
        }

        private ParameterMapping buildParameterMapping(String content) {
            return new ParameterMapping(content, atomicIndex.getAndIncrement());
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }
    }
}
