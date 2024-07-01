package com.swak.core.command;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import com.google.common.graph.Traverser;
import org.apache.commons.lang3.Validate;


/**
 * @author colley
 */
public class SwakTypeHelper {
    private SwakTypeHelper() {}

    public static boolean isGenericReturnType(Method method) {
        return isParametrizedType(method.getGenericReturnType())
            || isTypeVariable(method.getGenericReturnType());
    }

    /**
     * Check whether return type of the given method is parametrized or not.
     *
     * @param method the method
     * @return true - if return type is {@link ParameterizedType}, otherwise - false
     */
    public static boolean isReturnTypeParametrized(Method method) {
        return isParametrizedType(method.getGenericReturnType());
    }

    public static boolean isParametrizedType(Type t) {
        return t instanceof ParameterizedType;
    }

    public static boolean isTypeVariable(Type t) {
        return t instanceof TypeVariable;
    }

    public static boolean isWildcardType(Type t) {
        return t instanceof WildcardType;
    }

    /**
     * Unwinds parametrized type into plain list that contains all parameters for the given type
     * including nested
     * parameterized types, for example calling the method for the following type <code>
     * GType<GType<GDoubleType<GType<GDoubleType<Parent, Parent>>, Parent>>>
     * </code> will return list of 8 elements: <code>
     * [GType, GType, GDoubleType, GType, GDoubleType, Parent, Parent, Parent]
     * </code> if the given type is not parametrized then returns list with one element which is
     * given type passed into
     * method.
     *
     * @param type the parameterized type
     * @return list of {@link Type}
     */
    @ParametersAreNonnullByDefault
    public static List<Type> flattenTypeVariables(Type type) {
        Validate.notNull(type, "type cannot be null");
        List<Type> types = new ArrayList<>();
        Traverser<Type> typeTraverser = Traverser.forTree(root -> {
            if (root instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) root;
                return Arrays.asList(pType.getActualTypeArguments());
            } else if (root instanceof TypeVariable) {
                TypeVariable pType = (TypeVariable) root;
                return Arrays.asList(pType.getBounds());
            }
            return Collections.emptyList();
        });
        for (Type t : typeTraverser.breadthFirst(type)) {
            types.add(t);
        }

//        TreeTraverser<Type> typeTraverser = new TreeTraverser<Type>() {
//            @Override
//            public Iterable<Type> children(Type root) {
//                if (root instanceof ParameterizedType) {
//                    ParameterizedType pType = (ParameterizedType) root;
//                    return Arrays.asList(pType.getActualTypeArguments());
//                } else if (root instanceof TypeVariable) {
//                    @SuppressWarnings("rawtypes")
//                    TypeVariable pType = (TypeVariable) root;
//                    return Arrays.asList(pType.getBounds());
//                }
//                return Collections.emptyList();
//            }
//        };
//        for (Type t : typeTraverser.breadthFirstTraversal(type)) {
//            types.add(t);
//        }
        return types;
    }

    /**
     * Unwinds parametrized type into plain list that contains all parameters for the given type
     * including nested
     * parameterized types, for example calling the method for the following type <code>
     * GType<GType<GDoubleType<GType<GDoubleType<Parent, Parent>>, Parent>>>
     * </code> will return list of 8 elements: <code>
     * [GType, GType, GDoubleType, GType, GDoubleType, Parent, Parent, Parent]
     * </code> if the given type is not parametrized then returns list with one element which is
     * given type passed into
     * method.
     *
     * @param type the parameterized type
     * @return list of {@link Type}
     */
    @ParametersAreNonnullByDefault
    public static List<Type> getAllParameterizedTypes(Type type) {
        Validate.notNull(type, "type cannot be null");
        List<Type> types = new ArrayList<Type>();

        Traverser<Type> typeTraverser = Traverser.forTree(root -> {
            if (root instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) root;
                return Arrays.asList(pType.getActualTypeArguments());
            }
            return Collections.emptyList();
        });

        for (Type t : typeTraverser.breadthFirst(type)) {
            types.add(t);
        }
//        TreeTraverser<Type> typeTraverser = new TreeTraverser<Type>() {
//            @Override
//            public Iterable<Type> children(Type root) {
//                if (root instanceof ParameterizedType) {
//                    ParameterizedType pType = (ParameterizedType) root;
//                    return Arrays.asList(pType.getActualTypeArguments());
//
//                }
//                return Collections.emptyList();
//            }
//        };
//        for (Type t : typeTraverser.breadthFirstTraversal(type)) {
//            types.add(t);
//        }
        return types;
    }
}
