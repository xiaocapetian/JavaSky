package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from sky_take_out.employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据  这里记住变量写法,上面的是sql的写法,是update_time ,
     *                          下面的是Java的写法,是createTime
     * @param employee
     */
    @Insert("insert into sky_take_out.employee (name, username, password, phone, sex, id_number, " +
            "status, create_time, update_time, create_user, update_user) " +
            "values (#{name},#{username},#{password},#{phone},#{sex},#{idNumber}," +
            "#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);
}
