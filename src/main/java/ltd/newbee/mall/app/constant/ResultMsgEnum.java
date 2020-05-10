package ltd.newbee.mall.app.constant;

/**
 * Created by zhanghenan on 2020/4/4.
 */
public enum ResultMsgEnum {

    LOGIN_INFO_IS_NULL(1001, "登录信息为空,请登录!"),
    MSG_VERIFY_PARAM_IS_NULL(1002, "短信验证信息参数为空,请登录!"),
    VERIFICATION_CODE_OVERDUE(1003, "验证码过期,请重新验证"),
    VERIFICATION_CODE_ERROR(1004, "验证码错误,请重新输入"),
    USER_NOT_EXIST(1005, "用户不存在，请注册"),
    REGISTRY_PASSWORD_CONFIRM_ERROR(1006, "两次密码输入不一致"),
    USER_EXIST(1007, "用户已存在,请登录"),
    PASSWORD_ERROR(1008, "密码错误,请重试"),

    ORDER_PAID_ERROR(2001, "订单已支付，请不要重复操作"),
    ORDER_NOT_EXIST(2002, "该订单不存在!!"),

    MSG_LIMIT_IN_ONE_MINUTE(3001, "一分钟内短信发送受限"),
    MSG_LIMIT_IN_ONE_DAY(3002, "当天短信发送受限");

    private Integer code;
    private String msg;

    ResultMsgEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
