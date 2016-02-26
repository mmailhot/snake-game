(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [goog.events :as events]))

(def initial-board [35 25])

(def initial-snake {:direction [1 0]
                    :body [[3 2] [2 2] [1 2] [0 2]]})

(defn rand-free-position
  "Takes a snake and the board-size as arguments,
  and returns a random position not colliding with the snake body"
  [snake [x y]]
  (let [snake-positions-set (into #{} (:body snake))
        board-positions (for [x-pos (range x)
                              y-pos (range y)]
                          [x-pos y-pos])]
    (when-let [free-positions (seq (remove snake-positions-set board-positions))]
      (rand-nth free-positions))))

(def init-state {:board initial-board
                 :snake initial-snake
                 :point (rand-free-position initial-snake initial-board)
                 :points 0
                 :game-running? nil})

(register-handler
 :initialize
 (fn
   [db _]
   (merge db init-state)))

(register-sub
 :board
 (fn
   [db _]
   (reaction (:board @db))))

(register-sub
 :snake
 (fn
   [db _]
   (reaction (:body (:snake @db)))))

(register-sub
 :point
 (fn
   [db _]
   (reaction (:point @db))))

(defn render-board
  "Renders the game board area"
  []
  (let [board (subscribe [:board])
        snake (subscribe [:snake])
        point (subscribe [:point])]
    (fn  []
      (let [[width height] @board
            snake-positions (into #{} @snake)
            current-point @point
            cells (for [y (range height)]
                    (into [:tr]
                          (for [x (range width)
                                :let [current-pos [x y]]]
                            (cond
                              (snake-positions current-pos) [:td.snake-on-cell]
                              (= current-pos current-point) [:td.point]
                              :else [:td.cel]))))]
        (into [:table.stage {:style {:height 377
                                     :width 527}}]
              cells)))))

(defn game
  "The main rendering function"
  []
  [:div
   [render-board]])

(defn run []
  (dispatch-sync [:initialize])
  (reagent/render [game]
                  (js/document.getElementById "app")))

(run)
