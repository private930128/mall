package ltd.newbee.mall.service;

import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.config.redis.RedisUtil;
import ltd.newbee.mall.controller.vo.WechatAuthCodeResponseVO;
import ltd.newbee.mall.controller.vo.WechatAuthTokenVO;
import ltd.newbee.mall.dao.MallUserMapper;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.properties.WechatAuthProperties;
import ltd.newbee.mall.service.fegin.WeChatApiService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WechatService {

    private static final Long EXPIRES = 86400L;

    @Autowired
    private WeChatApiService weChatApiService;
    @Autowired
    private WechatAuthProperties wechatAuthProperties;
    @Autowired
    private MallUserMapper mallUserMapper;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 微信登录
     * @param user
     * @return
     * @throws Exception
     */
    public WechatAuthTokenVO wechatLogin(MallUser user) throws Exception {
        //1.获取openId
        WechatAuthCodeResponseVO response = this.weChatApiService.jscode2session(wechatAuthProperties.getAppId(),
                wechatAuthProperties.getSecret(),
                user.getCode(),
                wechatAuthProperties.getGrantType());

        //2.保存用户信息
        String wxOpenId = response.getOpenid();
        String wxSessionKey = response.getSession_key();
        user.setWechatOpenid(wxOpenId);
        this.mallUserMapper.insert(user);
        //3.生成token
        return new WechatAuthTokenVO(create3rdToken(wxOpenId, wxSessionKey, EXPIRES));

    }

    public String create3rdToken(String wxOpenId, String wxSessionKey, Long expires) {
        String thirdSessionKey = RandomStringUtils.randomAlphanumeric(64);
        StringBuffer sb = new StringBuffer();
        sb.append(wxSessionKey).append("#").append(wxOpenId);

        redisUtil.set(thirdSessionKey, sb.toString(), expires);
        return thirdSessionKey;
    }

}