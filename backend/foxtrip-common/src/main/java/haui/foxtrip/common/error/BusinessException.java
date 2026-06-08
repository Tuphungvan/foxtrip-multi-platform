package haui.foxtrip.common.error;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }
}
