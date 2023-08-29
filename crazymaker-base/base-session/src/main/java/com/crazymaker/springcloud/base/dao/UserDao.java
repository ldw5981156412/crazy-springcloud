package com.crazymaker.springcloud.base.dao;

import com.crazymaker.springcloud.base.dao.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserDao extends JpaRepository<UserPO, Long>, JpaSpecificationExecutor<UserPO> {

    UserPO findByUserId(Long id);

    List<UserPO> findAllByUsername(String loginName);
}
