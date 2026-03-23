package backend.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1xxx
    USER_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),

    // 活动相关 2xxx
    ACTIVITY_NOT_FOUND(2001, "活动不存在"),
    ACTIVITY_FULL(2002, "活动名额已满"),
    ACTIVITY_STATUS_ERROR(2003, "活动状态不允许该操作"),

    // 岗位 / 派岗相关 4xxx
    POSITION_NOT_FOUND(4001, "岗位不存在"),
    POSITION_QUOTA_FULL(4002, "岗位名额已满"),
    ALREADY_ASSIGNED(4003, "该报名记录已完成派岗，请勿重复操作"),

    // 报名相关 3xxx
    ALREADY_REGISTERED(3001, "已报名该活动，请勿重复提交"),
    REGISTRATION_NOT_FOUND(3002, "报名记录不存在"),

    // 签到相关 5xxx
    QR_TOKEN_INVALID(5001, "二维码令牌无效或已过期"),
    ALREADY_CHECKED_IN(5002, "您已签到，请勿重复操作"),
    ALREADY_CHECKED_OUT(5003, "您已完成签退，请勿重复操作"),
    CHECKIN_NOT_FOUND(5004, "签到记录不存在"),
    REGISTRATION_NOT_APPROVED(5005, "报名未审核通过，无法签到"),

    // 画像相关 6xxx
    PROFILE_NOT_FOUND(6001, "用户画像不存在（仅志愿者角色拥有画像）");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
