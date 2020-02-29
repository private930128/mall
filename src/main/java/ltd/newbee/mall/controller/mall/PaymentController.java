package ltd.newbee.mall.controller.mall;

import io.swagger.annotations.Api;
import java.util.Date;
import ltd.newbee.mall.common.PayStatusEnum;
import ltd.newbee.mall.common.PaymentStatusEnum;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.entity.PaymentJournal;
import ltd.newbee.mall.service.PaymentService;
import ltd.newbee.mall.util.DateUtil;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
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

    @Autowired
    private PaymentService paymentService;

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


}
