package backend.common.exception;

import backend.common.result.ResultCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final int code;

    public AppException(ResultCode rc) {
        super(rc.getMsg());
        this.code = rc.getCode();
    }

    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }
}
