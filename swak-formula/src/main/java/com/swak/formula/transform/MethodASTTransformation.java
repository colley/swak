package com.swak.formula.transform;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.Arrays;
import java.util.Objects;

/**
 * MethodASTTransformation.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MethodASTTransformation implements ASTTransformation {

    @Override
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        // 你可能需要遍历所有的 MethodCallExpression 节点来查找 Double.parseDouble 的调用
        MethodCallVisitor methodCallVisitor = new MethodCallVisitor(sourceUnit);
        sourceUnit.getAST().visit(methodCallVisitor);
    }
    private static class MethodCallVisitor extends CodeVisitorSupport {
        private ProcessingUnit processingUnit;
        public MethodCallVisitor(SourceUnit sourceUnit){
            this.processingUnit = sourceUnit;
        }
        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            String methodAsString = call.getMethodAsString();
            if(StringUtils.isNotEmpty(methodAsString)){
                if(Objects.equals("Abs",methodAsString)){
                    throw new CompilationFailedException(CompilePhase.SEMANTIC_ANALYSIS.getPhaseNumber(),processingUnit);
                }
            }
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {
            super.visitBlockStatement(block);
        }

        @Override
        public void visitExpressionStatement(ExpressionStatement statement) {
            super.visitExpressionStatement(statement);
        }
    }

}
