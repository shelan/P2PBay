package org.ist.p2pbay.rest;

import org.ist.P2PBayApp;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.util.Constants;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.MessageDigest;
import java.util.Arrays;

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
                boolean isSuccessful = false;
                try {
                    messageDigest = MessageDigest.getInstance(Constants.ALGORITHM_SHA_256);

                    messageDigest.update(password.getBytes(Constants.CHARSET_NAME_UTF_8));

                    byte[] enteredDigest = messageDigest.digest();

                    isSuccessful = app.registerUser(username, enteredDigest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(isSuccessful) {
                    return "Created user " + request.params(":username");
                } else {
                    return "User creation failed";
                }
            }
        });

        get(new Route("/node/user/login/:username/:password") {
            @Override
            public Object handle(Request request, Response response) {
                String username = request.params(":username");
                String password = request.params(":password");
                boolean isSuccessful = false;
                try {
                    isSuccessful = app.login(username, password);
                } catch (P2PBayException e1) {
                    e1.printStackTrace();
                }
                if(isSuccessful) {
                    return "logged in User " + request.params(":username");
                } else {
                    return "Log in failed";
                }
            }
        });

        get(new Route("/node/item/create/:name/:desc/:user") {
            @Override
            public Object handle(Request request, Response response) {
                String user = request.params(":user");
                String desc = request.params(":desc");
                String name = request.params(":name");
                if(name.contains("-")) {
                    name = name.replace("-"," ");
                }

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

        get(new Route("/node/search/:keyword") {
            @Override
            public Object handle(Request request, Response response) {
                String keyword = request.params(":keyword");
                String[] result = app.simpleSearch(keyword);
                return (Arrays.toString(result));
            }
        });

        get(new Route("/node/ad-search/:operator/:keywords") {
            @Override
            public Object handle(Request request, Response response) {
                char operator = request.params(":operator").charAt(0);
                String keywordString = request.params(":keywords");
                String[] keywords = keywordString.split(",");
                String[] result = app.advanceSearch(keywords, operator);
                return (Arrays.toString(result));
            }
        });

        get(new Route("/node/item-count") {
            @Override
            public Object handle(Request request, Response response) {
                return app.getItemCount();
            }
        });

    }
}