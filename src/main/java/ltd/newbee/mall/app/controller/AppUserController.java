package ltd.newbee.mall.app.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import ltd.newbee.mall.config.redis.RedisUtil;
import ltd.newbee.mall.controller.vo.SmsResultVO;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.properties.SmsProperties;
import ltd.newbee.mall.service.fegin.SmsApiService;
import ltd.newbee.mall.util.NumberUtil;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation(value = "用户登录接口")
    @PostMapping("login")
    public Result login(@RequestBody MallUser user) {
        return ResultGenerator.genSuccessResult();
    }
}
