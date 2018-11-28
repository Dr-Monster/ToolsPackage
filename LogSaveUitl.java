package com.zbjk.crc.utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * @author Luhaoran
 * log save to local disk
 */
public class LogSaveUitl {

    public static String getCurrentDate() {
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sm.format(new Date());
    }


    public static void appendLog(String logFileName,String newLog) {
        Scanner sc = null;
        PrintWriter pw = null;
        File log = new File("F:\\log\\" + logFileName + ".log");
        try {
            //如果文件不存在,则新建.
            if (!log.exists())
            {
                File parentDir = new File(log.getParent());
                //如果所在目录不存在,则新建.
                if (!parentDir.exists())
                {
                    parentDir.mkdirs();
                }
                log.createNewFile();
            }
            sc = new Scanner(log);
            StringBuilder sb = new StringBuilder();
            //先读出旧文件内容,并暂存sb中;
            while (sc.hasNextLine())
            {
                sb.append(sc.nextLine());
                //换行符作为间隔,扫描器读不出来,因此要自己添加.
                sb.append("\r\n");
            }
            sc.close();

            pw = new PrintWriter(new FileWriter(log), true);
            /**
             * A.
             * 写入旧文件内容.
             */

            pw.println(sb.toString());
            /**
             * B.
             * 写入新日志.
             */
            pw.println(newLog + "  [" + getCurrentDate() + "]");
            /**
             * 如果先写入A,最近日志在文件最后. 如是先写入B,最近日志在文件最前.
             */
            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
