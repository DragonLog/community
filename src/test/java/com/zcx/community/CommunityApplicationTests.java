package com.zcx.community;

import com.zcx.community.dao.AlphaDao;
import com.zcx.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
class CommunityApplicationTests {

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AlphaService alphaService;

    @Autowired
    @Qualifier("alphaDaoImpl")
    private AlphaDao alphaDao;

    @Test
    void contextLoads() {
//        System.out.println(applicationContext);
//        System.out.println(alphaDao.select());
//        System.out.println(alphaService);
        System.out.println(simpleDateFormat.format(new Date()));
    }

}
