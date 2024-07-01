
package com.swak.jdbc.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParameterMapping {
    private String propertyName;
    private int index;
}
