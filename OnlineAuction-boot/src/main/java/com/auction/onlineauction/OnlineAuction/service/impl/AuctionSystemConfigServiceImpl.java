package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionSystemConfig;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionSystemConfigMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionSystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuctionSystemConfigServiceImpl extends ServiceImpl<AuctionSystemConfigMapper, AuctionSystemConfig>
        implements IAuctionSystemConfigService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.05");

    @Override
    public BigDecimal getCommissionRate() {
        AuctionSystemConfig c = getOne(new QueryWrapper<AuctionSystemConfig>()
                .eq("config_key", AuctionSystemConfig.KEY_COMMISSION_RATE).eq("del_flag", 0));
        if (c == null || c.getConfigValue() == null || c.getConfigValue().trim().isEmpty()) {
            return DEFAULT_COMMISSION_RATE;
        }
        try {
            return new BigDecimal(c.getConfigValue().trim());
        } catch (Exception e) {
            return DEFAULT_COMMISSION_RATE;
        }
    }

    @Override
    public void setCommissionRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new RuntimeException("佣金比例需在 0～1 之间");
        }
        QueryWrapper<AuctionSystemConfig> q = new QueryWrapper<>();
        q.eq("config_key", AuctionSystemConfig.KEY_COMMISSION_RATE).eq("del_flag", 0);
        AuctionSystemConfig c = getOne(q);
        String val = rate.stripTrailingZeros().toPlainString();
        if (c != null) {
            c.setConfigValue(val);
            updateById(c);
        } else {
            c = new AuctionSystemConfig();
            c.setConfigKey(AuctionSystemConfig.KEY_COMMISSION_RATE);
            c.setConfigValue(val);
            c.setRemark("佣金比例，如0.05表示5%");
            c.setDelFlag(0);
            save(c);
        }
    }
}
