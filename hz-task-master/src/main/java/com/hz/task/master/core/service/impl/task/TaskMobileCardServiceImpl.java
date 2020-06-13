package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskMobileCardMapper;
import com.hz.task.master.core.model.mobilecard.MobileCardDataModel;
import com.hz.task.master.core.service.task.TaskMobileCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task：手机短信的Service的实现层
 * @Author yoko
 * @Date 2020/6/2 18:59
 * @Version 1.0
 */
@Service
public class TaskMobileCardServiceImpl <T> extends BaseServiceImpl<T> implements TaskMobileCardService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskMobileCardMapper taskMobileCardMapper;

    public BaseDao<T> getDao() {
        return taskMobileCardMapper;
    }

    @Override
    public List<MobileCardDataModel> getMobileCardDataList(Object obj) {
        return taskMobileCardMapper.getMobileCardDataList(obj);
    }

    @Override
    public int updateMobileCardDataStatus(Object obj) {
        return taskMobileCardMapper.updateMobileCardDataStatus(obj);
    }
}
