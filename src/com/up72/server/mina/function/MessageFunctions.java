package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MahjongUtils;

/**
 * Created by Administrator on 2017/7/10.
 * 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

    /**
     * 发送玩家信息
     * @param session
     * @param readData
     */
    public static void interface_100100(IoSession session, ProtocolData readData) throws Exception{
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Map<String,Object> info = new HashMap<>();
        if (interfaceId.equals(100100)){//刚进入游戏主动请求
            String openId = obj.getString("openId");
            Player currentPlayer = null;
            String cid = null;
            if (openId==null){
                Long userId = obj.getLong("userId");
                if (userId==null){
                    illegalRequest(interfaceId,session);
                    return;
                }else {
                    currentPlayer = getPlayerFromSession(session);
                }
            }else{
                String ip = (String) session.getAttribute(Cnst.USER_SESSION_IP);
                cid = obj.getString("cId");
                currentPlayer = HallFunctions.getPlayerInfos(openId,ip,cid,session);
            }
            if (currentPlayer==null){
                illegalRequest(interfaceId,session);
                return;
            }else{
        		playerMap.put(currentPlayer.getUserId(), currentPlayer);
            }
            
            //更新心跳为最新上线时间
            currentPlayer.setLastHeartTimeLong(new Date().getTime());
            if (cid!=null){
                currentPlayer.setCid(cid);
            }
            currentPlayer.setSessionId(session.getId());//更新sesisonId
            session.setAttribute(Cnst.USER_SESSION_USER_ID,currentPlayer.getUserId());
            
            if (openId!=null){
            	openIdUserMap.put(openId, currentPlayer.getUserId());
            }
            
            
            RoomResp room = null;
            List<Player> players = null;
             
            if (currentPlayer.getRoomId()!=null){//玩家下有roomId，证明在房间中
                room = roomMap.get(currentPlayer.getRoomId());
                if (!room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
                    info.put("roomInfo",getRoomInfo(room));
                    players = getPlayerList(room);
                    for (int m = 0; m < players.size(); m++) {
            			Player p = players.get(m);
            			if (p.getUserId().equals(currentPlayer.getUserId())) {
            				players.set(m, currentPlayer);
            				break;
            			}
            		}
                    
                    List<Map<String,Object>> anotherUsers = new ArrayList<>();
                    for(Player pp : players){
                        if (!pp.getUserId().equals(currentPlayer.getUserId())){
                            anotherUsers.add(getAnotherUsers(pp));
                        }
                    }
                    if (!room.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
						if (anotherUsers.size()<3) {
							System.out.println("fucking error");
						}
					}
                    
                    info.put("anotherUsers",anotherUsers);
				}else{
			        currentPlayer.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
				}
                
            }
            info.put("currentUser",getCurrentUserMap(currentPlayer));
            
            if (room!=null) {
                info.put("wsw_sole_main_id",room.getWsw_sole_main_id());
                info.put("wsw_sole_action_id",room.getWsw_sole_action_id());
                Map<String, Object> roomInfo = (Map<String, Object>) info.get("roomInfo");
                List<Map<String,Object>> anotherUsers = (List<Map<String, Object>>) info.get("anotherUsers");
                
                info.remove("roomInfo");
                info.remove("anotherUsers");

	            JSONObject result = getJSONObj(interfaceId,1,info);
	            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
	            session.write(pd);
	            
	            info.remove("currentUser");
	            info.put("roomInfo", roomInfo);
	            result = getJSONObj(interfaceId,1,info);
	            pd = new ProtocolData(interfaceId, result.toJSONString());
	            session.write(pd);

                info.remove("roomInfo");
	            info.put("anotherUsers", anotherUsers);
	            result = getJSONObj(interfaceId,1,info);
	            pd = new ProtocolData(interfaceId, result.toJSONString());
	            session.write(pd);
	            
				MessageFunctions.interface_100109(players, Cnst.PLAYER_LINE_STATE_INLINE, currentPlayer.getUserId(),session,currentPlayer.getPlayStatus());
			}else{
	            JSONObject result = getJSONObj(interfaceId,1,info);
	            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
	            session.write(pd);
			}
        }else if(interfaceId!=100100){
            Player currentPlayer = getPlayerFromSession(session);
            if (currentPlayer==null){//如果session中，没有用户，关闭连接
                session.close(true);
                return;
            }
            RoomResp room = roomMap.get(currentPlayer.getRoomId());
            List<Player> players = getPlayerList(room);
            for (int m = 0; m < players.size(); m++) {
    			Player p = players.get(m);
    			if (p.getUserId().equals(currentPlayer.getUserId())) {
    				players.set(m, currentPlayer);
    				break;
    			}
    		}
            
            room.setWsw_sole_main_id(room.getWsw_sole_main_id()+1);
            for(Player p:players){
                info = new HashMap<>();
                info.put("wsw_sole_main_id",room.getWsw_sole_main_id());
                info.put("wsw_sole_action_id",room.getWsw_sole_action_id());
                
                info.put("roomInfo",getRoomInfo(room));
                JSONObject result = getJSONObj(100100,1,info);
                ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
                IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
                info.remove("roomInfo");
                
                info.put("currentUser",getCurrentUserMap(p));
                result = getJSONObj(100100,1,info);
                pd = new ProtocolData(interfaceId, result.toJSONString());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
                info.remove("currentUser");
                
                List<Map<String,Object>> anotherUsers = new ArrayList<>();
                for (Player ops : players){
                    if (!ops.getUserId().equals(p.getUserId())){
                        anotherUsers.add(getAnotherUsers(ops));
                    }
                }
                info.put("anotherUsers",anotherUsers);
                result = getJSONObj(100100,1,info);
                pd = new ProtocolData(interfaceId, result.toJSONString());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
                info.remove("anotherUsers");
                
            }
        }else{
            session.close(true);
        }

    }

    //封装房间信息
    public static Map<String,Object> getRoomInfo(RoomResp room){
        Map<String,Object> roomInfo = new HashMap<>();
        roomInfo.put("userId",room.getCreateId());
        roomInfo.put("openName",room.getOpenName());
        roomInfo.put("roomSn",room.getRoomId());
        roomInfo.put("status",room.getStatus());
        roomInfo.put("lastNum",room.getLastNum()-1);
        roomInfo.put("totalNum",room.getCircleNum());
        roomInfo.put("dilou",room.getDilou());
        roomInfo.put("hunPai",room.getHunPai());
        roomInfo.put("status",room.getStatus());
        roomInfo.put("cnrrMJNum",room.getCurrentMjList()==null?0:room.getCurrentMjList().size());
        roomInfo.put("circleWind",room.getCircleWind());
        roomInfo.put("roomType",room.getRoomType());
        roomInfo.put("lastPai",room.getLastPai());
        roomInfo.put("lastUserId",room.getLastUserId());
        roomInfo.put("ct",room.getCreateTime());
        roomInfo.put("lz",room.getLz());
		roomInfo.put("xjst", room.getXiaoJuStartTime());
        roomInfo.put("dissolveRoom",room.getDissolveRoom());
        return roomInfo;
    }
    //封装其他玩家信息
    private static Map<String,Object> getAnotherUsers(Player p){
        Map<String,Object> anotherUsers = new HashMap<>();
        anotherUsers.put("userId",p.getUserId());
        anotherUsers.put("gender",p.getGender());
        anotherUsers.put("position",p.getPosition());
        anotherUsers.put("score",p.getScore());
        anotherUsers.put("money",p.getMoney());
        anotherUsers.put("status",p.getStatus());
        anotherUsers.put("lzState",p.getLz());
        anotherUsers.put("playStatus",p.getPlayStatus());
        anotherUsers.put("openName",p.getUserName());
        anotherUsers.put("openImg",p.getUserImg());
        anotherUsers.put("ip",p.getIp());
        anotherUsers.put("zhuang",p.getZhuang());
        anotherUsers.put("needFaPai",p.getNeedFaPai());
        if (p.getLastFaPai()!=null){
            anotherUsers.put("lastFaPai",new Integer[][]{{-1,-1}});
        }
        Map<String,Object> paiInfos = new HashMap<>();
        paiInfos.put("currentMjList",p.getCurrentMjList()==null?null:p.getCurrentMjList().size());
        paiInfos.put("chuList",p.getChuList());
        paiInfos.put("pengList",p.getPengList());
        paiInfos.put("gangListType3",p.getGangListType3());
        paiInfos.put("gangListType4",p.getGangListType4());
        if(p.getGangListType5()==null){
            paiInfos.put("gangListType5",null);
        }else{
            List<Map<String,String>> list = new ArrayList<>();
            for(InfoCount info:p.getGangListType5()){
                Map<String,String> map = new HashMap<>();
                map.put("t",info.getT().toString());
                map.put("l",null);
                list.add(map);
            }
            paiInfos.put("gangListType5",list);
        }

        anotherUsers.put("paiInfos",paiInfos);
        return anotherUsers;
    }
    //封装当前玩家信息
    public static Map<String,Object> getCurrentUserMap(Player p){
        Map<String,Object> currentUser = new HashMap<>();
        currentUser.put("version",String.valueOf(Cnst.version));
        currentUser.put("userId",p.getUserId());
        currentUser.put("position",p.getPosition());
        currentUser.put("score",p.getScore());
        currentUser.put("status",p.getStatus());
        currentUser.put("lzState",p.getLz());
        currentUser.put("playStatus",p.getPlayStatus());
        currentUser.put("openName",p.getUserName());
        currentUser.put("gender",p.getGender());
        currentUser.put("openImg",p.getUserImg());
        currentUser.put("ip",p.getIp());
        currentUser.put("userAgree",p.getUserAgree());
        currentUser.put("money",p.getMoney());
        currentUser.put("notice",p.getNotice());
        currentUser.put("actions",p.getCurrentActions());
        currentUser.put("zhuang",p.getZhuang());
        currentUser.put("lastFaPai",p.getLastFaPai());
        currentUser.put("needFaPai",p.getNeedFaPai());
        //牌的信息
        Map<String,Object> paiInfos = new HashMap<>();
        paiInfos.put("currentMjList",p.getCurrentMjList());
        paiInfos.put("chuList",p.getChuList());
        paiInfos.put("pengList",p.getPengList());
        paiInfos.put("gangListType3",p.getGangListType3());
        paiInfos.put("gangListType4",p.getGangListType4());
        paiInfos.put("gangListType5",p.getGangListType5());
        currentUser.put("paiInfos",paiInfos);
        return currentUser;
    }

    private static Map<String,Object> getPaiInfo(Player p,boolean isSelf){
        Map<String,Object> paiInfos = new HashMap<>();
        paiInfos.put("chuList",p.getChuList());
        paiInfos.put("pengList",p.getPengList());
        paiInfos.put("gangListType3",p.getGangListType3());
        paiInfos.put("gangListType4",p.getGangListType4());
        if (isSelf) {
            paiInfos.put("currentMjList",p.getCurrentMjList());
            paiInfos.put("gangListType5",p.getGangListType5());
		}else{
	        paiInfos.put("currentMjList",p.getCurrentMjList()==null?null:p.getCurrentMjList().size());
			List<Map<String,String>> list = new ArrayList<>();
            for(InfoCount info:p.getGangListType5()){
                Map<String,String> map = new HashMap<>();
                map.put("t",info.getT().toString());
                map.put("l",null);
                list.add(map);
            }
            paiInfos.put("gangListType5",list);
		}
        return paiInfos;
        
    }

    /**
     * 发牌推送
     * @param session
     * @param readData
     */
    public static void interface_100101(IoSession session, ProtocolData readData) throws Exception{
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Player currentPlayer = getPlayerFromSession(session);

        RoomResp room = roomMap.get(currentPlayer.getRoomId());
        room.setWsw_sole_action_id(room.getWsw_sole_action_id()+1);
        
        if (room.getCurrentMjList().size()==0){

            Map<String,Object> info = new HashMap<>();
            info.put("reqState",Cnst.REQ_STATE_1);
            
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            if (room.getStatus().equals(Cnst.ROOM_STATE_XJS)) {
				return;
			}
            liuJu(currentPlayer.getRoomId());
            return;
        }
        /*是否该给当前玩家发牌校验*/
        if (!currentPlayer.getNeedFaPai()) {
			return;
		}
        currentPlayer.setNeedFaPai(false);
        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);//出牌状态

        Integer[][] pai = MahjongUtils.faPai(room.getCurrentMjList(),1).get(0);

        currentPlayer.setLastFaPai(new Integer[][]{{pai[0][0],pai[0][1]}});
        
        currentPlayer.setZhuaPaiNum(currentPlayer.getZhuaPaiNum()+1);
        currentPlayer.getCurrentMjList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});
        currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
        boolean hasAction = GameFunctions.checkActions(currentPlayer,null,false,null);
        for(Integer[][] temp: currentPlayer.getCurrentMjList()){
            if (temp[0][0].equals(currentPlayer.getLastFaPai()[0][0])&&temp[0][1].equals(currentPlayer.getLastFaPai()[0][1])){
                currentPlayer.getCurrentMjList().remove(temp);
                break;
            }
        }
        if (hasAction) {
            currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		}
        List<Player> players = getPlayerList(room);

        Map<String,Object> users = new HashMap<String, Object>();
        for (int i=0;i<players.size();i++){
            Player p = players.get(i);
            Map<String,Object> info = new HashMap<>();
            if (p.getUserId().equals(currentPlayer.getUserId())){//给自己的信息
                info.put("userId",String.valueOf(p.getUserId()));
	        	
                info.put("pai",pai);
                info.put("currentMjList",p.getCurrentMjList());
                if (hasAction){
    				Map<String,Object> actionInfo = new HashMap<String, Object>();
    				actionInfo.put("actions",p.getCurrentActions());
    				actionInfo.put("userId",p.getUserId());
                    info.put("actionInfo", actionInfo);
                }
            }else{
            	if (hasAction) {
                    info.put("actionInfo", 1);
				}
                info.put("currentMjList",p.getCurrentMjList().size());
                info.put("userId",String.valueOf(currentPlayer.getUserId()));
            }
            
            info.put("needFaPai",p.getNeedFaPai());

            info.put("playStatus",p.getPlayStatus());
            info.put("mjNum",room.getCurrentMjList().size());
            info.put("wsw_sole_main_id",room.getWsw_sole_main_id());
            info.put("wsw_sole_action_id",room.getWsw_sole_action_id());
            
            JSONObject result = getJSONObj(100101,1,info);
            ProtocolData pd = new ProtocolData(100101, result.toJSONString());
            IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
            
            //写文件用
            if (p.getUserId().equals(currentPlayer.getUserId())) {
            	users.put("faPaiUser", info);
			}
            
            if (se!=null&&se.isConnected()){
                se.write(pd);
            }
        }

        BackFileUtil.write(null, 100101, room,players,users);//写入文件内容
        BackFileUtil.writeForCount(room, players, 100101, currentPlayer.getUserId(), null, null);
    }

    /**
     * 流局结算（小）
     */
    public static void liuJu(Integer roomId){
    	RoomResp room = roomMap.get(roomId);
        room.setStatus(Cnst.ROOM_STATE_XJS);
        List<Player> players = getPlayerList(room);
        Map<String,Object> info = new HashMap<>();
        info.put("lastNum",room.getLastNum());//剩余圈数不变
        List<Map<String,Object>> userInfos = new ArrayList<>();
        for(Player p:players){
        	p.setPlayStatus(Cnst.PLAYER_STATE_XJS);
            Map<String,Object> map = new HashMap<>();
            map.put("userId",p.getUserId());
            map.put("currentMjList",p.getCurrentMjList());
            map.put("chuList",p.getChuList());
            map.put("pengList",p.getPengList());
            map.put("gangListType3",p.getGangListType3());
            map.put("gangListType4",p.getGangListType4());
            map.put("gangListType5",p.getGangListType5());
            map.put("isWin",false);
            map.put("isDian",false);
            Integer gangScore = getGangScore(p,players);
            map.put("winScore",0);
            map.put("daHun",p.getDaHun());
            map.put("lz",p.getLz());
            map.put("gangScore",gangScore);
//            p.setScore(p.getScore());//杠分修改为即时结算，不在结算的时候结算
            p.setScore(p.getScore()+gangScore);//杠分修改为即时结算，不在结算的时候结算
            map.put("score",p.getScore());
            userInfos.add(map);
        }
        info.put("userInfos",userInfos);
        ProtocolData pd = null;
        for(Player p:players){
        	//清空玩家的牌数据
            p.initPlayer(p.getRoomId(),p.getPosition(),p.getZhuang(),p.getPlayStatus(),
            		p.getScore(),p.getHuNum(),p.getLoseNum());
            
            Integer interfaceId = 100102;
            JSONObject result = getJSONObj(interfaceId,1,info);
            pd = new ProtocolData(interfaceId, result.toJSONString());
            IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
            if (se!=null&&se.isConnected()){
                se.write(pd);
            }
        }
        setOverInfo(room,players);
        room.initRoom();

        BackFileUtil.write(pd, 100102, room,players,info);//写入文件内容
    }
    /**
     * 胡牌结算（小）
     */
    public static void hu(Player huUser,Player toUser,Integer[][] pai){
        RoomResp room = roomMap.get(huUser.getRoomId());
        
        if(pai[0][0]==null||pai[0][1]==null){//说明是自摸们，不用加如手牌
        	huUser.getCurrentMjList().add(new Integer[][]{{huUser.getLastFaPai()[0][0],huUser.getLastFaPai()[0][1]}});
        	pai = new Integer[][]{{huUser.getLastFaPai()[0][0],huUser.getLastFaPai()[0][1]}};
        }else{
            huUser.getCurrentMjList().add(pai);
        }

        int[] huInfo = MahjongUtils.checkHuInfo(huUser);//第一个元素是胡的名称，第二个元素是番数
        Map<String,Object> info = new HashMap<>();
        List<Map<String,Object>> userInfos = new ArrayList<>();
        Player zhuangUser = null;
        List<Player> players = getPlayerList(room);
        for(Player p:players){
            if (p.getZhuang()){
                zhuangUser = p;
            }
            Map<String,Object> userInfo = new HashMap<>();
            userInfo.put("userId",p.getUserId());
            userInfo.put("currentMjList",p.getCurrentMjList());
            userInfo.put("chuList",p.getChuList());
            userInfo.put("pengList",p.getPengList());
            userInfo.put("gangListType3",p.getGangListType3());
            userInfo.put("gangListType4",p.getGangListType4());
            userInfo.put("gangListType5",p.getGangListType5());
            Integer gangScore = getGangScore(p,players);
            userInfo.put("isWin",p.getIsHu());
            Integer currwinScore = getWinScore(huUser, players, p, huInfo[1]);
            if (huUser.getDaHun()) {
            	currwinScore = 0;
			}
            userInfo.put("winScore",currwinScore);
            userInfo.put("gangScore",gangScore);
            p.setScore(p.getScore()+currwinScore+gangScore);
            userInfo.put("score",p.getScore());
            userInfo.put("daHun",p.getDaHun());
            userInfo.put("lz",p.getLz());
            if (p.getIsHu()){
                userInfo.put("winInfo",huInfo[0]);
                userInfo.put("fanInfo",huInfo[1]);
                if (p.getHuNum()==null){
                    p.setHuNum(1);
                }else{
                    p.setHuNum(p.getHuNum()+1);
                }
            }else{
                if (p.getLoseNum()==null){
                    p.setLoseNum(1);
                }else{
                    p.setLoseNum(p.getLoseNum()+1);
                }
            }
            userInfos.add(userInfo);
        }
        info.put("userInfos",userInfos);
        Integer lastCircle = room.getLastNum();
        if (!huUser.getZhuang()){
            Integer position = zhuangUser.getPosition();
            if(position==4){
                lastCircle--;
                room.setCurrentJuNum(room.getCurrentJuNum()==null?1:room.getCurrentJuNum()+1);
                room.setZhuangId(players.get(0).getUserId());
                Integer circleWind = room.getCircleWind();
                if (circleWind==4){
                    circleWind=1;
                }else{
                    circleWind++;
                }
                room.setCircleWind(circleWind);
            }else{
                room.setZhuangId(players.get(position).getUserId());
            }
            for(Player p:players){
                if (p.getUserId().equals(room.getZhuangId())){
                    p.setZhuang(true);
                }else{
                    p.setZhuang(false);
                }
            }
        }
        room.setLastNum(lastCircle);
        info.put("lastNum",lastCircle-1);
        
        if (lastCircle-1<0) {
            room.setStatus(Cnst.ROOM_STATE_YJS);
		}else{
	        room.setStatus(Cnst.ROOM_STATE_XJS);
		}
        
        setOverInfo(room,players);
        
        if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
	        room.setHasInsertRecord(true);
            updateDatabasePlayRecord(room);
		}
        

        ProtocolData pd = null;
        for(Player p:players){
        	//清空玩家的牌数据
            p.initPlayer(p.getRoomId(),p.getPosition(),p.getZhuang(),p.getPlayStatus(),
            		p.getScore(),p.getHuNum(),p.getLoseNum());
            IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
            if (se!=null&&se.isConnected()){
                JSONObject result = getJSONObj(100102,1,info);
                pd = new ProtocolData(100102, result.toJSONString());
                se.write(pd);
            }
        }
        room.initRoom();
        BackFileUtil.write(pd, 100102, room,players,info);//写入文件内容
    }
    
    public static void setOverInfo(RoomResp room,List<Player> players){

    	List<Map<String, Object>> overInfoOld = room.getOverInfo();
    	List<Map<String,Object>> xiaoJieSuanInfo = new ArrayList<Map<String,Object>>();
    	
    	List<Map<String, Object>> overInfo = new ArrayList<Map<String,Object>>();
    	if (players!=null&&players.size()>0) {
			for(int i=0;i<players.size();i++){
				Player p = players.get(i);
				Map<String, Object> info = new HashMap<String, Object>();
				info.put("userId", p.getUserId());
				info.put("score", p.getScore());
				info.put("huNum",p.getHuNum()==null?0:p.getHuNum());
				info.put("loseNum",p.getLoseNum()==null?0:p.getLoseNum());
				info.put("zhuangNum",p.getZhuangNum()==null?0:p.getZhuangNum());
				info.put("zimoNum",p.getZimoNum()==null?0:p.getZimoNum());
				info.put("position", p.getPosition());
				info.put("xjn", room.getXiaoJuNum());
				overInfo.add(info);
				
				Map<String,Object> xjsi = new LinkedHashMap<String, Object>();
				xjsi.put("openName", p.getUserName());
				xjsi.put("openImg", p.getUserImg());
				if (overInfoOld==null) {
					xjsi.put("score",p.getScore());
				}else{
					xjsi.put("score",p.getScore()-(Integer)overInfoOld.get(i).get("score"));
				}
				xiaoJieSuanInfo.add(xjsi);
			}
		}
    	room.setOverInfo(null);
    	room.setOverInfo(overInfo);
    	
    	BackFileUtil.writeXiaoJieSuanInfo(room, xiaoJieSuanInfo);//写入文件
    	
    }



    /**
     * 大结算
     * @param session
     * @param readData
     */
    public static void interface_100103(IoSession session, ProtocolData readData){
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        RoomResp room = roomMap.get(roomId);
        
        if (room!=null&&room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
        	List<Map<String,Object>> info = new ArrayList<>();

            List<Map<String, Object>> overInfoList = room.getOverInfo();
            if (overInfoList!=null&&overInfoList.size()>0) {
            	for(Map<String, Object> infoMap:overInfoList){
                    info.add(infoMap);
                }
                JSONObject result = getJSONObj(interfaceId,1,info);
                ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
                session.write(pd);

                Player p = getPlayerFromSession(session);
                p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
                
                room.setOutNum(room.getOutNum()==null?1:room.getOutNum()+1);
                
                if (room.getHasInsertRecord()==null||!room.getHasInsertRecord()) {
                	room.setHasInsertRecord(true);
                    updateDatabasePlayRecord(room);
        		}

                if (room.getOutNum()==4) {
                	roomMap.remove(roomId);
                    room = null;
        		}
    		}
		}
        
    }

    public static void updateDatabasePlayRecord(RoomResp room){
    	try {
    		if (room==null)return;
            Integer roomId = room.getRoomId();
            
            if (roomService.roomExistInDB(roomId, room.getCreateId(), room.getCreateTime())!=null) {
            	//刷新数据库
                roomService.updateRoomState(roomId);
                
                PlayerRecord playerRecord = new PlayerRecord();
                playerRecord.setRoomId(roomId);
                playerRecord.setStartTime(String.valueOf(room.getCreateTime()));
                playerRecord.setEndTime(String.valueOf(new Date().getTime()));
                
                List<Map<String, Object>> overInfoList = room.getOverInfo();
                if (overInfoList!=null&&overInfoList.size()>0) {
                	for(Map<String, Object> infoMap : overInfoList){
                        if ((int)(infoMap.get("position"))==Cnst.WIND_EAST) {
                        	playerRecord.setEastUserId(String.valueOf(infoMap.get("userId")));
                            playerRecord.setEastUserMoneyRecord((int)infoMap.get("score"));
                            playerRecord.setEastUserMoneyRemain((int)infoMap.get("score"));
        				}else if ((int)(infoMap.get("position"))==Cnst.WIND_SOUTH) {
        					playerRecord.setSouthUserId(String.valueOf(infoMap.get("userId")));
        		            playerRecord.setSouthUserMoneyRecord((int)infoMap.get("score"));
        		            playerRecord.setSouthUserMoneyRemain((int)infoMap.get("score"));
        				}else if ((int)(infoMap.get("position"))==Cnst.WIND_WEST) {
        					playerRecord.setWestUserId(String.valueOf(infoMap.get("userId")));
        		            playerRecord.setWestUserMoneyRecord((int)infoMap.get("score"));
        		            playerRecord.setWestUserMoneyRemain((int)infoMap.get("score"));
        				}else if ((int)(infoMap.get("position"))==Cnst.WIND_NORTH) {
        					playerRecord.setNorthUserId(String.valueOf(infoMap.get("userId")));
        		            playerRecord.setNorthUserMoneyRecord((int)infoMap.get("score"));
        		            playerRecord.setNorthUserMoneyRemain((int)infoMap.get("score"));
        				}else {
        	            	return;
        	            }
                    }
        		}else{
        			return;
        		}
                userService.insertPlayRecord(playerRecord);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    

    private static Integer getWinScore(Player huUser,List<Player> players,Player currentPlayer,int fanInfo){
        int wins = 0;
        if (currentPlayer.getUserId().equals(huUser.getUserId())) {//胡牌人算分
			for(Player p:players){
				if (!p.getUserId().equals(huUser.getUserId())) {
					if (p.getLz()==1) {//玩家拉庄
						if (huUser.getZhuang()) {//胡牌人是庄
							wins += fanInfo*2;
						}else{
							wins += fanInfo;
						}
					}else{//玩家没有拉庄
						if (p.getZhuang()) {
							if (huUser.getLz()==1) {
								wins += fanInfo*2;
							}else{
								wins += fanInfo;
							}
						}else{
							wins += fanInfo;
						}
					}
				}
			}
		}else{//非胡牌人算分
			if ((huUser.getZhuang()&&currentPlayer.getLz()==1)||(currentPlayer.getZhuang()&&huUser.getLz()==1)) {
				wins += fanInfo*2;
			}else{
				wins += fanInfo;
			}
			wins = wins*-1;
		}
        
        return wins;
    }

    private static Integer getGangScore(Player p,List<Player> players){
        int finalScore = 0;
        for (Player ps:players){
            if (ps.getUserId().equals(p.getUserId())){
                finalScore = getPersonalGangScore(ps,finalScore,1,players,p);
            }else{
                finalScore = getPersonalGangScore(ps,finalScore,-1,players,p);
            }
        }
        return finalScore;
    }
    
    /**
     * type=1,+
     * type=-1,-
     */
    private static Integer getPersonalGangScore(Player p,int score,Integer type,List<Player> players,Player current){
        Integer temp = 0;
        if (p.getGangListType3()!=null&&p.getGangListType3().size()>0){
        	temp +=  p.getGangListType3().size()*Cnst.gangBaseScore;
        }
        if (p.getGangListType4()!=null&&p.getGangListType4().size()>0){
        	temp +=  p.getGangListType4().size()*Cnst.gangBaseScore;
        }
        if (p.getGangListType5()!=null&&p.getGangListType5().size()>0){
        	temp +=  p.getGangListType5().size()*Cnst.gangBaseScore*2;
        }
        if (type>0){//计算赢的钱，赢三份儿
        	if (p.getZhuang()) {
				for(Player pp:players){
					if (!pp.getUserId().equals(p.getUserId())) {
						if (pp.getLz()==1) {
							score = score + temp*2;
						}else{
							score = score + temp;
						}
					}
				}
			}else{
				if (p.getLz()==1) {//拉庄
					score = score + temp + temp + temp*2;
				}else{
					score = score + temp + temp + temp;
				}
			}
        }else{//计算输的钱，输一份儿
        	if (p.getZhuang()) {
				if (current.getLz()==1) {
					score = score - temp*2;
				}else{
					score = score - temp;
				}
			}else{
				if (current.getZhuang()) {
					if (p.getLz()==1) {
						score = score - temp*2;
					}else{
						score = score - temp;
					}
				}else {
					score = score - temp;
				}
			}
        }
        return score;
    }
    
    
    /**
     * 给其他玩家推送动作提示
     * @param players
     * @param userId
     * @param action
     */
    public static void interface_100104(List<Player> players,Long userId,Integer action,Long toUserId,Integer gangType,ProtocolData prod){
    	
        Integer interfaceId = 100104;

		Player actionUser = null;
		Player hasActionUser = null;
		List<Map<String,Object>> playStatusInfo = new ArrayList<Map<String,Object>>();
		for(Player p:players){
			Map<String,Object> pi = new HashMap<String, Object>();
			pi.put("userId", p.getUserId());
			pi.put("playStatus", p.getPlayStatus());
			playStatusInfo.add(pi);
			if (p.getUserId().equals(userId)) {
				actionUser = p;
			}
			if (p.getCurrentActions()!=null&&p.getCurrentActions().size()>0) {
				hasActionUser = p;
			}
		}
		
		Integer[][] fuyi = new Integer[][]{{-1,-1}};
		
		Map<String,Object> users = new HashMap<String, Object>();
		for(Player p:players){
			IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
            if (se!=null&&se.isConnected()){
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("userId",userId);
                map.put("action",action);

                RoomResp room = roomMap.get(p.getRoomId());
                map.put("wsw_sole_main_id",room.getWsw_sole_main_id());
                map.put("wsw_sole_action_id",room.getWsw_sole_action_id());
                
                if (action==4) {
                    map.put("huType",userId.equals(toUserId)?1:2);
				}
                
                if (p.getUserId().equals(userId)) {
                    map.put("lastFaPai",p.getLastFaPai());
                    map.put("paiInfos", getPaiInfo(actionUser,true));
				}else{
                    map.put("lastFaPai",fuyi);
                    map.put("paiInfos", getPaiInfo(actionUser,false));
				}
                
                if (hasActionUser!=null) {//还有玩家继续有动作
                	if (hasActionUser.getUserId().equals(p.getUserId())) {
        				Map<String,Object> actionInfo = new HashMap<String, Object>();
        				actionInfo.put("actions",p.getCurrentActions());
        				actionInfo.put("userId",p.getUserId());
                        map.put("actionInfo", actionInfo);
    				}else{
                        map.put("actionInfo", 1);
    				}
				}
                
                map.put("toUserId",toUserId);
                map.put("needFaPai",p.getNeedFaPai());
                map.put("playStatusInfo", playStatusInfo);
                JSONObject result = getJSONObj(interfaceId,1,map);
                ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
                
              //写文件用
                if (map.containsKey("actionInfo")&&!(map.get("actionInfo") instanceof Integer)) {
                	users.put("hasActionUser", map);
				}else if (p.getUserId().equals(userId)) {
                	users.put("actionUser", map);
				}else if (p.getNeedFaPai()){
                	users.put("needFaUser", map);
				}
                
                se.write(pd);
            }
		}

		RoomResp room = roomMap.get(players.get(0).getRoomId());
        BackFileUtil.write(null, interfaceId, room,players,users);//写入文件内容
        BackFileUtil.writeForCount(room, players, interfaceId, userId, action, null);
    }
    
    /**
     * 给其他玩家推送出牌提示
     * 如果actionPlayer==null的话，所有玩家都没有动作
     * @param others
     * @param userId
     * @param paiInfo
     */
    public static void interface_100105(Long userId,Integer[][] paiInfo,Integer roomId,Player actionPlayer,ProtocolData prod){
        Integer interfaceId = 100105;
        RoomResp room = roomMap.get(roomId);
        List<Player> players = getPlayerList(room);
        
        Player currentPlayer = null;
        for(Player p:players){
        	if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
        }
        
        
        
        Map<String,Object> users = new HashMap<String, Object>();
        /*给其他玩家推送出牌提示*/
        for(Player p:players){
            Map<String,Object> map = new JSONObject();
            map.put("userId",userId);
            map.put("paiInfo",paiInfo);
            map.put("wsw_sole_main_id",room.getWsw_sole_main_id());
            map.put("wsw_sole_action_id",room.getWsw_sole_action_id());
            map.put("needFaPai",p.getNeedFaPai());
			map.put("playStatus",p.getPlayStatus());
			
			//加入玩家手牌以及出牌校验
			if (p.getUserId().equals(userId)) {
				map.put("currentMjList",currentPlayer.getCurrentMjList());
			}else{
				map.put("currentMjList",currentPlayer.getCurrentMjList().size());
			}
			map.put("chuList",currentPlayer.getChuList());
            
            if (actionPlayer!=null) {
            	if (actionPlayer.getUserId().equals(p.getUserId())) {
//            		System.err.println("动作人id"+p.getUserId());
    				Map<String,Object> actionInfo = new HashMap<String, Object>();
    				actionInfo.put("actions",p.getCurrentActions());
    				actionInfo.put("userId",p.getUserId());
                    map.put("actionInfo", actionInfo);
				}else{
                    map.put("actionInfo", 1);
				}
			}
            
            JSONObject result = getJSONObj(interfaceId,1,map);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
            
            //写文件用
            if (map.containsKey("actionInfo")&&!(map.get("actionInfo") instanceof Integer)) {
            	users.put("hasActionUser", map);
			}else if (p.getUserId().equals(userId)) {
            	users.put("chuUser", map);
			}else if(p.getNeedFaPai()){
            	users.put("needFaUser", map);
			}
            
            if (se!=null&&se.isConnected()){
                se.write(pd);
            }
        }

        BackFileUtil.write(null, interfaceId, room,players,users);//写入文件内容
        BackFileUtil.writeForCount(room, players, interfaceId, userId, null, paiInfo);
    }


    /**
     * 多地登陆提示
     * @param session
     */
    public static void interface_100106(IoSession session){
        Integer interfaceId = 100106;
        JSONObject result = getJSONObj(interfaceId,1,"out");
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        session.close(true);
    }
    
    
    /**
     * 玩家被踢/房间被解散提示
     * @param session
     */
    public static void interface_100107(Long userId,String type,List<Player> players){
        Integer interfaceId = 100107;
        Map<String,Object> info = new HashMap<String, Object>();
        
        if (players==null||players.size()==0) {
			return;
		}
		info.put("userId", userId);
		info.put("type", type);
		
		
    	JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        for(Player p : players){
        	IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
            if (se!=null&&se.isConnected()){
                se.write(pd);
            }
        }
    }
    
    /**
     * 方法id不符合
     * @param session
     */
    public static void interface_100108(IoSession session){
        Integer interfaceId = 100108;
        Map<String,Object> info = new HashMap<String, Object>();
        info.put("reqState", Cnst.REQ_STATE_9);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    
    
    /**
     * 用户离线/上线提示
     * @param roomId
     * @param state
     */
    public static void interface_100109(List<Player> players,String status,Long userId,IoSession session,String playState){
    	Integer interfaceId = 100109;
        Map<String,Object> info = new HashMap<String, Object>();
        info.put("userId", userId);
        info.put("status", status);
        info.put("playStatus",playState);
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

        if (players!=null&&players.size()>0) {
        	for(Player p:players){
            	if (p!=null&&!p.getUserId().equals(userId)) {
            		IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
                	if (se!=null&&se.isConnected()) {
        				se.write(pd);
        			}
    			}
            	
            }
		}
    }
    
    /**
     * 后端主动解散房间推送
     * @param reqState
     * @param players
     */
    public static void interface_100111(int reqState,List<Player> players,Integer roomId){
    	Integer interfaceId = 100111;
        Map<String,Object> info = new HashMap<String, Object>();
        info.put("reqState",reqState);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        if (players!=null&&players.size()>0) {
			for(Player p:players){
				if (p.getRoomId()!=null&&p.getRoomId().equals(roomId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
					if (se!=null&&se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}
    	
    }
}
