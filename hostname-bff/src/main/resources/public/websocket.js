// small helper function for selecting element by id
let id = id => document.getElementById(id);

//Establish the WebSocket connection and set up event handlers
let ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/bff/counter");
ws.onmessage = msg => updateMessage(msg);
ws.onclose = () => alert("WebSocket connection closed");

function updateMessage(msg) {
    let data = JSON.parse(msg.data);
    id("message").innerHTML = data.count;
}
