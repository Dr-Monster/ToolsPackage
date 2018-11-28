package com.zbjk.crc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class StringUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
	
	public static boolean isEmpty(String str){
		
		return null == str || 0 == str.length();
 		
	}
	
	public static boolean isNotEmpty(String str){
		
		return !isEmpty(str);
 		
	}
	
	public static void main(String[] args) throws Exception{
		isIdentity("321183199005104451");
	}

	public static boolean isMobile(String mobile){
		if(isEmpty(mobile)){
			return false;
		}else if(mobile.matches("^(13|14|15|18|17)[0-9]{9}$")){
			return true;
		}else{
			return false;
		}
	}
	
	//is right identity card number
	public static boolean isIdentity(String num){
		num = num.toUpperCase();
		//身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X。  
		if (!num.matches("(^\\d{15}$)|(^\\d{17}([0-9]|X)$)")){
			return false;
		}
		
		//下面分别分析出生日期和校验位
		int len;
		len = num.length();
		try {
			if (len == 15){
				//检查生日日期是否正确
				Date dtmBirth = new SimpleDateFormat("yyMMdd").parse(num.substring(6,12));
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dtmBirth);
				int year = dtmBirth.getYear();
				int month = calendar.get(Calendar.MONTH) + 1;
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				if(!(Integer.valueOf(num.substring(6,8)) == year && Integer.valueOf(num.substring(8,10)) == month && Integer.valueOf(num.substring(10,12)) == day)){
					return false;
				}
				return true;
			}
			if (len == 18){
				//检查生日日期是否正确
				Date dtmBirth = new SimpleDateFormat("yyyyMMdd").parse(num.substring(6,14));
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dtmBirth);
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				if(!(Integer.valueOf(num.substring(6,10)) == year && Integer.valueOf(num.substring(10,12)) == month && Integer.valueOf(num.substring(12,14)) == day)){
					return false;
				}else {
					//检验18位身份证的校验码是否正确。
					//校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
					String valnum;
					int[] arrInt = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
					String[] arrCh = new String[]{"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
					int nTemp = 0;
					for(int i = 0; i < 17; i ++) {
						nTemp += Integer.valueOf(num.substring(i, i+1)) * arrInt[i];
					}
					valnum = arrCh[nTemp % 11];
					if (!valnum.equals(num.substring(17, 18))) {
						return false;
					}
					return true;
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
}
