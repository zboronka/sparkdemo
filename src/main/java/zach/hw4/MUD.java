package zach.hw4;

import static spark.Spark.*;
import spark.*;

public class MUD {
    public static void main( String[] args ) {
		get("/characters", (req, res) -> Characters.getMany(req, res));
		get("/characters/*", (req, res) -> Characters.getOne(req, res));
		post("/characters", (req, res) -> Characters.post(req, res));
    }
}
