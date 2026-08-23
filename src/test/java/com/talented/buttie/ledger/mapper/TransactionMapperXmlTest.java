package com.talented.buttie.ledger.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.talented.buttie.ledger.domain.ClassificationMethod;
import com.talented.buttie.ledger.domain.ExpenseCategory;
import com.talented.buttie.ledger.domain.TransactionSource;
import com.talented.buttie.ledger.domain.TransactionType;
import com.talented.buttie.ledger.domain.TransactionVO;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class TransactionMapperXmlTest {

    @Test
    void 배치_INSERT의_TransactionVO_속성을_MyBatis가_모두_바인딩한다() throws Exception {
        Configuration configuration = new Configuration();
        try (Reader reader = Resources.getResourceAsReader("mapper/ledger/TransactionMapper.xml")) {
            new XMLMapperBuilder(
                reader,
                configuration,
                "mapper/ledger/TransactionMapper.xml",
                configuration.getSqlFragments()
            ).parse();
        }

        MappedStatement statement = configuration.getMappedStatement(
            "com.talented.buttie.ledger.mapper.TransactionMapper.insertTransactionsIgnoreDuplicates"
        );
        Map<String, Object> parameter = Map.of("list", List.of(createTransaction()));
        BoundSql boundSql = statement.getBoundSql(parameter);

        PreparedStatement preparedStatement = (PreparedStatement) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{PreparedStatement.class},
            (proxy, method, arguments) -> null
        );

        new DefaultParameterHandler(statement, parameter, boundSql)
            .setParameters(preparedStatement);

        assertTrue(boundSql.getSql().contains("ON DUPLICATE KEY UPDATE"));
    }

    private TransactionVO createTransaction() {
        return TransactionVO.builder()
            .userId(1L)
            .accountId(2L)
            .externalTransactionId("external-transaction-1")
            .transactionSource(TransactionSource.ACCOUNT)
            .classificationMethod(ClassificationMethod.ACCOUNT_INFLOW)
            .transactionContent("급여")
            .transactionType(TransactionType.INCOME)
            .expenseCategory(ExpenseCategory.OTHER_FINANCE)
            .transactionAmount(1_000)
            .transactionAt(LocalDateTime.of(2026, 8, 23, 12, 0))
            .analysisExcluded(false)
            .build();
    }
}
