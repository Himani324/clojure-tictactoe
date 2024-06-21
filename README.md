# clojure-tictactoe
This is a simple implementation of the classic Tic-Tac-Toe game in Clojure, using MongoDB. Players use 'O's and 'X's to mark their moves on the board.

## Features

- Play Tic-Tac-Toe in the command line using O's and X's
- Game state is persisted in MongoDB
- Supports multiple games
- Detects wins and draws

## Prerequisites

Before you begin, ensure you have met the following requirements:

- You have installed Clojure (1.10.3 or later)
- You have installed Leiningen (2.9.0 or later)
- You have a MongoDB instance running (locally or remotely)

## Installing Tic-Tac-Toe with MongoDB

To install Tic-Tac-Toe with MongoDB, follow these steps:

1. Clone the repository: git clone https://github.com/Himani324/clojure-tictactoe
2. Navigate to the project directory: cd clojure-tictactoe
3. Configuring MongoDB

By default, the game tries to connect to a MongoDB instance running on localhost:27017. If your MongoDB is running on a different host or port, you'll need to modify the connection details in `src/tic_tac_toe/core.clj`:

```clojure
(def conn (mg/connect {:host "your-host" :port your-port}))

If you're using MongoDB Atlas or another cloud service, use the provided connection string:
(def conn (mg/connect-via-uri "your-mongodb-uri"))

4. Running Tic-Tac-Toe with MongoDB : clj -M -m tictactoe.core

5. The game board is displayed with O's, X's, and empty spaces. For example:
X |   | O
---------
  | X |  
---------
O |   | X
