package com.quinn.httpknife.github;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quinn.httpknife.HttpKnife;
import com.quinn.httpknife.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubImpl implements Github {

	public final static String HTTPS = "https://";
	public final static String HOST = "api.github.com";
	public final static String URL_SPLITTER = "/";
	public final static String API_HOST = HTTPS + HOST + URL_SPLITTER;

	public final static String ACCEPT = "application/vnd.github.beta+json";
	public final static String AGENT_USER = "GithubKnife/1.0";
	public final static String TOKEN_NOTE = "GithubKnife APP Token";

	public final static String CREATE_TOKEN = API_HOST + "authorizations"; // POST
	public final static String LIST_TOKENS = API_HOST + "authorizations"; // GET
	public final static String REMOVE_TOKEN = API_HOST + "authorizations"
			+ URL_SPLITTER; // DELETE
	public final static String LOGIN_USER = API_HOST +"user";
	public final static String MY_FOLLOWERS = API_HOST +"user/followers";
	public final static String MY_FOLLOWERSINGS = API_HOST +"user/following";
	
	public final static int DEFAULT_PAGE_SIZE = 10;
	public final static String PAGE = "page";
	public final static String PER_PAGE = "per_page";


	private HttpKnife http;

	public GithubImpl(Context context) {
		http = new HttpKnife(context);
	}

	@Override
	public String createToken(String username, String password)
			throws IllegalStateException {
		JSONObject json = new JSONObject();
		try {
			json.put("note", TOKEN_NOTE);
			// json.put("scopes", new String[]{"public_repo","repo"});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Response response = http.post(CREATE_TOKEN)
				.headers(configreHttpHeader())
				.basicAuthorization(username, password).json(json).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		if(response.statusCode() == 401){
			//账号密码错误
		}
		testResult(response);
		if (response.statusCode() == 422) {
			removeToken(username, password);
			return createToken(username, password);
		}
		Token token = new Gson().fromJson(response.body(), Token.class);
		System.out.println("token gson = " + token);
		return token.getToken();
	}

	@Override
	public String findCertainTokenID(String username, String password)
			throws IllegalStateException {
		Response response = http.get(LIST_TOKENS).headers(configreHttpHeader())
				.basicAuthorization(username, password).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		Gson gson = new Gson();
		ArrayList<Token> tokenList = gson.fromJson(response.body(),
				new TypeToken<List<Token>>() {
				}.getType());
		System.out.println("listToken gson = " + tokenList);
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			if (TOKEN_NOTE.equals(token.getNote()))
				return String.valueOf(token.getId());
		}
		return "";
	}

	@Override
	public void removeToken(String username, String password)
			throws IllegalStateException {
		String id = findCertainTokenID(username, password);
		Response response = http.delete(REMOVE_TOKEN + id)
				.headers(configreHttpHeader())
				.basicAuthorization(username, password).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		testResult(response);
	}

	@Override
	public User authUser(String token) throws IllegalStateException {
		Response response = http.get(LOGIN_USER).headers(configreHttpHeader()).tokenAuthorization(token).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		Gson gson = new Gson();
		User user = gson.fromJson(response.body(), User.class);
		return user;
	}

	
	
	
	@Override
	public Map<String, String> configreHttpHeader() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", ACCEPT);
		headers.put("User-Agent", AGENT_USER);
		return headers;
	}



	@Override
	public List<User> myFollwers(String token, int page) throws IllegalStateException {
		Map<String,String> params = new HashMap<String,String>();
		params.put(PAGE,String.valueOf(page));
		params.put(PER_PAGE,String.valueOf(DEFAULT_PAGE_SIZE));
		Response response = http.get(MY_FOLLOWERS,params).headers(configreHttpHeader()).tokenAuthorization(token).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		testResult(response);
		Gson gson = new Gson();
		List<User> tokenList = gson.fromJson(response.body(),
				new TypeToken<List<User>>() {
				}.getType());
		System.out.println("tuserlis = " + tokenList);

		return tokenList;
	}

	@Override
	public List<User> myFollwerings(String token, int page) throws IllegalStateException {
		Map<String,String> params = new HashMap<String,String>();
		params.put(PAGE,String.valueOf(page));
		params.put(PER_PAGE,String.valueOf(DEFAULT_PAGE_SIZE));
		Response response = http.get(MY_FOLLOWERSINGS,params).headers(configreHttpHeader()).tokenAuthorization(token).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		Gson gson = new Gson();
		ArrayList<User> tokenList = gson.fromJson(response.body(),
				new TypeToken<List<User>>() {
				}.getType());
		return tokenList;
	}

	@Override
	public List<User> follwerings(String user) throws IllegalStateException {
		String url = API_HOST + "users/" + user + "/followering";
		Response response = http.get(url).headers(configreHttpHeader()).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		Gson gson = new Gson();
		ArrayList<User> tokenList = gson.fromJson(response.body(),
				new TypeToken<List<User>>() {
				}.getType());
		return tokenList;	}

	@Override
	public List<User> followers(String user) throws IllegalStateException {
		String url = API_HOST + "users/" + user + "/followers";
		Response response = http.get(url).headers(configreHttpHeader()).response();
		if (response.isSuccess() == false)
			throw new IllegalStateException("网络链接有问题");
		Gson gson = new Gson();
		ArrayList<User> tokenList = gson.fromJson(response.body(),
				new TypeToken<List<User>>() {
				}.getType());
		return tokenList;
	}

	
	
	public void testResult(Response response) {
		System.out.println(response.statusCode());
		System.out.println(response.headers());
		System.out.println(response.body());
	}
	
}