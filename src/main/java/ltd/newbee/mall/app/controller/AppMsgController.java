package ltd.newbee.mall.app.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import ltd.newbee.mall.app.dto.AppMsgDto;
import ltd.newbee.mall.config.redis.RedisUtil;
import ltd.newbee.mall.controller.vo.SmsResultVO;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.properties.SmsProperties;
import ltd.newbee.mall.service.fegin.SmsApiService;
import ltd.newbee.mall.util.NumberUtil;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用于v1.0 app用户信息相关交互接口
 * 短信相关
 */
@RestController
@RequestMapping("/app/msg/")
@Api(value = "app发送短信相关接口")
public class AppMsgController {

    //验证码长度
    public static final int LENGTH = 6;
    //验证码超时时间（单位秒）
    public static final int TIME = 60;
    @Autowired
    private SmsApiService smsApiService;
    @Autowired
    private SmsProperties smsProperties;
    @Autowired
    private RedisUtil redisUtil;
    @ApiOperation(value = "短信发送接口")
    @PostMapping("sendMsg")
    public Result sendMsg(@RequestBody AppMsgDto appMsgDto) {
        //TODO 1.校验手机号正确性 2.
        //TODO 1.防刷 2.记录请求成功与否 3.校验验证码 4.添加渠道
        String verCode = String.valueOf(NumberUtil.genRandomNum(LENGTH));
        SmsResultVO smsResultVO = this.smsApiService.sendMessage(this.smsProperties.requestParam(appMsgDto.getPhone(), verCode));
        redisUtil.set(appMsgDto.getPhone(), verCode, TIME);
        return ResultGenerator.genSuccessResult(smsResultVO);
    }
}
