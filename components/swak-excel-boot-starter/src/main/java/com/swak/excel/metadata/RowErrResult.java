package com.swak.excel.metadata;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * RowErrResult.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
@Data
public class RowErrResult implements java.io.Serializable {
	private static final long serialVersionUID = -1541341814763537940L;
	private List<String> succRow = new ArrayList<String>();
	private List<String> failRow = new ArrayList<String>();

}
