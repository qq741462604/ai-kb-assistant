package com.aiassistant.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class ListToJsonTypeHandler extends BaseTypeHandler<List<String>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, mapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Error converting List<String> to JSON", e);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            String json = rs.getString(columnName);
            if (json == null) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON to List<String>", e);
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            String json = rs.getString(columnIndex);
            if (json == null) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON to List<String>", e);
        }
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            String json = cs.getString(columnIndex);
            if (json == null) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON to List<String>", e);
        }
    }
}
