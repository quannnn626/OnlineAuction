package com.auction.onlineauction.OnlineAuction.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源初始化配置
 * 确保应用启动时建立数据库连接
 *
 * @author MrYan
 * @since 2026-01-12
 */
@Component
@Order(1) // 设置优先级，确保在其他组件之前执行
public class DataSourceConfig implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        // 应用启动时测试数据库连接
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("数据库连接初始化成功！");
            }
        } catch (SQLException e) {
            System.err.println("数据库连接初始化失败：" + e.getMessage());
            throw new RuntimeException("数据库连接失败，请检查数据库配置和MySQL服务是否启动", e);
        }
    }
}
