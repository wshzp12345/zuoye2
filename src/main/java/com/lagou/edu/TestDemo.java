package com.lagou.edu;

import com.lagou.edu.dao.AccountDao;
import com.lagou.edu.dao.impl.JdbcAccountDaoImpl;
import com.lagou.edu.factory.BeanFactory;

public class TestDemo {
    public static void main(String[] args) throws NoSuchFieldException {
        JdbcAccountDaoImpl jdbcAccountDao = new JdbcAccountDaoImpl();
        System.out.println(AccountDao.class.isInstance(jdbcAccountDao));
        System.out.println(JdbcAccountDaoImpl.class.getDeclaredFields()[0].getName());
        BeanFactory beanFactory = new BeanFactory();

    }
}
