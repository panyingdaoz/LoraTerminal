//package com.kingbird.loraterminal.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//import best.oceanus.collect.dao.CollectNodeDao;
//import best.oceanus.collect.entity.Certification;
//import best.oceanus.collect.entity.CollectNode;
//
//@Service("CollectNodeService")
//public class CollectNodeService {
//    @Autowired
//    private CollectNodeDao collectNodeDao;
//
//    //服务端
//
//    /**
//     * 获取所有本中继的采集点
//     * 排除isDelete 的
//     * @param certification
//     * @return
//     */
//    public List<CollectNode> getAllCollectNode(Certification certification){
//        return collectNodeDao.findAllByRelayIDAndIsDelete(certification.getRelayID(),false);
//    }
//
//    //客户端
//
//    /**
//     * 清空并替换设备端所有采集点
//     * @param list
//     */
//    public int replaceCollectNode(List<CollectNode> list){
//        collectNodeDao.deleteAll();
//        List<CollectNode> replaceList=collectNodeDao.saveAll(list);
//        return replaceList.size();
//    }
//
//    /**
//     * 获取中继端所有采集点
//     * @return
//     */
//    public List<CollectNode> getAllCollectNode(){
//        return collectNodeDao.findAll();
//    }
//}
