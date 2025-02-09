package com.project.shortlink.project.service;

import org.springframework.stereotype.Service;

/**
 * 获取原链接标题接口层
 */
public interface UrlTitleService {

    String getLinkTitle(String url);
}
