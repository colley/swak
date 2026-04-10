package com.swak.core.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

/**
 * SwakUserDetails.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public interface SwakUserDetails extends TokenJwtDetails,UserDetails {
    
}
