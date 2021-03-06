package com.gameportal.manage.gameplatform.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gameportal.manage.gameplatform.model.GamePlatform;
import com.gameportal.manage.gameplatform.service.IGameServiceHandler;
import com.gameportal.manage.redis.service.IRedisService;
import com.gameportal.manage.user.model.UserInfo;
import com.gameportal.manage.util.Blowfish;
import com.gameportal.manage.util.HttpsUtil;
import com.gameportal.manage.util.WebConstants;

import net.sf.json.JSONObject;

/**
 * PT电子游戏接入。
 * 
 * @author sum
 *
 */
@Service("ptGameServiceHandlerImpl")
@SuppressWarnings("all")
public class PTGameServiceHandlerImpl implements IGameServiceHandler {
	private static Logger logger = Logger.getLogger(PTGameServiceHandlerImpl.class);

	@Resource(name = "redisServiceImpl")
	private IRedisService redisService;

	public PTGameServiceHandlerImpl() {
		super();
	}

	@Override
	public Object createAccount(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		Blowfish bf = new Blowfish(WebConstants.BLOWFISH_CODE);
		String requestUrl = gamePlatform.getDomainname() + "/create/playername/" + userName + "/adminname/"
				+ gamePlatform.getApiaccount() + "/kioskname/" + gamePlatform.getCiphercode() + "/password/"
				+ bf.decryptString(userInfo.getPasswd()) + "/custom02/" + gamePlatform.getCiphercode()+"/custom03/11"; // custom02参数表示注册会员账号
		logger.info("{" + userInfo.getAccount() + "}注册PT游戏账号请求地址" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}注册PT游戏账号返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return (json.get("errorcode") == null || String.valueOf(json.get("errorcode")).equals("19")) ? "0" : "-1"; // 注册成功或者用户已经注册
	}

	@Override
	public Object loginGame(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		String requestUrl = "http://cache.download.banner.greatfortune88.com/casinoclient.html?language=zh-cn&&game="
				+ paramMap.get("gameName");
		logger.info("{" + userInfo.getAccount() + "}登入PT游戏：" + requestUrl);
		return requestUrl;
	}

	@Override
	public Object logoutGame(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		String requestUrl = gamePlatform.getDomainname() + "/logout/playername/" + userName;
		logger.info("{" + userInfo.getAccount() + "}登出PT游戏请求地址:" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}登出PT游戏返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return json.get("errorcode") == null ? "0" : json.get("errorcode");
	}

	@Override
	public Object deposit(UserInfo userInfo, GamePlatform gamePlatform, String amount, Map paramMap) {
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		String requestUrl = gamePlatform.getDomainname() + "/deposit/playername/" + userName + "/amount/" + amount
				+ "/adminname/" + gamePlatform.getApiaccount() + "/externaltranid/" + paramMap.get("externaltranid");
		logger.info("{" + userInfo.getAccount() + "}存入PT游戏请求地址:" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}存入PT游戏返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return json.get("errorcode") == null ? "0" : json.getString("errorcode");
	}

	@Override
	public Object queryBalance(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		// 如果用户不存在，直接返回0；
		if(userInfo.isNotExist(gamePlatform.getGpname()+gamePlatform.getGpid())){
			return "0";
		}
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		String requestUrl = gamePlatform.getDomainname() + "/info/playername/" + userName + "/";
		logger.info("{" + userInfo.getAccount() + "}查询PT游戏余额请求地址:" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}查询PT游戏余额返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		if (json.get("errorcode") != null) {
			return "0";
		}
		result = json.getString("result");
		json = JSONObject.fromObject(result);
		String balance = json.getString("BALANCE");
		if(balance.startsWith(".")){
			balance ="0"+balance;
		}
		return balance;
	}

	@Override
	public Object withdrawal(UserInfo userInfo, GamePlatform gamePlatform, String amount, Map paramMap) {
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		String requestUrl = gamePlatform.getDomainname() + "/withdraw/playername/" + userName + "/amount/" + amount
				+ "/adminname/" + gamePlatform.getApiaccount() + "/externaltranid/" + paramMap.get("externaltranid")
				+ "/isForce/0"; // isForce=0游戏中不可以提款；
		logger.info("{" + userInfo.getAccount() + "}提款PT请求地址：" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}提款PT返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return json.get("errorcode") == null ? "0" : json.getString("errorcode");
	}

	@Override
	public Object transferCreditConfirm(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		return null;
	}

	@Override
	public Object queryOrderStatus(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		String requestUrl = gamePlatform.getDomainname() + "/checktransaction/externaltransactionid/"
				+ paramMap.get("externaltranid");
		logger.info("{" + userInfo.getAccount() + "}查询PT订单订单状态返回结果:" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}查询PT订单订单状态返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return json.get("errorcode") == null ? "0" : "-1";
	}

	@Override
	public Object betRecord(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		return null;
	}

	@Override
	public Object edit(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		Blowfish bf = new Blowfish(WebConstants.BLOWFISH_CODE);
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		String requestUrl = gamePlatform.getDomainname() + "/update/playername/" + userName+"/password/"+bf.decryptString(userInfo.getPasswd())+"/custom03/11";
		logger.info("{" + userInfo.getAccount() + "}修改PT密码请求地址：" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}修改PT密码返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return json.get("errorcode") == null ? "0" : "-1";
	}

	@Override
	public Object resetloginattempts(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap) {
		Blowfish bf = new Blowfish(WebConstants.BLOWFISH_CODE);
		String userName = WebConstants.PT_API_USERNAME_PREFIX + userInfo.getAccount().toUpperCase();
		if(userInfo.getAccount().startsWith("QB77")){
			userName = userInfo.getAccount();
		}
		String requestUrl = gamePlatform.getDomainname() + "/resetfailedlogin/playername/" + userName;
		logger.info("{" + userInfo.getAccount() + "}解锁PT账号请求地址：" + requestUrl);
		String result = HttpsUtil.processRequst(requestUrl, gamePlatform.getDeskey());
		logger.info("{" + userInfo.getAccount() + "}解锁PT账号返回结果:" + result);
		JSONObject json = JSONObject.fromObject(result);
		return json.get("errorcode") == null ? "0" : "-1";
	}

	@Override
	public Object getSessionGUID(UserInfo userInfo, GamePlatform gamePlatform, Map paramMap, boolean isCache) {
		return null;
	}
}
