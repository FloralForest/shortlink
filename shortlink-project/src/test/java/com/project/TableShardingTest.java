package com.project;


import org.junit.jupiter.api.Test;

//分表 分片
public class TableShardingTest {
    private static final String SQL = "CREATE TABLE `t_link_goto_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `gid` varchar(32) DEFAULT 'default' COMMENT '分组标识',\n" +
            "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";

    @Test
    public void userTableSharding() {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL) + "%n" , i);
        }
    }
}
