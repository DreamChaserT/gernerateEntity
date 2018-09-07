package top.dc.mysql2bean;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        reflectBean bean = new reflectBean();
        bean.getClassStr("skill_group_own");
        bean.getClassStr("skill_group_workno_own");
        bean.getClassStr("activity_out_bound_config");
//        bean.getClassStr("call_order");
//        bean.getClassStr("call_queue");
//        bean.getClassStr("case");
//        bean.getClassStr("name_list");
//        bean.getClassStr("phone_order");
//        bean.getClassStr("retry");
//        bean.getClassStr("call_time");
//        bean.getClassStr("call_config");
//        bean.getClassStr("call_result_log");
    }
}
