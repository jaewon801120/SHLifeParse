/**
 * @Project : 蹂묓뻾寃�利� �넄猷⑥뀡(Simulator Module)
 * @Class : BizException.java
 * @Description : 
 * @Author : 69800
 * @Since : 2019. 11. 1.
 * @Copyright �뱬 LG CNS
 *-------------------------------------------------------
 * Modification Information
 *-------------------------------------------------------
 * Date            Modifier             Reason 
 *-------------------------------------------------------
 * 2019. 11. 1.         69800             initial
 *-------------------------------------------------------
 */ 

package com.lgcns.testpilot.common.mapping.parser.nexacro;

public class UtilException extends BaseException {

	private static final long serialVersionUID = 1L;

	public UtilException(String message) {
		super(message);
	}
	
	public UtilException(String code, String message) {
		super(code, message);
	}

	public UtilException(String code, Object[] param) {
		super(code, param);
	}
	
	public UtilException(String code, String message, Object[] param) {
		super(code, message, param);
	}

	public UtilException(Throwable cause) {
		super(cause);
	}

	public UtilException(String message, Throwable cause) {
		super(message, cause);
	}

	public String toString() {
		String s = getClass().getName();
		String message = super.getMessage();
		String code = super.getCode();
		StringBuilder stringBuilder = new StringBuilder(s);
		stringBuilder.append(message == null ? "" : (new StringBuilder()).append(": ").append(message).toString());
		stringBuilder.append(code == null || "".equals(code) ? " " : (new StringBuilder()).append("(").append(code).append(")").toString());
		return stringBuilder.toString();
	}

}
