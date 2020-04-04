package ltd.newbee.mall.app.constant;

/**
 * Created by zhanghenan on 2020/4/4.
 */
public enum ResultMsgEnum {

    LOGIN_INFO_IS_NULL(1001, "登录信息为空,请登录!"),
    MSG_VERIFY_PARAM_IS_NULL(1002, "短信验证信息参数为空,请登录!"),
    VERIFICATION_CODE_OVERDUE(1003, "验证码过期,请重新验证"),
    VERIFICATION_CODE_ERROR(1004, "验证码错误,请重新输入"),;

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
