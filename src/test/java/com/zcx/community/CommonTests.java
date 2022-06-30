package com.zcx.community;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CommonTests {

    @Test
    public void test01() {
        System.out.println(StringUtils.isBlank(null));
    }

}
