package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.SystemMessage;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.CommonUtil;

/**
 * Created by Administrator on 2017/7/8.
 * 大厅方法类
 */
public class HallFunctions extends TCPGameFunctions{

    /**
     * 大厅查询战绩
     * @param session
     * @param readData
     */
    public static void interface_100002(IoSession session, ProtocolData readData){
        logger.I("大厅查询战绩,interfaceId -> 100002");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer page = obj.getInteger("page");
        List<PlayerRecord> pageSize = userService.findPlayerRecordByUserId(userId,0,99999999);
        List<PlayerRecord> list = userService.findPlayerRecordByUserId(userId,(page-1)*Cnst.PAGE_SIZE,Cnst.PAGE_SIZE);
        Map<String,Object> info = new HashMap<>();
        if (list!=null&&list.size()>0){
        	List<Map<String,Object>> infos = new ArrayList<>();
            for(int i=0;i<list.size();i++){
                PlayerRecord pr = list.get(i);
                Map<String,Object> map = new LinkedHashMap<>();
                map.put("roomId",pr.getRoomId());
                map.put("startTimne",pr.getStartTime());
                
                Map<String,String> userInfos = new LinkedHashMap<>();
                userInfos.put(pr.getEastUserName()+"_"+pr.getEastUserId(),String.valueOf(pr.getEastUserMoneyRecord()));
                userInfos.put(pr.getSouthUserName()+"_"+pr.getSouthUserId(),String.valueOf(pr.getSouthUserMoneyRecord()));
                userInfos.put(pr.getWestUserName()+"_"+pr.getWestUserId(),String.valueOf(pr.getWestUserMoneyRecord()));
                userInfos.put(pr.getNorthUserName()+"_"+pr.getNorthUserId(),String.valueOf(pr.getNorthUserMoneyRecord()));
                
                map.put("userInfos", userInfos);
                infos.add(map);
            }
            info.put("infos",infos);
        }
        info.put("pages",pageSize==null?0:pageSize.size()%Cnst.PAGE_SIZE==0?pageSize.size()/Cnst.PAGE_SIZE:(pageSize.size()/Cnst.PAGE_SIZE+1));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 大厅查询系统消息
     * @param session
     * @param readData
     */
    public static void interface_100003(IoSession session, ProtocolData readData){
        logger.I("大厅查询系统消息,interfaceId -> 100003");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer page = obj.getInteger("page");
        List<SystemMessage> info = userService.getSystemMessage(null,(page-1)*Cnst.PAGE_SIZE,Cnst.PAGE_SIZE);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 大厅请求联系我们
     * @param session
     * @param readData
     */
    public static void interface_100004(IoSession session, ProtocolData readData){
        logger.I("大厅请求联系我们,interfaceId -> 100004");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Map<String,String> info = new HashMap<>();
        info.put("connectionInfo",userService.getConectUs());
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 大厅请求帮助信息
     * @param session
     * @param readData
     */
    public static void interface_100005(IoSession session, ProtocolData readData){
        logger.I("大厅请求帮助信息,interfaceId -> 100005");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Map<String,String> info = new HashMap<>();
        info.put("help","帮助帮助帮助帮助帮助帮助帮助帮助帮助");
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 反馈信息
     * @param session
     * @param readData
     */
    public static void interface_100006(IoSession session, ProtocolData readData){
        logger.I("反馈信息,interfaceId -> 100006");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        String content = obj.getString("content");
        String tel = obj.getString("tel");
        Long createTime = new Date().getTime();
        //插入反馈信息
        Feedback back = new Feedback();
        back.setContent(content);
        back.setCreateTime(new Date().getTime());
        back.setTel(tel);
        back.setUserId(userId);
        userService.userFeedback(back);
        //返回反馈信息
        Map<String,String> info = new HashMap<>();
        info.put("content","感谢您的反馈！");
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 创建房间
     * @param session
     * @param readData
     */
    public static void interface_100007(IoSession session, ProtocolData readData) throws Exception{
        logger.I("创建房间,interfaceId -> 100007");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer circleNum = obj.getInteger("circleNum");
        Integer dilou = obj.getInteger("dilou");
        Integer roomType = obj.getInteger("roomType");
        Integer lz = obj.getInteger("lz");
        
        if (circleNum==null||dilou==null||roomType==null||lz==null) {
        	JSONObject result = getErrorJsonObj(interfaceId,0,"参数有误！");
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            return;
		}
        
        

        Player p = getPlayerFromSession(session);

        if(p.getMoney()<Cnst.moneyMap.get(circleNum)){//玩家房卡不足
            playerMoneyNotEnough(interfaceId,session,roomType);
            return ;
        }
        if (p.getRoomId()!=null){//已存在其他房间
            playerExistOtherRoom(interfaceId,session);
            return ;
        }
        
        
        if (roomType.equals(Cnst.ROOM_TYPE_2)) {//自由模式开房，玩家房卡必须大于等于100
			if (p.getMoney()<100) {
				playerMoneyNotEnough(interfaceId,session,roomType);
	            return ;
			}
		}
        
        if (roomType!=null&&roomType.equals(Cnst.ROOM_TYPE_2)) {
            int num = 0;
            if (roomMap!=null&&roomMap.size()>0) {
            	for(Integer roomId:roomMap.keySet()){
            		RoomResp room = roomMap.get(roomId);
            		if (room.getCreateId().equals(userId)&&room.getRoomType().equals(roomType)&&!room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
						num++;
					}
            	}
            	if (num>=10) {
					roomEnough(interfaceId, session);
					return;
				}
            }
        }
        

        String createTime = String.valueOf(new Date().getTime());
        RoomResp room = new RoomResp();
        room.setCreateId(userId);//创建人
        room.setStatus(Cnst.ROOM_STATE_CREATED);//房间状态为等待玩家入坐
        room.setCircleNum(circleNum);//房间的总圈数
        room.setLastNum(circleNum);//剩余圈数
        room.setCircleWind(Cnst.WIND_EAST);//圈风为东风
        room.setRoomType(roomType);//房间类型：房主模式或者自由模式
        room.setDilou(dilou);//是否允许低喽;1允许；0不允许
        room.setCreateTime(createTime);//创建时间，long型数据
        room.setOpenName(p.getUserName());
        room.setLz(lz);//是否允许拉庄
        
    	
    	//初始化大接口的id
    	room.setWsw_sole_action_id(1);
    	room.setWsw_sole_main_id(1);

        //toEdit  需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
        while (true){
            room.setRoomId(CommonUtil.getGivenRamdonNum(6));//设置随机房间密码
            if (!roomMap.containsKey(room.getRoomId())){
                break;
            }
        }
        
        Long[] userIds = new Long[4];
        
        Map<String,Object> info = new HashMap<>();
        //处理开房模式
       if(roomType.equals(Cnst.ROOM_TYPE_1)){//房主模式
	        //设置用户信息
	        p.setPosition(getWind(null));//设置庄家位置为东
	        if (p.getPosition().equals(Cnst.WIND_EAST)){
	            p.setZhuang(true);
	            room.setZhuangId(userId);
	        }else{
	            p.setZhuang(false);
	        }
	        p.setPlayStatus(Cnst.PLAYER_STATE_IN);//进入房间状态
	        p.setRoomId(room.getRoomId());
	        p.setJoinIndex(1);
	        p.initPlayer(p.getRoomId(),p.getPosition(),p.getZhuang(),Cnst.PLAYER_STATE_IN,p.getScore(),p.getHuNum(),p.getLoseNum());
	        userIds[p.getPosition()-1] = p.getUserId();
	        info.put("reqState",Cnst.REQ_STATE_1);
	        info.put("playerNum",1);
	       	p.setMoney(p.getMoney()-Cnst.moneyMap.get(room.getCircleNum()));
		}else if(roomType.equals(Cnst.ROOM_TYPE_2)){//自由模式
			//突然发现什么都不需要处理……
	       	p.setMoney(p.getMoney()-Cnst.moneyMap.get(room.getCircleNum()));
	        info.put("reqState",Cnst.REQ_STATE_10);
	        info.put("money",p.getMoney());
		}else{
        	illegalRequest(interfaceId, session);
        	return;
		}
        room.setPlayerIds(userIds);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        
        roomMap.put(room.getRoomId(), room);

        //解散房间命令
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_1);
    }

    /**
     * 加入房间
     * @param session
     * @param readData
     */
    public static void interface_100008(IoSession session, ProtocolData readData) throws Exception{
        logger.I("加入房间,interfaceId -> 100008");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer roomId = obj.getInteger("roomSn");
        

        Player p = getPlayerFromSession(session);
        
        //已经在其他房间里
        if (p.getRoomId()!=null){//玩家已经在非当前请求进入的其他房间里
            playerExistOtherRoom(interfaceId,session);
            return;
        }
        //房间不存在
        RoomResp room = roomMap.get(roomId);
        if (room==null){
            roomDoesNotExist(interfaceId,session);
            return;
        }
        //房间人满
        Long[] userIds = room.getPlayerIds();
        boolean hasNull = false;
        int jionIndex = 0;
        for(Long uId:userIds){
        	if (uId==null) {
				hasNull = true;
			}else{
				jionIndex++;
			}
        }
        if (!hasNull) {
        	roomFully(interfaceId,session);
            return;
		}
        
        //设置用户信息
        p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);//准备状态
        p.setRoomId(roomId);
        p.setPosition(getWind(userIds));
        if (p.getPosition().equals(Cnst.WIND_EAST)){
            p.setZhuang(true);
            room.setZhuangId(userId);
        }else{
            p.setZhuang(false);
        }
        userIds[p.getPosition()-1] = p.getUserId();
        p.initPlayer(p.getRoomId(),p.getPosition(),p.getZhuang(),Cnst.PLAYER_STATE_IN,p.getScore(),p.getHuNum(),p.getLoseNum());
        
        p.setJoinIndex(jionIndex+1);

        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
        info.put("playerNum",jionIndex+1);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 用户点击同意协议
     * @param session
     * @param readData
     */
    public static void interface_100009(IoSession session, ProtocolData readData) throws Exception{
        logger.I("用户点击同意协议,interfaceId -> 100009");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Player p = getPlayerFromSession(session);
        if (p==null){
            illegalRequest(interfaceId,session);
            return;
        }
        p.setUserAgree(1);
        Map<String,Object> info = new JSONObject();
        info.put("reqState",Cnst.REQ_STATE_1);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);

        /*刷新数据库，用户同意协议*/
        userService.updateUserAgree(p.getUserId());
    }
    
    
    /**
     * 查看代开房间列表
     * @param session
     * @param readData
     */
    public static void interface_100010(IoSession session, ProtocolData readData) throws Exception{
        logger.I("查看代开房间列表,interfaceId -> 100010");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        List<Map<String,Object>> info = new ArrayList<Map<String,Object>>();

        if (roomMap!=null&&roomMap.size()>0) {
        	for(Integer roomId:roomMap.keySet()){
        		RoomResp room = roomMap.get(roomId);
            	if (room.getCreateId().equals(userId)&&!room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
            		Map<String,Object> map = new HashMap<String, Object>();
            		map.put("roomId", room.getRoomId());
            		map.put("createTime", room.getCreateTime());
            		map.put("circleNum", room.getCircleNum());
            		map.put("dilou", room.getDilou());
            		map.put("status", room.getStatus());
            		map.put("lz", room.getLz());

                    List<Map<String,Object>> playerInfo = new ArrayList<Map<String,Object>>();

            		List<Player> list = getPlayerList(room);
            		if (list!=null&&list.size()>0) {
						for(Player p:list){
							Map<String,Object> pinfo = new HashMap<String, Object>();
							pinfo.put("userId", p.getUserId());
							pinfo.put("position", p.getPosition());
							pinfo.put("openName", p.getUserName());
							pinfo.put("openImg", p.getUserImg());
							pinfo.put("zhuang", p.getZhuang());
							pinfo.put("status", p.getStatus());
							playerInfo.add(pinfo);
						}
					}
            		map.put("playerInfo", playerInfo);
            		info.add(map);
				}
            }
		}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    
    /**
     * 查看历史代开房间列表
     * @param session
     * @param readData
     */
    public static void interface_100011(IoSession session, ProtocolData readData) throws Exception{
        logger.I("查看历史代开房间列表,interfaceId -> 100011");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer page = obj.getInteger("page");
        
        List<Map<String,Object>> data = roomService.getMyCreateRoom(userId, (page-1)*Cnst.PAGE_SIZE,Cnst.PAGE_SIZE, Cnst.ROOM_TYPE_2);
        Map<String,Object> infos = new HashMap<String, Object>();
        if (data!=null&&data.size()>0) {
			for(Map<String,Object> rinfo:data){
				List<Map<String,Object>> playerInfo = new ArrayList<Map<String,Object>>();
				Map<String,Object> info = new HashMap<String, Object>();
				info.put("openName", rinfo.get("openName1"));
				info.put("openImg", rinfo.get("openImg1"));
				info.put("score", rinfo.get("score1"));
				info.put("lz", rinfo.get("lz"));
				info.put("position", Cnst.WIND_EAST);
				playerInfo.add(info);
				
				info = new HashMap<String, Object>();
				info.put("openName", rinfo.get("openName2"));
				info.put("openImg", rinfo.get("openImg2"));
				info.put("score", rinfo.get("score2"));
				info.put("position", Cnst.WIND_SOUTH);
				playerInfo.add(info);
				
				info = new HashMap<String, Object>();
				info.put("openName", rinfo.get("openName3"));
				info.put("openImg", rinfo.get("openImg3"));
				info.put("score", rinfo.get("score3"));
				info.put("position", Cnst.WIND_WEST);
				playerInfo.add(info);
				
				info = new HashMap<String, Object>();
				info.put("openName", rinfo.get("openName4"));
				info.put("openImg", rinfo.get("openImg4"));
				info.put("score", rinfo.get("score4"));
				info.put("position", Cnst.WIND_NORTH);
				playerInfo.add(info);

				rinfo.remove("openName1");
				rinfo.remove("openImg1");
				rinfo.remove("score1");
				rinfo.remove("openName2");
				rinfo.remove("openImg2");
				rinfo.remove("score2");
				rinfo.remove("openName3");
				rinfo.remove("openImg3");
				rinfo.remove("score3");
				rinfo.remove("openName4");
				rinfo.remove("openImg4");
				rinfo.remove("score4");
				
				rinfo.put("playerInfo", playerInfo);
			}
			 infos.put("pages", 1);
		}else{
			data = new ArrayList<Map<String,Object>>();
		     infos.put("pages", 0);
		}
 
        infos.put("roomInfo", data);
        JSONObject result = getJSONObj(interfaceId,1,infos);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    
    
    
    
    
    /**
     * 代开模式中踢出玩家
     * @param session
     * @param readData
     */
    public static void interface_100012(IoSession session, ProtocolData readData){
    	logger.I("代开模式中踢出玩家,interfaceId -> 100012");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer roomId = obj.getInteger("roomSn");
        
        //房间不存在
        RoomResp room = roomMap.get(roomId);
        if (room==null){
            roomDoesNotExist(interfaceId,session);
            return;
        }
        try {
            //验证解散人是否是真正的房主
            Long createId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
            if (createId==null||!createId.equals(room.getCreateId())) {
    			illegalRequest(interfaceId, session);
    			return;
    		}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}
        //房间已经开局
        if (!room.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
        	roomIsGaming(interfaceId,session);
        	return;
		}

        List<Player> list = getPlayerList(room);
        boolean hasPlayer = false;//列表中有当前玩家
        for(Player p:list){
        	if (p.getUserId().equals(userId)) {
		        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);

		      //刷新房间用户列表
				Long[] pids = room.getPlayerIds();
		        if (pids!=null) {
					for(int i=0;i<pids.length;i++){
						if (userId.equals(pids[i])) {
							pids[i] = null;
							break;
						}
					}
				}
		        hasPlayer = true;
				MessageFunctions.interface_100107(userId, Cnst.EXIST_TYPE_EXIST,list);
				break;
			}
        }
        
        Map<String,String> info = new HashMap<String, String>();
        info.put("reqState", String.valueOf(hasPlayer?Cnst.REQ_STATE_1:Cnst.REQ_STATE_8));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 代开模式房主解散房间
     * @param session
     * @param readData
     */
    public static void interface_100013(IoSession session, ProtocolData readData){
    	logger.I("代开模式房主解散房间,interfaceId -> 100013");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        
      //房间不存在
        RoomResp room = roomMap.get(roomId);
        if (room==null){
            roomDoesNotExist(interfaceId,session);
            return;
        }
        
        try {
            //验证解散人是否是真正的房主
            Long createId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
            if (createId==null||!createId.equals(room.getCreateId())) {
    			illegalRequest(interfaceId, session);
    			return;
    		}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}
        //房间已经开局
        if (!room.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
        	roomIsGaming(interfaceId,session);
        	return;
		}

        
        
        List<Player> players = getPlayerList(room);
        if (players!=null&&players.size()>0) {
			for(Player p:players){
		        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
			}
		}
        
        MessageFunctions.interface_100107(room.getCreateId(), Cnst.EXIST_TYPE_DISSOLVE,players);
        roomMap.remove(roomId);
        notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,false);
        //归还玩家房卡
        Player cp = getPlayerFromSession(session);
        cp.setMoney(cp.getMoney()+Cnst.moneyMap.get(room.getCircleNum()));

        Map<String,String> info = new HashMap<String, String>();
        info.put("reqState", String.valueOf(Cnst.REQ_STATE_1));
        info.put("money", String.valueOf(cp.getMoney()));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    /**
     * 回放的时候，获取房间的局数
     * @param session
     * @param readData
     */
    public static void interface_100014(IoSession session, ProtocolData readData){
    	logger.I("回放的时候，获取房间的局数,interfaceId -> 100014");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Map<String,Object> info = new HashMap<String, Object>();
        int juNum = BackFileUtil.getFileNumByRoomId(roomId);
        info.put("num", juNum);
        info.put("url", Cnst.HTTP_URL.concat(Cnst.BACK_FILE_PATH));
        info.put("roomSn", String.valueOf(roomId));
        info.put("createTime", obj.getString("createTime"));
        
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 强制解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100015(IoSession session,ProtocolData readData) throws Exception{
    	JSONObject obj = JSONObject.parseObject(readData.getJsonString());
    	Integer interfaceId = obj.getInteger("interfaceId");
    	Integer roomId = obj.getInteger("roomSn");
    	System.out.println("*******强制解散房间"+roomId);
    	Long userId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
    	if (userId==null) {
			illegalRequest(interfaceId, session);
			return;
		}
		if (roomId!=null) {
			RoomResp room = roomMap.get(roomId);
			if (room!=null&&room.getCreateId().equals(userId)) {
				room.setStatus(Cnst.ROOM_STATE_YJS);
				List<Player> players = TCPGameFunctions.getPlayerList(room);
				MessageFunctions.setOverInfo(room,players);
				MessageFunctions.updateDatabasePlayRecord(room);
				roomMap.remove(roomId);
				if (players!=null&&players.size()>0) {
					for(Player p:players){
				        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
					}
					for(Player p:players){
				        IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
                        if(se!=null&&se.isConnected()){
                            MessageFunctions.interface_100100(se, new ProtocolData(100100,"{\"interfaceId\":\"100100\",\"userId\":\""+p.getUserId()+"\"}"));
                        }
			        }
				}

		        BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
			}else{
				System.out.println("*******强制解散房间"+roomId+"，房间不存在");
			}
		}

        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
        info.put("roomSn",roomId);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
		
    }
    
    

    /**
     * 产生随机的风
     * @param players
     * @return
     */
    private static Integer getWind(Long[] userIds){
        List<Integer> ps = new ArrayList<>();
        ps.add(Cnst.WIND_EAST);
        ps.add(Cnst.WIND_SOUTH);
        ps.add(Cnst.WIND_WEST);
        ps.add(Cnst.WIND_NORTH);
        if (userIds!=null){
            for(int i=userIds.length-1;i>=0;i--){
                if (userIds[i]!=null){
                    ps.remove(i);
                }
            }
        }
        return ps.get(CommonUtil.getRamdonInNum(ps.size()));
    }
    
    /**
     * 或得到的是一个正数，要拿当前玩家的剩余房卡，减去这个值
     * @param userId
     * @return
     */
    private static int getFrozenMoney(long userId){
    	int frozenMoney = 0;
    	if (roomMap!=null&&roomMap.size()>0) {
        	for(Integer roomId:roomMap.keySet()){
        		RoomResp room = roomMap.get(roomId);
            	if (room.getCreateId().equals(userId)&&room.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
            		frozenMoney += Cnst.moneyMap.get(room.getCircleNum());
				}
            }
		}
    	return frozenMoney;
    }

    /**
     * 返回用户
     * @param openId
     * @param ip
     * @return
     * @throws Exception
     */
    public static Player getPlayerInfos(String openId,String ip,String cid,IoSession session){
    	if (cid==null||!cid.equals(Cnst.cid)) {
			return null;
		}
        Player p = null;
        try {
        	String notice = getStringByKey(Cnst.NOTICE_KEY);
        	if (notice==null) {
        		notice = userService.getNotice();
        		setStringByKey(Cnst.NOTICE_KEY, notice);
			}
            if (openIdUserMap!=null&&openIdUserMap.containsKey(openId)){//用户是断线重连
            	Long userId = openIdUserMap.get(openId);
            	p = playerMap.get(userId);
            	IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
                p.setNotice(notice);
                p.setStatus(Cnst.PLAYER_LINE_STATE_INLINE);
                if (se!=null&&se.getId()!=session.getId()){
                    MessageFunctions.interface_100106(se);
                }
                if (p.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {//去数据库重新请求用户，//需要减去玩家开的房卡
                	p = userService.getByOpenId(openId,cid);
                	if (p==null) {
                		p = userService_login.getUserInfoByOpenId(openId);
                        if (p==null){
                            return null;
                        }else{
                            while(true){
                                p.setUserId(Long.valueOf(CommonUtil.getGivenRamdonNum(6)));//唯一的userId，需要去数据库检测是否存在此id
                                Player temp = userService.isExistUserId(p.getUserId());
                                if (temp==null){
                                    break;
                                }
                            }
                            p.setUserAgree(0);
                            p.setGender(p.getGender());
                            p.setTotalGameNum("0");
                            p.setMoney(Cnst.MONEY_INIT);
                            p.setLoginStatus(1);
                            p.setCid(cid);
                            String time = String.valueOf(new Date().getTime());
                            p.setLastLoginTime(time);
                            p.setSignUpTime(time);
                            userService.save(p);
                        }
					}
                    p.setScore(0);
                    p.setIp(ip);
                    p.setNotice(notice);
                    p.setStatus(Cnst.PLAYER_LINE_STATE_INLINE);
                    p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
                    p.setMoney(p.getMoney()-getFrozenMoney(p.getUserId()));
				}
                return p;
            }
            p = userService.getByOpenId(openId,cid);
            if (p!=null){//当前游戏的数据库中存在该用户
                p.setNotice(notice);
            }else{//如果没有，需要去微信的用户里查询
                p = userService_login.getUserInfoByOpenId(openId);
                if (p==null){
                    return null;
                }else{
                    while(true){
                        p.setUserId(Long.valueOf(CommonUtil.getGivenRamdonNum(6)));//唯一的userId，需要去数据库检测是否存在此id
                        Player temp = userService.isExistUserId(p.getUserId());
                        if (temp==null){
                            break;
                        }
                    }
                    p.setUserAgree(0);
                    p.setGender(p.getGender());
                    p.setTotalGameNum("0");
                    p.setMoney(Cnst.MONEY_INIT);
                    p.setLoginStatus(1);
                    p.setCid(cid);
                    String time = String.valueOf(new Date().getTime());
                    p.setLastLoginTime(time);
                    p.setSignUpTime(time);
                    userService.save(p);
                }
            }
            p.setScore(0);
            p.setIp(ip);
            p.setNotice(notice);
            p.setStatus(Cnst.PLAYER_LINE_STATE_INLINE);
            p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
            p.setMoney(p.getMoney()-getFrozenMoney(p.getUserId()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return p;
    }


}
