package com.project.shortlink.project.service.impl;

import com.project.shortlink.project.service.UrlTitleService;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 获取原链接标题实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    //@SneakyThrows在方法体中自动捕获并处理异常，将异常转换为非受检异常（Unchecked Exception）并抛出。
    @SneakyThrows
    @Override
    public String getLinkTitle(String url) {
        final URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            return document.title();
        }
        return "获取原链接标题失败！请手动添加";
    }
}
