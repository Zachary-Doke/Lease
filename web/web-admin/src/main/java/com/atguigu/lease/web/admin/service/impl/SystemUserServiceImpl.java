package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.SystemPost;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.SystemPostService;
import com.atguigu.lease.web.admin.service.SystemUserService;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【system_user(员工信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private SystemPostService systemPostService;

    @Override
    public Page<SystemUserItemVo> pageSystemUser(Page<SystemUserItemVo> page, SystemUserQueryVo queryVo) {
        return systemUserMapper.pageSystemUser(page,queryVo);
    }

    @Override
    public SystemUserItemVo getSystemUserItemVoById(Long id) {
        SystemUser systemUser =this.getById(id);
        SystemPost systemPost = systemPostService.getById(systemUser.getPostId());

        SystemUserItemVo systemUserItemVo = new SystemUserItemVo();
        BeanUtils.copyProperties(systemUser,systemUserItemVo);
        systemUserItemVo.setPostName(systemPost.getName());

        return systemUserItemVo;
    }


}




