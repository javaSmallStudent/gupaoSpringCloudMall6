package com.gupaoedu.vip.mall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gupaoedu.mall.util.RespResult;
import com.gupaoedu.vip.mall.cart.feign.CartFeign;
import com.gupaoedu.vip.mall.cart.model.Cart;
import com.gupaoedu.vip.mall.goods.feign.SkuFeign;
import com.gupaoedu.vip.mall.order.mapper.OrderMapper;
import com.gupaoedu.vip.mall.order.mapper.OrderSkuMapper;
import com.gupaoedu.vip.mall.order.model.Order;
import com.gupaoedu.vip.mall.order.model.OrderSku;
import com.gupaoedu.vip.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderSkuMapper orderSkuMapper;

    @Autowired
    private CartFeign cartFeign;

    @Autowired
    private SkuFeign skuFeign;

    /***
     * 添加订单
     */
    @Override
    public Boolean add(Order order) {
        //数据完善
        order.setId(IdWorker.getIdStr());   //ID
        order.setCreateTime(new Date());    //创建时间
        order.setUpdateTime(order.getCreateTime());

        //1、查询购物车数据
        RespResult<List<Cart>> cartResp = cartFeign.list(order.getCartIds());
        List<Cart> carts = cartResp.getData();
        if(carts==null || carts.size()==0){
            return false;
        }

        //2、递减库存
        skuFeign.dcount(carts);

        //3、添加订单明细
        int totalNum=0;
        int moneys = 0;
        for (Cart cart : carts) {
            //将Cart转成OrderSku
            OrderSku orderSku = JSON.parseObject(JSON.toJSONString(cart), OrderSku.class);
            orderSku.setId(IdWorker.getIdStr());
            orderSku.setOrderId(order.getId()); //提前赋值
            orderSku.setMoney(orderSku.getPrice()*orderSku.getNum());

            //添加
            orderSkuMapper.insert(orderSku);

            //统计计算
            totalNum +=orderSku.getNum();
            moneys += orderSku.getMoney();
        }

        //4、添加订单
        order.setTotalNum(totalNum);
        order.setMoneys(moneys);
        orderMapper.insert(order);

        //5、删除购物车数据
        cartFeign.delete(order.getCartIds());
        return true;
    }
}
