package com.swak.security.converter;

import com.swak.core.security.DefaultTokenJwtDetails;
import com.swak.core.security.SwakUserDetails;
import com.swak.core.security.TokenJwtDetails;
import com.swak.security.dto.SwakUserDetailsImpl;
import org.mapstruct.Mapper;

/**
 * UserDetailsConverter.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Mapper(componentModel = "spring")
public interface UserDetailsConverter {

     DefaultTokenJwtDetails toTokenJwtDetails(SwakUserDetails swakUserDetails);

     SwakUserDetailsImpl toUserDetails(TokenJwtDetails tokenJwtDetails);
}
