package ltd.newbee.mall.manager;

import ltd.newbee.mall.entity.NewBeeMallOrder;

import java.util.List;

/**
 * Created by zhanghenan on 2020/1/21.
 */
public interface NewBeeMallOrderManager {

    void createOrder();

    NewBeeMallOrder getOrderInfoById();

    List<NewBeeMallOrder> listOrder();
}
