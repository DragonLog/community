package com.zcx.community.dao.impl;

import com.zcx.community.dao.AlphaDao;
import org.springframework.stereotype.Repository;

@Repository
public class AlphaDaoImpl implements AlphaDao {
    @Override
    public String select() {
        return "访问数据库";
    }
}
