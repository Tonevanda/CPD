# Parallel and Distributed Computing

### Distributed Systems Assignment

##### How to run the code

The code is divided in 2 parts: The Server and the Client. To first run our program, it is necessary to run the server. To do this,
simply compile the Server.java that is located in the src directory. We can compile it using the following command in the cmd terminal inside src directory:

```
javac -cp "..\dependencies\json-20240303.jar";. "Server.java"
```

And run it using:

```
java -cp "..\dependencies\json-20240303.jar";. "Server"
```

Afterwards, we will just let the Server remain in the background running. Now's the time to 
start connecting Clients to the Server. To connect various Clients we found that the best way was to open a different terminal window for each one.
We then compile the Client class using javac like we did with the Server, and then run it, again using the same commands as in the server.

```
javac Client.java
```

And run it using:

```
java Client
```

We can now interact with the server using the different terminal windows as a different Client. And we can send messages by typing in the terminal and clicking enter to send it.
The other steps to run our program are given by the server to the client. 

##### Server

The server is creating an SSL socket to communicate with the clients. In the beginning we manage the SSL socket by setting up
the key encryptions and the certificates. Then we load all the players that are in the database to local memory (hashmap where the key is the player's name and it returns a class Object).

The database that we are using is a JSON file, therefore whenever we interact with this database we must first lock it, since we have to read from the file or when we update it we need to write it all back to the file, and if we
don't lock it, this might lead to synchronization problems.

Therefore, we create an array of locks on which the first entry will be the JSON database locks, which we will use whenever a client is logging in
and when we update the rank of the player. 

To update the rank of the player, we decided to do so periodically. We create an extra thread, when the server starts and in that thread we update the json database of all current user's, but only if their
ranks have changed since the previous update or if new users have registered into the server. In this thread we also manage the ranked queue. To make use of these features
we also created a new timer for the server and a MyTimerTask class which extends TimerTask and we scheduled the timer with our MyTimerTask, and in the run function of the MyTimerTask we know it will be called periodically,
so we simply increase a timer variable to keep track of the time and whenever we want to see if a second has passed we can just compare that time variable variance and see if it is bigger or equal than 1.

To summarize, in the beginning the server does the following:

- Load players from JSON file to local memory;
- Create a Server SSL Socket with key and certificates setup;
- Create a new Timer and timer task that will run periodically;
- Create a new thread where we can manage the updating of the JSON file and ranked queue management.

After this server setup we go into a while(true), where we wait for clients to connect and we extract their Socket, PrintWritter, BufferedReader, and 
we pass them to a server state machine. Here we seperate the process in 4 different sections:

- Authentication;
- Menu;
- Queue;
- Game.

During Authentication the player simply logs in writing his name and password. If the name and password are correct he will go to the Menu, 
or in case of relogging in after a disconnection, he will go to the state he was previously before disconnecting. If the name does not exist in the current users, then a new user will be registered with rank = 0.
Also during the authentication, the user's password is hashed and all the messages between server client and client server have extra chars in the beginning of the message, so that we can coordinate
the states and the flow of the program. We simply append one char to the beggining of the message and the receiver extracts the first char and uses it as a form of instruction on which state or action to perform next.
If there is no instruction then the default char being appended will be a no encoding char which is represented by the char "N". In this case the user just doesn't perform any new action or changes state, but instead he
remains performing the same action or in the same state.

During the Menu state the server will ask the client if he wishes to either quit the connection, or join the simple game mode queue or join the ranked game mode queue.
The user responds with one of these 3 options and the state will be changed to either Queue or Quit in case of the client wanting to quit the connection.
Before we go to the Queue state we must set the socket timeout to a relatively low number like 10 milliseconds.
This will make the reading of the BufferedReader only block for that amount of time and if the time has passed it will throw a SocketTimeoutException which we can catch gracefully so we can know if player has disconnected or not.
This non-blocking reading mode is done during the queue and game states, and the procedure is as was described earlier. Once we catch this exception, we can then 
start a timeout and the player has that amount of time to rejoin back into the game or else it will just start from the beginning again.
Note that the player must still need to authenticate himself in order to be able to reconnect.

During the Queue state the server will periodically send a message to the client every 1 second to inform him of the time he has spent on the queue.
There are two queues for the two game modes: Simple and Ranked. In the Simple after a player enters the queue, it will lock the simple queue, and it will attempt to remove 
the required number of players needed for the game, but only if they aren't disconnected. Then they will start a game with these players and to start each game we create another thread to manage the game and then the player thread goes on to the game state.
In ranked mode each time a player joins the ranked queue, they won't check if there is a game available to be played, because this is done by the extra thread that was created to manage the ranked queue and the JSON file updating.
In this thread we can periodically check the ranked queue, locking it, and using a sorting algorithm we can pick certain type of players to choose for games. This evaluation is made based on the rank of the players +- the time they have been waiting on the queue.
The more the player waits on the queue the more the range of players increases, and only if all the players that are starting the game are within the same ranges and none of them are disconnected will the game be started.

During the Game state we just want to keep the connection open in case the player after the game wants to play again or go to another queue,... We also check if the player disconnects during the game and throw a SocketException that will trigger a timeout countdown, so he can re-login.
After the player ends the game they will be brought back to the menu state.

##### Client

In the Client side of things we must initialize the SSL socket using the keys and the certificates and we must also connect with the server. 
After that the Client goes into a state machine, similar to the one in the Server, having the same states, except on the Client side the actions will be more about responding to the servers messages and acting upon them.

On the Authentication State the user reads the username and password inputs, clearing the terminal screen during the password input so that it is semi hidden.
We use a Scanner to read the inputs of the user, and we send the result to the server. Depending on the server answer we remain on the same state or go to the Menu State.
Note that if authentication is successfully the server will send a char determining which state the Client should head onto next, since the Client doesn't always want to go to the Menu state after authentication, since he could be re-logging in after a disconnection and want to go directly into the queue or game state instead.

On the Menu State the user simply writes the game mode that he wishes to play or indicates he wants to quit in which case the Client will exit the State handler and the server will close the Client's socket.

In the Queue State the user simply reads the messages and eventually receives an additional message indicating that the game has started, therefore he will head on to the Game State and set the socket timeout to 10 milliseconds so that it enters non-blocking mode, since 
the game requires the clients not to block, because they can play their moves whenever they want.

In the Game State the client simply reads the servers responses and writes to server the action they want to make, and eventually when the game ends the Client will receive a char that will make him go back to the Menu State, resting the socket timeout to 0, so that it is in blocking mode.

##### Register and Login

Here are some login examples:

| Username | Password | Rank |
|----------|----------|------|
| user1    | pass1    | 0    |
| user2    | pass2    | 0    |
| user3    | pass3    | 0    |

To register simply input a new username and password and the server will register the new user with rank 0.

##### Game

The game is a roguelike card game, where each player must alternate between improving the cards in their hands and fighting each other.
The game has 2 phases: Store and Fighting.

During the Store phase the players can swap positions, buy, sell cards. To know which actions you can do at a given time, you can simply look at all the inputs described inside the "()".

For example: If a card has a (1) that means to interact with that card you must input the number 1 and click enter.

If you click on a store card you will either buy them if they are an item, consume them if they are a consumable or visit them if they are merchants or monsters.

It's like a shop or a market, where each day (which is shown on the top left), shows you 3 events where you can choose, and after you choose the events you will be able to either buy cards, fight monsters, train skills,...
This phase of the game will end when you end your turn, and when the days remaining are 0 there will be nobody in the market since everyone is on vacation.

Each card has a cost (0$ if it doesn't show a cost) and they have powers. They can also have additional stats like cooldowns.
You also have stats, like health, max Health, Armor, Strength, Speed,... These stats can be improved during your game experience.

After everyone ends their turns, the game will transition into a Fighting Phase, where each player will fight another player. If the player number is odd then one player will be left out doing nothing.

During the fight the player's have a Timer that starts out at 30 seconds but on each fight it increases the time. The player's will fight until either one of them has their Health below 0 or the timer runs out.
During this time the cards that each player has will activate powers which range from dealing damage, gaining Strength, Healing life,...

When a player's health goes below 0 then that player loses the game and gets removed from the game and updates it's ranking based on it's position in the game. The last player remaining wins the game.

The stats of the game are the following:

- Gold: Represents how much money you have to buy cards;
- Health: Represents your current life points;
- MaxHealth: Represents your total life points (max life points);
- Armor: Extra Health that blocks the entirety of an attack once it's removed;
- Strength: Increases your card's damage by this amount;
- Speed: Speeds up the cooldowns of your card's by this amount (in percentage);
- Poison: Take this amount of damage every second;


