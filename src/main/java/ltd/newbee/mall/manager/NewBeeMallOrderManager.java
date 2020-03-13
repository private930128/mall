package ltd.newbee.mall.manager;

import ltd.newbee.mall.common.PayStatusEnum;
import ltd.newbee.mall.controller.vo.NewBeeMallOrderDetailVO;
import ltd.newbee.mall.controller.vo.NewBeeMallShoppingCartItemVO;
import ltd.newbee.mall.controller.vo.NewBeeMallUserVO;
import ltd.newbee.mall.entity.NewBeeMallOrder;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;

import java.util.List;

public interface NewBeeMallOrderManager {

    String createOrder(NewBeeMallUserVO user, List<NewBeeMallShoppingCartItemVO> myShoppingCartItems);

    NewBeeMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    PageResult getMyOrders(PageQueryUtil pageUtil);

    String cancelOrder(String orderNo, Long userId);

    boolean completeOrderPayment(String orderNo, PayStatusEnum payStatusEnum);
}
