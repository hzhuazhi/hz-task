package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskWxOrderMapper;
import com.hz.task.master.core.model.wx.WxOrderModel;
import com.hz.task.master.core.service.task.TaskWxOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:小微给出订单记录的Service层的实现层
 * @Author yoko
 * @Date 2020/8/8 17:08
 * @Version 1.0
 */
@Service
public class TaskWxOrderServiceImpl<T> extends BaseServiceImpl<T> implements TaskWxOrderService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskWxOrderMapper taskWxOrderMapper;


    public BaseDao<T> getDao() {
        return taskWxOrderMapper;
    }

    @Override
    public List<Long> getWxIdByOrderList(WxOrderModel model) {
        return taskWxOrderMapper.getWxIdByOrderList(model);
    }

    @Override
    public List<WxOrderModel> getWxOrderList(WxOrderModel model) {
        return taskWxOrderMapper.getWxOrderList(model);
    }
}
