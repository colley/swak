package com.swak.formula.transform;

import com.swak.common.util.GetterUtil;
import com.swak.formula.ExpressionRunner;
import com.swak.formula.entity.FormulaMetaMethod;
import com.swak.formula.executor.CompileComposite;
import com.swak.formula.reflect.FormulaPluginRegistry;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.Objects;

public class MethodCallCustomizer extends CompilationCustomizer {

    private CompileComposite compileComposite;
    public MethodCallCustomizer(ExpressionRunner expressionRunner) {
        super(CompilePhase.SEMANTIC_ANALYSIS);
        this.compileComposite = new CompileComposite(expressionRunner);
    }

    @Override
    public void call(SourceUnit sourceUnit, GeneratorContext generatorContext, ClassNode classNode) throws CompilationFailedException {
        MethodCallVisitor methodCallVisitor = new MethodCallVisitor(sourceUnit);
        classNode.getMethods().forEach(method -> {
            method.getCode().visit(methodCallVisitor);
        });
    }

    protected class MethodCallVisitor extends CodeVisitorSupport {
        private SourceUnit sourceUnit;

        public MethodCallVisitor(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            String functionName = call.getMethodAsString();
            if(Character.isUpperCase(functionName.charAt(0))){
                FormulaMetaMethod formulaMetaMethod = FormulaPluginRegistry.getInstance().getCustomFormula(GetterUtil.firstToLowerCase(functionName));
                if (Objects.isNull(formulaMetaMethod)) {
                    sourceUnit.getErrorCollector().addError(Message.create(new SyntaxException(functionName+" Formula not found", call), sourceUnit));
                    return;
                }
                Expression arguments = call.getArguments();
            }
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {
            System.out.println(block.getText());
            super.visitBlockStatement(block);
        }

        @Override
        public void visitExpressionStatement(ExpressionStatement statement) {
            if(Objects.nonNull(statement.getExpression())){
                System.out.println(statement.getExpression().getText());
            }
            super.visitExpressionStatement(statement);
        }
    }


}