package ltd.newbee.mall.app.dto;

import ltd.newbee.mall.controller.vo.NewBeeMallShoppingCartItemVO;

import java.util.List;

/**
 * Created by zhanghenan on 2020/4/4.
 */
public class CreateOrderRequest {

    private String token;

    private List<NewBeeMallShoppingCartItemVO> goodsInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<NewBeeMallShoppingCartItemVO> getGoodsInfo() {
        return goodsInfo;
    }

    public void setGoodsInfo(List<NewBeeMallShoppingCartItemVO> goodsInfo) {
        this.goodsInfo = goodsInfo;
    }
}
