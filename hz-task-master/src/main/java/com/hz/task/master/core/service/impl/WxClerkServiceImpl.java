package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.WxClerkMapper;
import com.hz.task.master.core.model.wx.WxClerkModel;
import com.hz.task.master.core.service.WxClerkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 小微旗下店员的Service层的实现层
 * @Author yoko
 * @Date 2020/5/25 17:35
 * @Version 1.0
 */
@Service
public class WxClerkServiceImpl<T> extends BaseServiceImpl<T> implements WxClerkService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private WxClerkMapper wxClerkMapper;

    public BaseDao<T> getDao() {
        return wxClerkMapper;
    }

    @Override
    public int updateWxClerkIsYn(WxClerkModel model) {
        return wxClerkMapper.updateWxClerkIsYn(model);
    }
}
