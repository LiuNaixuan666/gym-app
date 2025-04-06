package com.liu.gymmanagement.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class LocalTimeTypeHandler extends BaseTypeHandler<LocalTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString()); // 将 LocalTime 转换为字符串
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String time = rs.getString(columnName);
        return time != null ? LocalTime.parse(time) : null;
    }

    @Override
    public LocalTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String time = rs.getString(columnIndex);
        return time != null ? LocalTime.parse(time) : null;
    }

    @Override
    public LocalTime getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String time = cs.getString(columnIndex);
        return time != null ? LocalTime.parse(time) : null;
    }
}
