package com.zbjk.general.nrc;

import org.junit.Test;

import java.io.*;

/**
 * @author: LuHaoran
 * @Date: 18-12-17 16:38
 * @Description:
 */
public class Trans {
    @Test
    public void test(){
        File file = new File("C:\\Users\\Administrator\\Desktop\\new 1.txt");
        Long filelength = file.length(); // 获取文件长度
        byte[] filecontent = new byte[filelength.intValue()];
        try
        {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String[] fileContentArr = new String(filecontent).split("\r\n");
        for(String item : fileContentArr){
            String[] strs = item.split(",");
            int code = Integer.valueOf(strs[0]);
            String name = strs[1];
            if(name.startsWith("1")){
                name.replace("1" , "一");
            }else if(name.startsWith("3")){
                name.replace("3" , "三");
            }else if(name.startsWith("7")){
                name.replace("7" , "七");
            }else{

            }
            String type = strs[2];
            Boolean turn = Boolean.valueOf(strs[3]);

            String joint = name + "(" + code + "," +
                    "\"" + name + "\"" + "," +
                    "\"" + type + "\"" + "," +
                    turn + ")," + "\n";
            writeFileContent("C:\\Users\\Administrator\\Desktop\\new_2.txt" , joint);
        }
        System.out.println("AAA");
    }

    public boolean writeFileContent(String filepath,String newstr){
        Boolean bool = false;
        String filein = newstr+"\r\n";//新写入的行，换行
        String temp  = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos  = null;
        PrintWriter pw = null;
        try {
            File file = new File(filepath);//文件路径(包括文件名称)
            //将文件读入输入流
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();

            //文件原有内容
            for(int i=0;(temp =br.readLine())!=null;i++){
                buffer.append(temp);
                // 行与行之间的分隔符 相当于“\n”
                buffer = buffer.append(System.getProperty("line.separator"));
            }
            buffer.append(filein);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally {
            try{
                if (pw != null) {
                    pw.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        return bool;
    }
}
