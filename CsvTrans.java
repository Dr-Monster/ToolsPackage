package com.zbjk.crc.utils;

import com.csvreader.CsvWriter;
import com.zbjk.crc.domain.rating.*;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CsvTrans {
    private static  <T> void writeCSV(Collection<T> dataset, String csvFilePath, String[] csvHeaders) {
        try {
            // 定义路径，分隔符，编码
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("UTF-8"));
            // 写表头
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            //遍历集合
            Iterator<T> it = dataset.iterator();
            while (it.hasNext()) {
                T t = (T) it.next();
                //获取类属性
                Field[] fields = t.getClass().getDeclaredFields();
                String[] csvContent=new String[fields.length];
                for (short i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    String fieldName = field.getName();
                    String getMethodName = "get"
                            + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);
                    try {
                        Class tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,new Class[] {});
                        Object value = getMethod.invoke(t, new Object[] {});
                        if (value == null) {
                            continue;
                        }
                        //取值并赋给数组
                        String textvalue=value.toString();
                        csvContent[i]=textvalue;
                        //System.out.println("fieldname="+fieldName+"||getMethodname="+getMethodName+"||textvalue="+textvalue);
                    }catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                //迭代插入记录
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("<--------CSV文件写入成功-------->");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void csvTrans(List<T> list , String startTime , String endTime){
        T t = list.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("F://");
        String[] heads = null;
        if(QUserBaseInfo.class.isInstance(t)){
            heads = new String[]{
                    "编号",
                    "用户ID","用户名称","用户状态","用户类型",
                    "证件类型","证件号码","联系人","联系人电话",
                    "联系人地址","注册时间","注册账号",
                    "注册时IP地址","账户级别","VIP标识",
                    "注册身份","注册城市","注册区县","注册详细地址"
            };
            sb.append("UserInfo" + startTime + "_" + endTime);
        }

        if(QOrderInfo.class.isInstance(t)){
            heads = new String[]{
                    "编号",
                    "订单编号","子订单编号",
                    "商品ID","商品名称","商品一级类别名称",
                    "商品二级类别名称","商品三级类别名称",
                    "买家ID","买家名称","订单状态","支付状态",
                    "订单商品单价","订单商品数量","订单商品体积",
                    "订单商品重量","订单商品总金额","订单运费",
                    "订单其他费用","订单折扣金额","订单最终结算金额",
                    "订单收货人姓名","订单收货人电话","订单收货省",
                    "订单收货市","订单收货区县","订单收货详细地址",
                    "订单发货人姓名","订单发货人电话","订单发货省",
                    "订单发货市","订单发货区县","订单发货详细地址",
                    "订单创建时间","订单完成时间","物流单编号",
                    "物流公司ID","物流公司名称","物流状态","开始配送时间",
                    "配送结束时间"
            };
            sb.append("OrderInfo" + startTime + "_" + endTime);
        }
        if(QLogisticsInfo.class.isInstance(t)){
            heads = new String[]{
                    "编号",
                    "物流单编号","订单编号","配送方式",
                    "物流状态","运费","物流起点","物流终点",
                    "物流公司ID","物流公司名称","司机姓名",
                    "司机手机号","司机身份证号","车辆类型",
                    "车牌号","商品类别","仓库地址","装车时间",
                    "配送开始时间","配送完成时间","到达时间",
                    "车架号","确认收货时间","GPS数据","保单类型",
                    "保单号"
            };
            sb.append("LogisticsInfo" + startTime + "_" + endTime);
        }

        if(QLoanIntentionApplicationInfo.class.isInstance(t)){
            heads = new String[]{
                    "客户编号",
                    "产品编号",
                    "产品名称",
                    "客户类型",
                    "真实姓名",
                    "证件类型",
                    "证件号码",
                    "手机号码",
                    "申请金额",
                    "申请期限",
                    "申请时间",
                    "贷款用途",
                    "还款来源",
                    "担保类型",
                    "附件名称",
                    "附件地址"
            };
            sb.append("LoanIntentionApplicationInfo" + startTime + "_" + endTime);
        }

        if(QRateInfo.class.isInstance(t)){
            heads = new String[]{
                    "平台用户ID",
                    "客户编号",
                    "客户类型",
                    "客户名称",
                    "创建时间",
                    "请求流水号",
                    "额度",
                    "评级",
                    "评分",
                    "风险等级"

            };
            sb.append("RateInfo" + startTime + "_" + endTime);
        }
        sb.append(".csv");
        String fileName = sb.toString();
        writeCSV(list , fileName , heads);
    }
}
