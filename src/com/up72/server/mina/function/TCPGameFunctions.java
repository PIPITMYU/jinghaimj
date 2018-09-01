package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSONObject;
import com.leo.rms.utils.StringUtils;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.Room;
import com.up72.game.service.IRoomService;
import com.up72.game.service.IUserService;
import com.up72.game.service.IUserService_login;
import com.up72.game.service.impl.RoomServiceImpl;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.game.service.impl.UserService_loginImpl;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.DataLoader;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.PostUtil;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.redis.MyRedis;

public class TCPGameFunctions {

    public static final MyLog logger = MyLog.getLogger(TCPGameFunctions.class);
    public static IUserService userService = new UserServiceImpl();
    public static IUserService_login userService_login = new UserService_loginImpl();
    public static IRoomService roomService = new RoomServiceImpl();
    
    public static ConcurrentHashMap<Integer, RoomResp> roomMap = new ConcurrentHashMap<>(); //房间信息
    public static ConcurrentHashMap<String, Long> openIdUserMap = new ConcurrentHashMap<>(); //openId-userId数据
    public static ConcurrentHashMap<Long, Player> playerMap = new ConcurrentHashMap<>(); //openId-user数据
    

    //由于需要线程notify，需要保存线程的锁，所以保留这两个静态变量
    //独立id，对应相对的任务，无论什么type的任务，id是唯一的
    public static ConcurrentHashMap<Integer, TaskUtil.DissolveRoomTask> disRoomIdMap = new ConcurrentHashMap<>(); //解散房间的任务
    //如果房间开局或者解散时没超过5分钟就有结果了，才会向这个集合里放数据，数据格式为id--1
    public static ConcurrentHashMap<Integer, Integer> disRoomIdResultInfo = new ConcurrentHashMap<>(); //房间解散状态集合

    
    public static List<Player> getPlayerList(RoomResp room){
    	List<Player> list = new ArrayList<Player>();
    	Long[] uids = room.getPlayerIds();
    	if (uids!=null&&uids.length>0) {
			for(int i=0;i<uids.length;i++){
				if (uids[i]!=null) {
					Player p = playerMap.get(uids[i]);
					if (p!=null) {
						list.add(p);
					}
				}
			}
		}
    	return list;
    }
    
    public static String getStringByKey(String key){
    	String value = null;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		value = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return value;
    }
    public static boolean setStringByKey(String key,String value){
    	boolean result = true;
    	Jedis jedis = null;
    	try {
    		jedis = MyRedis.getRedisClient().getJedis();
    		jedis.set(StringUtils.getBytes(key), StringUtils.getBytes(value));
		} catch (Exception e) {
			result = false;
			MyRedis.getRedisClient().returnBrokenJedis(jedis);
		}finally{
			if (jedis!=null) {
				MyRedis.getRedisClient().returnJedis(jedis);
			}
		}
    	return result;
    }
    
    public static Player getPlayerFromSession(IoSession session){
    	Object obj = session.getAttribute(Cnst.USER_SESSION_USER_ID);
    	if (obj==null) {
			return null;
		}
    	return playerMap.get(Long.valueOf(String.valueOf(obj)));
    }
    
    public static void function_1(IoSession session, ProtocolData readData) {
        try {
            session.getService().broadcast(readData);
        } catch (Exception e) {
            logger.E("function_1 异常", e);
        }
    }

    
    public static JSONObject getErrorJsonObj(Integer interfaceId,Integer state,Object message){
        JSONObject obj = new JSONObject();
        obj.put("interfaceId",interfaceId);
        obj.put("state",state);
        obj.put("message",message);
        obj.put("info",null);
        obj.put("others","");
        return obj;
    }

    /**
     * 获取统一格式的返回obj
     * @param interfaceId
     * @param state
     * @param object
     * @return
     */
    public static JSONObject getJSONObj(Integer interfaceId,Integer state,Object object){
        JSONObject obj = new JSONObject();
        obj.put("interfaceId",interfaceId);
        obj.put("state",state);
        obj.put("message","");
        obj.put("info",object);
        obj.put("others","");
        return obj;
    }


    /**
     * 房间不存在
     * @param interfaceId
     * @param session
     */
    public static void roomDoesNotExist(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState", Cnst.REQ_STATE_4);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 玩家在其他房间
     * @param interfaceId
     * @param session
     */
    public static void playerExistOtherRoom(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_3);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 房间已满
     * @param interfaceId
     * @param session
     */
    public static void roomFully(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_5);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 玩家房卡不足
     * @param interfaceId
     * @param session
     */
    public static void playerMoneyNotEnough(Integer interfaceId,IoSession session,Integer roomType){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_2);//余额不足，请及时充值
        info.put("roomType",roomType);//余额不足，请及时充值
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    

    
    /**
     * 代开房间不能超过10个
     * @param interfaceId
     * @param session
     */
    public static void roomEnough(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_11);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 非法请求
     * @param session
     * @param interfaceId
     */
    public static void illegalRequest(Integer interfaceId,IoSession session){
        JSONObject result = getJSONObj(interfaceId,0,null);
        result.put("message","非法请求！");
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        session.close(true);
    }

    /**
     * 游戏中，不能退出房间
     * @param interfaceId
     * @param session
     */
    public static void roomIsGaming(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_6);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 开启等待解散房间任务
     * @param roomId
     * @param type
     */
    public static void startDisRoomTask(int roomId,int type){
        RoomResp room = roomMap.get(roomId);
        Integer createDisId = null;
        while(true){
            createDisId = CommonUtil.getGivenRamdonNum(8);
            if (!disRoomIdMap.containsKey(createDisId)) {
				break;
			}
        }
        if (type==Cnst.DIS_ROOM_TYPE_1){
            room.setCreateDisId(createDisId);
            
        }else if(type==Cnst.DIS_ROOM_TYPE_2){
            room.setApplyDisId(createDisId);
        }
        TaskUtil.DissolveRoomTask task = new TaskUtil().new DissolveRoomTask(roomId,type,createDisId);
        disRoomIdMap.put(createDisId, task);
        new Thread(task).start();
    }

    /**
     * 关闭解散房间任务
     * @param roomId
     * @param type
     */
    public static void notifyDisRoomTask(RoomResp room,int type,boolean needAddRoomToDB){
        if (room==null) {
			return;
		}
        Integer taskId = 0;
        if (type==Cnst.DIS_ROOM_TYPE_1){
            taskId = room.getCreateDisId();
            room.setCreateDisId(null);
        }else if (type==Cnst.DIS_ROOM_TYPE_2){
            taskId = room.getApplyDisId();
            room.setApplyDisId(null);
            notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1,needAddRoomToDB);
        }
        if (taskId==null) {
			return;
		}
        //移除解散任务
        TaskUtil.DissolveRoomTask task = disRoomIdMap.get(taskId);
        disRoomIdResultInfo.put(taskId, Cnst.DIS_ROOM_RESULT);
        if (task!=null) {
        	if (type==Cnst.DIS_ROOM_TYPE_1&&needAddRoomToDB) {
                //首先向数据库添加房间记录
                addRoomToDB(room);
			}
        	synchronized (task){
                task.notify();
            }
		}
    }
    
    public static void addRoomToDB(RoomResp room){
    	List<Player> players = getPlayerList(room);
    	int circle = room.getCircleNum();

      	userService.updateMoney(userService.getUserMoneyByUserId(room.getCreateId())-Cnst.moneyMap.get(circle), String.valueOf(room.getCreateId()));
         
         //添加玩家消费记录
         PlayerMoneyRecord mr = new PlayerMoneyRecord();
         mr.setUserId(room.getCreateId());
         mr.setMoney(Cnst.moneyMap.get(circle));
         mr.setType(100);
         mr.setCreateTime(new Date().getTime());
         userService.insertPlayerMoneyRecord(mr);

         /* 向数据库添加房间信息*/
         Room r = new Room();
         r.setRoomId(room.getRoomId());
         r.setCreateId(room.getCreateId());
         r.setCreateTime(room.getCreateTime());
         r.setIsPlaying(1);
         r.setDilou(room.getDilou());
         r.setRoomType(room.getRoomType());
         r.setCircleNum(room.getCircleNum());
         r.setUserId1(players.get(0).getUserId());
         r.setUserId2(players.get(1).getUserId());
         r.setUserId3(players.get(2).getUserId());
         r.setUserId4(players.get(3).getUserId());
         r.setLz(room.getLz());
         
         roomService.save(r);

         new Thread( new Runnable() {
 			@Override
 			public void run() {
 				 //统计消费
 				try {
 					PostUtil.doCount(room.getCreateId(),Cnst.moneyMap.get(circle), room.getRoomType());
 				} catch (Exception e) {
 					System.out.println("调用统计借口出错");
 					e.printStackTrace();
 				}
 				Thread.currentThread().interrupt();
 			}
 		}).start();
    }
}