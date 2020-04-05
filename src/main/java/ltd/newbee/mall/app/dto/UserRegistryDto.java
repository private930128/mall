package ltd.newbee.mall.app.dto;

/**
 * Created by zhanghenan on 2020/4/5.
 */
public class UserRegistryDto {

    private String phone;

    private String address;

    private String verCode;

    private String code;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVerCode() {
        return verCode;
    }

    public void setVerCode(String verCode) {
        this.verCode = verCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
