package com.leo.rms.corecode.count.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mina.core.session.IoSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.handler.MinaServerHandler;

@Controller
@RequestMapping("/count")
public class CountController {
	
	/**
	 * 请求在线人数
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/open/onlineNum",method = RequestMethod.POST,produces = "application/json")
	public String onlineNum(Model model,HttpServletRequest request,HttpServletResponse response){
		Map<String, Object> infos = new HashMap<String, Object>();
		infos.put("onlineNum", MinaServerHandler.acceptor.getManagedSessionCount());
		return JSONObject.toJSONString(infos);
	}
	
	/**
	 * 请求主线程状态
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/open/mainThread",method = RequestMethod.POST,produces = "application/json")
	public String mainThread(Model model,HttpServletRequest request,HttpServletResponse response){
		Map<String, Object> infos = new HashMap<String, Object>();
		try {
			IoSession session = null;
			ProtocolData pdData = new ProtocolData(100000,"{\"interfaceId\":\"100000\"}");
//			for(Long userId:TCPGameFunctions.ioSessionMap.keySet()){
//				session = TCPGameFunctions.ioSessionMap.get(userId);
//			}
//			if (session!=null) {
//				TCPFunctionExecutor.heart(session, pdData);
//			}
			infos.put("mainThread", 1);
		} catch (Exception e) {
			infos.put("mainThread", 0);
		}
		return JSONObject.toJSONString(infos);
	}
	
	

}