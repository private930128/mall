package ltd.newbee.mall.app.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import ltd.newbee.mall.app.constant.ResultMsgEnum;
import ltd.newbee.mall.config.redis.RedisUtil;
import ltd.newbee.mall.controller.vo.WechatAuthTokenVO;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.service.WechatService;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import ltd.newbee.mall.util.wxpay.PaymentControllerbak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用于v1.0 app用户信息相关交互接口
 * 包括注册、登录、个人信息等接口
 * 具体接口信息待与fe确定
 */
@RestController
@RequestMapping("/app/user/")
@Api(value = "app用户信息相关交互接口")
public class AppUserController {

    private static Logger logger = LoggerFactory.getLogger(PaymentControllerbak.class);
    @Autowired
    private WechatService wechatService;
    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "用户登录接口")
    @RequestMapping(value = "login", method = RequestMethod.GET)
    @ResponseBody
    public Result login(MallUser user) {
        logger.info("login param : user = {}", JSON.toJSON(user));
        try {
            WechatAuthTokenVO wechatAuthTokenVO = wechatService.wechatLogin(user);
            logger.info("login response : wechatAuthTokenVO = {}", JSON.toJSON(wechatAuthTokenVO));
            return ResultGenerator.genSuccessDateResult(wechatAuthTokenVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultGenerator.genFailResult("微信登录失败");
        }
    }

    @ApiOperation(value = "用户注册接口")
    @RequestMapping(value = "/registry", method = RequestMethod.POST)
    @ResponseBody
    public Result registry(@RequestBody MallUser user, String verCode, String code) {
        try {
            logger.info("registry param : user = {}, verCode = {}, code = {}", JSON.toJSON(user), verCode, code);
            if (user == null || StringUtils.isEmpty(user.getLoginName()) || StringUtils.isEmpty(verCode)) {
                return ResultGenerator.genErrorResult(ResultMsgEnum.MSG_VERIFY_PARAM_IS_NULL.getCode(), ResultMsgEnum.MSG_VERIFY_PARAM_IS_NULL.getMsg());
            }
            Object object = redisUtil.get(user.getLoginName());
            if (object == null) {
                return ResultGenerator.genErrorResult(ResultMsgEnum.VERIFICATION_CODE_OVERDUE.getCode(), ResultMsgEnum.VERIFICATION_CODE_OVERDUE.getMsg());
            }
            String verCodeFromRedis = object.toString();
            if (StringUtils.isEmpty(verCodeFromRedis)) {
                return ResultGenerator.genErrorResult(ResultMsgEnum.VERIFICATION_CODE_OVERDUE.getCode(), ResultMsgEnum.VERIFICATION_CODE_OVERDUE.getMsg());
            }
            if (verCodeFromRedis.equals(verCode)) {
                return ResultGenerator.genErrorResult(ResultMsgEnum.VERIFICATION_CODE_ERROR.getCode(), ResultMsgEnum.VERIFICATION_CODE_ERROR.getMsg());
            }
            wechatService.registry(user);
            return ResultGenerator.genSuccessResult();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultGenerator.genFailResult("注册失败");
        }
    }
}
