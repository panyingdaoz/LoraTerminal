//package com.kingbird.loraterminal.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import best.oceanus.collect.connect.StatusGroup;
//import best.oceanus.collect.dao.CollectNodeStatusDao;
//import best.oceanus.collect.dao.NodeStatusHaveStopDao;
//import best.oceanus.collect.datacollect.StatusEnum;
//import best.oceanus.collect.entity.CollectNodeStatus;
//import best.oceanus.collect.entity.NodeStatusHaveStop;
//
//@Service("CollectNodeStatusService")
//public class CollectNodeStatusService {
//    @Autowired
//    private CollectNodeStatusDao collectNodeStatusDao;
//    @Autowired
//    private NodeStatusHaveStopDao nodeStatusHaveStopDao;
//
//    public List<CollectNodeStatus> addAll(List<CollectNodeStatus> list){//保存客户端提交离线数据
//        ArrayList<String> clientIdList=new ArrayList<>(list.size());
//        String nodeID=null;
//        for(CollectNodeStatus nodes:list){
//            if(nodeID==null)nodeID=nodes.getNodeID();
//            clientIdList.add(nodes.getClientId());
//        }
//        List<CollectNodeStatus> findStatus=collectNodeStatusDao.findAllByNodeIDAndClientIdIn(nodeID,clientIdList);
//        if(findStatus!=null&&findStatus.size()>0)collectNodeStatusDao.deleteAll(findStatus);
//        saveAllToNodeStatusHaveStop(list);
//        return collectNodeStatusDao.saveAll(list);
//    }
//    public List<CollectNodeStatus> findAll(){
//        return collectNodeStatusDao.findAllByOrderByActionTimeAsc();
//    }
//    public void removeAll(List<CollectNodeStatus> list){
//        collectNodeStatusDao.deleteAll(list);
//    }
//    public void save(StatusGroup statusGroup){//保存客户端提交实时状态数据
//        CollectNodeStatus currentStatus=statusGroup.getCurrentStatus();
//        CollectNodeStatus beforceStatus = statusGroup.getBeforceStatus();
//        if(beforceStatus!=null){
//            CollectNodeStatus findBeforce=collectNodeStatusDao.findByClientIdAndNodeID(beforceStatus.getClientId(),beforceStatus.getNodeID());
//            if(findBeforce!=null){
//                findBeforce.setDurationTime(beforceStatus.getDurationTime());
//                collectNodeStatusDao.save(findBeforce);
//            }else{
//                collectNodeStatusDao.save(beforceStatus);
//            }
//        }else{//设备端第一条数据
//            CollectNodeStatus findBeforce =collectNodeStatusDao.findTop1ByActionTimeBeforeAndNodeIDOrderByActionTimeDesc(currentStatus.getActionTime(),currentStatus.getNodeID());
//            if(findBeforce!=null) {
//                findBeforce.setDurationTime(currentStatus.getActionTime().getTime()-findBeforce.getActionTime().getTime());
//                collectNodeStatusDao.save(findBeforce);
//            }
//        }
//        saveAllToNodeStatusHaveStop(currentStatus);
//        collectNodeStatusDao.save(currentStatus);
//    }
//    public void checkZeroAndReSet(){
//        List<CollectNodeStatus> list=collectNodeStatusDao.findAllByDurationTimeIsZero();
//        for(CollectNodeStatus zeroNode:list){
//            CollectNodeStatus nextNode=collectNodeStatusDao.findTop1ByActionTimeAfterAndNodeIDOrderByActionTimeAsc(zeroNode.getActionTime(),zeroNode.getNodeID());
//            if(nextNode!=null){
//                zeroNode.setDurationTime(nextNode.getActionTime().getTime()-zeroNode.getActionTime().getTime());
//                collectNodeStatusDao.save(zeroNode);
//            }
//        }
//    }
//    private void saveAllToNodeStatusHaveStop(List<CollectNodeStatus> list){//异步处理存储至包含停机状态的表
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ArrayList<String> clientIdList=new ArrayList<>(list.size());
//                String nodeID=null;
//                for(CollectNodeStatus nodes:list){
//                    if(nodeID==null)nodeID=nodes.getNodeID();
//                    clientIdList.add(nodes.getClientId());
//                }
//                List<CollectNodeStatus> findStatus=collectNodeStatusDao.findAllByNodeIDAndClientIdIn(nodeID,clientIdList);
//                if(findStatus!=null&&findStatus.size()>0)collectNodeStatusDao.deleteAll(findStatus);
//                for(CollectNodeStatus currentStatus:list){
//                    saveNodeStatusHaveStop(getNodeStatusHaveStopByNode(currentStatus));
//                }
//            }
//        }).start();
//    }
//    private void saveAllToNodeStatusHaveStop(CollectNodeStatus currentStatus){//异步处理存储至包含停机状态的表
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                saveNodeStatusHaveStop(getNodeStatusHaveStopByNode(currentStatus));
//            }
//        }).start();
//    }
//
//    private synchronized void saveNodeStatusHaveStop(NodeStatusHaveStop statusHaveStop){//保存状态至包含停机状态表
//        NodeStatusHaveStop beforeStatus=nodeStatusHaveStopDao.findTop1ByActionTimeBeforeAndNodeIDOrderByActionTimeDesc(statusHaveStop.getActionTime(),statusHaveStop.getNodeID());
//        if(beforeStatus!=null){
//            if(beforeStatus.getStatus()!= StatusEnum.STOPINPLAN&&beforeStatus.getStatus()!= StatusEnum.STOPOUTPLAN){
//                beforeStatus.setDurationTime(statusHaveStop.getActionTime().getTime()-beforeStatus.getActionTime().getTime());
//                nodeStatusHaveStopDao.save(beforeStatus);
//                nodeStatusHaveStopDao.save(statusHaveStop);
//            }
//        }else {
//            nodeStatusHaveStopDao.save(statusHaveStop);
//        }
//    }
//    private NodeStatusHaveStop getNodeStatusHaveStopByNode(CollectNodeStatus nodeStatus){
//        NodeStatusHaveStop statusHaveStop=new NodeStatusHaveStop();
//        statusHaveStop.setActionTime(nodeStatus.getActionTime());
//        statusHaveStop.setClientId(nodeStatus.getClientId());
//        statusHaveStop.setDurationTime(nodeStatus.getDurationTime());
//        statusHaveStop.setId(nodeStatus.getId());
//        statusHaveStop.setNodeID(nodeStatus.getNodeID());
//        statusHaveStop.setStatus(nodeStatus.getStatus());
//        return statusHaveStop;
//    }
//}
