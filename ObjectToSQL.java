package com.zbjk.general.nrc;

import com.zbjk.nrc.dto.tongdun.TongDunDto;
import com.zbjk.nrc.dto.tongdun.threePoints.Risk_items;
import com.zbjk.nrc.utils.BeanFieldCheckUtil;
import com.zbjk.nrc.utils.JDBCUtil;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class ObjectToSQL {

    String url = "";
    String userName = "";
    String password = "";
    @Test
    public void test1() {
        creator(Risk_items.class, "r" , "qy");
    }

    public static void main(String[] args) {
        ObjectToSQL objectToSQL = new ObjectToSQL();
        objectToSQL.creator(Risk_items.class, "r" , "qy");
    }

    public <T> void creator(Class<T> t, String prefix , String suffix) {

        String className = t.getSimpleName().toLowerCase();
        String tableName = prefix + "_" + className + "_" + suffix;

//        if (tableExist("nrc", tableName)){
//            return;
//        }
        Field[] fields = t.getFields();
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE " + tableName + "(\n");
        sb.append(" id int(11) NOT NULL AUTO_INCREMENT,\n");
        for (Field field : fields) {
            sb.append(" ");
            sb.append(field.getName());
            sb.append(" varchar(255) NOT NULL DEFAULT '' COMMENT ");
            sb.append("'" + "" + "',");
        }
        sb.append("  add_time datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '新增时间',\n" +
                "  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                "  del_flag tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否删除 0:不删除 1:删除',\n" +
                "  PRIMARY KEY (id)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");


        try {
            Statement statement = JDBCUtil.getStatement(url, userName, password);
            statement.execute(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean tableExist(String dbName, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(" t.table_name ");
        sb.append(" FROM ");
        sb.append(" information_schema. TABLES t ");
        sb.append(" WHERE ");
        sb.append(" t.TABLE_SCHEMA = ");
        sb.append("\'" + dbName + "\'");
        sb.append(" AND t.TABLE_NAME = ");
        sb.append("\'" + tableName + "\'" + ";");
        Boolean checkResult = null;
        try {
            Statement statement = JDBCUtil.getStatement(url, userName, password);
            ResultSet rs = statement.executeQuery(sb.toString());
            while (rs.next()) {
                String result = rs.getString(1);
                if (result != null) {
                    checkResult = true;
                } else {
                    checkResult = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkResult;
    }

}
