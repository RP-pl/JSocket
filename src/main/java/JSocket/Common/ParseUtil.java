package JSocket.Common;

import java.util.HashMap;
import java.util.Map;

public class ParseUtil {
    public static Map<String,String> parseHttpRequest(String requestData) {
        Map<String,String> headers = new HashMap<>();
        String[] reqDta = requestData.split("\r\n");
        boolean dataFlag = false;
        for (int i=1;i<reqDta.length;i++) {
            if(!reqDta[i].contains(": ")){
                dataFlag = true;
            }
            if(!dataFlag) {
                String[] kv = reqDta[i].split(": ");
                headers.put(kv[0], kv[1]);
            }
        }
        return headers;
    }
}
