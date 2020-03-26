package zach.hw4;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.*;

public class Characters {
	final static String[] stat_names = {"Strength", "Dexterity", "Constitution", "Wisdom", "Intelligence", "Charisma"};
	final static String max_query = "SELECT MAX(ID) AS Max FROM Characters";
	static Connection conn;
	static ObjectMapper om = new ObjectMapper();

	public static String getMany(Request request, Response response) {
		response.type("application/json");
		String body = "";

		try {
			conn = connectDB("com.mysql.cj.jdbc.Driver", "root", "root", "jdbc:mysql://localhost:2837/mudDB");

			String select = "SELECT * FROM Characters";
			Statement statement = conn.createStatement();
			ResultSet rs;

			rs = statement.executeQuery(select);
			ArrayList<CharURL> chars = new ArrayList<>();
			String url = request.url();
			while(rs.next()) {
				Char c = new Char(rs.getString("Name"),
					    rs.getInt(stat_names[0]),
					    rs.getInt(stat_names[1]),
					    rs.getInt(stat_names[2]),
					    rs.getInt(stat_names[3]),
					    rs.getInt(stat_names[4]),
					    rs.getInt(stat_names[5]));
				chars.add(new CharURL(c.name, url + "/" + rs.getInt("ID"))); 
			}

			body += om.writeValueAsString(chars);
		} catch(SQLException ex) {
			ex.printStackTrace();
			response.status(500);
			body = ex.getMessage();
		} catch(ClassNotFoundException ex) {
			ex.printStackTrace();
			response.status(500);
			body = "Could not find class";
		} catch(JsonProcessingException ex) {
			body = "Jackson error";
		} finally {
			disconnectDB(conn);
		}

		return body;
	}

	public static String getOne(Request request, Response response) {
		response.type("application/json");
		String body = "";

		try {
			conn = connectDB("com.mysql.cj.jdbc.Driver", "root", "root", "jdbc:mysql://localhost:2837/mudDB");

			String select = "SELECT * FROM Characters";
			Statement statement = conn.createStatement();
			ResultSet rs;

			select += " WHERE ID = " + request.splat()[0];
			rs = statement.executeQuery(select);
			if(!rs.next()) {
				response.status(404);
			} else {
				Char c = new Char(rs.getString("Name"),
					    rs.getInt(stat_names[0]),
					    rs.getInt(stat_names[1]),
					    rs.getInt(stat_names[2]),
					    rs.getInt(stat_names[3]),
					    rs.getInt(stat_names[4]),
					    rs.getInt(stat_names[5]));
				body = om.writeValueAsString(c);
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
			response.status(500);
			body = ex.getMessage();
		} catch(ClassNotFoundException ex) {
			ex.printStackTrace();
			response.status(500);
			body = "Could not find class";
		} catch(JsonProcessingException ex) {
			body = "Jackson error";
		} finally {
			disconnectDB(conn);
		}

		return body;
	}

	public static String post(Request request, Response response) {
		String body = "";
		try {
			conn = connectDB("com.mysql.cj.jdbc.Driver", "root", "root", "jdbc:mysql://localhost:2837/mudDB");

			String name = request.queryParams("name");
			if(name == null) {
				response.status(400);
				disconnectDB(conn);
				return "Name required";
			}

			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery(max_query);
			rs.next();
			int id = rs.getInt("Max") + 1;

			StringBuffer insert = new StringBuffer("INSERT INTO Characters(ID,Name");

			Integer[] stat_values = new Integer[6];
			for(int i = 0; i < 6; i++) {
				String stat = request.queryParams(stat_names[i]);

				if(stat != null) {
					stat_values[i] = Integer.parseInt(stat);
					insert.append("," + stat_names[i]);
				}
			}
			insert.append(") VALUES (" + id + ",'" + name + "'");
			for(int i = 0; i < 6; i++) {
				insert.append(stat_values[i] != null ? "," + stat_values[i] : "");
			}
			insert.append(")");

			statement.execute(insert.toString());
			response.status(201);
			response.header("Location", request.url() + "/" + id); 
		} catch(SQLException ex) {
			ex.printStackTrace();
			response.status(500);
			body = ex.getMessage();
		} catch(ClassNotFoundException ex) {
			ex.printStackTrace();
			response.status(500);
			body = "Could not find class";
		} finally {
			disconnectDB(conn);
		}

		return body;
	}

	private static Connection connectDB(String driver, String user, String pass, String db) throws SQLException, ClassNotFoundException {
		Connection conn = null;

		Class.forName(driver);

		String userName = user;
		String password = pass;
		String url = db;

		conn = DriverManager.getConnection(url, userName, password);

		return conn;
	}

	private static void disconnectDB(Connection conn) {
		if (conn != null) {
			try {
				conn.close ();
			} catch (Exception ex) {
				System.err.println ("Error in connection termination");
				ex.printStackTrace();
			}
		}
	}
}
