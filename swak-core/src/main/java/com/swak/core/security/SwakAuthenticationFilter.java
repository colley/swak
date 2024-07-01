package com.swak.core.security;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SwakAuthenticationFilter
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/13 10:24
 **/
public interface SwakAuthenticationFilter {

     /**
      *  doFilter
      * @param request
      * @param response
      * @throws ServletException
      * @throws IOException
      */
     void doFilter(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException;
}
