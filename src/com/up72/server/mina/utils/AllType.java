package com.up72.server.mina.utils;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.utils.redis.MyRedis;

public class AllType {
	
	static Map<Integer,List<Integer[][]>> allType = new ConcurrentHashMap<Integer, List<Integer[][]>>();
	static List<Integer[][]> allPais = MahjongUtils.initMahjongs();
	static{
		allPais.remove(0);
	}
	static Jedis jedis = null;
	static Integer totalNum = 0;
	static String roomId = "888888";
	
	static int uselessTotalNum = 0;
	

	static int fileNum = 1;
	static String filePrefix = "allType";
	static File file = null;
	
	public static void main(String[] args) throws Exception {
		h();//删除所有已知文件
		d();//初始化一个新文件
		e();//初始化room
//		j();//初始化无效数据文件
		int[] shouPaiNums = new int[]{4,7,10,13};
		List<String> list = new ArrayList<String>();
//		for(int i=0;i<shouPaiNums.length;i++){
//			a(allPais.size(), 0, list,shouPaiNums[i] ,  "");
//		}
		a(allPais.size(), 0, list,4 ,  "");
		if (list.size()>0) {
			System.out.println("有效胡牌类型一共"+list.size());
			System.out.println("对比："+(totalNum-1));
			System.out.println("一共的类型："+uselessTotalNum);
		}
		System.out.println("done...");
	}
	
	static String key = "all";
	
	public static List<String> a(int allSize,int start,List<String> list,int need,String str) throws Exception{
		String temp = str;
		for(int i=start;i<allSize;i++){
			if (need==1) {
				String mm = str.concat(i+"");
				if (g(mm)) {
//					System.out.println(mm);
					hu(mm);
				}
//				Thread.sleep(5000);
//				System.out.println(mm);
				uselessTotalNum++;
			}else{
				temp = str.concat(i+"_");
				a(allSize, i+1, list, need-1, temp);
			}
		}
		return list;
	}
	
	public static void h(){
		File f = new File("D://allType");
		File[] files = f.listFiles();
		if (files!=null&&files.length>0) {
			for(int i=files.length-1;i>=0;i--){
				files[i].delete();
			}
		}
		
	}
	
	public static boolean g(String mm){
		String[] strs = mm.split("_");
		for(int i=0;i<strs.length;i++){
			String s = strs[i];
			int num = 1;
			for(int j=0;j<strs.length;j++){
				if (i!=j) {
					String s1 = strs[j];
					if (s.equals(s1)) {
						num++;
					}
				}
			}
			
			if (num>=4) {
				return false;
			}
		}
		
		return true;
		
		
	}
	
	static Player p = new Player();
	static{
		p.setRoomId(Integer.valueOf(roomId));
	}
	
	static List<String> macthers = new ArrayList<String>();
	
	public static void hu(String mm){
		a:for(int n=0;n<allPais.size();n++){
			p = new Player();
			p.setRoomId(Integer.valueOf(roomId));
			
			String temp = new String(mm);
			temp = temp.concat("_"+n);
			String[] str = temp.split("_");
			String paisStr = "";
			List<Integer[][]> pais = new ArrayList<Integer[][]>();
			for(int i=0;i<str.length;i++){
				pais.add(allPais.get(Integer.valueOf(str[i])));
				if (i==str.length-1) {
					//验证是否包含
					paisStr = "";
					StringBuffer sb = new StringBuffer();
					for(Integer[][] ps:pais){
						sb.append("_").append(paisStr).append(ps[0][0]).append(ps[0][1]);
					}
					paisStr = sb.toString().substring(1);
					/*不用写文件*/
					if (macthers.contains(paisStr)) {
						continue a;
					}else{
						macthers.add(paisStr);
					}
					/*不用写文件*/
					
					/*写入文件*/
//					boolean contains = false;
//					int index = -1;
//					for(String ss:macthers){
//						if (ss.contains(paisStr)) {
//							index = macthers.indexOf(ss);
//							contains = true;
//							break;
//						}
//					}
//					paisStr = paisStr.concat("@@"+temp);
//					if (contains) {
//						c(null, null, 2, index, macthers.get(index), paisStr);
//						continue a;
//					}else{
//						macthers.add(paisStr);
//					}
					/*写入文件*/
					
					p.setCurrentMjList(pais);
					p.setLastFaPai(allPais.get(Integer.valueOf(str[i])));
				}
			}
			for(int i=0;i<pais.size();i++){
				int num = i(pais, pais.get(i), 0);
				if (pais.get(i)[0][0]==1&&pais.get(i)[0][1]==1) {
					if (num>3) {
						continue a;
					}
				}else{
					if (num>4) {
						continue a;
					}
				}
			}
			
			
			String huInfoStr = "";
			
			if (MahjongUtils.checkHuNew(p, null)) {
				int[] huInfo = MahjongUtils.checkHuInfo(p);
				huInfoStr = b(huInfo[0]) +"_"+ huInfo[1];
				totalNum++;
				c(p.getCurrentMjList(), huInfoStr,1,0,null,null);
				
				System.out.println("位置为："+temp+"\t"+";手牌为："+paisStr+"\t"+huInfoStr);
			}
		}
	}
	
	static FileWriter fw = null;
	static BufferedWriter w = null;
	
	static FileWriter fw1 = null;
	static BufferedWriter w1 = null;
	
	static File uselessFile = null;
	static String filePredisUseless = "uselessData";
	static Integer uselessFileNum = 1;
	static int userlessDataNun = 0;
	
	public static void j(){
		try {
			//初始化文件
			uselessFile = new File("D://allType/"+filePredisUseless+uselessFileNum+".txt");
			if (uselessFile.exists()) {
				uselessFile.delete();
			}
			fw1 = new FileWriter(uselessFile);
			w1 = new BufferedWriter(fw1);
			uselessFile.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	static int dataNum = 0;
	public static void d(){
		try {
			//初始化文件
			file = new File("D://allType/"+filePrefix+fileNum+".txt");
			if (file.exists()) {
				file.delete();
			}
			fw = new FileWriter(file);
			w = new BufferedWriter(fw);
			file.createNewFile();
			c(null, null,1,0,null,null);//初始化文件内容
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void e(){
		MyRedis.initRedis();
		jedis = MyRedis.getRedisClient().getJedis();
		RoomResp room = new RoomResp();
		List<Integer[][]> hunList = new ArrayList<Integer[][]>();
		hunList.add(new Integer[][]{{1,1}});
		hunList.add(new Integer[][]{{1,2}});
		room.setHunPai(hunList);
		jedis.set(Cnst.REDIS_PREFIX_ROOMMAP.concat(roomId), JSON.toJSONString(room));
	}
	
	
	public static Integer i(List<Integer[][]> paiList,Integer[][] pai,Integer start){
        Integer num = 0;
        for(int i=start;i<paiList.size();i++){
            if(paiList.get(i)[0][2].equals(pai[0][2])){
                num++;
            }
        }
        return num;
    }
	
	
	public static void c(List<Integer[][]> shouPais,String huInfosStr,int type,int index,String str1,String str2){
		try {
			if (type==1) {//有效数据
				if (shouPais==null&&huInfosStr==null) {
					w.write("[");
					w.newLine();
					w.flush();
				}else{
					Map<String,Object> obj = new ConcurrentHashMap<String, Object>();
					obj.put("shouPai", shouPais);
					obj.put("huInfo", huInfosStr);
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					dataNum++;
					if (dataNum%100000==0) {//十万条一个文件
						fileNum++;
						w.newLine();
						w.write("]");
						w.flush();
						d();
					}else{
						w.write(",");
						w.newLine();
					}
					w.flush();
				}
			}else{//无效数据
				w1.write("与原list第为重复，原来数据为："+str1+"，新数据为："+str2);
				w1.newLine();
				w1.flush();
				userlessDataNun++;
				if (userlessDataNun%100000==0) {//十万条一个文件
					uselessFileNum++;
					j();
				}
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String b(int huType){
		String name = "";
		switch (huType) {
		case 1:
			name = "素胡";
			break;
		case 2:
			name = "低喽胡";
			break;
		case 3:
			name = "混吊";
			break;
		case 4:
			name = "坐三万";
			break;
		case 5:
			name = "做五万";
			break;
		case 6:
			name = "低喽龙";
			break;
		case 7:
			name = "素龙";
			break;
		case 8:
			name = "混吊龙";
			break;
		case 9:
			name = "坐三龙";
			break;
		case 10:
			name = "坐五龙";
			break;
		case 11:
			name = "低喽本混龙";
			break;
		case 12:
			name = "混吊本混龙";
			break;
		case 13:
			name = "十三幺";
			break;
		case 14:
			name = "混吊三万";
			break;
		case 15:
			name = "混吊五万";
			break;
		}
		return name;
	}
	
	
	

}
