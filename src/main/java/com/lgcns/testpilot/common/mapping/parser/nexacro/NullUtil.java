/**
 * @Project : 蹂묓뻾寃�利� �넄猷⑥뀡(Simulator Module)
 * @Class : NullUtil.java
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NullUtil {

	private NullUtil() {
	}
	/**
	 * �엯�젰媛믪씠 �꼸�씤吏� �뿬遺�瑜� 寃��궗�븳�떎. �떒 湲곕낯�� ""怨� null�쓣 紐⑤몢 true濡� 由ы꽩�븳�떎. 紐⑤뱺 DB Access硫붿냼�뱶�뒗 null泥댄겕媛�
	 * �븘�슂�븳 寃쎌슦�뿉 �씠寃껋쓣 �씠�슜�븳�떎.
	 *
	 * @param value
	 * @return boolean
	 * @throws FrmException
	 */
	public static boolean isNull(String value) {
		return value == null || "".equals(value);
	}

	/**
	 * �엯�젰媛믪씠 �꼸�씤吏� �뿬遺�瑜� 寃��궗�븳�떎. �떒 湲곕낯�� ""怨� null�쓣 紐⑤몢 true濡� 由ы꽩�븳�떎. 紐⑤뱺 DB Access硫붿냼�뱶�뒗 null泥댄겕媛�
	 * �븘�슂�븳 寃쎌슦�뿉 �씠寃껋쓣 �씠�슜�븳�떎.
	 *
	 * @param String[] value
	 * @return boolean
	 */

	public static boolean isNull(String[] value) {
		if ((value == null) || (value.length < 1)) {
			return true;
		}

		for (int i = 0; i < value.length; i++) {
			if (isNull(value[i])) {
				return true;
			}
		}

		return false;
	}

	/**
	 * �엯�젰媛믪씠 �꼸�씤吏� �뿬遺�瑜� 寃��궗�븳�떎. �떒 湲곕낯�� ""怨� null�쓣 紐⑤몢 true濡� 由ы꽩�븳�떎. 紐⑤뱺 DB Access硫붿냼�뱶�뒗 null泥댄겕媛�
	 * �븘�슂�븳 寃쎌슦�뿉 �씠寃껋쓣 �씠�슜�븳�떎.
	 *
	 * @param value
	 * @return boolean
	 */
	public static boolean isNull(Object value) {
		return value == null;
	}

	/**
	 * �엯�젰媛믪씠 �꼸�씤吏� �뿬遺�瑜� 寃��궗�븳�떎. �떒 湲곕낯�� ""怨� null�쓣 紐⑤몢 true濡� 由ы꽩�븳�떎. 紐⑤뱺 DB Access硫붿냼�뱶�뒗 null泥댄겕媛�
	 * �븘�슂�븳 寃쎌슦�뿉 �씠寃껋쓣 �씠�슜�븳�떎.
	 *
	 * @param List<?> value
	 * @return boolean
	 */
	public static boolean isNull(List<?> value) {
		if ((value == null) || (value.isEmpty())) {
			return true;
		}

		final int size = value.size();
		for (int i = 0; i < size; i++) {
			/** if (isNull((String) value.get(i))) { syc.mod **/
			if (isNull((Object) value.get(i))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param value
	 * @return boolean
	 */
	public static boolean isNone(String value) {
		return value == null || value.length() == 0;
	}

	public static boolean isNone(byte[] value) {
		return value == null || value.length == 0;
	}

	/**
	 * @param value
	 * @return boolean
	 */
	public static boolean isNone(Number value) {
		return value == null || value.doubleValue() == 0;
	}

	/**
	 * @param value
	 * @return boolean
	 */
	public static boolean isNone(List<?> value) {
		return value == null || value.isEmpty();
	}

	/**
	 * @param value
	 * @return boolean
	 */
	public static boolean isNone(Object[] value) {
		return value == null || value.length == 0;
	}

	/**
	 * @param       <K>
	 * @param value
	 * @return boolean
	 */
	public static <K, V> boolean isNone(Map<K, V> value) {
		return value == null || value.size() == 0;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static boolean notNone(String value) {
		return (value != null) && (value.length() > 0);
	}

	/**
	 * 
	 * @param originalStr
	 * @param defaultStr
	 * @return
	 */
	public static String nvl(String originalStr, String defaultStr) {
		if ((originalStr == null) || (originalStr.length() < 1)) {
			return defaultStr;
		}
		return originalStr;
	}

	/**
	 * 
	 * @param object
	 * @param defaultValue
	 * @return
	 */
	public static String nvl(Object object, String defaultValue) {
		if (object == null) {
			return defaultValue;
		}
		return nvl(object.toString(), defaultValue);
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static String print(Object o) {
		if (o == null) {
			return "";
		}
		return o.toString();
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, Object> nullToEmptyString(Map<String, Object> map) {
		if (map != null) {
			Set set = map.entrySet();
			Iterator it = set.iterator();

			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();
				map.put((String) e.getKey(), e.getValue() == null ? "" : e.getValue());
			}
		}

		return map;
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<Map<String, Object>> nullToEmptyString(List<Map<String, Object>> list) {
		if (list != null) {
			for (Map<String, Object> map : list) {
				map = nullToEmptyString(map);
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static Object nullToEmptyString(Object obj) {
		return obj == null ? "" : obj;
	}
}