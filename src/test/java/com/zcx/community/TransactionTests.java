package com.zcx.community;

import com.zcx.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionTests {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void testSave01() {
        Object obj = alphaService.save01();
        System.out.println(obj);
    }

    @Test
    public void testSave02() {
        Object obj = alphaService.save02();
        System.out.println(obj);
    }

}
