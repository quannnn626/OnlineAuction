package com.auction.onlineauction;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.auction.onlineauction.OnlineAuction.mapper")
@EnableScheduling
public class OnlineAuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineAuctionApplication.class, args);
    }

}
