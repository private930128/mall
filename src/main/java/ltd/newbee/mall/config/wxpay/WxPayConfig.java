package ltd.newbee.mall.config.wxpay;

public class WxPayConfig {

    // 小程序appid
    public static final String appid = "wxd7c9a7b80f0b224c";
    // 微信支付的商户id
    public static final String mch_id = "xxxxx";
    // 微信支付的商户密钥
    public static final String key = "ab463a2de2212c761718d8e236e440c8";
    // 支付成功后的服务器回调url
    public static final String notify_url = "http://ip/xx/xx/wxNotify";
    // 签名方式，固定值
    public static final String SIGNTYPE = "MD5";
    // 交易类型，小程序支付的固定值为JSAPI
    public static final String TRADETYPE = "JSAPI";
    // 微信统一下单接口地址
    public static final String pay_url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
}
