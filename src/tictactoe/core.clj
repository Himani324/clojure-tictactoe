;;(ns tictactoe.core
;;  (:require [clojure.string :as str]))
;;
;;(def initial-board (vec (repeat 9 nil)))
;;
;;(defn print-board [board]
;;  (println "\n-------------")
;;  (doseq [row (partition 3 board)]
;;    (print "| ")
;;    (doseq [cell row]
;;      (print (str (or cell " ") " | ")))
;;    (println "\n-------------")))
;;
;;(defn available-moves [board]
;;  (keep-indexed #(when (nil? %2) %1) board))
;;
;;(defn winner? [board]
;;  (let [winning-combinations [[0 1 2] [3 4 5] [6 7 8]  ; rows
;;                              [0 3 6] [1 4 7] [2 5 8]  ; columns
;;                              [0 4 8] [2 4 6]]]        ; diagonals
;;    (some (fn [combo]
;;            (let [line (map #(get board %) combo)]
;;              (when (and (apply = line)
;;                         (not (nil? (first line))))
;;                (first line))))
;;          winning-combinations)))
;;
;;(defn full-board? [board]
;;  (every? some? board))
;;
;;(defn game-over? [board]
;;  (or (winner? board) (full-board? board)))
;;
;;(defn make-move [board move player]
;;  (assoc board move player))
;;
;;(defn get-move [board player]
;;  (loop []
;;    (println (str player "'s turn. Enter a number (0-8):"))
;;    (let [input (read-line)
;;          move (try (Integer/parseInt input)
;;                    (catch NumberFormatException _ nil))]
;;      (if (and move (<= 0 move 8) (nil? (get board move)))
;;        move
;;        (do (println "Invalid move. Try again.")
;;            (recur))))))
;;
;;(defn play-game []
;;  (loop [board initial-board
;;         player "X"]
;;    (print-board board)
;;    (if (game-over? board)
;;      (let [winner (winner? board)]
;;        (if winner
;;          (println (str winner " wins!"))
;;          (println "It's a draw!")))
;;      (let [move (get-move board player)
;;            new-board (make-move board move player)]
;;        (recur new-board (if (= player "X") "O" "X"))))))
;;
;;(defn -main [& args]
;;  (println "Welcome to Tic-Tac-Toe!")
;;  (play-game))



(ns tictactoe.core
  (:require [clojure.string :as str]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:import [org.bson.types ObjectId]))

;; MongoDB setup
(def db-name "tictactoe")
(def coll-name "games")

(defn connect-to-db []
  (:db (mg/connect-via-uri (str "mongodb+srv://hima5:Open1997!@cluster0.wfbzmtq.mongodb.net/tictoegame?retryWrites=true&w=majority" db-name))))


;; Game logic
(def initial-board (vec (repeat 9 nil)))

(defn print-board [board]
  (println "\n-------------")
  (doseq [row (partition 3 board)]
    (print "| ")
    (doseq [cell row]
      (print (str (or cell " ") " | ")))
    (println "\n-------------")))

(defn winner? [board]
  (let [winning-combinations [[0 1 2] [3 4 5] [6 7 8]  ; rows
                              [0 3 6] [1 4 7] [2 5 8]  ; columns
                              [0 4 8] [2 4 6]]]        ; diagonals
    (some (fn [combo]
            (let [line (map #(get board %) combo)]
              (when (and (apply = line)
                         (not (nil? (first line))))
                (first line))))
          winning-combinations)))

(defn full-board? [board]
  (every? some? board))

(defn game-over? [board]
  (or (winner? board) (full-board? board)))

(defn make-move [board move player]
  (assoc board move player))

(defn get-move [board player]
  (loop []
    (println (str player "'s turn. Enter a number (0-8):"))
    (let [input (read-line)
          move (try (Integer/parseInt input)
                    (catch NumberFormatException _ nil))]
      (if (and move (<= 0 move 8) (nil? (get board move)))
        move
        (do (println "Invalid move. Try again.")
            (recur))))))

;; MongoDB operations
(defn save-game [db game-id board current-player]
  (mc/update db coll-name
             {:_id game-id}
             {:board board
              :current-player current-player}
             {:upsert true}))

(defn load-game [db game-id]
  (mc/find-one-as-map db coll-name {:_id game-id}))

(defn create-new-game [db]
  (let [game-id (ObjectId.)]
    (save-game db game-id initial-board "X")
    game-id))

;; Game loop
(defn play-game [db game-id]
  (loop [{:keys [board current-player] :as game} (or (load-game db game-id)
                                                     {:board initial-board :current-player "X"})]
    (print-board board)
    (if (game-over? board)
      (let [winner (winner? board)]
        (if winner
          (println (str winner " wins!"))
          (println "It's a draw!")))
      (let [move (get-move board current-player)
            new-board (make-move board move current-player)
            next-player (if (= current-player "X") "O" "X")]
        (save-game db game-id new-board next-player)
        (recur {:board new-board :current-player next-player})))))

(defn start-game []
  (let [db (connect-to-db)]
    (println "Welcome to Tic-Tac-Toe!")
    (println "1. New Game")
    (println "2. Load Game")
    (print "Choose an option: ")
    (flush)
    (let [choice (read-line)]
      (case choice
        "1" (let [game-id (create-new-game db)]
              (println "New game created. Game ID:" (.toString game-id))
              (play-game db game-id))
        "2" (do
              (print "Enter Game ID: ")
              (flush)
              (let [game-id (ObjectId. (read-line))]
                (if-let [game (load-game db game-id)]
                  (do
                    (println "Game loaded.")
                    (play-game db game-id))
                  (println "Game not found."))))
        (println "Invalid choice.")))))

(defn -main [& args]
  (start-game))