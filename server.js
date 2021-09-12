var WebSocketServer = require('websocket').server
var http = require('http')

var server = http.createServer((req, res) => {})

server.listen(3000, ()=>{
    console.log("Listening on port 3000...")
})

wsServer = new WebSocketServer({httpServer:server})

var connections = []

wsServer.on('request', (req) => {
    var connection = req.accept()
    console.log('new connection')
    connections.push(connection)

    connection.on('message', (mes) => {
        connections.forEach(element => {
            if (element != connection)
                element.sendUTF(mes.utf8Data)
        })
    })

    connection.on('close', (resCode, des) => {
        console.log('connection closed')
        connections.splice(connections.indexOf(connection), 1)
        //splice:remove an element from an array
    })

})
