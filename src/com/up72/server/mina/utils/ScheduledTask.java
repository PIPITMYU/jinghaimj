package com.up72.server.mina.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.main.MinaServerManager;

/**
 * Created by Administrator on 2017/7/28.
 */
@Component()
public class ScheduledTask {
	
	
//    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = Cnst.CLEAN_3)
    public void testTaskWithDate() {
        System.out.println("每天凌晨3点清理任务开始执行 ");
        Set<Integer> roomIds = TCPGameFunctions.roomMap.keySet();
        long startTime;
        //从房间角度清理
        if (roomIds!=null&&roomIds.size()>0){
            for(Integer roomId:roomIds){
                RoomResp room = TCPGameFunctions.roomMap.get(roomId);
                List<Player> players = TCPGameFunctions.getPlayerList(room);
                startTime = Long.parseLong(room.getCreateTime());
                long currentTime = new Date().getTime();
                if ((currentTime-startTime)>=Cnst.ROOM_OVER_TIME){//房间存在超过24小时，强制解散
                    MessageFunctions.updateDatabasePlayRecord(room);//解散房间
                    TCPGameFunctions.roomMap.remove(roomId);
                    //通知在线玩家房间被解散
                    MessageFunctions.interface_100111(Cnst.REQ_STATE_12, players,Integer.valueOf(roomId));
                }
            }
        }
        System.out.println("每天3点任务执行结束");
    }
    
//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = Cnst.CLEAN_EVERY_HOUR)
    public void JVMCount() {
        System.out.println("每小时清理任务开始…… ");
        try {
        } catch (Exception e) {
        	System.out.println("每小时清理任务异常");
        } finally {
            BackFileUtil.deletePlayRecord();
        }
        System.out.println("每小时清理任务结束");
    }
    
    @Scheduled(cron = Cnst.COUNT_EVERY_TEN_MINUTE)
    public void onlineNumCount(){
    	Map<String,Object> ipcounts = new HashMap<String, Object>();
    	Map<String,Object> temp = new HashMap<String, Object>();
    	int count = MinaServerManager.tcpServer.getSessions().size();
    	temp.put("onlineNum", String.valueOf(count));
    	List<Long> userIds = new ArrayList<Long>();
    	
    	Iterator<IoSession> iterator = MinaServerManager.tcpServer.getSessions().values().iterator();
    	while(iterator.hasNext()){
    		IoSession se = iterator.next();
    		try {
        		Long userId = (Long) se.getAttribute(Cnst.USER_SESSION_USER_ID);
        		if (userId!=null) {
        			userIds.add(userId);
				}else{
					se.close(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	temp.put("userIds", userIds);
    	
    	ipcounts.put(Cnst.SERVER_IP,temp);
    	TCPGameFunctions.setStringByKey(Cnst.REDIS_ONLINE_NUM_COUNT.concat(Cnst.SERVER_IP), JSONObject.toJSONString(ipcounts));
    	System.out.println("每10分钟统计在线人数完成");
    }

}