# Simple broadcast service driven by RSockets
## Description
Repo contains simple service which gets message from a client and send to other clients (broadcasting).
 WebSockets are using as transport.
## Structure
Repo contains 3 modules
### Server
`rsocket-server` is the module with server. It  runs on `7000` port and is ready for connection by `/websocket`
For broadcasting message `broadcast` is using. 
### Client
`rsocket-client` is the module with client.
<p>

`RSocketClient` is the shell component. Once it starts `shell>` welcoming phrase will appear.
Client has only one shell method, in order to invoke it use `broadcast` command with message for broadcasting in quites, e.g. `broadcast "Hello!"`
<p>There is `ClientHandler.java` for handling messages from the server. It counts byte in array, decodes message and sends it back. 

## How it is work
Once broadcast method of client is invoked with content. Then content converts to `byte[]` and sends to server with client id.
Server got the message, goes through client list and re-send content of message to all clients except invoker.
These clients get messages, counts amount of bytes, decodes and send that info back as usual String.
Finally, server logs all responses from clients.

## How to run
#### From IDE
Just run server using IDE, and a few instances of clients.
In any client type `broadcast "Hello!"` and in logs of server will be answer from clients with decoded message.

#### Using gradle
From root folder run `./gradlew bootJar`
<p>
once will be completed run server using command from root of project
<p>

`java -jar rsocket-server/build/libs/rsocket-server-1.0.0.jar`
<p>
then run a few instances of client using command from root of project
<p>

`java -jar rsocket-client/build/libs/rsocket-client-1.0.0.jar`
<p>
once clint will be run there will be welcoming prompt `shell>` then just type `broadcast "Hello!"` and in logs of server will be answer from clients with decoded message.