package com.up72.game.dto.resp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.up72.game.model.Room;
import com.up72.server.mina.bean.DissolveRoom;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RoomResp extends Room implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 7019484436048389958L;
	private List<Integer[][]> currentMjList;//房间内剩余麻将集合；
    private Long zhuangId;
    //本房间状态，0等待玩家入坐；1人满等待；2游戏中；3小结算
    private Integer status;
    private Integer lastNum;//房间剩余局数
    private Integer currentJuNum;//当前第几局

    private Integer circleWind;//圈风...
    private Integer circleNum;//圈数
    private Integer roomType;//房间模式，房主模式1；自由模式2

    private DissolveRoom dissolveRoom;//申请解散信息

    private Integer[][] lastPai;//最后出的牌
    private Long lastUserId;//最后出牌的玩家

    private Integer createDisId;//创建的时候，40分钟解散房间的任务id
    private Integer applyDisId;//申请解散房间的任务id
    private Integer outNum;//请求大结算的人数
    private List<Long> guoUserIds;//动作  点击过的人
    
    private List<Map<String, Object>> overInfo;
    
    private List<Integer[][]> hunPai;//格式为：[[[3,3]],[[3,4]]]，第一个元素为开的混牌，第二个为另一张混牌，没有排序
	
    private Boolean hasInsertRecord;
    private String openName;
    
    private Long[] playerIds;
    
    private Integer wsw_sole_main_id;//大接口id
    private Integer wsw_sole_action_id;//吃碰杠出牌发牌id
    
    private Integer xiaoJuNum;//每次小局（刘局或者有人胡），这个字段++，回放用
    private Long xiaoJuStartTime;//小局开始时间
    
    public void initRoom(){
    	this.lastPai = null;
    	this.lastUserId = null;
    	this.hunPai = null;
    }

	public Long getXiaoJuStartTime() {
		return xiaoJuStartTime;
	}

	public void setXiaoJuStartTime(Long xiaoJuStartTime) {
		this.xiaoJuStartTime = xiaoJuStartTime;
	}

	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}



	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}



	public Long[] getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(Long playerIds[]) {
		this.playerIds = playerIds;
	}



	public String getOpenName() {
		return openName;
	}

	public void setOpenName(String openName) {
		this.openName = openName;
	}

	public List<Integer[][]> getHunPai() {
		return hunPai;
	}

	public void setHunPai(List<Integer[][]> hunPai) {
		this.hunPai = hunPai;
	}

	public List<Map<String, Object>> getOverInfo() {
		return overInfo;
	}

	public void setOverInfo(List<Map<String, Object>> overInfo) {
		this.overInfo = overInfo;
	}

	public List<Long> getGuoUserIds() {
		return guoUserIds;
	}

	public void setGuoUserIds(List<Long> guoUserIds) {
		this.guoUserIds = guoUserIds;
	}

	public List<Integer[][]> getCurrentMjList() {
        return currentMjList;
    }

    public void setCurrentMjList(List<Integer[][]> currentMjList) {
        this.currentMjList = currentMjList;
    }

    public Long getZhuangId() {
        return zhuangId;
    }

    public void setZhuangId(Long zhuangId) {
        this.zhuangId = zhuangId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLastNum() {
        return lastNum;
    }

    public void setLastNum(Integer lastNum) {
        this.lastNum = lastNum;
    }

    public Integer getCurrentJuNum() {
        return currentJuNum;
    }

    public void setCurrentJuNum(Integer currentJuNum) {
        this.currentJuNum = currentJuNum;
    }

    public Integer getCircleWind() {
        return circleWind;
    }

    public void setCircleWind(Integer circleWind) {
        this.circleWind = circleWind;
    }

    public Integer getCircleNum() {
        return circleNum;
    }

    public void setCircleNum(Integer circleNum) {
        this.circleNum = circleNum;
    }

    public Integer getRoomType() {
        return roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public DissolveRoom getDissolveRoom() {
        return dissolveRoom;
    }

    public void setDissolveRoom(DissolveRoom dissolveRoom) {
        this.dissolveRoom = dissolveRoom;
    }

	public Integer[][] getLastPai() {
        return lastPai;
    }

    public void setLastPai(Integer[][] lastPai) {
        this.lastPai = lastPai;
    }

    public Long getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(Long lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Integer getCreateDisId() {
        return createDisId;
    }

    public void setCreateDisId(Integer createDisId) {
        this.createDisId = createDisId;
    }

    public Integer getApplyDisId() {
        return applyDisId;
    }

    public void setApplyDisId(Integer applyDisId) {
        this.applyDisId = applyDisId;
    }

	public Integer getOutNum() {
		return outNum;
	}

	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}

	public Boolean getHasInsertRecord() {
		return hasInsertRecord;
	}

	public void setHasInsertRecord(Boolean hasInsertRecord) {
		this.hasInsertRecord = hasInsertRecord;
	}

	public Integer getWsw_sole_main_id() {
		return wsw_sole_main_id;
	}

	public void setWsw_sole_main_id(Integer wsw_sole_main_id) {
		this.wsw_sole_main_id = wsw_sole_main_id;
	}

	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}

	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}
    
}
