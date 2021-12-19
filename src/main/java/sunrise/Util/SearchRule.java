package sunrise.Util;

public class SearchRule {

        public String  op  = "";
        public String field = "";
        public String value = "";

        public SearchRule(String f, String o, String v) {

                field = f;
                value = v;
                op = o;
        }
}