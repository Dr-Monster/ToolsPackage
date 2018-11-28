package com.zbjk.crc.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class EncodingTool {
    public static String encodeStr(String str) {
        try {
            return new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String urlDeCode(String string){
        String trans = null ;
        try {
            trans = URLDecoder.decode(string,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return trans;
    }
}