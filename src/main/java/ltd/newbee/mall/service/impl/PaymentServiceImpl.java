package ltd.newbee.mall.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.common.PaymentStatusEnum;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.dao.PaymentJournalMapper;
import ltd.newbee.mall.entity.PaymentJournal;
import ltd.newbee.mall.service.PaymentService;
import ltd.newbee.mall.util.DateUtil;
import ltd.newbee.mall.util.OrderUtil;
import ltd.newbee.mall.util.SnowflakeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentJournalMapper paymentJournalDao;

    @Override
    public PaymentJournal buildPaymentJournal(Long userId, String nickName, String payAppId,
            String payCode, String merchantId, String merchantOrderNo, String desc,
            Integer payAmount) throws NewBeeMallException {
        // 首先该userId下面这个商户订单号是否已经存在
        PaymentJournal where = new PaymentJournal();
        where.setMerchantOrderNo(merchantOrderNo);
        where.setUserId(userId);
        List<PaymentJournal> paymentJournal = paymentJournalDao.select(where);

        long payCount =
                paymentJournal
                        .stream()
                        .filter(p -> p.getPayStatus() == PaymentStatusEnum.PAY.getIndex()
                                || p.getPayStatus() == PaymentStatusEnum.REFUND.getIndex()
                                || p.getPayStatus() == PaymentStatusEnum.WAIT_PAY.getIndex())
                        .count();
        if (payCount > 0) {
            throw new NewBeeMallException(ServiceResultEnum.ORDER_IS_PAYED.getResult());
        }

        long notPayCount =
                paymentJournal
                        .stream()
                        .filter(p -> p.getPayStatus() == PaymentStatusEnum.CREATE_ORDER.getIndex()
                                || p.getPayStatus() == PaymentStatusEnum.WAIT_PAY.getIndex())
                        .count();
        if (notPayCount > 0) {
            return paymentJournal
                    .stream()
                    .filter(p -> p.getPayStatus() == PaymentStatusEnum.CREATE_ORDER.getIndex()
                            || p.getPayStatus() == PaymentStatusEnum.WAIT_PAY.getIndex())
                    .collect(Collectors.toList()).get(0);
        }

        if (payAmount == null) {
            payAmount = 0;
        }

        PaymentJournal insertPaymentJournal = new PaymentJournal();
        insertPaymentJournal.setPaymentJournalId(SnowflakeUtil.getInstance().nextId());
        insertPaymentJournal.setPaymentDealNo(OrderUtil.generatePaymentDealNo());
        insertPaymentJournal.setUserId(userId);
        insertPaymentJournal.setUserName(nickName);
        insertPaymentJournal.setPayAppId(payAppId);
        insertPaymentJournal.setMerchantId(merchantId);
        insertPaymentJournal.setMerchantOrderNo(merchantOrderNo);
        insertPaymentJournal.setPaymentDealId(null);
        insertPaymentJournal.setPayStatus(PaymentStatusEnum.CREATE_ORDER.getIndex());
        insertPaymentJournal.setPayCode(payCode);
        insertPaymentJournal.setPayAmount(payAmount);
        insertPaymentJournal.setAccountAmount(0);
        insertPaymentJournal.setMedicareAmount(0);
        insertPaymentJournal.setInsuranceAmount(0);
        insertPaymentJournal.setTotalAmount(insertPaymentJournal.getPayAmount()
                + insertPaymentJournal.getInsuranceAmount());
        insertPaymentJournal.setDescription(desc);
        insertPaymentJournal.setExtraParams(null);
        insertPaymentJournal.setCreateTime(DateUtil.dateToString(new Date()));
        insertPaymentJournal.setDataSource("web");

        try {
            paymentJournalDao.insert(insertPaymentJournal);
        } catch (Exception ex) {
            throw new NewBeeMallException(ServiceResultEnum.INSRT_PAYMENT_JOURNAL_ERROR.getResult());
        }
        return insertPaymentJournal;
    }

    @Override
    public void updatePaymentJournalMoney(Long paymentJournalId, Integer payAmount,
            Integer payStatus) {
        PaymentJournal updatePaymentJournal = new PaymentJournal();
        updatePaymentJournal.setPaymentJournalId(paymentJournalId);
        updatePaymentJournal.setPayStatus(payStatus);
        updatePaymentJournal.setPayAmount(payAmount);
        updatePaymentJournal.setTotalAmount(payAmount);
        try {
            paymentJournalDao.update(updatePaymentJournal);
        } catch (Exception ex) {
            throw new NewBeeMallException(
                    ServiceResultEnum.UPDATE_PAYMENT_JOURNAL_ERROR.getResult());
        }
    }

    @Override
    public PaymentJournal getPaymentJournalById(Long paymentJournalId) {
        return paymentJournalDao.selectById(String.valueOf(paymentJournalId));
    }

    @Override
    public PaymentJournal getPaymentJournalByNo(String paymentDealNo) {
        PaymentJournal wheres = new PaymentJournal();
        wheres.setPaymentDealNo(paymentDealNo);
        List<PaymentJournal> paymentJournalList = paymentJournalDao.select(wheres);
        if (CollectionUtils.isEmpty(paymentJournalList)) {
            return null;
        }
        return paymentJournalList.get(0);
    }

    @Override
    public void updatePaymentJoural(PaymentJournal paymentJournal) {
        paymentJournalDao.update(paymentJournal);
    }

    @Override
    public PaymentJournal buildRefundPaymentJournal(PaymentJournal paymentJournal)
            throws NewBeeMallException {
        PaymentJournal insertPaymentJournal = new PaymentJournal();
        insertPaymentJournal.setPaymentJournalId(SnowflakeUtil.getInstance().nextId());
        insertPaymentJournal.setPaymentDealNo(OrderUtil.generatePaymentDealNo());
        insertPaymentJournal.setUserId(paymentJournal.getUserId());
        insertPaymentJournal.setUserName(paymentJournal.getUserName());
        insertPaymentJournal.setPayAppId(paymentJournal.getPayAppId());
        insertPaymentJournal.setMerchantId(paymentJournal.getMerchantId());
        insertPaymentJournal.setMerchantOrderNo(paymentJournal.getMerchantOrderNo());
        insertPaymentJournal.setPaymentDealId(null);
        insertPaymentJournal.setPayStatus(PaymentStatusEnum.WAIT_REFUND.getIndex());
        insertPaymentJournal.setPayCode(paymentJournal.getPayCode());
        insertPaymentJournal.setPayAmount(paymentJournal.getPayAmount());
        insertPaymentJournal.setAccountAmount(paymentJournal.getAccountAmount());
        insertPaymentJournal.setMedicareAmount(paymentJournal.getMedicareAmount());
        insertPaymentJournal.setInsuranceAmount(paymentJournal.getInsuranceAmount());
        insertPaymentJournal.setTotalAmount(paymentJournal.getTotalAmount());
        insertPaymentJournal.setDescription("正常退费");
        insertPaymentJournal.setExtraParams(null);
        insertPaymentJournal.setCreateTime(DateUtil.dateToString(new Date()));
        insertPaymentJournal.setDataSource("refund");

        try {
            paymentJournalDao.insert(insertPaymentJournal);
        } catch (Exception ex) {
            throw new NewBeeMallException(ServiceResultEnum.INSRT_PAYMENT_JOURNAL_ERROR.getResult());
        }
        return insertPaymentJournal;
    }
}
