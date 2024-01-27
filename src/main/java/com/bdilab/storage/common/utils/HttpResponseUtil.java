package com.bdilab.storage.common.utils;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseUtil {
    public static Map<String, Object> generateExceptionResponseData(String errorInfo) {
        return new HashMap<String, Object>(){
            {
                put("Error Info", errorInfo);
            }
        };
    }
    public static Map<String, Object> generateSuccessResponseData(String successInfo) {
        return new HashMap<String, Object>(){
            {
                put("Success Info", successInfo);
            }
        };
    }
}
