package com.auction.onlineauction.OnlineAuction.service;

import com.auction.onlineauction.OnlineAuction.entity.AuctionAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IAuctionAddressService extends IService<AuctionAddress> {

    List<AuctionAddress> getAddressList(Long userId);

    AuctionAddress saveAddress(AuctionAddress address, Long userId);

    void deleteAddress(Long id, Long userId);

    void setDefault(Long id, Long userId);
}
