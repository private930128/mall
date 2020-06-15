package ltd.newbee.mall.app.dto;

import java.math.BigDecimal;
import lombok.Data;
import ltd.newbee.mall.entity.NewBeeMallShoppingCartItem;

/**
 * Created by wangyf on 2020/6/6. 购物车中促销信息的封装
 */
@Data
public class CartPromotionItem extends NewBeeMallShoppingCartItem {

    // 促销活动信息
    private String promotionMessage;
    // 促销活动减去的金额，针对每个商品
    private Long reduceAmount;
    // 购买商品赠送积分
    private Integer integration;
}
