package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.WxFriendMapper;
import com.hz.task.master.core.service.WxFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 小微加好友记录的Service层的实现层
 * @Author yoko
 * @Date 2020/8/1 16:58
 * @Version 1.0
 */
@Service
public class WxFriendServiceImpl<T> extends BaseServiceImpl<T> implements WxFriendService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private WxFriendMapper wxFriendMapper;

    public BaseDao<T> getDao() {
        return wxFriendMapper;
    }
}
