package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.InfoCount;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.MahjongUtils;
import com.up72.server.mina.utils.TaskUtil;

/**
 * Created by Administrator on 2017/7/13.
 * 游戏中
 */

public class GameFunctions extends TCPGameFunctions {
	
//	public static Map<Integer,String> count = new LinkedHashMap<Integer, String>();


    /**
     * 用户点击准备，用在小结算那里，
     * @param session
     * @param readData 
     */
    public static void interface_100200(IoSession session, ProtocolData readData) throws Exception{
        logger.I("准备,interfaceId -> 100200");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");

        Player currentPlayer = getPlayerFromSession(session);
        RoomResp roomResp = roomMap.get(currentPlayer.getRoomId());
        if (roomResp.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
			return;
		}
        if (currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
			return;
		}
        if (!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_XJS)&&!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_IN)) {
			return;
		}

        currentPlayer.initPlayer(currentPlayer.getRoomId(),currentPlayer.getPosition(),currentPlayer.getZhuang(),Cnst.PLAYER_STATE_PREPARED,
        		currentPlayer.getScore(),currentPlayer.getHuNum(),currentPlayer.getLoseNum());

        List<Player> players = getPlayerList(roomResp);
        for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				players.set(m, currentPlayer);
				break;
			}
		}
        
        boolean allPrepared = true;
        
        for (Player p:players){
            if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)){
                allPrepared = false;
            } 
        }
        
        roomResp.setCurrentMjList(null);
        
        if (allPrepared&&players!=null&&players.size()==4){
        	if (roomResp.getStatus().equals(Cnst.ROOM_STATE_CREATED)) {
        		roomResp.setStatus(Cnst.ROOM_STATE_XJS);
			}
        	
        	//需要检测房间是否允许拉庄
        	if (roomResp.getLz().equals(0)) {//房间不允许拉庄
        		startGame(roomResp, players);
                BackFileUtil.write(null, interfaceId, roomResp,players,null);//写入文件内容
			}else if (roomResp.getLz().equals(1)) {//房间允许拉庄
				//都准备之后，所有人都置为拉庄状态
	        	for(Player p:players){
	        		if (allPrepared) {
	        			if (p.getZhuang()) {
	                		p.setPlayStatus(Cnst.PLAYER_STATE_YLZ);
						}else{
		            		p.setPlayStatus(Cnst.PLAYER_STATE_LZ);
						}
					}
	        	}
			}
        }
        List<Map<String, Object>> info = new ArrayList<Map<String,Object>>();
        for(Player p:players){
            Map<String,Object> i = new HashMap<String, Object>();
            i.put("userId", p.getUserId());
            i.put("playStatus",p.getPlayStatus()); 
            info.add(i);
        }
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        
        for(Player p:players){
        	IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
        	if (se!=null&&se.isConnected()) {
				se.write(pd);
			}
        }
    }
    
    /**
     * 根据第一张混排，计算第二个
     * @return
     */
    public static Integer[][] getHunPai(Integer[][] firstHun){
    	Integer[][] anotherHun = null;
    	if ((firstHun[0][1].equals(9))||(firstHun[0][0].equals(4)&&firstHun[0][1].equals(4))||(firstHun[0][0].equals(5)&&firstHun[0][1].equals(3))) {//9万、9条、9桶
    		anotherHun = new Integer[][]{{firstHun[0][0],1}};
		}else{//正常情况，递增即可
			anotherHun = new Integer[][]{{firstHun[0][0],firstHun[0][1]+1}};
		}
    	return anotherHun;
    }

    /**
     * 开局发牌
     * @param roomId
     */
    public static void startGame(RoomResp room,List<Player> players){
        
        //关闭解散房间计时任务
        notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,true);
        
        
        if (room.getXiaoJuNum()==null) {
			room.setXiaoJuNum(1);
		}else{
			room.setXiaoJuNum(room.getXiaoJuNum()+1);
		}
        room.setXiaoJuStartTime(new Date().getTime());
        
        room.setStatus(Cnst.ROOM_STATE_GAMIING);
        room.setCurrentMjList(MahjongUtils.xiPai(MahjongUtils.initMahjongs()));
        
        for (Player p:players) {//设置庄家需要请求发牌
        	if (p.getZhuang()){
            	p.setNeedFaPai(true);
            }else{
            	p.setNeedFaPai(false);
            }
		}
        if (room.getHunPai()==null||room.getHunPai().size()==0) {//未打混
            List<Integer[][]> hunPai = MahjongUtils.faPai(room.getCurrentMjList(),1);
            hunPai.add(getHunPai(hunPai.get(0)));
            
            room.setHunPai(hunPai);
		}
        
        for(Player p:players){
            p.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
			p.setCurrentMjList(MahjongUtils.paiXu(MahjongUtils.faPai(room.getCurrentMjList(),13)));
            if (p.getZhuang()) {
				p.setZhuangNum(p.getZhuangNum()==null?1:p.getZhuangNum()+1);
			}
        }
    }


    /**
     * 出牌
     * @param session
     * @param readData
     */
    public static void interface_100201(IoSession session, ProtocolData readData) throws Exception{
        logger.I("出牌,interfaceId -> 100201");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");

        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        Integer[][] paiInfo = getIntegerList(obj.getString("paiInfo"));

        Player currentPlayer = null;
        RoomResp room = roomMap.get(roomId);
        List<Player> players = getPlayerList(room);
        for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}
        
        if (!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_CHU)) {
			return;
		}

        boolean hasChuPai = false;
        if(currentPlayer.getLastFaPai()!=null){
        	if (paiInfo[0][0].equals(currentPlayer.getLastFaPai()[0][0])&&paiInfo[0][1].equals(currentPlayer.getLastFaPai()[0][1])) {
        		hasChuPai = true;
			}
        }
        if (!hasChuPai) {
			for(int i=0;i<currentPlayer.getCurrentMjList().size();i++){
				Integer[][] p = currentPlayer.getCurrentMjList().get(i);
				if (p[0][0].equals(paiInfo[0][0])&&p[0][1].equals(paiInfo[0][1])) {
					hasChuPai = true;
					break;
				}
			}
		}
        if (!hasChuPai) {
        	illegalRequest(interfaceId, session);
			return;
		}
        
        
        //设置递增id
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}else{
			room.setWsw_sole_action_id(wsw_sole_action_id+1);
		}
        

        List<Player> others = new ArrayList<>();

        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
        room.setLastUserId(userId);
        room.setLastPai(new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}});
        //需要检测出的哪张牌是不是发的那个，如果不是，需要把发的哪张牌加入手牌集合
        if (currentPlayer.getLastFaPai()!=null&&
                paiInfo[0][0].equals(currentPlayer.getLastFaPai()[0][0])&&paiInfo[0][1].equals(currentPlayer.getLastFaPai()[0][1])){
            currentPlayer.getChuList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});

        }else{
            for(int i=0;i<currentPlayer.getCurrentMjList().size();i++){
                if (currentPlayer.getCurrentMjList().get(i)[0][0].equals(paiInfo[0][0])&&currentPlayer.getCurrentMjList().get(i)[0][1].equals(paiInfo[0][1])){
                    currentPlayer.getChuList().add(currentPlayer.getCurrentMjList().get(i));
                    currentPlayer.getCurrentMjList().remove(i);
                    break;
                }
            }
            if (currentPlayer.getLastFaPai()!=null){
                currentPlayer.getCurrentMjList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});
                currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
            }
        }
        currentPlayer.setLastFaPai(null);
        currentPlayer.setNeedFaPai(false);
        currentPlayer.setChuPaiNum(currentPlayer.getChuPaiNum()==null?1:currentPlayer.getChuPaiNum()+1);
        
        //打混信息
        if ((paiInfo[0][0].equals(room.getHunPai().get(0)[0][0])&&paiInfo[0][1].equals(room.getHunPai().get(0)[0][1]))||
        		(paiInfo[0][0].equals(room.getHunPai().get(1)[0][0])&&paiInfo[0][1].equals(room.getHunPai().get(1)[0][1]))) {
        	currentPlayer.setDaHun(true);
		}

        Player nextUser = null;
        
        for(int i=0;i<players.size();i++){
            if (!players.get(i).getUserId().equals(userId)){//非当前用户之外的其他三家
                others.add(players.get(i));
            }else{
                if (i == players.size()-1){
                    nextUser = players.get(0);
                }else{
                    nextUser = players.get(i+1);
                }
            }
        }
        
        //给其他玩家检测动作
        Boolean hasAction = false;
        for(Player ps:others){
            if(checkActions(ps,new Integer[][]{{paiInfo[0][0],paiInfo[0][1]}},ps.getUserId().equals(nextUser.getUserId()),currentPlayer)){
                hasAction = true;
            }
        }
        //检测玩家动作优先级,删除优先级低的玩家动作
        removeActions(others,currentPlayer);
        
        if (hasAction){//有玩家有动作
          //添加自己为过的人（出牌人对自己的牌肯定不能有动作）
            if (room.getGuoUserIds()==null) {
    			room.setGuoUserIds(new ArrayList<Long>());
    		}
            room.getGuoUserIds().add(currentPlayer.getUserId());
        }else{//没有动作了,推发牌
        	//出牌提示
        	nextUser.setNeedFaPai(true);
        	nextUser.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
            room.setGuoUserIds(null);
        }
        MessageFunctions.interface_100105(userId,paiInfo,roomId,getActionPlayer(players),readData);
    }
    
    public static Long time = new Date().getTime();
    
    
    
    public static Player getActionPlayer(List<Player> players){
    	//在按照优先级移除之后，只有一个玩家有动作
        Player actionPlayer = null;
        for(Player p:players){
        	if (p.getCurrentActions()!=null&&p.getCurrentActions().size()>0) {
        		actionPlayer = p;
        		break;
			}
        }
        return actionPlayer;
    }

    /**
     * 对比两个玩家的动作，把优先级低的玩家动作清空
     * @param others
     */
    private static void removeActions(List<Player> others,Player chuUser){
        a:for(int i=0;i<others.size();i++){
            Player p1 = others.get(i);
            if (p1.getCurrentActions()!=null&&p1.getCurrentActions().size()>0){
                for(int j=i+1;j<others.size();j++){
                    Player p2 = others.get(j);
                    if (p2.getCurrentActions()!=null&&p2.getCurrentActions().size()>0){
                        Integer p1Act = 0;
                        Integer[] p1as = new Integer[p1.getCurrentActions().keySet().size()];
                        Integer p2Act = 0;
                        Integer[] p2as = new Integer[p2.getCurrentActions().keySet().size()];
                        int num = 0;
                        for(String act1 : p1.getCurrentActions().keySet()){
                            p1as[num++] = Integer.valueOf(act1);
                        }
                        num = 0;
                        for(String act2 : p2.getCurrentActions().keySet()){
                            p2as[num++] = Integer.valueOf(act2);
                        }
                        Arrays.sort(p1as);
                        Arrays.sort(p2as);
                        p1Act = p1as[p1as.length-1];
                        p2Act = p2as[p2as.length-1];
                        if (p1Act.equals(p2Act)){//两家都胡牌，分局圈风确定向下推
                            Integer circleWind = chuUser.getPosition();
                            //玩家的风向跟出牌人的位置对比，都大于牌人的位置，则取大的；都小于牌人的位置则取小的；一大一小则取大
                            Integer wind1 = p1.getPosition();
                            Integer wind2 = p2.getPosition();
                            Integer[] winds = new Integer[3];
                            winds[0] = wind1;
                            winds[1] = wind2;
                            winds[2] = circleWind;
                            Arrays.sort(winds);
                            if(winds[0].equals(circleWind)){
                                if (winds[1].equals(wind1)){
                                    p2.setCurrentActions(null);
                                }else{
                                    p1.setCurrentActions(null);
                                    continue a;
                                }
                            }else if(winds[1].equals(circleWind)){
                                if (winds[2].equals(wind1)){
                                    p2.setCurrentActions(null);
                                }else{
                                    p1.setCurrentActions(null);
                                    continue a;
                                }
                            }else if(winds[2].equals(circleWind)){
                                if (winds[0].equals(wind1)){
                                    p2.setCurrentActions(null);
                                }else{
                                    p1.setCurrentActions(null);
                                    continue a;
                                }
                            }
                        }else if (p1Act>p2Act){//玩家1优先级高
                            p2.setCurrentActions(null);
                        }else if (p1Act<p2Act){//玩家2优先级高
                            p1.setCurrentActions(null);
                            continue a;
                        }
                    }
                }
            }
        }
    }
    
    public static String getShowPaiString(List<Integer[][]> pais){
    	StringBuffer sb = new StringBuffer();
    	if (pais!=null&&pais.size()>0) {
			for(Integer[][] pai:pais){
				sb.append(pai[0][0]+"_"+pai[0][1]+"\t\t");
			}
		}
    	return sb.toString();
    }

    /**
     * 玩家动作
     * @param session
     * @param readData
     */
    public static void interface_100202(IoSession session, ProtocolData readData) throws Exception{
        logger.I("玩家动作,interfaceId -> 100202");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        Integer action = obj.getInteger("action");
        Long toUserId = obj.getLong("toUserId");
        Integer[][] actionPai = getIntegerList(obj.getString("actionPai"));
        if (actionPai==null||actionPai[0][0]==null||actionPai[0][1]==null) {
        	actionPai = null;
		}
        Integer[][] pais = getIntegerList(obj.getString("pais"));

        RoomResp room = roomMap.get(roomId);
        Player currentPlayer = null;
        List<Player> players = getPlayerList(room);
        for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}
        
        //设置递增id
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}else{
			room.setWsw_sole_action_id(wsw_sole_action_id+1);
		}
        
        Boolean isNextUser = false;
        Player nextUser = null;//下一个发牌的人
        Player chuPlayer = null;//最后出牌人

    	currentPlayer.setNeedFaPai(false);
    	currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
        
        Map<String, Object> userActionMap = new HashMap<String, Object>();
        
        //清空玩家动作
        for (int i=0;i<players.size();i++){
        	if (players.get(i).getUserId().equals(room.getLastUserId())) {
        		chuPlayer = players.get(i);
        		if (i==3) {
					nextUser = players.get(0);
				}else{
					nextUser = players.get(i+1);
				}
        		if (nextUser.getUserId().equals(currentPlayer.getUserId())) {
					isNextUser = true;
				}
			}
        	if (players.get(i).getUserId().equals(userId)) {
            	userActionMap = players.get(i).getCurrentActions();
			}
            players.get(i).setCurrentActions(null);
        }
        
        if (nextUser==null) {//说明是首轮发牌，还没有人出牌，计算当前玩家的下家
			if (currentPlayer.getPosition().equals(Cnst.WIND_NORTH)) {//
				nextUser = players.get(0);
			}else{
				nextUser = players.get(currentPlayer.getPosition());
			}
		}

        InfoCount info = new InfoCount();
        info.setActionType(action);
        info.setUserId(currentPlayer.getUserId());
        info.setToUserId(toUserId);
        info.setT(new Date().getTime());

        List<Integer[][]> list = new ArrayList<>();
        switch (action){
        	case Cnst.ACTION_PENG:
        		
        		
        			
        		
                MahjongUtils.peng(currentPlayer.getCurrentMjList(),actionPai);
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                list.add(new Integer[][]{{actionPai[0][0],actionPai[0][1]}});
                info.setL(list);
                currentPlayer.getPengList().add(info);
                currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
                chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
                room.setLastPai(null);
                room.setLastUserId(null);

                MessageFunctions.interface_100104(players,userId,action,chuPlayer.getUserId(),null,readData);
                break;
            case Cnst.ACTION_GANG:
                currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
                for(int i=0;i<pais.length;i++){
                    list.add(new Integer[][]{{pais[i][0],pais[i][1]}});
                }
                Integer gangType = MahjongUtils.gang(currentPlayer,list);
                switch (gangType){
                    case 3:
                    	currentPlayer.setNeedFaPai(true);
                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);

                        MessageFunctions.interface_100104(players,userId,action,currentPlayer.getUserId(),gangType,readData);
                        break;
                    case 4:
                        list.add(new Integer[][]{{list.get(0)[0][0],list.get(0)[0][1]}});
                        info.setL(list);
                        currentPlayer.getGangListType4().add(info);
                        chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
                        room.setLastPai(null);
                        room.setLastUserId(null);
                        
                    	currentPlayer.setNeedFaPai(true);
                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
                        MessageFunctions.interface_100104(players,userId,action,chuPlayer.getUserId(),gangType,readData);
                        break;
                    case 5:
                        info.setL(list);
                        currentPlayer.getGangListType5().add(info);
                        if (currentPlayer.getLastFaPai()!=null) {
                    		currentPlayer.getCurrentMjList().add(new Integer[][]{{currentPlayer.getLastFaPai()[0][0],currentPlayer.getLastFaPai()[0][1]}});
                        	currentPlayer.setCurrentMjList(MahjongUtils.paiXu(currentPlayer.getCurrentMjList()));
                        	currentPlayer.setLastFaPai(null);
						}

                    	currentPlayer.setNeedFaPai(true);
                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
                        MessageFunctions.interface_100104(players,userId,action,currentPlayer.getUserId(),gangType,readData);
                        break;
                }
                break;
            case Cnst.ACTION_HU:

                MessageFunctions.interface_100104(players,userId,action,toUserId,null,readData);
                for(Player p:players){
                	if (p.getUserId().equals(currentPlayer.getUserId())) {
						p.setIsHu(true);
					}else{
						p.setIsHu(false);
					}
                    p.setPlayStatus(Cnst.PLAYER_STATE_XJS);
                }
                Player dianUser = null;
                if (toUserId.equals(userId)){
                	currentPlayer.setZimoNum(currentPlayer.getZimoNum()==null?1:currentPlayer.getZimoNum()+1);
                	dianUser = currentPlayer;
                }else{
                	chuPlayer.getChuList().remove(chuPlayer.getChuList().size()-1);//把出牌人的最后一张从出牌list中移除
                	dianUser = chuPlayer;
                }


                MessageFunctions.hu(currentPlayer,dianUser,actionPai);
                
                break;
            case Cnst.ACTION_GUO:
            	if (currentPlayer.getLastFaPai()==null&&room.getLastPai()!=null) {//弃别人的牌
                	//向过的人里面添加自己
                	if (room.getGuoUserIds()==null) {
                    	room.setGuoUserIds(new ArrayList<>());
    				}
                	room.getGuoUserIds().add(currentPlayer.getUserId());
                	
					if (isNextUser) {//自己是下家
						if (userActionMap.containsKey(String.valueOf(Cnst.ACTION_HU))) {//胡牌  弃，因为胡牌的优先级较高，如果胡牌过了之后，要检测其他玩家动作
							boolean hasAction = false;
		                    for(Player p:players){
		                        if (!room.getGuoUserIds().contains(p.getUserId())){//检测没有过的人是否有动作
		                            if (checkActions(p,actionPai,p.getUserId().equals(nextUser.getUserId()),chuPlayer)){
		                                hasAction = true;
		                            }
		                        }
		                    }
		                    if (hasAction){//有动作，推送大接口
		                    	 List<Player> othersList = new ArrayList<Player>();
		                         for(Player pppp:players){
		                         	if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
		                         		othersList.add(pppp);
		         					}
		                         }
		                         
		                        removeActions(othersList, chuPlayer);
		                    }else{//没有动作，发牌
		    					//置空过的人
		    					room.setGuoUserIds(null);
		                    	currentPlayer.setNeedFaPai(true);
		                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		                    }

	                        MessageFunctions.interface_100104(players,userId,action,toUserId,null,readData);
						}else{//非胡牌 弃
							//置空过的人
							room.setGuoUserIds(null);
	                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
		                    currentPlayer.setNeedFaPai(true);
		                    

	                        MessageFunctions.interface_100104(players,userId,action,toUserId,null,readData);
		                    
						}
					}else{//不是下家    检测其他
						boolean hasAction = false;
	                    for(Player p:players){
	                        if (!room.getGuoUserIds().contains(p.getUserId())){//检测没有过的人是否有动作
	                            if (checkActions(p,actionPai,p.getUserId().equals(nextUser.getUserId())	,chuPlayer)){
	                                hasAction = true;
	                            }
	                        }
	                    }
	                    if (hasAction){//有动作，推送大接口
	                    	List<Player> othersList = new ArrayList<Player>();
	                         for(Player pppp:players){
	                         	if (!chuPlayer.getUserId().equals(pppp.getUserId())) {
	                         		othersList.add(pppp);
	         					}
	                         }
	                         
	                         removeActions(othersList, chuPlayer);
	                    }else{//没有动作，发牌
	    					//置空过的人
	    					room.setGuoUserIds(null);
	    					nextUser.setNeedFaPai(true);
	                        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_WAIT);
	                    }

                        MessageFunctions.interface_100104(players,userId,action,toUserId,null,readData);
					}
				}else{//弃自己的牌
					//置空过的人
					room.setGuoUserIds(null);
					nextUser.setNeedFaPai(false);
                    currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_CHU);
                    

                    MessageFunctions.interface_100104(players,userId,action,toUserId,null,readData);
				}
            	break;
        }
    }

    public static Integer[][] getIntegerList(String str) {
        if (str==null){
            return null;
        }
        JSONArray arr = JSONArray.parseArray(str);
        Integer[][] list = new Integer[arr.size()][2];
        for(int i = 0; i < arr.size(); i ++){
            JSONArray arr2 = arr.getJSONArray(i);
            list[i][0] = (Integer) arr2.get(0);
            list[i][1] = (Integer) arr2.get(1);
        }
        return list;
    }

    public static boolean checkActions(Player p,Integer[][] pai,boolean isNextUser,Player chuUser){

        Map<String,Object> currentActions = p.getCurrentActions();
        if (currentActions==null){
            currentActions = new LinkedHashMap<>();
        }
        RoomResp room = roomMap.get(p.getRoomId());
        p.setCurrentActions(currentActions);
        List<Integer[][]> huns = room.getHunPai();
        if (pai==null){//初始检测，只需要检测杠或者胡
            if (MahjongUtils.checkHuNew(p,null)){
            	if (room.getDilou()!=1) {
            		int[] huInfo = MahjongUtils.checkHuInfo(p);
            		if (huInfo[0]!=Cnst.HUTYPE_DILOUHU) {
    	                currentActions.put(String.valueOf(Cnst.ACTION_HU),p.getUserId());
					}
				}else{
	                currentActions.put(String.valueOf(Cnst.ACTION_HU),p.getUserId());
				}
            }
            List<Integer[][]> gangs = MahjongUtils.checkGang(p,null);
            if (gangs!=null){
            	
            	for(Integer[][] hun:huns){
            		
            		Iterator<Integer[][]> iterator = gangs.iterator();
            		while(iterator.hasNext()) {
            			Integer[][] gang = iterator.next();  
            			if (gang[0][0].equals(hun[0][0])&&gang[0][1].equals(hun[0][1])) {
            				iterator.remove();
						}
            		}
            	}
            	if (gangs!=null&&gangs.size()>0) {
            		currentActions.put(String.valueOf(Cnst.ACTION_GANG),gangs);
				}
            }
        }else{//出牌过程中检测
            List<Integer[][]> gangs = MahjongUtils.checkGang(p,pai);
            if (gangs!=null){
            	for(Integer[][] hun:huns){
            		if (gangs.get(0)[0][0].equals(hun[0][0])&&gangs.get(0)[0][1].equals(hun[0][1])) {
            			gangs = null;
            			break;
					}
            	}
            	if (gangs!=null) {
            		currentActions.put(String.valueOf(Cnst.ACTION_GANG),gangs);
				}
            }
            List<Integer[][]> pengs = MahjongUtils.checkPeng(p,pai);
            if (pengs!=null){
            	for(Integer[][] hun:huns){
            		if (pengs.get(0)[0][0].equals(hun[0][0])&&pengs.get(0)[0][1].equals(hun[0][1])) {
            			pengs = null;
            			break;
					}
            	}
            	if (pengs!=null) {
                    currentActions.put(String.valueOf(Cnst.ACTION_PENG),pengs);
				}
            }
        }
        if (currentActions.size()==0){
            p.setCurrentActions(null);
            return false;
        }else{
            currentActions.put(String.valueOf(Cnst.ACTION_GUO),new ArrayList<>());
            return true;
        }




    }

    /**
     * 玩家申请解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100203(IoSession session, ProtocolData readData) throws Exception{
        logger.I("玩家请求解散房间,interfaceId -> 100203");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        RoomResp room = roomMap.get(roomId);
        if (room.getDissolveRoom()!=null){
            return ;
        }
        DissolveRoom dis = new DissolveRoom();
        dis.setDissolveTime(new Date().getTime());
        dis.setUserId(userId);
        List<Map<String,Object>> othersAgree = new ArrayList<>();
        List<Player> players = getPlayerList(room);
        for(Player p:players){
            if (!p.getUserId().equals(userId)){
                Map<String,Object> map = new HashMap<>();
                map.put("userId",p.getUserId());
                map.put("agree",0);//1同意；2解散；0等待
                othersAgree.add(map);
            }
        }
        dis.setOthersAgree(othersAgree);
        room.setDissolveRoom(dis);

        Map<String,Object> info = new HashMap<>();
        info.put("dissolveTime",dis.getDissolveTime());
        info.put("userId",dis.getUserId());
        info.put("othersAgree",dis.getOthersAgree());
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        for(Player p:players){
            IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
            if(se!=null&&se.isConnected()){
                se.write(pd);
            }
        }
        //解散房间超时任务开启
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_2);
    }

    /**
     * 同意或者拒绝解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100204(IoSession session, ProtocolData readData) throws Exception{
        logger.I("同意或者拒绝解散房间,interfaceId -> 100203");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        Integer userAgree = obj.getInteger("userAgree");
        RoomResp room = roomMap.get(roomId);
        if (room==null){//房间已经自动解散
            Map<String,Object> info = new HashMap<>();
            info.put("reqState",Cnst.REQ_STATE_4);
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            return;
        }
        if (room.getDissolveRoom()==null){
            Map<String,Object> info = new HashMap<>();
            info.put("reqState",Cnst.REQ_STATE_7);
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            return;
        }
        List<Map<String,Object>> othersAgree = room.getDissolveRoom().getOthersAgree();
        for(Map m:othersAgree){
            if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))){
                m.put("agree",userAgree);
                break;
            }
        }
        Map<String,Object> info = new HashMap<>();
        info.put("dissolveTime",room.getDissolveRoom().getDissolveTime());
        info.put("userId",room.getDissolveRoom().getUserId());
        info.put("othersAgree",room.getDissolveRoom().getOthersAgree());
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        
        if(userAgree==2){
            room.setDissolveRoom(null);
        }
        int agreeNum = 0;
        int rejectNunm = 0;

        for(Map m:othersAgree){
            if (m.get("agree").equals(1)){
                agreeNum++;
            }else if(m.get("agree").equals(2)){
                rejectNunm++;
            }
        }

        List<Player> players = getPlayerList(room);
        if (agreeNum==3||rejectNunm>=1){
        	if (agreeNum==3) {
				MessageFunctions.setOverInfo(room,players);
				room.setHasInsertRecord(true);
				room.setStatus(Cnst.ROOM_STATE_YJS);
				MessageFunctions.updateDatabasePlayRecord(room);
				for(Player p:players){
			        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
		        }
		        BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
			}
        	

            //关闭超时任务
            notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_2,false);
            
        }
        
        for(Player p:players){
            IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
            if(se!=null&&se.isConnected()){
                se.write(pd);
            }
        }

    }

    /**
     * 退出房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100205(IoSession session, ProtocolData readData) throws Exception{
        logger.I("退出房间,interfaceId -> 100205");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        RoomResp room = roomMap.get(roomId);
        if (room==null){
            roomDoesNotExist(interfaceId,session);
            return;
        }
        if (room.getStatus().equals(Cnst.ROOM_STATE_CREATED)){
        	List<Player> players = getPlayerList(room);
            Map<String,Object> info = new HashMap<>();
            info.put("userId",userId);
            if (room.getCreateId().equals(userId)){//房主退出，
                if (room.getRoomType().equals(Cnst.ROOM_TYPE_1)){//房主模式
                	int circle = room.getCircleNum();
                    info.put("type",Cnst.EXIST_TYPE_DISSOLVE);
                    for(Player p:players){
                    	if (p.getUserId().equals(userId)) {
        					p.setMoney(p.getMoney()+Cnst.moneyMap.get(circle));
        					break;
        				}
                    }

                    roomMap.remove(roomId);
                    //关闭解散房间计时任务
                    notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,false);
                    
                    for(Player p:players){
                        p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
                    }
                }else{//自由模式，走正常退出
                    info.put("type",Cnst.EXIST_TYPE_EXIST);
                    existRoom(room, players, userId);
                }
            }else{//正常退出
                info.put("type",Cnst.EXIST_TYPE_EXIST);
                existRoom(room, players, userId);
            }
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());


            for(Player p : players){
            	IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
            }
        }else{
            roomIsGaming(interfaceId,session);
        }
    }

    private static void existRoom(RoomResp room,List<Player> players,Long userId){
    	for(Player p:players){
        	if (p.getUserId().equals(userId)) {
        		p.initPlayer(null,null,null,Cnst.PLAYER_STATE_DATING,0,0,0);
        		break;
			}
        }
        Long[] pids = room.getPlayerIds();
        if (pids!=null) {
			for(int i=0;i<pids.length;i++){
				if (userId.equals(pids[i])) {
					pids[i] = null;
					break;
				}
			}
		}
    }


    /**
     * 语音表情
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100206(IoSession session, ProtocolData readData) throws Exception{
        logger.I("语音表情,interfaceId -> 100206");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer roomId = obj.getInteger("roomSn");
        Long userId = obj.getLong("userId");
        String type = obj.getString("type");
        String idx = obj.getString("idx");
        Map<String,Object> info = new HashMap<>();
        info.put("roomId",roomId);
        info.put("userId",userId);
        info.put("type",type);
        info.put("idx",idx);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        List<Player> players = getPlayerList(roomMap.get(roomId));
        for(Player p:players){
            if (!p.getUserId().equals(userId)){
                IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
                if (se!=null&&se.isConnected()){
                    se.write(pd);
                }
            }
        }
    }
    
    
    
    /**
     * 补牌指令
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100207(IoSession session, ProtocolData readData) throws Exception{
        logger.I("补牌指令,interfaceId -> 100207");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Long userId = obj.getLong("userId");
        Integer wsw_sole_action_id = obj.getInteger("wsw_sole_action_id");

        Player currentPlayer = getPlayerFromSession(session);

        RoomResp room = roomMap.get(currentPlayer.getRoomId());
        
        if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			MessageFunctions.interface_100108(session);
			return ;
		}
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);

        MessageFunctions.interface_100101(session, readData);
    }
    
    /**
     * 用户点击是否拉庄，
     * @param session
     * @param readData
     */
    public static void interface_100208(IoSession session, ProtocolData readData) throws Exception{
    	logger.I("用户点击是否拉庄,interfaceId -> 100207");
        JSONObject obj = JSONObject.parseObject(readData.getJsonString());
        Integer interfaceId = obj.getInteger("interfaceId");
        Integer lz = obj.getInteger("lz");

        Player currentPlayer = getPlayerFromSession(session);

        RoomResp room = roomMap.get(currentPlayer.getRoomId());
        
        if (room.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
			return;
		}
        if (!currentPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_LZ)) {
			return;
		}
        
        currentPlayer.setLz(lz);
        currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_YLZ);
        
        List<Player> players = getPlayerList(room);
        for (int m = 0; m < players.size(); m++) {
			Player p = players.get(m);
			if (p.getUserId().equals(currentPlayer.getUserId())) {
				players.set(m, currentPlayer);
				break;
			}
		}
        
        boolean isAllLz = true;
        for(Player p:players){
        	if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_YLZ)) {
        		isAllLz = false;
			}else{//解决庄的lz为null问题
				if (p.getLz()==null) {
					p.setLz(0);
				}
			}
        }
        if (isAllLz) {
        	startGame(room,players);
            BackFileUtil.write(null, interfaceId, room,players,null);//写入文件内容
            /*test*/
            BackFileUtil.writeForCount(room, players, interfaceId, null, null, null);
            /*test*/
		}

        Map<String,Object> allInfo = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> info = new ArrayList<Map<String,Object>>();
        for(Player p:players){
            Map<String,Object> i = new HashMap<String, Object>();
            i.put("userId", p.getUserId());
            i.put("playStatus",p.getPlayStatus()); 
            i.put("lzState",p.getLz()); 
            info.add(i);
        }
        allInfo.put("allLz", isAllLz);
        allInfo.put("playStatusInfo", info);
        JSONObject result = getJSONObj(interfaceId,1,allInfo);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        
        for(Player p:players){
        	IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
        	if (se!=null&&se.isConnected()) {
				se.write(pd);
			}
        }
        
    }
    
    

}
