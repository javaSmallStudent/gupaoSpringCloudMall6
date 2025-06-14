package com.gupaoedu.vip.mall.order.controller;

import com.gupaoedu.mall.util.RespCode;
import com.gupaoedu.mall.util.RespResult;
import com.gupaoedu.vip.mall.order.model.Order;
import com.gupaoedu.vip.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*****
 * @Author:
 * @Description:
 ****/
@RestController
@RequestMapping(value = "/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    /***
     * 添加订单
     */
    @PostMapping
    public RespResult add(@RequestBody Order order){
        //用户名字
        order.setUsername("gp");

        //下单
        Boolean bo = orderService.add(order);
        return bo? RespResult.ok() : RespResult.error(RespCode.ERROR);
    }


}
