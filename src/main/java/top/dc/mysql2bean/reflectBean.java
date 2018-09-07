package top.dc.mysql2bean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

public class reflectBean {
    private Connection connection;
    private PreparedStatement UserQuery;
    /*mysql url的连接字符串*/
//    private static String url = "jdbc:sqlserver://10.10.10.102:1433;DatabaseName=kty_awdb";
    private static String url = "jdbc:mysql://10.10.10.16:3306/poc_new?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true";
    //账号
    private static String user = "kefu";
    //密码  
    private static String password = "123456";
    private Vector<String> vector = new Vector<String>();
    //mysql jdbc的java包驱动字符串  
//    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String driverClassName = "com.mysql.jdbc.Driver";
    //数据库中的表名
//    String table = "`case`";
    //数据库的列名称  
    private String[] colnames; // 列名数组  
    //列名类型数组    
    private String[] colTypes;

    public reflectBean() {
        try {//驱动注册
            Class.forName(driverClassName);
            if (connection == null || connection.isClosed())
                //获得链接
                connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Oh,not");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Oh,not");
        }
    }

    public void doAction(String table) {
//        String sql = "select * from " + table+"";
        String sql = "select * from `" + table+"`";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            //获取数据库的元数据   
            ResultSetMetaData metadata = statement.getMetaData();
            //数据库的字段个数  
            int len = metadata.getColumnCount();
            //字段名称  
            colnames = new String[len + 1];
            //字段类型 --->已经转化为java中的类名称了  
            colTypes = new String[len + 1];
            for (int i = 1; i <= len; i++) {
                //System.out.println(metadata.getColumnName(i)+":"+metadata.getColumnTypeName(i)+":"+sqlType2JavaType(metadata.getColumnTypeName(i).toLowerCase())+":"+metadata.getColumnDisplaySize(i));  
                //metadata.getColumnDisplaySize(i);  
                colnames[i] = metadata.getColumnName(i); //获取字段名称  
                System.out.println(colnames[i]);
                colTypes[i] = sqlType2JavaType(metadata.getColumnTypeName(i)); //获取字段类型   
                System.out.println(metadata.getColumnTypeName(i));
                System.out.println(colTypes[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * mysql的字段类型转化为java的类型*/
    private String sqlType2JavaType(String sqlType) {

        if (sqlType.equalsIgnoreCase("bit")) {
            return "boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "short";
        } else if (sqlType.equalsIgnoreCase("int")) {
            return "int";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "float";
        } else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
                || sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money")
                || sqlType.equalsIgnoreCase("smallmoney")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
                || sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
                || sqlType.equalsIgnoreCase("text")) {
            return "String";
        } else if (sqlType.equalsIgnoreCase("datetime") || sqlType.equalsIgnoreCase("date")) {
            return "Date";
        } else if (sqlType.equalsIgnoreCase("image")) {
            return "Blod";
        } else if (sqlType.equalsIgnoreCase("timestamp")) {
            return "Timestamp";
        }

        return null;
    }

    /*获取整个类的字符串并且输出为java文件
     * */
    public StringBuffer getClassStr(String table) {


        //输出的类字符串  
        StringBuffer str = new StringBuffer("");
        //获取表类型和表名的字段名  
        this.doAction(table);
        //校验  
        if (null == colnames && null == colTypes) return null;
        str.append("import lombok.*;\n" +
                "import javax.persistence.*;\n" +
                "import java.util.*;\n");
        str.append("@Entity\r\n@Data\r\n");
        //拼接  
        str.append("public class " + GetTuoFeng(table, false) + " {\r\n");
        //拼接属性  
        for (int index = 1; index < colnames.length; index++) {
            str.append(getAttrbuteString(colnames[index], colTypes[index]));
        }
        //拼接get，Set方法         
//        for(int index=1; index < colnames.length ; index++){
//            str.append(getGetMethodString(colnames[index],colTypes[index]));
//            str.append(getSetMethodString(colnames[index],colTypes[index]));
//        }
        str.append("}\r\n");
        //输出到文件中  
        File file = new File("I:/entity/" + GetTuoFeng(table, false) + ".java");
        BufferedWriter write = null;

        try {
            write = new BufferedWriter(new FileWriter(file));
            write.write(str.toString());
            write.close();
        } catch (IOException e) {

            e.printStackTrace();
            if (write != null)
                try {
                    write.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        return str;
    }

    /*
     * 获取字段字符串*/
    public StringBuffer getAttrbuteString(String name, String type) {
        if (!check(name, type)) {
            System.out.println("类中有属性或者类型为空");
            return null;
        }
        name = GetTuoFeng(name, true);
        String format = String.format("    private %s %s;\n\r", new String[]{type, name});
        return new StringBuffer(format);
    }

    /*
     * 校验name和type是否合法*/
    public boolean check(String name, String type) {
        if ("".equals(name) || name == null || name.trim().length() == 0) {
            return false;
        }
        if ("".equals(type) || type == null || type.trim().length() == 0) {
            return false;
        }
        return true;

    }

    //驼峰
    private String GetTuoFeng(String name, boolean first) {
        name = name.trim();
        String[] split = name.split("_");
        StringBuffer sb = new StringBuffer();
        for (String one : split) {
            if (first) {
                first = false;
                sb.append(one);
                continue;
            }
            if (one.length() > 0) {
                sb.append(one.substring(0, 1).toUpperCase());
                if (one.length() > 1) {
                    sb.append(one.substring(1));
                }
            }
        }
        return sb.toString();
    }

}