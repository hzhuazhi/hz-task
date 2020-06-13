package com.hz.task.master.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.mapper.MobileCardMapper;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import com.hz.task.master.core.service.MobileCardService;
import com.hz.task.master.util.ComponentUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 手机卡的Service层的实现层
 * @Author yoko
 * @Date 2020/5/18 17:23
 * @Version 1.0
 */
@Service
public class MobileCardServiceImpl<T> extends BaseServiceImpl<T> implements MobileCardService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private MobileCardMapper mobileCardMapper;

    public BaseDao<T> getDao() {
        return mobileCardMapper;
    }

    @Override
    public int upUseStatus(MobileCardModel model) {
        return mobileCardMapper.upUseStatus(model);
    }

    @Override
    public MobileCardModel getMobileCard(MobileCardModel model, int isCache) throws Exception {
        MobileCardModel dataModel = null;
        if (isCache == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO){
            String strKeyCache = CachedKeyUtils.getCacheKey(TkCacheKey.MOBILE_CARD, model.getPhoneNum());
            String strCache = (String) ComponentUtil.redisService.get(strKeyCache);
            if (!StringUtils.isBlank(strCache)) {
                // 从缓存里面获取数据
                dataModel = JSON.parseObject(strCache, MobileCardModel.class);
            } else {
                //查询数据库
                dataModel = (MobileCardModel) mobileCardMapper.findByObject(model);
                if (dataModel != null && dataModel.getId() != ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO) {
                    // 把数据存入缓存
                    ComponentUtil.redisService.set(strKeyCache, JSON.toJSONString(dataModel, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty), FIVE_MIN);
                }
            }
        }else {
            // 直接查数据库
            // 查询数据库
            dataModel = (MobileCardModel) mobileCardMapper.findByObject(model);
        }
        return dataModel;
    }
}
