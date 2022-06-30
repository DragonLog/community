package com.zcx.community.dao.impl;

import com.zcx.community.dao.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

//@Repository("AnotherAlphaDaoImpl")
@Repository
//@Primary
public class AnotherAlphaDaoImpl implements AlphaDao {
    @Override
    public String select() {
        return "另一个方法访问数据库";
    }
}
