package lib;
import core.Entity;
import entity.Member;
import jdbc.JdbcConnector;
import utils.EntityExtractor;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JpaImpl<ENTITY,ID> implements JpaRepository<ENTITY, ID> {
    private final ConcurrentHashMap<ENTITY,ID> data = new ConcurrentHashMap<>();
    private final Class<?> c;
    private static JpaImpl instance;

    static {
        try { instance = new JpaImpl(Member.class);}
        catch (Exception e) {throw new RuntimeException("JPA LOAD FAILED");}
    }

    public JpaImpl(Class<?> c) {
        this.c = c;
    }
    public static JpaImpl getInstance(){
        return JpaImpl.instance;
    }

    @Override
    public Optional<Entity> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public List<Entity> findAll() throws IntrospectionException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, SQLException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        // 쿼리 생성기로 쪼개야됨
        String tableName = EntityExtractor.getTableNameFromEntity(c);
        Field[] fields = Member.class.getDeclaredFields();

        StringBuilder queryBuilder = new StringBuilder("select").append(" ");
        List<String> queryFieldList = Collections.synchronizedList(new ArrayList<>());
        queryBuilder.append(
                Arrays.stream(fields).map(field -> {
                    queryFieldList.add(field.getName());
                    return field.getName();
                }).collect(Collectors.joining(", "))
        );
        queryBuilder.append(" from ").append(tableName);
        System.out.println(queryBuilder.toString());

        try {
            JdbcConnector jdbcConnector = new JdbcConnector();
            ResultSet rs = jdbcConnector.selectAll(queryBuilder.toString());

            while (rs.next()) {
                for (String field : queryFieldList) {
                    System.out.println(rs.getString(field));
                }
            }

            Constructor<?> constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            Member o = (Member) constructor.newInstance();
            Field idField = o.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(o, 2L);
            idField.setAccessible(false);

        } catch (ClassNotFoundException | SQLException e) {
            e.getStackTrace();
        } catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void save(Entity entity) {

    }
}
