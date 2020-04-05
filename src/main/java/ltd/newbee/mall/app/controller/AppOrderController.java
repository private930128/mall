package ltd.newbee.mall.app.controller;


import com.alibaba.fastjson.JSON;
import ltd.newbee.mall.app.constant.ResultMsgEnum;
import ltd.newbee.mall.app.dto.CancelOrderRequest;
import ltd.newbee.mall.app.dto.CreateOrderRequest;
import ltd.newbee.mall.app.dto.CreateOrderResultDto;
import ltd.newbee.mall.config.redis.RedisUtil;
import ltd.newbee.mall.controller.vo.NewBeeMallShoppingCartItemVO;
import ltd.newbee.mall.controller.vo.NewBeeMallUserVO;
import ltd.newbee.mall.dao.MallUserMapper;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.manager.NewBeeMallOrderManager;
import ltd.newbee.mall.service.PaymentService;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import ltd.newbee.mall.util.wxpay.PaymentControllerbak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于v1.0 app订单信息相关交互接口
 * 包括订单列表、订单详情、生成订单等接口
 * 具体接口信息待与fe确定
 */
@Controller
@RequestMapping("/app/order")
public class AppOrderController {

    private static Logger logger = LoggerFactory.getLogger(PaymentControllerbak.class);

    @Resource
    private NewBeeMallOrderManager newBeeMallOrderManager;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MallUserMapper mallUserMapper;
    @Autowired
    private PaymentService paymentService;

    @RequestMapping(value = "/createOrder", method = RequestMethod.POST)
    @ResponseBody
    public Result createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        logger.info("createOrder param : createOrderRequest = {}", JSON.toJSON(createOrderRequest));
        Object object = redisUtil.get(createOrderRequest.getToken());
        logger.info("createOrder getOpenId : object = {}", object);
        if (object == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        String openId = object.toString();
        if (StringUtils.isEmpty(openId)) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        MallUser mallUser = mallUserMapper.selectByOpenId(openId);
        if (mallUser == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        NewBeeMallUserVO userVO = new NewBeeMallUserVO();
        userVO.setUserId(mallUser.getUserId());
        userVO.setChannelId(1);
        userVO.setAddress(mallUser.getAddress());
        String orderNo = newBeeMallOrderManager.createOrder(userVO, createOrderRequest.getGoodsInfo());
        CreateOrderResultDto createOrderResultDto = paymentService.assemblyCreateOrderResultDto();
        return ResultGenerator.genSuccessDateResult(orderNo);
    }

    @RequestMapping(value = "/myOrderList", method = RequestMethod.GET)
    @ResponseBody
    public Result myOrderList(String token) {
        Object object = redisUtil.get(token);
        logger.info("cancelOrder getOpenId : object = {}", object);
        if (object == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        String openId = object.toString();
        if (StringUtils.isEmpty(openId)) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        MallUser mallUser = mallUserMapper.selectByOpenId(openId);
        if (mallUser == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        Map<String, Object> params = new HashMap();
        params.put("page", 1);
        params.put("limit", 100);
        params.put("userId", mallUser.getUserId());
        PageQueryUtil pageQueryUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessDateResult(newBeeMallOrderManager.getMyOrders(pageQueryUtil));
    }

    @RequestMapping(value = "/orderDetail", method = RequestMethod.GET)
    @ResponseBody
    public Result orderDetail(String token, String orderNo) {
        Object object = redisUtil.get(token);
        logger.info("cancelOrder getOpenId : object = {}", object);
        if (object == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        String openId = object.toString();
        if (StringUtils.isEmpty(openId)) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        MallUser mallUser = mallUserMapper.selectByOpenId(openId);
        if (mallUser == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }

        return ResultGenerator.genSuccessDateResult(newBeeMallOrderManager.getOrderDetailByOrderNo(orderNo, mallUser.getUserId()));
    }

    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    @ResponseBody
    public Result cancelOrder(@RequestBody CancelOrderRequest cancelOrderRequest) {
        Object object = redisUtil.get(cancelOrderRequest.getToken());
        logger.info("cancelOrder getOpenId : object = {}", object);
        if (object == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        String openId = object.toString();
        if (StringUtils.isEmpty(openId)) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }
        MallUser mallUser = mallUserMapper.selectByOpenId(openId);
        if (mallUser == null) {
            return ResultGenerator.genErrorResult(ResultMsgEnum.LOGIN_INFO_IS_NULL.getCode(), ResultMsgEnum.LOGIN_INFO_IS_NULL.getMsg());
        }

        newBeeMallOrderManager.cancelOrder(cancelOrderRequest.getOrderNo(), mallUser.getUserId());
        return ResultGenerator.genSuccessResult();
    }
}
