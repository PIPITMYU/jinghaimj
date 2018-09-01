package com.up72.server.mina.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.JudegHu.checkHu.Hulib;
import com.up72.server.mina.utils.JudegHu.checkHu.TableMgr;

/**
 * Created by Administrator on 2017/6/29.
 */
public class MahjongUtils {
	
	static{
		//加载胡的可能
		TableMgr.getInstance().load();
	}
	
    public static Map<Integer,List<Integer[][]>> staticPais = new HashMap<>();
    public static List<Integer[][]> list1 = new ArrayList<>();
    public static List<Integer[][]> list2 = new ArrayList<>();
    public static List<Integer[][]> list3 = new ArrayList<>();
    public static List<Integer[][]> list4 = new ArrayList<>();
    static {
        list1.add(new Integer[][]{{1,1}});list1.add(new Integer[][]{{1,2}});list1.add(new Integer[][]{{1,3}});
        list1.add(new Integer[][]{{2,1}});list1.add(new Integer[][]{{2,2}});list1.add(new Integer[][]{{2,3}});
        list1.add(new Integer[][]{{3,1}});list1.add(new Integer[][]{{3,2}});list1.add(new Integer[][]{{3,3}});
        list1.add(new Integer[][]{{4,1}});list1.add(new Integer[][]{{4,1}});list1.add(new Integer[][]{{1,3}});
        list1.add(new Integer[][]{{2,2}});
        list2.add(new Integer[][]{{1,1}});list2.add(new Integer[][]{{1,1}});list2.add(new Integer[][]{{1,1}});
        list2.add(new Integer[][]{{2,1}});list2.add(new Integer[][]{{2,2}});list2.add(new Integer[][]{{2,3}});
        list2.add(new Integer[][]{{5,1}});list2.add(new Integer[][]{{5,2}});list2.add(new Integer[][]{{5,3}});
        list2.add(new Integer[][]{{4,1}});list2.add(new Integer[][]{{4,2}});list2.add(new Integer[][]{{4,3}});
        list2.add(new Integer[][]{{4,4}});
        list3.add(new Integer[][]{{1,1}});list3.add(new Integer[][]{{1,1}});list3.add(new Integer[][]{{1,3}});
        list3.add(new Integer[][]{{2,2}});list3.add(new Integer[][]{{2,2}});list3.add(new Integer[][]{{1,3}});
        list3.add(new Integer[][]{{3,3}});list3.add(new Integer[][]{{3,3}});list3.add(new Integer[][]{{3,2}});
        list3.add(new Integer[][]{{3,2}});list3.add(new Integer[][]{{5,1}});list3.add(new Integer[][]{{5,2}});
        list3.add(new Integer[][]{{5,3}});
        list4.add(new Integer[][]{{1,1}});list4.add(new Integer[][]{{1,1}});list4.add(new Integer[][]{{1,1}});
        list4.add(new Integer[][]{{2,1}});list4.add(new Integer[][]{{2,2}});list4.add(new Integer[][]{{2,3}});
        list4.add(new Integer[][]{{3,1}});list4.add(new Integer[][]{{3,2}});list4.add(new Integer[][]{{3,3}});
        list4.add(new Integer[][]{{4,1}});list4.add(new Integer[][]{{4,1}});list4.add(new Integer[][]{{4,1}});
        list4.add(new Integer[][]{{2,2}});

        staticPais.put(Cnst.WIND_EAST,list1);
        staticPais.put(Cnst.WIND_SOUTH,list2);
        staticPais.put(Cnst.WIND_WEST,list3);
        staticPais.put(Cnst.WIND_NORTH,list4);
    }
    
    public static List<Integer[][]> testFaPaiList = new ArrayList<Integer[][]>();
    static{
    	testFaPaiList.add(new Integer[][]{{1,1}});
    }
    
    public static List<Integer[][]> testHunPaiList = new ArrayList<Integer[][]>();
    static{
    	testHunPaiList.add(new Integer[][]{{1,1}});
    }
    
    private static void printPaiArray(int[] paiArray){
    	if (paiArray!=null && paiArray.length>0) {
    		int num = 0;
    		for(int i:paiArray){
    			num++;
    			System.out.print(i+" ");
    			if (num%9==0) {
    				System.out.println();
    			}
    		}
    		System.out.println();
		}
    }
    
    /**
     * 经过此方法之后，混牌跟手牌就分开了
     * @param tempShouPai
     * @param p
     * @return
     */
    private static List<Integer[][]> getHunList(List<Integer[][]> tempShouPai,Player p){
    	List<Integer[][]> hunPai = new ArrayList<Integer[][]>();
    	RoomResp room = TCPGameFunctions.roomMap.get(p.getRoomId());
    	for(int i=0;i<room.getHunPai().size();i++){
    		for(int j=0;j<tempShouPai.size();j++){
    			if (tempShouPai.get(j)[0][0].equals(room.getHunPai().get(i)[0][0])&&tempShouPai.get(j)[0][1].equals(room.getHunPai().get(i)[0][1])) {
    				hunPai.add(tempShouPai.get(j));
    				tempShouPai.remove(tempShouPai.get(j));
    				j--;
				}
    		}
    	}
    	return hunPai;
    }
    
    
    
    public static boolean checkHuNew(Player p,Integer[][] pai){
    	List<Integer[][]> tempShouPai = getNewList(p.getCurrentMjList());
    	List<Integer[][]> hunPai = getHunList(tempShouPai,p);
    	int[] paiArray = getShouPaiArray(tempShouPai, pai);
    	
    	/** 如果哪个牌的个数是0，就让guiIndex是这个*/
    	int gui_index = 34;
    	if (hunPai!=null&&hunPai.size()>0) {
    		gui_index = addHun(paiArray, hunPai.size());
		}
    	boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
    	if (!hu&&p.getCurrentMjList().size()==14) {
    		if (gui_index<34) {
    			paiArray[gui_index] = 0;
			}
    		hu = checkShiSanYao(tempShouPai);
		}
    	
    	return hu;
    }
    /**
     * 不带混的手牌
     * @param tempShouPai
     * @return
     */
    private static boolean checkShiSanYao(List<Integer[][]> tempShouPai){
    	boolean isShiSanYao = true;
		int twoNum = 0;
		for(int i=0;i<tempShouPai.size();i++){
			Integer[][] pais = tempShouPai.get(i);
			if ((pais[0][0]==1&&pais[0][1]==1)||
					(pais[0][0]==1&&pais[0][1]==9)||
					(pais[0][0]==2&&pais[0][1]==1)||
					(pais[0][0]==2&&pais[0][1]==9)||
					(pais[0][0]==3&&pais[0][1]==1)||
					(pais[0][0]==3&&pais[0][1]==9)||
					(pais[0][0]==4&&pais[0][1]==1)||
					(pais[0][0]==4&&pais[0][1]==2)||
					(pais[0][0]==4&&pais[0][1]==3)||
					(pais[0][0]==4&&pais[0][1]==4)||
					(pais[0][0]==5&&pais[0][1]==1)||
					(pais[0][0]==5&&pais[0][1]==2)||
					(pais[0][0]==5&&pais[0][1]==3)) {
				int nums = checkPaiNum(tempShouPai, pais, tempShouPai.indexOf(pais));
				if (nums>2) {
					isShiSanYao = false;
					break;
				}
				if (nums==2) {
					twoNum++;
					i++;
				}
			}else{
				isShiSanYao = false;
				break;
			}
		}
		if (isShiSanYao&&twoNum<2) {
			return true;
		}
		return false;
    }
    
    /**
     * 产生如下的数组，去检测是否胡牌
     * int[] cards = { 0, 0, 0, 1, 1, 1, 0, 0, 0, 
	 *	1, 1, 1, 0, 0, 0, 0, 0, 0, 
	 *	2, 0, 0, 0, 0, 0, 0, 0, 0, 
	 *	0, 0, 0, 0, 0, 0, 0 };
     * 如果牌为null则判断自摸
     * 如果不是null，则判断点炮
     * @param playersPais
     * @param pai
     * @return
     */
    private static int[] getShouPaiArray(List<Integer[][]> playersPais,Integer[][] pai){
    	int[] array = initArrays();
    	for(int i=0;i<playersPais.size();i++){
    		addArray(array, playersPais.get(i));
    	}
    	if (pai!=null) {
    		addArray(array, pai);
		}
    	return array;
    }
    
    private static void addArray(int[] array,Integer[][] pai){
    	int type = pai[0][0];
    	int num = pai[0][1];
    	switch (type) {
		case 1:
		case 2:
		case 3:
			array[(type-1)*9+num-1]++;
			break;
		case 4:
			array[3*9+num-1]++;
			break;
		case 5:
			array[3*9+4+num-1]++;
			break;
		}
    }
    
    /**
     * length为34的新数组
     * @return
     */
    private static int[] initArrays(){
    	int[] array = new int[34];
    	for(int i=0;i<34;i++){
    		array[i] = 0;
    	}
    	return array;
    }
    
    

    public static List<Integer[][]> getNewList(List<Integer[][]> old){
    	List<Integer[][]> newList = new ArrayList<Integer[][]>();
    	if (old!=null&&old.size()>0) {
			for(Integer[][] pai:old){
				newList.add(new Integer[][]{{pai[0][0],pai[0][1]}});
			}
		}
    	return newList;
    }









    /**
     * 检测单个牌出现的次数
     * 传入牌的集合，以及要统计的拍出现次数的牌，然后返回出现的次数
     * @param paiList
     * @param pai
     * @return
     */
    public static Integer checkPaiNum(List<Integer[][]> paiList,Integer[][] pai,Integer start){
        Integer num = 0;
        for(int i=start;i<paiList.size();i++){
            if(paiList.get(i)[0][0].equals(pai[0][0])&&paiList.get(i)[0][1].equals(pai[0][1])){
                num++;
            }
        }
        return num;
    }


    /**
     * 排序
     * 给出一组牌
     * 返回按照类型以及大小拍好顺序的牌
     * @param pais
     * @return
     */
    public static List<Integer[][]> paiXu(List<Integer[][]> pais){
        Integer[] arrays = new Integer[pais.size()];
        for(int i=0;i<arrays.length;i++){
            arrays[i] = pais.get(i)[0][0]*10+pais.get(i)[0][1];
        }
        Arrays.sort(arrays);
        for(int i=0;i<arrays.length;i++){
            pais.get(i)[0][0] = arrays[i]/10;
            pais.get(i)[0][1] = arrays[i]%10;
        }
        return pais;
    }




    /**
     * 碰牌检测
     * 如果玩家只有四张牌，需要检测是不是飘，
     * 如果是飘，继续检测能不能碰，如果不是飘，就不能碰
     * @param p
     * @param pai
     * @return
     */
    public static List<Integer[][]> checkPeng(Player p,Integer[][] pai){
        List<Integer[][]> result = new ArrayList<>();
        result = pengGangCheck(p.getCurrentMjList(),pai,3);
        return result!=null&&result.size()>0?result:null;
    }


    /**
     * 杠牌检测 必须先检测飘的情况
     * 1.起手牌检测 pai参数为null
     *  特殊杠
     *  正常杠检测
     * 2.游戏中检测
     *  特殊杠
     *  正常杠
     *  3.手牌与碰牌集合检测
     * @param p
     * @param pai
     * @return
     */
    public static List<Integer[][]> checkGang(Player p,Integer[][] pai){
        List<Integer[][]> result = new ArrayList<>();
        List<Integer[][]> list2 = pengGangCheck(p.getCurrentMjList(),pai,4);
        if (list2!=null) result.addAll(list2);
        if (pai==null) {
        	//需要检测手中的牌与碰牌集合有没有能组成杠的
            if (p.getPengList()!=null&&p.getPengList().size()>0){
                for(int i=0;i<p.getPengList().size();i++){
                    List<Integer[][]> list = p.getPengList().get(i).getL();
                    for(int j=0;j<p.getCurrentMjList().size();j++){
                        if (p.getCurrentMjList().get(j)[0][0].equals(list.get(0)[0][0])&&p.getCurrentMjList().get(j)[0][1].equals(list.get(0)[0][1])){
                            result.add(new Integer[][]{{p.getCurrentMjList().get(j)[0][0],p.getCurrentMjList().get(j)[0][1]}});
                            break;
                        }
                    }
                }
            }
		}
        return result.size()>0?result:null;
    }


    /**
     * 碰杠检测的核心方法
     * 在传入pai参数的情况下，检测碰或者杠的逻辑基本相符（杠是四张牌的基本杠），提取出的公共方法
     * @param palyerPai
     * @param pai
     * @return
     */
    private static List<Integer[][]> pengGangCheck(List<Integer[][]> palyerPai,Integer[][] pai,Integer ckeckNum){
        if(pai==null){//初始杠检测
            List<Integer[][]> list = new LinkedList<>();
            Set<String> set = new HashSet<>();
            for(int i=0;i<palyerPai.size();i++){
                int num = 0;
                if(!set.contains(palyerPai.get(i)[0][0]+"_"+palyerPai.get(i)[0][1])){
                    for(int j=0;j<palyerPai.size();j++){
                        if(palyerPai.get(i)[0][0].equals(palyerPai.get(j)[0][0])&&palyerPai.get(i)[0][1].equals(palyerPai.get(j)[0][1])){
                            num++;
                        }
                    }
                }
                if(num==4){
                    set.add(palyerPai.get(i)[0][0]+"_"+palyerPai.get(i)[0][1]);
                    list.add(new Integer[][]{
                            {palyerPai.get(i)[0][0],palyerPai.get(i)[0][1]},
                            {palyerPai.get(i)[0][0],palyerPai.get(i)[0][1]},
                            {palyerPai.get(i)[0][0],palyerPai.get(i)[0][1]},
                            {palyerPai.get(i)[0][0],palyerPai.get(i)[0][1]}
                    });
                }
            }
            return list.size()>0?list:null;
        }else{
            Integer[][] pai1 = new Integer[][]{{pai[0][0],pai[0][1]}};
            ckeckNum--;
            Map<String,Integer> map = new HashMap<>();//牌---次数
            for(int i=0;i<palyerPai.size();i++){
                if(pai1[0][0].equals(palyerPai.get(i)[0][0])&&pai1[0][1].equals(palyerPai.get(i)[0][1])){//与传入的牌值相同
                    if (map.containsKey(pai1[0][0]+"_"+pai1[0][1])){
                        map.put(pai1[0][0]+"_"+pai1[0][1],map.get(pai1[0][0]+"_"+pai1[0][1])+1);
                    }else{
                        map.put(pai1[0][0]+"_"+pai1[0][1],1);
                    }
                }
            }
            if (map.containsKey(pai1[0][0]+"_"+pai1[0][1])&&map.get(pai1[0][0]+"_"+pai1[0][1])>=ckeckNum){
                List<Integer[][]> result = new ArrayList<>();
                if(ckeckNum==3){
                    result.add(new Integer[][]{{pai1[0][0],pai1[0][1]},{pai1[0][0],pai1[0][1]},{pai1[0][0],pai1[0][1]}});
                }else if(ckeckNum==2){
                    result.add(new Integer[][]{{pai1[0][0],pai1[0][1]},{pai1[0][0],pai1[0][1]}});
                }
                return result;
            }
        }
        return null;
    }


    public static List<Integer[][]> initMahjongs() {
        List<Integer[][]> list = new ArrayList<>();
        int num = 1;
        for (Integer type: MahjongCons.mahjongType.keySet()){
            for (int i = 0;i < MahjongCons.mahjongType.get(type) ; i ++){
                for (int j = 0 ; j < 4 ; j ++){
                    list.add(new Integer[][]{{type,i+1,num}});
                }
                num++;
            }
        }
        return list;
    }
    
    /**
     * 洗牌
     * 传入一副麻将，打乱顺序之后，返回麻将
     * @param mahjongs
     * @return
     */
    public static List<Integer[][]> xiPai(List<Integer[][]> mahjongs){
        int random = 0;
    	if (mahjongs!=null&&mahjongs.size()>0) {
			for(int i=mahjongs.size()-1;i>=0;i--){
				Integer[][] temp = mahjongs.get(i);
	            random = (int)(Math.random()*i);
	            mahjongs.set(i, mahjongs.get(random));
	            mahjongs.set(random, temp);
			}
		}
    	
    	return mahjongs;
    	
//        List<Integer[][]> temp = new ArrayList<>();
//        int last = mahjongs.size();
//        int[] pai = new int[2];
//        for(int i = 0; i <mahjongs.size() ;i++){
//            random = (int)(Math.random()*last);
//            temp.add(new Integer[][]{{mahjongs.get(random)[0][0],mahjongs.get(random)[0][1]}});
//            mahjongs.remove(random);
//            i--;
//            last = mahjongs.size();
//        }
//        return temp;
    }

    /**
     * 发牌/揭牌
     * 传入麻将列表，以及要发几张牌，返回对应的数组
     * 如果牌数少于要求返回的张数，返回null
     * @param mahjongs
     * @param num
     * @return
     */
    public static List<Integer[][]> faPai(List<Integer[][]> mahjongs,Integer num){
        if(mahjongs.size()==0){
            return null;
        }
        List<Integer[][]> result = new ArrayList<>();
//        int random = 0;
        for(int i = 0; i < num;i ++){
//            random = (int)(Math.random()*mahjongs.size());
            result.add(new Integer[][]{{mahjongs.get(i)[0][0],mahjongs.get(i)[0][1]}});
            mahjongs.remove(i);
        }
        if (num>1){
            result = paiXu(result);
        }
        return result;
    }


    /**
     * 碰牌操作，传入玩家手中的牌，以及要碰的牌，返回碰之后的牌
     * @param playerPais
     * @param pai
     * @return
     */
    public static List<Integer[][]> peng(List<Integer[][]> playerPais,Integer[][] pai){
        return pengOrGang(playerPais,pai,2);
    }

    /**
     * 杠牌操作，传入玩家手中的牌，以及要杠的牌，返回杠的类型
     * @param p
     * @param pais
     * @return
     */
    public static Integer gang(Player p,List<Integer[][]> pais){
        //1特殊杠（第一手的中发白）,2特殊杠（东南西北），3碰的杠（明杠），4点的杠（明杠），5暗杠
        Integer gangType = null;
        if (pais.size()==3){//1初始中发白
        	gangType = 4;
            p.setCurrentMjList(pengOrGang(p.getCurrentMjList(),pais.get(0),3));
        }else if(pais.size()==4){//
            if (pais.get(0)[0][1].equals(pais.get(1)[0][1])){//5
                gangType = 5;
                if (p.getLastFaPai()!=null&&p.getLastFaPai()[0][0].equals(pais.get(0)[0][0])&&p.getLastFaPai()[0][1].equals(pais.get(0)[0][1])) {
					p.setLastFaPai(null);
				}
                p.setCurrentMjList(pengOrGang(p.getCurrentMjList(),pais.get(0),4));
            }
        }else if(pais.size()==1){//游戏中的杠或者补得中发白东南西北杠
            Integer[][] pai = pais.get(0);
            if (p.getPengList()!=null&&p.getPengList().size()>0){
                boolean flag = false;//是不是碰的杠3
                for(int i=0;i<p.getPengList().size();i++){
                    List<Integer[][]> pengList = p.getPengList().get(i).getL();
                    if (pengList.get(0)[0][0].equals(pai[0][0])&&pengList.get(0)[0][1].equals(pai[0][1])){
                        gangType = 3;
                        flag = true;
                        if (p.getLastFaPai()!=null) {
                        	if (pai[0][0].equals(p.getLastFaPai()[0][0])&&pai[0][1].equals(p.getLastFaPai()[0][1])) {//移除随后发的牌
                                p.setLastFaPai(null);
    						}else{//需要把最后发的牌加入手牌
    							p.getCurrentMjList().add(new Integer[][]{{p.getLastFaPai()[0][0],p.getLastFaPai()[0][1]}});
                                p.setLastFaPai(null);
                                pengOrGang(p.getCurrentMjList(), pai, 1);
    						}
						}else{
                            pengOrGang(p.getCurrentMjList(), pai, 1);
						}
                        
                        p.getGangListType3().add(p.getPengList().get(i));
                        p.getPengList().remove(i);
                        p.getGangListType3().get(p.getGangListType3().size()-1).getL().add(new Integer[][]{{pai[0][0],pai[0][1]}});
                        break;
                    }
                }
                if (!flag){//4点杠
                    gangType = 4;
                    p.setCurrentMjList(pengOrGang(p.getCurrentMjList(),pais.get(0),3));
                }
            }else{//4点杠
                gangType = 4;
                p.setCurrentMjList(pengOrGang(p.getCurrentMjList(),pais.get(0),3));
            }
        }
        return gangType;
    }

    private static void removePais(List<Integer[][]> playerPais,List<Integer[][]> toRemovePais){
        for(int i=0;i<toRemovePais.size();i++){
            for(int j=0;j<playerPais.size();j++){
                if (playerPais.get(j)[0][0].equals(toRemovePais.get(i)[0][0])&&playerPais.get(j)[0][1].equals(toRemovePais.get(i)[0][1])){
                    playerPais.remove(j);
                    break;
                }
            }
        }
    }
    private static void addAllPais(List<Integer[][]> playerPais,List<Integer[][]> toAddPais){
    	for(int i=0;i<toAddPais.size();i++){
    		playerPais.add(new Integer[][]{{toAddPais.get(i)[0][0],toAddPais.get(i)[0][1]}});
    	}
    }

    /**
     * 碰杠的执行方法
     * @param playerPais
     * @param pai
     * @param num
     * @return
     */
    private static List<Integer[][]> pengOrGang(List<Integer[][]> playerPais,Integer[][] pai,Integer num){
        for(int n=0;n<num;n++){
            for(int i=0;i<playerPais.size();i++){
                if (pai[0][0].equals(playerPais.get(i)[0][0])&&pai[0][1].equals(playerPais.get(i)[0][1])){
                    playerPais.remove(i);
                    break;
                }
            }
        }
        return playerPais;
    }



    private static Boolean heartCheckHu(List<Integer[][]> playerPais,List<Integer[][]> shunzi){
        List<Integer[][]> currentList = new ArrayList<>();
        currentList.addAll(playerPais);

        for(int j=0;j<currentList.size();j++){
            Integer num = checkPaiNum(currentList,currentList.get(j),j);//可以看成是顺子的个数
            if(num<3){
                if(checkShunZi(currentList,j,num)){
                    //移除已经组成的顺子，然后递归
                    return heartCheckHu(removeShunZi(currentList,currentList.get(j),num,shunzi),shunzi);
                }else{
                    return false;
                }
            }else{
                Integer[][] iPai = currentList.get(j);
                Integer[][] iPai1 = currentList.get(j+1);
                Integer[][] iPai2 = currentList.get(j+2);
                currentList.remove(j+2);
                currentList.remove(j+1);
                currentList.remove(j);
                if(heartCheckHu(currentList,shunzi)){//按照暗刻来计算胡牌
                    return true;
                }else{//按照三个顺子来计算胡牌
                    currentList.add(j,iPai);
                    currentList.add(j+1,iPai1);
                    currentList.add(j+2,iPai2);
                    if(checkShunZi(currentList,j,num)){
                        return heartCheckHu(removeShunZi(currentList,currentList.get(j),num,shunzi),shunzi);
                    }else{
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 胡牌检测--检测顺子
     * 传入牌的集合，以及要检测的开始位置，和顺子个数
     * 检测顺子时首先要检测牌型，不能为风牌和箭牌
     * 返回是否是顺子
     * @param playerPais
     * @param position
     * @param num
     * @return
     */
    private static Boolean checkShunZi(List<Integer[][]> playerPais,Integer position,Integer num){
        if(playerPais.get(position)[0][0]==4||playerPais.get(position)[0][0]==5){
            return false;
        }
        if((position+3*num-1)<playerPais.size()){
            List<Integer> firstPosi = new ArrayList<>();
            List<Integer> secondPosi = new ArrayList<>();
            List<Integer> thirdPosi = new ArrayList<>();
            for(int i=position;i<playerPais.size();i++){
                if(playerPais.get(position)[0][0].equals(playerPais.get(i)[0][0])&&
                        playerPais.get(position)[0][1].equals(playerPais.get(i)[0][1])){
                    firstPosi.add(i);
                }else if(playerPais.get(position)[0][0].equals(playerPais.get(i)[0][0])&&
                        playerPais.get(i)[0][1].equals(playerPais.get(position)[0][1]+1)){
                    secondPosi.add(i);
                }else if(playerPais.get(position)[0][0].equals(playerPais.get(i)[0][0])&&
                        playerPais.get(i)[0][1].equals(playerPais.get(position)[0][1]+2)){
                    thirdPosi.add(i);
                }
            }
            if(firstPosi.size()<num||secondPosi.size()<num||thirdPosi.size()<num){
                return false;
            }
        }else{
            return false;
        }
        return true;
    }

    /**
     * 胡牌检测--移除顺子部分
     * 把可以组成顺子的移除掉，返回移除后的list
     * @param playerPais
     * @param pai
     * @param num
     * @return
     */
    private static List<Integer[][]> removeShunZi(List<Integer[][]> playerPais,Integer[][] pai,Integer num,List<Integer[][]> shunzi){
        int fn = 0,sn = 0,tn = 0;
        for(int k=0;k<playerPais.size();k++){
            if(fn<num&&pai[0][0].equals(playerPais.get(k)[0][0])&&
                    pai[0][1].equals(playerPais.get(k)[0][1])){
                fn++;
                shunzi.add(playerPais.get(k));
                playerPais.remove(k--);
            }else if(sn<num&&pai[0][0].equals(playerPais.get(k)[0][0])&&
                    playerPais.get(k)[0][1].equals(pai[0][1]+1)){
                sn++;
                shunzi.add(playerPais.get(k));
                playerPais.remove(k--);
            }else if(tn<num&&pai[0][0].equals(playerPais.get(k)[0][0])&&
                    playerPais.get(k)[0][1].equals(pai[0][1]+2)){
                tn++;
                shunzi.add(playerPais.get(k));
                playerPais.remove(k--);
            }
        }
        return playerPais;
    }
    
    
    /**
     * 返回的数组中，length为2，第一个为胡的类型，第二个为番数
     * @param huUser
     * @return
     */
    public static int[] checkHuInfo(Player huUser){
    	int[] huInfo = new int[2];
    	List<Integer[][]> playerPais = getNewList(huUser.getCurrentMjList());
    	List<Integer[][]> hunList = getHunList(playerPais, huUser);
    	if (playerPais.size()==0) {//全是混牌的情况
    		getAllHunHuInfo(hunList.size(), huInfo);
			return huInfo;
		}
    	boolean hasHun = hunList!=null&&hunList.size()>0;
    	
    	boolean lastFaPaiIsHun = false;
    	int hunType = 0;
		for(int i=0;i<hunList.size();i++){
			hunType = hunList.get(i)[0][0];
			if (huUser.getLastFaPai()[0][0].equals(hunList.get(i)[0][0])
					&&huUser.getLastFaPai()[0][1].equals(hunList.get(i)[0][1])) {
				lastFaPaiIsHun = true;
				break;
			}
		}
    	
    	boolean shisanyao = false;
    	if (huUser.getCurrentMjList().size()==14) {
    		shisanyao = checkShiSanYao(playerPais);
		}
    	if (shisanyao) {//十三幺
    		huInfo[0] = Cnst.HUTYPE_SHISANYAO;
    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
		}else{
			int longNum = checkLong(playerPais, hunList);
			if (longNum!=0) {//有龙
				//有龙的情况，需要把龙移除，在检测其他项
				List<Integer[][]> tempShowPai = getNewList(playerPais);
				
				Object[] objs = removeShouPaiLong(tempShowPai, longNum, hunList.size());
				int hunNum = (int) objs[0];//把tempShowPai的龙移除之后
				tempShowPai = (List<Integer[][]>) objs[1];
				Integer[][] lastPai = huUser.getLastFaPai();
				if (hasHun) {//有混
					boolean hasHunDiao = false;
					boolean lastPaiUsed = false;//计算最后一张牌是否被使用
					//计算最后一张牌是否被用了
					if (!lastFaPaiIsHun&&huUser.getLastFaPai()[0][0]==longNum) {//最后发的牌是跟龙一直的
						int num = checkPaiNum(huUser.getCurrentMjList(), huUser.getLastFaPai(), 0);
						if (num==1) {//最后发的这张牌只有一个，然后去校验最后的这张牌用没用
							
							//计算lastPaiUsed
							if (hunNum>=1) {
								int[] paiArray = getShouPaiArray(tempShowPai, lastPai);
								int gui_index = addHun(paiArray, hunNum-1);
								boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
								if (!hu) {
									lastPaiUsed = true;
								}
							}
							
							//计算hasHunDiao
							if (hunNum>=2) {
								hunNum--;
								List<Integer[][]> temps = getNewList(tempShowPai);
								temps.add(new Integer[][]{{lastPai[0][0],lastPai[0][1]}});
								if (checkHunDiao(temps, lastPai,hunNum ,lastFaPaiIsHun)) {
									hasHunDiao = true;
								}else{
									lastPaiUsed = true;
									hunNum++;
								}
							}
						}
					}
					
					if (!hasHunDiao&&!lastPaiUsed) {
						hasHunDiao = checkHunDiao(tempShowPai, lastPai, hunNum, lastFaPaiIsHun);
					}
					
					if (hasHunDiao) {//混吊
						if (longNum==hunType) {//混吊本混long
				    		huInfo[0] = Cnst.HUTYPE_HUNDIAOBENHUNLONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else{
				    		huInfo[0] = Cnst.HUTYPE_HUNDIAOLONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}
					}else{
						if (longNum==hunType) {
				    		huInfo[0] = Cnst.HUTYPE_DILOUBENHUNLLONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else{
//							在龙的情况下检测坐三五，得单独把龙拿出来，检测
							if (longNum==1) {
								List<Integer[][]> longPais = (List<Integer[][]>) objs[2];
								boolean hasSan = false;
								boolean hasWu = false;
								for(Integer[][] pais:longPais){
									if(pais[0][1]==3){
										hasSan = true;
									}else if(pais[0][1]==5){
										hasWu = true;
									}
								}
								
								if (!lastFaPaiIsHun) {//最后来的不是混
									if (lastPai[0][0]==1&&lastPai[0][1]==3) {//最后来的是三万
							    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}else if(lastPai[0][0]==1&&lastPai[0][1]==5){//最后来的是5万
							    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}else{
										/*最后来的牌不是35万，此处不用检测混吊*/
							    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}
								}else{//最后来的是混
									if (!hasSan) {
							    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}else if(!hasWu){
							    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}else{
										hunNum = hunList.size();
										int zuosanwu = 0;
										if (hunNum>0) {
											zuosanwu = isZuoSanOrZuoWu(tempShowPai, true, hunNum, huUser.getLastFaPai(), lastFaPaiIsHun,false);
										}else{
											if (lastFaPaiIsHun) {
												zuosanwu = isZuoSanOrZuoWu(tempShowPai, false, hunNum, new Integer[][]{{-1,-1}}, false,false);
											}else{
												zuosanwu = isZuoSanOrZuoWu(tempShowPai, false, hunNum, huUser.getLastFaPai(), false,false);
											}
										}
										if (zuosanwu==0) {
											//在低喽龙的时候，需要单独拿整副手牌检测是不是混吊35万
											if(checkHunDiao(playerPais, lastPai, hunList.size(), lastFaPaiIsHun)){//是混吊
												if (lastPai[0][0]==1&&lastPai[0][1]==3||lastFaPaiIsHun) {//最后来的是三万
										    		huInfo[0] = Cnst.HUTYPE_HUNDIAOSAN;
										    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
										    		huInfo[0] = Cnst.HUTYPE_HUNDIAO;
												}else if(lastPai[0][0]==1&&lastPai[0][1]==5){//最后来的是5万
										    		huInfo[0] = Cnst.HUTYPE_HUNDIAOWU;
										    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
										    		huInfo[0] = Cnst.HUTYPE_HUNDIAO;
												}else{
										    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
										    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
												}
											}else{
									    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
									    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
											}
										}else if(zuosanwu==3){
								    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
								    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
										}else if(zuosanwu==5){
								    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
								    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
										}
									}
								}
							}else{//筒或者条的龙，需要看剩下的手牌能否构成坐三五
								int zuosanwu = 0;
								if (hunNum>0) {
									zuosanwu = isZuoSanOrZuoWu(tempShowPai, true, hunNum, huUser.getLastFaPai(), lastFaPaiIsHun,false);
								}else{
									if (lastFaPaiIsHun) {
										zuosanwu = isZuoSanOrZuoWu(tempShowPai, false, hunNum, new Integer[][]{{-1,-1}}, false,false);
									}else{
										zuosanwu = isZuoSanOrZuoWu(tempShowPai, false, hunNum, huUser.getLastFaPai(), false,false);
									}
								}
								if (zuosanwu==0) {
						    		//在低喽龙的时候，需要单独拿整副手牌检测是不是混吊35万
									if(checkHunDiao(playerPais, lastPai, hunList.size(), lastFaPaiIsHun)){//是混吊
										if (lastPai[0][0]==1&&lastPai[0][1]==3||lastFaPaiIsHun) {//最后来的是三万
								    		huInfo[0] = Cnst.HUTYPE_HUNDIAOSAN;
								    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								    		huInfo[0] = Cnst.HUTYPE_HUNDIAO;
										}else if(lastPai[0][0]==1&&lastPai[0][1]==5){//最后来的是5万
								    		huInfo[0] = Cnst.HUTYPE_HUNDIAOWU;
								    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								    		huInfo[0] = Cnst.HUTYPE_HUNDIAO;
										}else{
								    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
								    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
										}
									}else{
							    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}
								}else if(zuosanwu==3){
						    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
						    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								}else if(zuosanwu==5){
						    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
						    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								}
								
							}
						}
					}
				}else{//没有混
					

//					在龙的情况下检测坐三五，得单独把龙拿出来，检测
					if (longNum==1) {
						List<Integer[][]> longPais = (List<Integer[][]>) objs[2];
						boolean hasSan = false;
						boolean hasWu = false;
						for(Integer[][] pais:longPais){
							if(pais[0][1]==3){
								hasSan = true;
							}else if(pais[0][1]==5){
								hasWu = true;
							}
						}

						if (lastPai[0][0]==1&&lastPai[0][1]==3) {//最后来的是三万
				    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else if(lastPai[0][0]==1&&lastPai[0][1]==5){//最后来的是5万
				    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else{
				    		huInfo[0] = Cnst.HUTYPE_SULONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}
					}else{//筒或者条的龙，需要看剩下的手牌能否构成坐三五
						if (lastPai[0][0]==1&&(lastPai[0][1]==3||lastPai[0][1]==5)) {//最后发的牌是三万或者五万
							int zuosanwu = 0;
							zuosanwu = isZuoSanOrZuoWu(tempShowPai, hasHun, hunNum, huUser.getLastFaPai(), lastFaPaiIsHun,false);
							if (zuosanwu==0) {
					    		huInfo[0] = Cnst.HUTYPE_SULONG;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}else if(zuosanwu==3){
					    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}else if(zuosanwu==5){
					    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}
						}else{
				    		huInfo[0] = Cnst.HUTYPE_SULONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}
						
						
					}
					
				}
			}else{//没有龙
				//飘牌+混吊=混吊龙（混吊龙的另一种情况）
				if (checkPiao(playerPais, hunList.size(), hasHun)) {
					//飘情况下的混吊检测
					if (hunList.size()>=1) {
						int[] paiArray = getShouPaiArray(playerPais, null);
						int hunNum = hunList.size();
						if (lastFaPaiIsHun) {
							hunNum-=2;
						}else{
							hunNum-=1;
							Integer[][] lastFaPai = huUser.getLastFaPai();
				    		if (lastFaPai[0][0]<=3) {
								paiArray[9*(lastFaPai[0][0]-1)+lastFaPai[0][1]-1]--;
							}else if (lastFaPai[0][0]==4) {
								paiArray[9*3+lastFaPai[0][1]-1]--;
							}else if (lastFaPai[0][0]==5) {
								paiArray[9*3+4+lastFaPai[0][1]-1]--;
							}
						}
						boolean hu = checkPiao(getPaiListByArray(paiArray), hunNum, true);
//				    	boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
				    	if (hu) {//构成混吊 8番
							huInfo[0] = Cnst.HUTYPE_HUNDIAOLONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else{
							//如果没有构成混吊，需要检测坐三坐五
							int zuosanwu = 0;
							zuosanwu = isZuoSanOrZuoWu(playerPais, hasHun, hunList.size(), huUser.getLastFaPai(), lastFaPaiIsHun,true);
							if (zuosanwu==0) {
								//在低喽龙的时候，需要单独拿整副手牌检测是不是混吊35万
								Integer[][] lastPai = huUser.getLastFaPai();
								if(checkHunDiao(playerPais, lastPai, hunList.size(), lastFaPaiIsHun)){//是混吊
									if (lastPai[0][0]==1&&lastPai[0][1]==3||lastFaPaiIsHun) {//最后来的是三万
							    		huInfo[0] = Cnst.HUTYPE_HUNDIAOSAN;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							    		huInfo[0] = Cnst.HUTYPE_HUNDIAO;
									}else if(lastPai[0][0]==1&&lastPai[0][1]==5){//最后来的是5万
							    		huInfo[0] = Cnst.HUTYPE_HUNDIAOWU;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							    		huInfo[0] = Cnst.HUTYPE_HUNDIAO;
									}else{
							    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
							    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
									}
								}else{
						    		huInfo[0] = Cnst.HUTYPE_DILOULONG;
						    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								}
							}else if(zuosanwu==3){
					    		huInfo[0] = Cnst.HUTYPE_ZUOSANLONG;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}else if(zuosanwu==5){
					    		huInfo[0] = Cnst.HUTYPE_ZUOWULONG;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}
						}
					}else{
						//如果有混就是低喽龙
						if (hasHun) {
							huInfo[0] = Cnst.HUTYPE_DILOULONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else{//没混就是素龙
							huInfo[0] = Cnst.HUTYPE_SULONG;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}
					}
				}else{
					if(hasHun){//检测是否是混吊
						if (checkHunDiao(playerPais, huUser.getLastFaPai(), hunList.size(),lastFaPaiIsHun)) {
							//在混掉的时候，如果混吊35万或者混吊混，也是坐三五
							if (lastFaPaiIsHun||(huUser.getLastFaPai()[0][0]==1&&huUser.getLastFaPai()[0][1]==3)) {//如果是混吊3万或者混吊混，则都按坐三处理
								huInfo[0] = Cnst.HUTYPE_HUNDIAOSAN;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								huInfo[0] = Cnst.HUTYPE_HUNDIAO;
							}else if(huUser.getLastFaPai()[0][0]==1&&huUser.getLastFaPai()[0][1]==5){//混吊5万为坐五
								huInfo[0] = Cnst.HUTYPE_HUNDIAOWU;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
								huInfo[0] = Cnst.HUTYPE_HUNDIAO;
							}else{
								huInfo[0] = Cnst.HUTYPE_HUNDIAO;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}
						}else{
							int zuosanwu = isZuoSanOrZuoWu(playerPais, hasHun, hunList.size(), huUser.getLastFaPai(), lastFaPaiIsHun,false);
							if (zuosanwu==0) {//不是坐3/5
								huInfo[0] = Cnst.HUTYPE_DILOUHU;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}else if(zuosanwu == 3){
								huInfo[0] = Cnst.HUTYPE_ZUOSANWAN;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}else if(zuosanwu == 5){
								huInfo[0] = Cnst.HUTYPE_ZUOWUWAN;
					    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
							}	
						}
					}else{//素胡
						int zuosanwu = isZuoSanOrZuoWu(playerPais, hasHun, hunList.size(), huUser.getLastFaPai(), lastFaPaiIsHun,false);
						if (zuosanwu==0) {//不是坐3/5
							huInfo[0] = Cnst.HUTYPE_SUHU;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else if(zuosanwu == 3){
							huInfo[0] = Cnst.HUTYPE_ZUOSANWAN;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}else if(zuosanwu == 5){
							huInfo[0] = Cnst.HUTYPE_ZUOWUWAN;
				    		huInfo[1] = Cnst.getHuScore(huInfo[0], hasHun);
						}	
						
					}
				}
				
			}
		}
    	return huInfo;
    }
    
    public static void main(String[] args) {

    	List<Integer[][]> pais = new ArrayList<Integer[][]>();
		pais.add(new Integer[][]{{1,7,136}});
		pais.add(new Integer[][]{{1,9,136}});
		
		System.out.println(checkPiao(pais, 1,true));
		
//		int[] array = new int[34];
//		for(int i=0;i<34;i++){
//			array[i] = 0;
//		}
//		System.out.println(Hulib.getInstance().get_hu_info(array, 34, 34));
	}
    
    /**
     * 前提是必须有混
     * 移除当前手牌中的龙，去检测其他项
     * 手牌跟混牌完全分开，参数shouPai中不包含混
     * 返回的数组为：0-剩余混的个数；1-取出龙牌后的剩余手牌；2-纯龙牌；3-龙用的混的个数
     * @param shouPai
     * @param longType
     * @param hunNum
     * @return 返回剩余混的个数
     */
    private static Object[] removeShouPaiLong(List<Integer[][]> shouPai,int longType,int hunNum){
    	int[] paiArray = getShouPaiArray(shouPai, null);
    	int[] longPais =initArrays();
    	int useHuns = 0;
		for(int i=0;i<9;i++){
			int num = (longType-1)*9+i;
			if (paiArray[num]==0) {
				hunNum--;
				useHuns++;
			}else{
				paiArray[num]--;
				longPais[num]++;
			}
    	}
		shouPai = getPaiListByArray(paiArray);
		List<Integer[][]> longs = getPaiListByArray(longPais);
    	return new Object[]{hunNum,shouPai,longs,useHuns};
    }
    
    private static List<Integer[][]> getPaiListByArray(int[] paiArray){
    	List<Integer[][]> result = new ArrayList<Integer[][]>();
    	for(int i=0;i<paiArray.length;i++){
    		if (paiArray[i]!=0) {
    			for(int j=0;j<paiArray[i];j++){
    				if (i<27) {
            			result.add(new Integer[][]{{i/9+1,i%9+1}});
					}else if(i>=27&&i<31){
            			result.add(new Integer[][]{{i/9+1,i%9+1}});
					}else{
            			result.add(new Integer[][]{{i/9+2,i%9+1-4}});
					}
    			}
			}
    	}
    	return result;
    }
    
    public static void main3(String[] args) {
    	int[] array = {
    			1,1,1,1,1,1,1,1,1,
    			1,1,1,1,1,1,1,3,1,
    			1,1,1,1,1,1,1,1,1,
    			1,1,1,1,1,1,1
    	};
    	List<Integer[][]> ll = getPaiListByArray(array);
    	for(int i=0;i<ll.size();i++){
    		System.out.print(ll.get(i)[0][0]+"_"+ll.get(i)[0][1]+"\t");
    		if ((i+1)%9==0) {
				System.out.println();
			}
    	}
	}
   
    /**
     * 只有混的情况
     * 一共只有7个混牌，
     * 而且胡牌的个数只有2 5 8 11 14,
     * @param hunNum
     * @param huInfo
     */
    private static void getAllHunHuInfo(int hunNum,int[] huInfo){
		//无论剩余2张牌，还是5张牌，飘牌的情况（即低喽龙）的番数最高，所以都默认为飘牌的情况
		//因为静海只能碰杠，而且飘牌的情况（即低喽龙）的番数最高
    	switch (hunNum) {
		case 2:
			huInfo[0] = Cnst.HUTYPE_HUNDIAOLONG;
    		huInfo[1] = Cnst.getHuScore(huInfo[0], true);
			break;
		case 5:
			huInfo[0] = Cnst.HUTYPE_HUNDIAOLONG;
    		huInfo[1] = Cnst.getHuScore(huInfo[0], true);
			break;
		}
    }
    /**
     * 检测飘牌，低喽龙的另外一种情况
     * @param playersPais
     * @param hunNum
     * @param hasHun
     * @return
     */
    private static boolean checkPiao(List<Integer[][]> playersPais,int hunNum,boolean hasHun){
    	boolean piao = false;
    	int[] paiArray = getShouPaiArray(playersPais, null);
    	if (hasHun) {
    		int oneNum = 0;//手牌个数为1 的牌的个数
    		int twoNum = 0;//手牌个数为2 的牌的个数
			for(int i=0;i<paiArray.length;i++){
				if (paiArray[i]>3) {
					piao = false;
					return piao;
				}else if(paiArray[i]==1){//如果个数为1，首先默认为将，看能不能胡牌
					oneNum++;
				}else if(paiArray[i]==2){//如果个数为2，首先默认为将，看能不能胡牌
					twoNum++;
				}
			}
			//经过isPiao方法穷举之后，总结isPiao方法的规律为以下代码
			if (hunNum>=(oneNum+twoNum+(oneNum-1))) {
				piao = true;
			}
			
		} else {
			int twoNum = 0;
			boolean hasOne = false;//是否有牌的个数为1的
			for(int i=0;i<paiArray.length;i++){
				if (paiArray[i]==2) {
					twoNum++;
				}else if(paiArray[i]==1) {
					hasOne = true;
					break;
				}
			}
			if (!hasOne&&twoNum==1) {
				piao = true;
			}
		}
    	return piao ;
    }
    
    /**
     * 仅供checkPiao方法使用
     * 穷举所有可能的情况
     * @param oneNum
     * @return
     */
    private static boolean isPiao(int oneNum,int twoNum,int hunNum){
    	boolean isPiao = false;
    	switch (oneNum) {
		case 0:
			switch (twoNum) {
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				if (hunNum>=oneNum+twoNum-1) {
					isPiao = true;
				}
				break;
			}
			break;
		case 1:
			switch (twoNum) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
				if (hunNum>=oneNum+twoNum) {
					isPiao = true;
				}
				break;
			}
			break;
		case 2:
			switch (twoNum) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
				if (hunNum>=twoNum+oneNum+1) {
					isPiao = true;
				}
				break;
			}
			break;
		case 3:
			switch (twoNum) {
			case 0:
			case 1:
			case 2:
				if (hunNum>=twoNum+oneNum+2) {
					isPiao = true;
				}
				break;
			}
			break;
		case 4:
			switch (twoNum) {
			case 0:
				if (hunNum>=twoNum+oneNum+3) {
					isPiao = true;
				}
				break;
			}
			break;
		}
    	return isPiao;
    }
    
    
    public static void main1(String[] args) {
    	List<Integer[][]> pais = new ArrayList<Integer[][]>();
		pais.add(new Integer[][]{{1,7,136}});
		
		System.out.println(checkHunDiao(pais, new Integer[][]{{1,7}}, 1, false));
		
		int[] array = new int[34];
		for(int i=0;i<34;i++){
			array[i] = 0;
		}
		System.out.println(Hulib.getInstance().get_hu_info(array, 34, 34));
		

//		System.out.println(checkHunDiao(pais, lastFaPai, hunNum, lastFaPaiIsHun));
	}
    
    
    
    private static boolean checkHunDiao(List<Integer[][]> playerPais,Integer[][] lastFaPai,int hunNum,boolean lastFaPaiIsHun){
    	int[] newArray = getShouPaiArray(playerPais, null);
    	int gui_index = 34; 
    	if (lastFaPaiIsHun) {
    		if (hunNum<2) {
				return false;
			}
    		gui_index = addHun(newArray, hunNum-2);
		}else{
			if (lastFaPai[0][0]<=3&&lastFaPai[0][0]>0) {
				newArray[9*(lastFaPai[0][0]-1)+lastFaPai[0][1]-1]--;
			}else if (lastFaPai[0][0]==4) {
				newArray[9*3+lastFaPai[0][1]-1]--;
			}else if (lastFaPai[0][0]==5) {
				newArray[9*3+4+lastFaPai[0][1]-1]--;
			}
			if (lastFaPai[0][0]!=-1) {
				gui_index = addHun(newArray, hunNum-1);
			}else{//说明lastFaPai被用了
//				gui_index = addHun(newArray, hunNum);
				return false;
			}
		}
		return Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
    }
    
    /**
     * 待定，暂时没用
     * @param playerPais
     * @param hasHun
     * @param hunNum
     * @param longNum
     * @param lastFaPai
     * @param lastFaPaiIsHun
     * @return
     */
    private static int isZuoSanOrZuoWuHasLong(List<Integer[][]> playerPais,boolean hasHun,int hunNum,int longNum,Integer[][] lastFaPai,boolean lastFaPaiIsHun){
    	int zuosanwu = 0;
    	if (longNum == 1) {
        	int[] paiArray = getShouPaiArray(playerPais, null);
        	int[] newArray = getOnlyLongPaiArray(paiArray, longNum);
        	if (hasHun) {
        		if (lastFaPai[0][0]==1&&lastFaPai[0][1]==3) {//最后发的牌是3万
        			zuosanwu = 3;
    			}else if (lastFaPai[0][0]==1&&lastFaPai[0][1]==5) {//最后发的牌是3万
    				zuosanwu = 5;
    			}else if(lastFaPaiIsHun){
    				if (newArray[2]==0) {
    					zuosanwu = 3;
					}else if(newArray[4]==0){
						zuosanwu = 5;
					}else{
						for(int i=(longNum-1)*9;i<longNum*9;i++){
							if (newArray[i]==0) {
								hunNum--;
							}else{
								newArray[i]--;
							}
						}
						
					}
    			}
    		}else{
    			
    		}
		}
    	return zuosanwu;
    }
    
    private static int[] getOnlyLongPaiArray(int[] paiArray,int longNum){
    	int[] newArray = initArrays();
    	for(int i=(longNum-1)*9;i<longNum*9;i++){
    		newArray[i] = paiArray[i];
    	}
    	return newArray;
    }
    
    
    /**
     * 返回0，不是
     * 返回3为坐3
     * 返回5为坐5
     * 需要加入单吊的情况，单吊35万或者混吊335都是坐三五,边三也是坐三
     * @param shoupai 不能有混牌
     * @param hunNum
     * @return
     */
    private static int isZuoSanOrZuoWu(List<Integer[][]> playerPais,boolean hasHun,int hunNum,Integer[][] lastFaPai,boolean lastFaPaiIsHun,boolean isPiao){ 
    	int zuosanwu = 0;
    	int[] paiArray = getShouPaiArray(playerPais, null);
    	if (hasHun) {
    		if (lastFaPaiIsHun) {//最后发的牌是混牌
    			int[] newArray = getNewArray(paiArray);
				int newHunNum = hunNum;
				int queNum = 0;
    			if (!isPiao) {//如果是飘的话，就不检测顺子的情况了
    				if (newArray[1]==0) {
    					queNum++;
    				}else{
    					newArray[1]--;
    				}
    				if (newArray[3]==0) {
    					queNum++;
    				}else{
    					newArray[3]--;
    				}
    				if (queNum==0) {
    					//强制拿出1和3万，减去一张混牌，检测胡牌
    					//如果queNum==0的话，paiArray[1] 和 paiArray[3]已经减了
//    					paiArray[1] = paiArray[1] - 1;
//    					paiArray[3] = paiArray[3] - 1;
    					hunNum-- ;
    					int gui_index = addHun(newArray, hunNum);
    					boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
    					if (hu) {
    						zuosanwu = 3;
    					}
    				}else if(queNum+1<=hunNum){
    					hunNum = hunNum - queNum - 1;
    					int gui_index = addHun(newArray, hunNum);
    					boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
    					if (hu) {
    						zuosanwu = 3;
    					}
    				}
				}
				
				newArray = getNewArray(paiArray);
				hunNum = newHunNum;
				if (zuosanwu==0) {
					if (zuosanwu == 0) {//如果不是夹胡的话，检测单吊3万
						if (newArray[2]>=1) {
							newArray[2]-=1;
							int gui_index = addHun(newArray, hunNum==0?0:hunNum-1);
							boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
							if (hu) {
								zuosanwu = 3;
							}
						}
					}
				}
				
				newArray = getNewArray(paiArray);
				if(zuosanwu==0){//说明不是坐3，需要检测是否是坐5
					queNum = 0;
					if (!isPiao) {
						if (newArray[3]==0) {
							queNum++;
						}else{
							newArray[3]--;
						}
						if (newArray[5]==0) {
							queNum++;
						}else{
							newArray[5]--;
						}
						if (queNum==0) {
							//强制拿出4和6万，减去一张混牌，检测胡牌
//							newArray[3] = newArray[3] - 1;
//							newArray[5] = newArray[5] - 1;
							newHunNum-- ;
							int gui_index = addHun(newArray, newHunNum);
							boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
							if (hu) {
								zuosanwu = 5;
							}
						}else if(queNum+1<=newHunNum){
							newHunNum = newHunNum - queNum-1;
							int gui_index = addHun(newArray, newHunNum);
							boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
							if (hu) {
								zuosanwu = 5;
							}
						}
					}

					newArray = getNewArray(paiArray);
					newHunNum = hunNum;
					if (zuosanwu==0) {
						if (zuosanwu == 0) {//如果不是夹胡的话，检测单吊5万
							if (newArray[4]>=1) {
								newArray[4]-=1;
								int gui_index = addHun(newArray, newHunNum==0?0:newHunNum-1);
								boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
								if (hu) {
									zuosanwu = 5;
								}
							}
						}
					}
				}
				
			}else if (lastFaPai[0][0]==1&&lastFaPai[0][1]==3) {//最后发的牌是3万
				int newHunNum = hunNum;
				int[] newArray = getNewArray(paiArray);
				int queNum = 0;
				if (!isPiao) {
					if (newArray[1]==0) {
						queNum++;
					}else{
						newArray[1]--;
					}
					if (newArray[3]==0) {
						queNum++;
					}else{
						newArray[3]--;
					}
					newArray[2]--;
					if (queNum<=hunNum) {
						hunNum = hunNum - queNum;
						int gui_index = addHun(newArray, hunNum);
						boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
						if (hu) {
							zuosanwu = 3;
						}
					}
				}
				
				hunNum = newHunNum;
				if (zuosanwu == 0) {//如果不是夹胡的话，检测单吊3万
					if (paiArray[2]>=2) {
						paiArray[2]-=2;
						int gui_index = addHun(paiArray, hunNum);
						boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
						if (hu) {
							zuosanwu = 3;
						}
					}
				}
			} else if (lastFaPai[0][0]==1&&lastFaPai[0][1]==5) {//最后发的牌是5万
				int newHunNum = hunNum;
				int[] newArray = getNewArray(paiArray);
				int queNum = 0;
				if (!isPiao) {
					if (newArray[3]==0) {
						queNum++;
					}else{
						newArray[3]--;
					}
					if (newArray[5]==0) {
						queNum++;
					}else{
						newArray[5] --;
					}
					newArray[4]--;
					if (queNum<=hunNum) {
						hunNum = hunNum - queNum;
						int gui_index = addHun(newArray, hunNum);
						boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
						if (hu) {
							zuosanwu = 5;
						}
					}
				}

				hunNum = newHunNum;
				if (zuosanwu == 0) {//如果不是夹胡的话，检测单吊5万
					if (paiArray[4]>=2) {
						paiArray[4]-=2;
						int gui_index = addHun(paiArray, hunNum);
						boolean hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
						if (hu) {
							zuosanwu = 5;
						}
					}
				}
				
			}
		}else{
			if (lastFaPai[0][0]==1&&lastFaPai[0][1]==3) {
				int newHunNum = hunNum;
				int[] newArray = getNewArray(paiArray);
				if (newArray[1]>0&&newArray[3]>0) {
					newArray[1]--;
					newArray[2]--;
					newArray[3]--;
					int gui_index = addHun(newArray, hunNum);
					boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
					if (hu) {
						zuosanwu = 3;
					}
				}
				hunNum = newHunNum;
				newArray = getNewArray(paiArray);
				if (zuosanwu==0) {
					if (newArray[2]>=2) {
						newArray[2]-=2;
						int gui_index = addHun(newArray, hunNum);
						boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
						if (hu) {
							zuosanwu = 3;
						}
					}
				}
			} else if (lastFaPai[0][0]==1&&lastFaPai[0][1]==5) {
				int newHunNum = hunNum;
				int[] newArray = getNewArray(paiArray);
				if (paiArray[3]>0&&paiArray[5]>0) {
					newArray[3]--;
					newArray[4]--;
					newArray[5]--;
					int gui_index = addHun(newArray, hunNum);
					boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
					if (hu) {
						zuosanwu = 5;
					}
				}
				hunNum = newHunNum;
				newArray = getNewArray(paiArray);
				if (zuosanwu==0) {
					if (newArray[4]>=2) {
						newArray[4]-=2;
						int gui_index = addHun(newArray, hunNum);
						boolean hu = Hulib.getInstance().get_hu_info(newArray, 34, gui_index);
						if (hu) {
							zuosanwu = 5;
						}
					}
				}
			}
		}
    	return zuosanwu;
    }
    
    /**
     * 本方法检测手中是否有龙，手牌中混排跟其他牌分开
     * 返回0没有龙，
     * 返回1为万；
     * 返回2为条上龙；
     * 返回3为饼上龙
     * @param playerPais
     * @return
     */
    private static int checkLong(List<Integer[][]> playerPais,List<Integer[][]> hunList){
    	int longType = 0;
    	int[] paiArray = getShouPaiArray(playerPais, null);
    	for(int n=0;n<3;n++){
    		int zeroNum = 0;
    		for(int i=0;i<9;i++){
    			int num = n*9+i;
    			if (paiArray[num]==0) {
    				zeroNum++;
				}
        	}
    		if (zeroNum==0) {//当前类型里面没有个数是0的牌
    			if (removeLongCkeckHu(n, paiArray, hunList!=null&&hunList.size()>0?hunList.size():0)) {
					longType = n+1;
					break;
				}
			}else if(zeroNum>0&&hunList!=null&&hunList.size()>0&&zeroNum<=hunList.size()){//当前类型里有个个数是0的牌，而且数量小于混牌数量
				int[] newArray = getNewArray(paiArray);
				for(int i=0;i<9;i++){
	    			int num = n*9+i;
	    			if (newArray[num]==0) {
	    				newArray[num] = 1;
					}
	        	}
				if (removeLongCkeckHu(n, newArray,hunList.size()-zeroNum)) {
					longType = n+1;
					break;
				}
			}
    	}
    	return longType;
    }
    
    /**
     * 重新生成长度为34的手牌个数数组
     * @param paiArray
     * @return
     */
    private static int[] getNewArray(int[] paiArray){
    	int[] newArray = new int[paiArray.length];
    	for(int n=0;n<paiArray.length;n++){
    		newArray[n] = paiArray[n];
    	}
    	return newArray;
    }
    
    /**
     * 如果checkLong检测出可能有龙，就把龙移除，
     * 然后再检测剩下的手牌是否能胡，如果能胡，则龙成立
     * @param n
     * @param paiArray
     * @param hunNum
     * @return
     */
    private static boolean removeLongCkeckHu(int n,int[] paiArray,int hunNum){
    	boolean hu = false;
    	//把龙移除，检测是或否胡牌
		for(int i=0;i<9;i++){
			int num = n*9+i;
			paiArray[num]--;
    	}
		//加入混牌
		int gui_index = 34;
		if (hunNum>0) {
			gui_index = addHun(paiArray, hunNum);
		}
		//移除龙之后，检测胡牌
		hu = Hulib.getInstance().get_hu_info(paiArray, 34, gui_index);
		if (!hu) {//不胡牌，需要添加上之前去掉的龙
			for(int i=0;i<9;i++){
				int num = n*9+i;
				paiArray[num]++;
        	}
			if (gui_index < 34) {//把混牌也移除
				paiArray[gui_index] = 0;
			}
		}
		return hu;
    }
    
    /**
     * 给数组中添加混，
     * 如果数组中某个位置的个数为0的话，
     * 就用混顶替当前位置，
     * 返回混的位置
     * @param paiArray
     * @param hunNum
     * @return
     */
    private static int addHun(int[] paiArray,int hunNum){
    	for(int i=0;i<paiArray.length;i++){
    		if (paiArray[i]==0) {
    			paiArray[i] = hunNum;
				return i;
			}
    	}
    	return 34;
    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
