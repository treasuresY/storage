package com.bdilab.storage.common.exception;

import java.util.Map;

public class HttpServerErrorException extends BaseException{
    public HttpServerErrorException(Map<String, Object> data) {
        super(ErrorCode.REQUEST_FAILED, data);
    }
}
