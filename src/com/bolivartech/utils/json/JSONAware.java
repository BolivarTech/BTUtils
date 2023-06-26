package com.bolivartech.utils.json;

/**
 * Beans that support customized output of JSON text shall implement this interface.  
 * 
 * @author FangYidong.
 */
public interface JSONAware {
	/**
	 * @return JSON text
	 */
	String toJSONString();
}
