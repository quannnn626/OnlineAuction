package com.auction.onlineauction.OnlineAuction.service.impl;

import com.auction.onlineauction.OnlineAuction.entity.AuctionAddress;
import com.auction.onlineauction.OnlineAuction.mapper.AuctionAddressMapper;
import com.auction.onlineauction.OnlineAuction.service.IAuctionAddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuctionAddressServiceImpl extends ServiceImpl<AuctionAddressMapper, AuctionAddress> implements IAuctionAddressService {

    @Autowired
    private AuctionAddressMapper addressMapper;

    @Override
    public List<AuctionAddress> getAddressList(Long userId) {
        return list(new QueryWrapper<AuctionAddress>()
                .eq("user_id", userId)
                .eq("del_flag", 0)
                .orderByDesc("is_default")
                .orderByDesc("update_time"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuctionAddress saveAddress(AuctionAddress address, Long userId) {
        if (address.getId() != null) {
            AuctionAddress exist = getById(address.getId());
            if (exist == null || !exist.getUserId().equals(userId)) {
                throw new RuntimeException("地址不存在");
            }
            exist.setReceiverName(address.getReceiverName());
            exist.setReceiverPhone(address.getReceiverPhone());
            exist.setProvince(address.getProvince());
            exist.setCity(address.getCity());
            exist.setDistrict(address.getDistrict());
            exist.setDetailAddress(address.getDetailAddress());
            exist.setUpdateTime(LocalDateTime.now());
            updateById(exist);
            return exist;
        }

        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        address.setDelFlag(0);
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        if (address.getIsDefault() == 1) {
            addressMapper.clearDefaultByUserId(userId);
        }
        save(address);
        return address;
    }

    @Override
    public void deleteAddress(Long id, Long userId) {
        AuctionAddress exist = getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        exist.setDelFlag(1);
        exist.setUpdateTime(LocalDateTime.now());
        updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id, Long userId) {
        AuctionAddress exist = getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        addressMapper.clearDefaultByUserId(userId);
        exist.setIsDefault(1);
        exist.setUpdateTime(LocalDateTime.now());
        updateById(exist);
    }
}
