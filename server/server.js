var express = require("express");
var myParser = require("body-parser");
var app = express();

app.use(myParser.urlencoded({extended: true}));
app.post("/auth-done", function (request, response) {
    handleApiRequest(request, response);
});

var handleApiRequest = function (request, response) {

    console.log("Serving API");

    var responseHeaders = {
        'Content-Type': 'application/json',
    };

    var origin = request.headers["origin"];

    if (request.method === 'OPTIONS') {
        console.log("Accepting probable preflight request");

        if (origin) {
            responseHeaders['Access-Control-Allow-Origin'] = origin;
            responseHeaders['Access-Control-Allow-Methods'] = "GET, HEAD, OPTIONS, POST";
            responseHeaders['Access-Control-Allow-Headers'] = 'Authorization, WWW-Authenticate, Content-Type';
            responseHeaders["Access-Control-Allow-Credentials"] = "true";
        }

        responseHeaders["Allow"] = "GET, HEAD, OPTIONS, POST";

        response.writeHead(200, responseHeaders);
        response.end();
        return;
    }
    else if (origin) {
        responseHeaders["Access-Control-Allow-Origin"] = origin;
        responseHeaders["Access-Control-Allow-Credentials"] = "true";
    }

    response.writeHead(200, responseHeaders);

    var data = authDoneHandler(request);
    response.end(JSON.stringify(data), 'utf-8');

    if (!response.finished) {
        console.log("handleApiRequest: Ending response");
    }
};

var authDoneHandler = function (request) {

    console.log("received request at auth done handler");

    var data = {};

    var authToken = request.body.token;

    if (!authToken) {
        data["sub"] = "fail";
        data["acr"] = "fail";
        data["reason"] = "no auth token in post";
        return data;
    }

    // TODO add signature check
    var parts = authToken.split(".");
    if (parts.length != 3) {
        data["sub"] = "fail";
        data["acr"] = "fail";
        data["reason"] = "auth toke not a jwt";
        return data;
    }

    var body = JSON.parse(Buffer.from(parts[1], 'base64').toString())
    data = Object.assign({}, data, body);

    return data;
}

//Start the server and make it listen for connections on port 8080

app.listen(8100);
console.log('Server running at http://127.0.0.1:8100');