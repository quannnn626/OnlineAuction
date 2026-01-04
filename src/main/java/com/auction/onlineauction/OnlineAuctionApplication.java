package com.auction.onlineauction;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.auction.onlineauction.OnlineAuction.mapper")
public class OnlineAuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineAuctionApplication.class, args);
    }

}
