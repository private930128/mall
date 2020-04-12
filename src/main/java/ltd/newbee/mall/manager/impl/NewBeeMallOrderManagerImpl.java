package ltd.newbee.mall.manager.impl;

import ltd.newbee.mall.common.*;
import ltd.newbee.mall.controller.vo.*;
import ltd.newbee.mall.dao.NewBeeMallGoodsMapper;
import ltd.newbee.mall.dao.NewBeeMallOrderItemMapper;
import ltd.newbee.mall.dao.NewBeeMallOrderMapper;
import ltd.newbee.mall.dao.NewBeeMallShoppingCartItemMapper;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.entity.NewBeeMallOrder;
import ltd.newbee.mall.entity.NewBeeMallOrderItem;
import ltd.newbee.mall.manager.NewBeeMallOrderManager;
import ltd.newbee.mall.util.BeanUtil;
import ltd.newbee.mall.util.NumberUtil;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


@Service
public class NewBeeMallOrderManagerImpl implements NewBeeMallOrderManager {

    @Autowired
    private NewBeeMallOrderMapper newBeeMallOrderMapper;
    @Autowired
    private NewBeeMallOrderItemMapper newBeeMallOrderItemMapper;
    @Autowired
    private NewBeeMallShoppingCartItemMapper newBeeMallShoppingCartItemMapper;
    @Autowired
    private NewBeeMallGoodsMapper newBeeMallGoodsMapper;

    @Override
    public String createOrder(NewBeeMallUserVO user, List<NewBeeMallShoppingCartItemVO> myShoppingCartItems) {
        List<Long> goodsIds = myShoppingCartItems.stream().map(NewBeeMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());

        List<NewBeeMallGoods> newBeeMallGoods = newBeeMallGoodsMapper.selectByPrimaryKeys(goodsIds);
        Map<Long, NewBeeMallGoods> goodsMap = new HashMap<>();
        for (NewBeeMallGoods goods : newBeeMallGoods) {
            goodsMap.put(goods.getGoodsId(), goods);
        }
        if (!CollectionUtils.isEmpty(newBeeMallGoods)) {
            //生成订单号
            String orderNo = NumberUtil.genOrderNo();
            int priceTotal = 0;
            //保存订单
            NewBeeMallOrder newBeeMallOrder = new NewBeeMallOrder();
            newBeeMallOrder.setOrderNo(orderNo);
            newBeeMallOrder.setUserId(user.getUserId());
            newBeeMallOrder.setUserAddress(user.getAddress());
            //总价
            for (NewBeeMallShoppingCartItemVO newBeeMallShoppingCartItemVO : myShoppingCartItems) {
                Integer price = goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()) == null ? 1 : goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()).getSellingPrice();
                priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * price;
            }
            if (priceTotal < 1) {
                NewBeeMallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
            }
            newBeeMallOrder.setTotalPrice(priceTotal);
            //todo 订单body字段，用来作为生成支付单描述信息，暂时未接入第三方支付接口，故该字段暂时设为空字符串
            String extraInfo = "";
            newBeeMallOrder.setExtraInfo(extraInfo);
            // 保存渠道
            newBeeMallOrder.setChannelId(user.getChannelId() == null ? 0 : user.getChannelId());
            //生成订单项并保存订单项纪录
            newBeeMallOrderMapper.insertSelective(newBeeMallOrder);
            //生成所有的订单项快照，并保存至数据库
            List<NewBeeMallOrderItem> newBeeMallOrderItems = new ArrayList<>();
            Long orderId = newBeeMallOrder.getOrderId();
            for (NewBeeMallShoppingCartItemVO newBeeMallShoppingCartItemVO : myShoppingCartItems) {
                NewBeeMallOrderItem newBeeMallOrderItem = new NewBeeMallOrderItem();
                BeanUtil.copyProperties(newBeeMallShoppingCartItemVO, newBeeMallOrderItem);
                Integer price = goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()) == null ? 1 : goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()).getSellingPrice();
                String goodName = goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()) == null ? "" : goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()).getGoodsName();
                newBeeMallOrderItem.setSellingPrice(price);
                newBeeMallOrderItem.setOrderId(orderId);
                newBeeMallOrderItem.setGoodsName(goodName);
                newBeeMallOrderItem.setGoodsCoverImg(goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()) == null ? "" : goodsMap.get(newBeeMallShoppingCartItemVO.getGoodsId()).getGoodsCoverImg());
                newBeeMallOrderItems.add(newBeeMallOrderItem);
            }
            //保存至数据库
            newBeeMallOrderItemMapper.insertBatch(newBeeMallOrderItems);
            //所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
            return orderNo;

        }
        throw new NewBeeMallException(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
    }

    @Override
    public NewBeeMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        NewBeeMallOrder newBeeMallOrder = newBeeMallOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder != null) {
            List<NewBeeMallOrderItem> orderItems = newBeeMallOrderItemMapper.selectByOrderId(newBeeMallOrder.getOrderId());
            //获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<NewBeeMallOrderItemVO> newBeeMallOrderItemVOS = new ArrayList<>();
                for (NewBeeMallOrderItem newBeeMallOrderItem : orderItems) {
                    NewBeeMallOrderItemVO newBeeMallOrderItemVO = new NewBeeMallOrderItemVO();
                    newBeeMallOrderItemVO.setGoodsCount(newBeeMallOrderItem.getGoodsCount());
                    newBeeMallOrderItemVO.setGoodsCoverImg(newBeeMallOrderItem.getGoodsCoverImg());
                    newBeeMallOrderItemVO.setGoodsId(newBeeMallOrderItem.getGoodsId());
                    newBeeMallOrderItemVO.setGoodsName(newBeeMallOrderItem.getGoodsName());
                    BigDecimal result = new BigDecimal(newBeeMallOrderItem.getSellingPrice()).divide(new BigDecimal(100));
                    newBeeMallOrderItemVO.setSellingPrice(result.toString());
                    newBeeMallOrderItemVOS.add(newBeeMallOrderItemVO);
                }
                NewBeeMallOrderDetailVO newBeeMallOrderDetailVO = new NewBeeMallOrderDetailVO();
                BeanUtil.copyProperties(newBeeMallOrder, newBeeMallOrderDetailVO);
                newBeeMallOrderDetailVO.setOrderStatusString(NewBeeMallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(newBeeMallOrderDetailVO.getOrderStatus()).getName());
                newBeeMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(newBeeMallOrderDetailVO.getPayType()).getName());
                newBeeMallOrderDetailVO.setNewBeeMallOrderItemVOS(newBeeMallOrderItemVOS);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm");
                newBeeMallOrderDetailVO.setCreateTime(simpleDateFormat.format(newBeeMallOrder.getCreateTime()));
                if (newBeeMallOrder.getPayTime() != null && newBeeMallOrder.getOrderStatus() >= 1 && newBeeMallOrder.getOrderStatus() <= 4) {
                    newBeeMallOrderDetailVO.setPayTime(simpleDateFormat.format(newBeeMallOrder.getPayTime()));
                }
                BigDecimal result = new BigDecimal(newBeeMallOrder.getTotalPrice()).divide(new BigDecimal(100));
                newBeeMallOrderDetailVO.setTotalPrice(result.toString());
                return newBeeMallOrderDetailVO;
            }
        }
        return null;
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        if (pageUtil == null || pageUtil.get("userId") == null) {
            throw new NewBeeMallException(ServiceResultEnum.USER_ERROR.getResult());
        }
        int total = newBeeMallOrderMapper.getTotalNewBeeMallOrders(pageUtil);
        List<NewBeeMallOrder> newBeeMallOrders = newBeeMallOrderMapper.findNewBeeMallOrderList(pageUtil);
        List<NewBeeMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            //数据转换 将实体类转成vo
//            orderListVOS = BeanUtil.copyList(newBeeMallOrders, NewBeeMallOrderListVO.class);
            for (NewBeeMallOrder newBeeMallOrder : newBeeMallOrders) {
                NewBeeMallOrderListVO newBeeMallOrderListVO = new NewBeeMallOrderListVO();
                BeanUtil.copyProperties(newBeeMallOrder, newBeeMallOrderListVO);
                orderListVOS.add(newBeeMallOrderListVO);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm");
                newBeeMallOrderListVO.setCreateTime(simpleDateFormat.format(newBeeMallOrder.getCreateTime()));
                if (newBeeMallOrder.getPayTime() != null && newBeeMallOrder.getOrderStatus() >= 1 && newBeeMallOrder.getOrderStatus() <= 4) {
                    newBeeMallOrderListVO.setPayTime(simpleDateFormat.format(newBeeMallOrder.getPayTime()));
                }
                BigDecimal result = new BigDecimal(newBeeMallOrder.getTotalPrice()).divide(new BigDecimal(100));
                newBeeMallOrderListVO.setTotalPrice(result.toString());
            }
            //设置订单状态中文显示值
            for (NewBeeMallOrderListVO newBeeMallOrderListVO : orderListVOS) {
                newBeeMallOrderListVO.setOrderStatusString(NewBeeMallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(newBeeMallOrderListVO.getOrderStatus()).getName());
            }
            List<Long> orderIds = newBeeMallOrders.stream().map(NewBeeMallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<NewBeeMallOrderItem> orderItems = newBeeMallOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<NewBeeMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(NewBeeMallOrderItem::getOrderId));
                for (NewBeeMallOrderListVO newBeeMallOrderListVO : orderListVOS) {
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(newBeeMallOrderListVO.getOrderId())) {
                        List<NewBeeMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(newBeeMallOrderListVO.getOrderId());
                        //将NewBeeMallOrderItem对象列表转换成NewBeeMallOrderItemVO对象列表
                        List<NewBeeMallOrderItemVO> newBeeMallOrderItemVOS = new ArrayList<>();
                        for (NewBeeMallOrderItem newBeeMallOrderItem : orderItemListTemp) {
                            NewBeeMallOrderItemVO newBeeMallOrderItemVO = new NewBeeMallOrderItemVO();
                            newBeeMallOrderItemVO.setGoodsCount(newBeeMallOrderItem.getGoodsCount());
                            newBeeMallOrderItemVO.setGoodsCoverImg(newBeeMallOrderItem.getGoodsCoverImg());
                            newBeeMallOrderItemVO.setGoodsId(newBeeMallOrderItem.getGoodsId());
                            newBeeMallOrderItemVO.setGoodsName(newBeeMallOrderItem.getGoodsName());
                            BigDecimal result = new BigDecimal(newBeeMallOrderItem.getSellingPrice()).divide(new BigDecimal(100));
                            newBeeMallOrderItemVO.setSellingPrice(result.toString());
                            newBeeMallOrderItemVOS.add(newBeeMallOrderItemVO);
                        }
                        newBeeMallOrderListVO.setNewBeeMallOrderItemVOS(newBeeMallOrderItemVOS);
                    }
                }
            }
        }
        PageResult pageResult = new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        NewBeeMallOrder newBeeMallOrder = newBeeMallOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder != null) {
            // 验证是否是当前userId下的订单，否则报错
            if (!userId.equals(newBeeMallOrder.getUserId())) {
                throw new NewBeeMallException(ServiceResultEnum.ORDER_PERMISSIONS_ERROR.getResult());
            }
            // 订单状态判断, 待支付与已支付 可取消
            if (!(newBeeMallOrder.getOrderStatus() == NewBeeMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                    || newBeeMallOrder.getOrderStatus() == NewBeeMallOrderStatusEnum.OREDER_PAID.getOrderStatus())) {
                throw new NewBeeMallException(ServiceResultEnum.ORDER_CANCEL_STATUS_ERROR.getResult());
            }
            if (newBeeMallOrderMapper.closeOrder(Collections.singletonList(newBeeMallOrder.getOrderId()), NewBeeMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public boolean completeOrderPayment(String orderNo, PayStatusEnum payStatusEnum) {
        NewBeeMallOrder newBeeMallOrder = newBeeMallOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder != null && payStatusEnum == PayStatusEnum.PAY_ING) {
            NewBeeMallOrder newBeeMallOrder1 = new NewBeeMallOrder();
            newBeeMallOrder1.setOrderId(newBeeMallOrder.getOrderId());
            newBeeMallOrder1.setOrderStatus((byte) NewBeeMallOrderStatusEnum.OREDER_PAID.getOrderStatus());
            newBeeMallOrder1.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            newBeeMallOrder1.setPayTime(new Date());
            newBeeMallOrder1.setUpdateTime(new Date());
            newBeeMallOrderMapper.updateByPrimaryKeySelective(newBeeMallOrder);
            return true;
        }
        return false;
    }

    @Override
    public List<NewBeeMallOrderListVO> getOrdersByExport(PageQueryUtil pageUtil) {

        List<NewBeeMallOrder> newBeeMallOrders = newBeeMallOrderMapper.findNewBeeMallOrderListByExport(pageUtil);
        List<NewBeeMallOrderListVO> orderListVOS = new ArrayList<>();
        //数据转换 将实体类转成vo
        orderListVOS = BeanUtil.copyList(newBeeMallOrders, NewBeeMallOrderListVO.class);
        //设置订单状态中文显示值
        for (NewBeeMallOrderListVO newBeeMallOrderListVO : orderListVOS) {
            newBeeMallOrderListVO.setOrderStatusString(NewBeeMallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(newBeeMallOrderListVO.getOrderStatus()).getName());
        }
        List<Long> orderIds = newBeeMallOrders.stream().map(NewBeeMallOrder::getOrderId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(orderIds)) {
            List<NewBeeMallOrderItem> orderItems = newBeeMallOrderItemMapper.selectByOrderIds(orderIds);
            Map<Long, List<NewBeeMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(NewBeeMallOrderItem::getOrderId));
            for (NewBeeMallOrderListVO newBeeMallOrderListVO : orderListVOS) {
                //封装每个订单列表对象的订单项数据
                if (itemByOrderIdMap.containsKey(newBeeMallOrderListVO.getOrderId())) {
                    List<NewBeeMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(newBeeMallOrderListVO.getOrderId());
                    //将NewBeeMallOrderItem对象列表转换成NewBeeMallOrderItemVO对象列表
                    List<NewBeeMallOrderItemVO> newBeeMallOrderItemVOS = BeanUtil.copyList(orderItemListTemp, NewBeeMallOrderItemVO.class);
                    newBeeMallOrderListVO.setNewBeeMallOrderItemVOS(newBeeMallOrderItemVOS);
                }
            }
        }

        return orderListVOS;
    }
}
