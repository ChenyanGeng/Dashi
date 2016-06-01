package api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.MongoDBConnection;
import db.MySQLDBConnection;

/**
 * Servlet implementation class VisitHistory
 */
@WebServlet({ "/VisitHistory", "/history" })
public class VisitHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VisitHistory() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//DBConnection connection = new MongoDBConnection();
		DBConnection connection = new MySQLDBConnection();
		JSONObject input = RpcParser.parseInput(request);
		if (input.has("user_id") && input.has("visited")) {
			try {
				String userID = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("visited");
				List<String> visitedRestaurants = new ArrayList<String>();
				for (int i = 0; i < array.length(); i++) {
					visitedRestaurants.add((String) array.get(i));
				}
				connection.unsetVisitedRestaurants(userID, visitedRestaurants);
				RpcParser.writeOutput(response, new JSONObject().put("status", "OK"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	// Display the list of visit history of a given user_id
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try{
		//DBConnection connection = new MongoDBConnection();
		DBConnection connection = new MySQLDBConnection();
		// allow access only if session exists
		HttpSession session = request.getSession();
		System.out.println(session.getAttribute("user"));
		System.out.println(session.getAttribute("pwdHash"));
		if (!connection.verifyLogin(request.getParameter("user_id"), (String)session.getAttribute("pwdHash"))) {
			response.setStatus(403);
			return;
		}
		JSONArray array = null;
		if (request.getParameterMap().containsKey("user_id")) {
			String userID = request.getParameter("user_id");
			Set<String> set = connection.getVisitedRestaurants(userID);
			array = new JSONArray();
			for (String id : set) {
				array.put(connection.getRestaurantsById(id, true));
			}
			RpcParser.writeOutput(response, array);
		} else {
			RpcParser.writeOutput(response, new JSONObject().put("status", "Invalid"));
		}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			//DBConnection connection = new MongoDBConnection();
			DBConnection connection = new MySQLDBConnection();
			JSONObject input = RpcParser.parseInput(request);
			if (input.has("user_id") && input.has("visited")) {
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("visited");
				List<String> visitedRestaurants = new ArrayList<>();
				for (int i = 0; i < array.length(); i++) {
					String businessId = (String) array.get(i);
					visitedRestaurants.add(businessId);
				}
				connection.setVisitedRestaurants(userId, visitedRestaurants);
				RpcParser.writeOutput(response, new JSONObject().put("status", "OK"));
			} else {
				RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
