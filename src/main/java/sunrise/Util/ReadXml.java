/**
 * Project Name:testDownload
 * File Name:ReadXML.java
 * Package Name:com.zsy.xml
 * Date:2016-3-31上午08:37:47
 * Copyright (c) 2016, syzhao@zsy.com All Rights Reserved.
 *
 */

package sunrise.Util;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

public  class ReadXml
{
        /**
         * getXml:(读取xml文件。获取document树). <br/>
         * @author syzhao
         * @param route xml数字
         * @return Document
         * @since JDK 1.6
         */
        public static String getSql(String route)
        {
                String sql = "";

                try
                {
                        String[] xmlPath = route.split("/");
                        sql = new XMLConfiguration(xmlPath[0] + ".xml").getString(xmlPath[1]);
                }
                catch (ConfigurationException e)
                {
                        System.out.println("获取xml " + route + " 文件失败！");
                        e.printStackTrace();
                }

                return sql;
        }
}