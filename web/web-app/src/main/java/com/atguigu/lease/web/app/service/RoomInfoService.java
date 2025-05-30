package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface RoomInfoService extends IService<RoomInfo> {
    Page<RoomItemVo> pageRoomItemVo(Page<RoomItemVo> page, RoomQueryVo queryVo);

    RoomDetailVo getRoomDetailVo(Long id);

    Page<RoomItemVo> pageRoomItemVoByApartmentId(Page<RoomItemVo> page, Long id);
}
