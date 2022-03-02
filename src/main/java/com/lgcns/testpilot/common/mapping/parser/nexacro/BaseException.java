/**
 * @Project : 蹂묓뻾寃�利� �넄猷⑥뀡(Simulator Module)
 * @Class : BaseException.java
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

import java.io.PrintWriter;
import java.io.StringWriter;


public class BaseException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private String message;
	private String code;
	private Object[] param;
	
	public BaseException() {
		super();
		message = "";
		code = "";
		param = null;
	}

	public BaseException(String message) {
		super(message);
		this.code = "";
		this.param = null;
		this.message = message;
	}

	public BaseException(String code, Object[] param) {
		super();
		this.message="";
		this.code = code;
		this.param = param;
	}
	
	public BaseException(Throwable cause) {
		super(cause);
		this.message = "";
		this.code = "";
		this.param = null;
	}
	
	public BaseException(String code, String message) {
		super(message);
		this.message = message;
		this.code = code;
		this.param = null;
	}

	public BaseException(String code, String message, Object[] param) {
		super(message);
		this.message = message;
		this.code = code;
		this.param = param;
	}
	
	
	public BaseException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
		this.code = "";
		this.param = null;
	}

	public String getMessage() {
		return message;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	protected void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public Object[] getParam() {
		return param;
	}
	
	public void setParam(Object[] param) {
		this.param = param;
	}

	public String getStackTraceString() {
		StringWriter s = new StringWriter();
		super.printStackTrace(new PrintWriter(s));
		return s.toString();
	}

	public void printStackTrace(PrintWriter log) {
		log.println(getStackTraceString());
	}

}
