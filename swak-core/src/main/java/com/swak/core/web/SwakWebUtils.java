package com.swak.core.web;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class SwakWebUtils {

    public static Object getRequestAttribute(HttpServletRequest request, String name) {
        Assert.notNull(request, "Request must not be null");
        return request.getAttribute(name);
    }

    public static Object getAttribute(HttpServletRequest request, String name) {
        Object value = getRequestAttribute(request, name);
        if (Objects.isNull(value)) {
            return getSessionAttribute(request, name);
        }
        return value;
    }

    public static void setAttribute(HttpServletRequest request, String name, @Nullable Object value) {
        setRequestAttribute(request, name, value);
        setSessionAttribute(request, name, value);
    }

    public static void setRequestAttribute(HttpServletRequest request, String name, @Nullable Object value) {
        Assert.notNull(request, "Request must not be null");
        if (value != null) {
            request.setAttribute(name, value);
        } else {
            request.removeAttribute(name);
        }
    }

    public static Object getSessionAttribute(HttpServletRequest request, String name) {
        Assert.notNull(request, "Request must not be null");
        HttpSession session = request.getSession(false);
        return (session != null ? session.getAttribute(name) : null);
    }

    public static void setSessionAttribute(HttpServletRequest request, String name, @Nullable Object value) {
        Assert.notNull(request, "Request must not be null");
        if (value != null) {
            request.getSession().setAttribute(name, value);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(name);
            }
        }
    }

}
