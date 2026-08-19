package com.talented.buttie.catalog.mapper;

import com.talented.buttie.catalog.domain.PolicyCategory;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(PolicyCategory.class)
public class PolicyCategoryTypeHandler extends BaseTypeHandler<PolicyCategory> {

    @Override
    public void setNonNullParameter(PreparedStatement statement, int index, PolicyCategory parameter, JdbcType jdbcType)
        throws SQLException {
        statement.setString(index, parameter.getValue());
    }

    @Override
    public PolicyCategory getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return PolicyCategory.from(resultSet.getString(columnName));
    }

    @Override
    public PolicyCategory getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return PolicyCategory.from(resultSet.getString(columnIndex));
    }

    @Override
    public PolicyCategory getNullableResult(CallableStatement statement, int columnIndex) throws SQLException {
        return PolicyCategory.from(statement.getString(columnIndex));
    }
}
