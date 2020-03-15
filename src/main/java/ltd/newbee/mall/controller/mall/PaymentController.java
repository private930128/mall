package ltd.newbee.mall.controller.mall;

import io.swagger.annotations.Api;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.common.PayStatusEnum;
import ltd.newbee.mall.common.PaymentStatusEnum;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.config.wxpay.WxPayConfig;
import ltd.newbee.mall.controller.vo.NewBeeMallUserVO;
import ltd.newbee.mall.entity.PaymentJournal;
import ltd.newbee.mall.service.NewBeeMallOrderService;
import ltd.newbee.mall.service.PaymentService;
import ltd.newbee.mall.util.DateUtil;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import ltd.newbee.mall.util.wxpay.PayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@Api(value = "支付操作")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NewBeeMallOrderService newBeeMallOrderService;

    @PostMapping("/save")
    public Result save() {

        String success = "";
        PaymentJournal paymentJournal =
                paymentService.buildPaymentJournal(1L, "test", "appId", "paycode", "merchantId",
                        "merchantOrderNo", "desc", 1000);
        if (paymentJournal != null)
            success = "success";
        // 添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(success)) {
            return ResultGenerator.genSuccessResult();
        }
        // 添加失败
        return ResultGenerator.genFailResult("添加失败");
    }

    @GetMapping("/findById")
    @ResponseBody
    public PaymentJournal getPaymentJournalById() {

        PaymentJournal paymentJournal = paymentService.getPaymentJournalById(572349760023822336L);

        return paymentJournal;
    }

    @GetMapping("/findByNo")
    @ResponseBody
    public PaymentJournal getPaymentJournalByNo(String dealNo) {
        PaymentJournal paymentJournal = paymentService.getPaymentJournalByNo(dealNo);
        return paymentJournal;
    }


    @PostMapping("/updatePaymentJoural")
    public Result updatePaymentJoural(String dealNo) {
        PaymentJournal paymentJournal = paymentService.getPaymentJournalByNo(dealNo);
        if (paymentJournal == null) {
            return null;
        }

        PaymentJournal updatePaymentJournal = new PaymentJournal();
        updatePaymentJournal.setPaymentJournalId(paymentJournal.getPaymentJournalId());
        updatePaymentJournal.setPayStatus(PaymentStatusEnum.PAY.getIndex());
        updatePaymentJournal.setPayAmount(2000);
        updatePaymentJournal.setTotalAmount(3000);
        updatePaymentJournal.setUpdateTime(DateUtil.dateToString(new Date()));
        paymentService.updatePaymentJoural(updatePaymentJournal);
        return ResultGenerator.genFailResult("修改成功");
    }

    @PostMapping("updatePaymentJournalMoney")
    public Result updatePaymentJournalMoney() {
        paymentService.updatePaymentJournalMoney(572349760023822336L, 2, 3);
        return ResultGenerator.genFailResult("修改成功");
    }

    @PostMapping("buildRefundPaymentJournal")
    @ResponseBody
    public PaymentJournal buildRefundPaymentJournal() {

        PaymentJournal paymentJournal = new PaymentJournal();
        paymentJournal.setUserId(2L);
        paymentJournal.setUserName("2fen");

        PaymentJournal paymentJournalResult =
                paymentService.buildRefundPaymentJournal(paymentJournal);

        return paymentJournalResult;
    }

    /**
     * @Description: 发起微信支付
     * @param openid
     * @param request
     */
    @RequestMapping("wxrPay")
    @ResponseBody
    public Map<String, Object> wxrPay(HttpServletRequest request, String orderNo, HttpSession httpSession) {
        Map<String, Object> map = new HashMap<>();
        NewBeeMallUserVO user = (NewBeeMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String finishOrderResult = newBeeMallOrderService.finishOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            map.put("400", "订单已支付，请不要重复操作");
        } else if(ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult().equals(finishOrderResult)){
            map.put("400", ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        } else if(ServiceResultEnum.DB_ERROR.getResult().equals(finishOrderResult)){
            map.put("400", ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult());
        } else {
            map = paymentService.paywxr(request, orderNo);
        }
        return map;
    }

    /**
     * @Description:微信支付-回调接口
     * @return
     * @throws Exception
     */
    @RequestMapping("wxNotify")
    @ResponseBody
    public void wxNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("进入回调");
        BufferedReader br =
                new BufferedReader(new InputStreamReader(
                        (ServletInputStream) request.getInputStream()));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        // sb为微信返回的xml
        String notityXml = sb.toString();
        String resXml = "";
        log.info("接收到的报文：" + notityXml);

        Map map = PayUtil.doXMLParse(notityXml);

        String returnCode = (String) map.get("return_code");
        if ("SUCCESS".equalsIgnoreCase(returnCode)) {
            // 验证签名是否正确
            Map<String, String> validParams = PayUtil.paraFilter(map); // 回调验签时需要去除sign和空值参数
            String validStr = PayUtil.createLinkString(validParams);// 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
            String sign = PayUtil.sign(validStr, WxPayConfig.key, "utf-8").toUpperCase();// 拼装生成服务器端验证的签名
            // 根据微信官网的介绍，此处不仅对回调的参数进行验签，还需要对返回的金额与系统订单的金额进行比对等
            if (sign.equals(map.get("sign"))) {
                String orderNumber = (String) map.get("out_trade_no");// 订单号
                String amount = (String) map.get("total_fee");// 价格
                Integer totalPrice = Integer.valueOf(amount);// 服务器这边记录的是钱的分
//                paymentService.updatePaymentJournalMoney();
                // 通知微信服务器已经支付成功
                resXml =
                        "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                                + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
            }
        } else {
            resXml =
                    "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                            + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
        }
        log.info(resXml);
        log.info("微信支付回调数据结束");


        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(resXml.getBytes());
        out.flush();
        out.close();
    }

}
