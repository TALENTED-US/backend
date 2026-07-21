package org.scoula.config;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j2
class RootConfigTest {


    @Autowired
    private DataSource dataSource;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    @DisplayName("DataSource Connection")
    void dataSource() throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            log.info("Connection established");
            log.info(connection.getMetaData().getURL());
        }
    }

    @Test
    public void testSqlSessionFactory() throws SQLException {
        try (
            SqlSession session = sqlSessionFactory.openSession();
            Connection con = session.getConnection();
        ) {
            log.info(session);
            log.info(con);
        }
    }
}