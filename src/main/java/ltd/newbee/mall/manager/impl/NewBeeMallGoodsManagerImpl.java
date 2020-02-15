package ltd.newbee.mall.manager.impl;

import ltd.newbee.mall.app.dto.AppGoodsInfoDto;
import ltd.newbee.mall.app.dto.AppGoodsQueryDto;
import ltd.newbee.mall.dao.NewBeeMallGoodsMapper;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.manager.NewBeeMallGoodsManager;
import ltd.newbee.mall.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhanghenan on 2020/2/15.
 */
@Service
public class NewBeeMallGoodsManagerImpl implements NewBeeMallGoodsManager {

    @Autowired
    private NewBeeMallGoodsMapper goodsMapper;

    @Override
    public List<AppGoodsInfoDto> queryGoods(AppGoodsQueryDto queryDto) {
        if (queryDto.getPageSize() == null) {
            queryDto.setPageSize(20);
        }
        if (queryDto.getPageNum() == null) {
            queryDto.setPageSize(1);
        }
        Integer pageStartNum = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
        queryDto.setPageStartNum(pageStartNum);
        List<NewBeeMallGoods> list = goodsMapper.queryGoods(queryDto);

        return BeanUtil.copyList(list, AppGoodsInfoDto.class);
    }

    @Override
    public AppGoodsInfoDto queryGoodsById(Long goodsId) {
        NewBeeMallGoods newBeeMallGoods = goodsMapper.selectById(goodsId);
        if (newBeeMallGoods == null) {
            return null;
        }
        AppGoodsInfoDto appGoodsInfoDto = new AppGoodsInfoDto();
        return (AppGoodsInfoDto) BeanUtil.copyProperties(newBeeMallGoods, appGoodsInfoDto);
    }
}
