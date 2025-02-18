package com.project.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化限流配置
 * 继承了InitializingBean spring启动时会初始化该方法
 */
@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule createOrderRule = new FlowRule();
        //创建规则名
        createOrderRule.setResource("create_short-link");
        //设置规则为每秒查询率
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //设置每秒访问上限
        createOrderRule.setCount(1000);
        rules.add(createOrderRule);
        FlowRuleManager.loadRules(rules);
    }
}
