package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo> implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Autowired
    private FacilityInfoMapper  facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired LeaseTermMapper  leaseTermMapper;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOrUpdateRoomSubmitVo(RoomSubmitVo roomSubmitVo) {
        Long id = roomSubmitVo.getId();
        boolean isUpdate = id!= null;

        if(isUpdate){

            // 1.删除图片列表
            LambdaUpdateWrapper<GraphInfo> graphInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            graphInfoLambdaUpdateWrapper
                    .eq(GraphInfo::getItemType, ItemType.ROOM)
                    .eq(GraphInfo::getItemId,  id);
            graphInfoService.remove(graphInfoLambdaUpdateWrapper);

            // 2.删除属性列表
            LambdaUpdateWrapper<RoomAttrValue> roomAttrValueLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            roomAttrValueLambdaUpdateWrapper.eq(RoomAttrValue::getRoomId,  id);
            roomAttrValueService.remove(roomAttrValueLambdaUpdateWrapper);

            // 3.删除配套列表
            LambdaQueryWrapper<RoomFacility> roomFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityLambdaQueryWrapper.eq(RoomFacility::getRoomId,  id);
            roomFacilityService.remove(roomFacilityLambdaQueryWrapper);

            // 4.删除标签列表
            LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelLambdaQueryWrapper.eq(RoomLabel::getRoomId,id);
            roomLabelService.remove(roomLabelLambdaQueryWrapper);

            // 5.删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getRoomId,  id);
            roomPaymentTypeService.remove(roomPaymentTypeLambdaQueryWrapper);

            // 6.删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId,  id);
            roomLeaseTermService.remove(roomLeaseTermLambdaQueryWrapper);

            // 7.删除缓存
            redisTemplate.delete(RedisConstant.APP_LOGIN_PREFIX + roomSubmitVo.getId());

        }

        // 1.保存基础信息
        super.saveOrUpdate(roomSubmitVo);

        // 2.保存图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            List<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo:graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(id);
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        // 3.保存属性列表
        List<Long> roomAttrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(roomAttrValueIds)){
            List<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for(Long roomAttrValueId:roomAttrValueIds){
                RoomAttrValue roomAttrValue=RoomAttrValue
                        .builder()
                        .roomId(id)
                        .attrValueId(roomAttrValueId)
                        .build();
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }

        // 4.保存配套列表
        List<Long> roomFacilityIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(roomFacilityIds)){
            List<RoomFacility> roomFacilityList = new ArrayList<>();
            for(Long roomFacilityId:roomFacilityIds){
                RoomFacility roomFacility=RoomFacility
                        .builder()
                        .roomId(id)
                        .facilityId(roomFacilityId)
                        .build();
                roomFacilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityList);
        }

        // 5.保存标签列表
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            List<RoomLabel> roomLabelList = new ArrayList<>();
            for(Long labelInfoId:labelInfoIds){
                RoomLabel roomLabel=RoomLabel
                        .builder()
                        .roomId(id)
                        .labelId(labelInfoId)
                        .build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }

        // 6.保存支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            List<RoomPaymentType> paymentTypeList = new ArrayList<>();
            for(Long paymentTypeId:paymentTypeIds){
                RoomPaymentType roomPaymentType=RoomPaymentType
                        .builder()
                        .roomId(id)
                        .paymentTypeId(paymentTypeId)
                        .build();
                paymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(paymentTypeList);
        }

        // 7.保存可选租期列表
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(leaseTermIds)){
            List<RoomLeaseTerm> leaseTermList = new ArrayList<>();
            for(Long leaseTermId:leaseTermIds){
                RoomLeaseTerm roomLeaseTerm=RoomLeaseTerm
                        .builder()
                        .roomId(id)
                        .leaseTermId(leaseTermId)
                        .build();
                leaseTermList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(leaseTermList);
        }

    }

    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> roomItemVoPage, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(roomItemVoPage,queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = super.getById(id);
        if (roomInfo==null){
            throw new RuntimeException("房间不存在");
        }
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());

        List<GraphVo> GraphVoList=graphInfoMapper.selectGraph(ItemType.ROOM,id);

        List<AttrValueVo> attrValueVoList=attrValueMapper.selectListByRoomId(id);

        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByRoomId(id);

        List<LabelInfo>  labelInfoList=labelInfoMapper.selectListByRoomId(id);

        List<PaymentType> paymentTypeList=paymentTypeMapper.selectListByRoomId(id);

        List<LeaseTerm> leaseTermList=leaseTermMapper.selectListByRoomId(id);

        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo,roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(GraphVoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);
        return roomDetailVo;
    }

    @Override
    public void removeAllById(Long id) {
        super.removeById(id);

        // 1.删除图片列表
        LambdaUpdateWrapper<GraphInfo> graphInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        graphInfoLambdaUpdateWrapper
                .eq(GraphInfo::getItemType, ItemType.ROOM)
                .eq(GraphInfo::getId,  id);
        graphInfoService.remove(graphInfoLambdaUpdateWrapper);

        // 2.删除属性列表
        LambdaUpdateWrapper<RoomAttrValue> roomAttrValueLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        roomAttrValueLambdaUpdateWrapper.eq(RoomAttrValue::getRoomId,  id);
        roomAttrValueService.remove(roomAttrValueLambdaUpdateWrapper);

        // 3.删除配套列表
        LambdaQueryWrapper<RoomFacility> roomFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityLambdaQueryWrapper.eq(RoomFacility::getRoomId,  id);
        roomFacilityService.remove(roomFacilityLambdaQueryWrapper);

        // 4.删除标签列表
        LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelLambdaQueryWrapper.eq(RoomLabel::getRoomId,id);
        roomLabelService.remove(roomLabelLambdaQueryWrapper);

        // 5.删除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getRoomId,  id);
        roomPaymentTypeService.remove(roomPaymentTypeLambdaQueryWrapper);

        // 6.删除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId,  id);
        roomLeaseTermService.remove(roomLeaseTermLambdaQueryWrapper);
    }


}




