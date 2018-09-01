package com.up72.server.mina.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.GameFunctions;
import com.up72.server.mina.function.MessageFunctions;

public class BackFileUtil {
	
	static int num = 1;
	
	public static void rr(){
		try {
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append("redis-set-fail-log-"+num++).toString();
			File file = new File(fineName);
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void tt(){}
	
	public static boolean write(ProtocolData pd,Integer interfaceId,RoomResp room,List<Player> players,Object infos){
		if (room.getXiaoJuNum()==null) {
			return true;
		}
		boolean result = true;
		FileWriter fw = null;
		BufferedWriter w = null;
		try {
			
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append(new SimpleDateFormat("yyyyMMddHHmmss").format(d))
					.append("-")
					.append(room.getRoomId())
					.append("-")
					.append(room.getXiaoJuNum()==null?1:room.getXiaoJuNum())
					.append(".txt").toString();
			File file = new File(fineName);
			boolean fileExists = true;
			if (!file.exists()) {
				fileExists = false;
				file.createNewFile();
			}
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			if (!fileExists) {
				w.write("{\"state\":1,\"info\":[");
				w.newLine();
			}
			Map<String,Object> obj = new LinkedHashMap<String, Object>();
			switch (interfaceId) {
			case 100208://开局
				if (room.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
					Map<String,Object> map = new HashMap<String, Object>();
					List<Map<String,Object>> userInfos = new ArrayList<Map<String,Object>>();
					for (Player p:players) {
						userInfos.add(MessageFunctions.getCurrentUserMap(p));
					}
					map.put("userInfos", userInfos);
					map.put("roomInfo", MessageFunctions.getRoomInfo(room));
					obj.put("interfaceId", 100200);
					obj.put("jsonStr", map);
				}
				break;

			case 100200://开局
				if (room.getStatus().equals(Cnst.ROOM_STATE_GAMIING)) {
					Map<String,Object> map = new HashMap<String, Object>();
					List<Map<String,Object>> userInfos = new ArrayList<Map<String,Object>>();
					for (Player p:players) {
						userInfos.add(MessageFunctions.getCurrentUserMap(p));
					}
					map.put("userInfos", userInfos);
					map.put("roomInfo", MessageFunctions.getRoomInfo(room));
					obj.put("interfaceId", 100200);
					obj.put("jsonStr", map);
				}
				break;
			case 100102://小结算
				obj.put("interfaceId", 100102);
				obj.put("jsonStr", infos);
				break;
			case 100103://大结算
				obj.put("interfaceId", 100103);
				obj.put("jsonStr", room.getOverInfo());
				break;
			case 100104://动作提示
				obj.put("interfaceId", 100104);
				obj.put("jsonStr", infos);
				break;
			case 100105://出牌提示
				obj.put("interfaceId", 100105);
				obj.put("jsonStr", infos);
				break;
			case 100101://发牌提示
				obj.put("interfaceId", 100101);
				obj.put("jsonStr", infos);
				break;
			}
			if (obj.size()>0) {
				if (interfaceId.equals(100102)) {
					w.write(",");
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
					w.write("]}");
				}else if(interfaceId.equals(100103)){
					if (room.getDissolveRoom()!=null) {
						BufferedReader re = new BufferedReader(new FileReader(file));
						String lastLine = null;
						String temp = null;
						while((temp = re.readLine())!=null){
							lastLine = temp;
						}
						if (lastLine!=null&&lastLine.equals("]}")) {
							
						}else{
							w.write(",");
							w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
							w.newLine();
							w.write("]}");
						}
						re.close();
					}
				}else if(interfaceId.equals(100208)||interfaceId.equals(100200)){
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
				}else{
					w.write(",");
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
				}
			}
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}finally{
			try {
				if (w!=null) {
					w.close();
				}
				if (fw!=null) {
					fw.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		return result;
	}
	
	
	public static boolean writeXiaoJieSuanInfo(RoomResp room,Object infos){
		if (room.getXiaoJuNum()==null) {
			return true;
		}
		boolean result = true;

		FileWriter fw = null;
		BufferedWriter w = null;
		try {
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append(new SimpleDateFormat("yyyyMMddHHmmss").format(d))
					.append("-")
					.append(room.getRoomId())
					.append(".txt").toString();
			File file = new File(fineName);
			boolean fileExists = true;
			if (!file.exists()) {
				fileExists = false;
				file.createNewFile();
			}
			
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			if (!fileExists) {
				w.write("{\"state\":1,\"info\":[");
				w.newLine();
			}
			
			Map<String,Object> obj = new LinkedHashMap<String, Object>();
			obj.put("xjn", room.getXiaoJuNum());
			obj.put("jsInfo", infos);
			if (room.getXiaoJuNum()==1) {//第一句，不用写逗号
				if (room.getDissolveRoom()!=null) {//解散了
					if (room.getStatus().equals(Cnst.ROOM_STATE_XJS)) {//小结算状态解散，不写入数据
						w.write("]}");
					}else{
						w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
						w.newLine();
						w.write("]}");
					}
				}else{//没解散
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
				}
			}else{//大于第一局
				if (room.getDissolveRoom()!=null) {//解散了
					if (room.getStatus().equals(Cnst.ROOM_STATE_XJS)) {//小结算状态解散，不写入数据
						w.write("]}");
					}else{
						w.write(",");
						w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
						w.newLine();
						w.write("]}");
					}
				}else{//没解散
					w.write(",");
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
					if (room.getStatus().equals(Cnst.ROOM_STATE_YJS)) {
						w.write("]}");
					}
				}
			}
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if (w!=null) {
					w.close();
				}
				if (fw!=null) {
					fw.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		
		return result;
	}
	
	
	//文件格式为yyyyMMddHHmmss-roomId-xiaoJuNum.txt
	public static void deletePlayRecord(){
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			long currentDate = new Date().getTime();
			if (files!=null&&files.length>0) {
				for(int i=0;i<files.length;i++){
					File f = files[i];
					String name = f.getName();
					String dateStr = name.split("_")[0];
					Date createDate = sdf.parse(dateStr);
					if ((currentDate-createDate.getTime())>=Cnst.BACKFILE_STORE_TIME) {
						f.delete();
					}else{
						break;
					}
				}
			}
			System.out.println("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
	
	
	/**
	 * 每次启动服务，清零所有回放文件
	 */
	public static void deleteAllRecord(){
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();

			if (files!=null&&files.length>0) {
				for(int i=0;i<files.length;i++){
					File f = files[i];
					f.delete();
				}
			}
			path.delete();
			System.out.println("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    
	}
	
	
	
	public static Integer getFileNumByRoomId(Integer roomId){
		Integer num = 0;
		File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
		File[] files = path.listFiles();
		if (files!=null&&files.length>0) {
			for(int i=0;i<files.length;i++){
				File f = files[i];
				if (f.getName().contains("-".concat(String.valueOf(roomId)).concat("-"))) {
					num++;
				}
			}
		}
		return num;
	}
	
	
	
	public static void writeForCount(RoomResp room,List<Player> players,Integer interfaceId,Long userId,Integer action,Integer[][] chuPai){
		FileWriter fw = null;
		BufferedWriter w = null;
		try {
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append("fuckingCountFile-"+room.getRoomId())
					.append(".txt").toString();
			File file = new File(fineName);
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			
			StringBuffer sb = new StringBuffer();
			
			Player cp = null;
			Player zhuangUser = null;
			for(Player p:players){
				if (p.getUserId().equals(userId)) {
					cp = p;
				}
				if (p.getZhuang()) {
					zhuangUser = p;
				}
			}
			Player actionPlayer = GameFunctions.getActionPlayer(players);
			String actionKeys = "";
			String actionUserId = "";
			if (actionPlayer!=null) {
				for(String key:actionPlayer.getCurrentActions().keySet()){
					actionKeys = actionKeys+key+"_";
					actionUserId = String.valueOf(actionPlayer.getUserId());
				}
			}
			if (actionKeys.equals("")) {
				actionKeys = "无";
				actionUserId = "无";
			}
			
			switch (interfaceId) {
			case 100101:
				sb.append("****系统发牌******************************************************************").append("\r\n")
				.append("动作id为："+room.getWsw_sole_action_id()).append("\r\n")
				.append("发牌人："+userId).append("\r\n")
				.append("发完牌之后，手牌："+GameFunctions.getShowPaiString(cp.getCurrentMjList())).append("\r\n")
				.append("最后发的牌为："+cp.getLastFaPai()[0][0]+"_"+cp.getLastFaPai()[0][1]).append("\r\n")
				.append("当前有动作的人的动作有："+actionKeys).append("\r\n")
				.append("当前有动作的人的id为："+actionUserId).append("\r\n")
				.append(players.get(0).getUserId()+"_"+players.get(1).getUserId()+"_"+players.get(2).getUserId()+"_"+players.get(3).getUserId()).append("\r\n")
				.append(players.get(0).getNeedFaPai()+"_"+players.get(1).getNeedFaPai()+"_"+players.get(2).getNeedFaPai()+"_"+players.get(3).getNeedFaPai()).append("\r\n")
				.append(players.get(0).getPlayStatus()+"_"+players.get(1).getPlayStatus()+"_"+players.get(2).getPlayStatus()+"_"+players.get(3).getPlayStatus()).append("\r\n");
				break;
			case 100104:
				sb.append("****玩家动作******************************************************************").append("\r\n")
				.append("动作id为："+room.getWsw_sole_action_id()).append("\r\n")
				.append("动作人："+userId).append("\r\n")
				.append("动作类型："+action).append("\r\n")
				.append("动作后剩余手牌："+GameFunctions.getShowPaiString(cp.getCurrentMjList())).append("\r\n")
				.append("当前有动作的人的动作有："+actionKeys).append("\r\n")
				.append("当前有动作的人的id为："+actionUserId).append("\r\n")
				.append(players.get(0).getUserId()+"_"+players.get(1).getUserId()+"_"+players.get(2).getUserId()+"_"+players.get(3).getUserId()).append("\r\n")
				.append(players.get(0).getNeedFaPai()+"_"+players.get(1).getNeedFaPai()+"_"+players.get(2).getNeedFaPai()+"_"+players.get(3).getNeedFaPai()).append("\r\n")
				.append(players.get(0).getPlayStatus()+"_"+players.get(1).getPlayStatus()+"_"+players.get(2).getPlayStatus()+"_"+players.get(3).getPlayStatus()).append("\r\n");
				break;
			case 100105:
				sb.append("****玩家出牌******************************************************************").append("\r\n")
				.append("动作id为："+room.getWsw_sole_action_id()).append("\r\n")
				.append("出牌人："+userId).append("\r\n")
				.append("出的牌是："+chuPai[0][0]+"_"+chuPai[0][1]).append("\r\n")
				.append("出完牌剩余手牌："+GameFunctions.getShowPaiString(cp.getCurrentMjList())).append("\r\n")
				.append("当前有动作的人的动作有："+actionKeys).append("\r\n")
				.append("当前有动作的人的id为："+actionUserId).append("\r\n")
				.append(players.get(0).getUserId()+"_"+players.get(1).getUserId()+"_"+players.get(2).getUserId()+"_"+players.get(3).getUserId()).append("\r\n")
				.append(players.get(0).getNeedFaPai()+"_"+players.get(1).getNeedFaPai()+"_"+players.get(2).getNeedFaPai()+"_"+players.get(3).getNeedFaPai()).append("\r\n")
				.append(players.get(0).getPlayStatus()+"_"+players.get(1).getPlayStatus()+"_"+players.get(2).getPlayStatus()+"_"+players.get(3).getPlayStatus()).append("\r\n");
				break;
			case 100208:
				sb.append("****开局发牌******************************************************************").append("\r\n")
				.append("玩家"+players.get(0).getUserId()+"_"+players.get(0).getOpenId()+"的牌为："+GameFunctions.getShowPaiString(players.get(0).getCurrentMjList())).append("\r\n")
				.append("玩家"+players.get(1).getUserId()+"_"+players.get(1).getOpenId()+"的牌为："+GameFunctions.getShowPaiString(players.get(1).getCurrentMjList())).append("\r\n")
				.append("玩家"+players.get(2).getUserId()+"_"+players.get(2).getOpenId()+"的牌为："+GameFunctions.getShowPaiString(players.get(2).getCurrentMjList())).append("\r\n")
				.append("玩家"+players.get(3).getUserId()+"_"+players.get(3).getOpenId()+"的牌为："+GameFunctions.getShowPaiString(players.get(3).getCurrentMjList())).append("\r\n")
				.append(players.get(0).getUserId()+"_"+players.get(1).getUserId()+"_"+players.get(2).getUserId()+"_"+players.get(3).getUserId()).append("\r\n")
				.append(players.get(0).getNeedFaPai()+"_"+players.get(1).getNeedFaPai()+"_"+players.get(2).getNeedFaPai()+"_"+players.get(3).getNeedFaPai()).append("\r\n")
				.append(players.get(0).getPlayStatus()+"_"+players.get(1).getPlayStatus()+"_"+players.get(2).getPlayStatus()+"_"+players.get(3).getPlayStatus()).append("\r\n")
				.append("庄家为："+zhuangUser.getUserId());
				break;

			default:
				break;
			}
			w.write(sb.toString());
			w.newLine();
			w.newLine();
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if (w!=null) {
					w.close();
				}
				if (fw!=null) {
					fw.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}

}
