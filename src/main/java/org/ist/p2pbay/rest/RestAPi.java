package org.ist.p2pbay.rest;

import org.ist.P2PBayApp;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.util.Constants;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.MessageDigest;

import static spark.Spark.get;
import static spark.Spark.setPort;

public class RestAPi {
    P2PBayApp app;

    public RestAPi(P2PBayApp app) {
        this.app = app;
    }

    public void startRestApi() {
        setPort(4567);

        get(new Route("/node/stop") {
            @Override
            public Object handle(Request request, Response response) {
                app.shutDown();
                return "Node App stopped";
            }
        });

        get(new Route("/node/user/create/:username/:password") {
            @Override
            public Object handle(Request request, Response response) {
                String username = request.params(":username");
                String password = request.params(":password");
                MessageDigest messageDigest = null;
                try {
                    messageDigest = MessageDigest.getInstance(Constants.ALGORITHM_SHA_256);

                    messageDigest.update(password.getBytes(Constants.CHARSET_NAME_UTF_8));

                    byte[] enteredDigest = messageDigest.digest();

                    app.registerUser(username, enteredDigest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "Created user " + request.params(":username");
            }
        });

        get(new Route("/node/user/login/:username/:password") {
            @Override
            public Object handle(Request request, Response response) {
                String username = request.params(":username");
                String password = request.params(":password");

                try {
                    app.login(username, password);
                } catch (P2PBayException e1) {
                    e1.printStackTrace();
                }
                return "logged in User " + request.params(":username");
            }
        });

        get(new Route("/node/item/create/:name/:desc/:user") {
            @Override
            public Object handle(Request request, Response response) {
                String user = request.params(":user");
                String desc = request.params(":desc");
                String name = request.params(":name");

                try {
                    app.addItem(name, desc, user);
                } catch (P2PBayException e1) {
                    e1.printStackTrace();
                }
                return "Created Item " + request.params(":name");
            }
        });

        get(new Route("/node/item/:name") {
            @Override
            public Object handle(Request request, Response response) {
                String name = request.params(":name");

                Item item;
                item = app.getItem(name);

                return "Item Name: " + item.getTitle() + "Description " + item.getDescription();
            }
        });


    }
}