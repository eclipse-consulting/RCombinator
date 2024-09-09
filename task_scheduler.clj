(ns task-scheduler.core
  (:require [clojure.core.async :as async :refer [<! >! go-loop]]
            [clojure.edn :as edn]))

;; ---- Task Registry ----

(defonce task-registry (atom {}))

(defn register-task [task]
  (swap! task-registry assoc (:name task) task)
  (println (str "Registered task: " (:name task))))

(defn deregister-task [task-name]
  (swap! task-registry dissoc task-name)
  (println (str "Deregistered task: " task-name)))

(defn get-task [task-name]
  (@task-registry task-name))

(defn update-task [task-name new-task]
  (swap! task-registry assoc task-name new-task)
  (println (str "Updated task: " task-name)))

;; ---- DSL Parser and Hot Loading ----

(defn schedule-task [task-dsl]
  (let [task-name (:name task-dsl)]
    (if (get-task task-name)
      (update-task task-name task-dsl)
      (register-task task-dsl))
    (println (str "Task " task-name " scheduled."))))

(defn hot-load-task [task-dsl]
  (schedule-task task-dsl))

;; ---- Task Execution ----

(defn interval->ms [interval-str]
  (let [[_ num unit] (re-matches #"(\d+)([smh])" interval-str)
        multiplier (case unit
                     "s" 1000
                     "m" (* 60 1000)
                     "h" (* 60 60 1000))]
    (* (Integer/parseInt num) multiplier)))

(defn task-loop [task-name]
  (go-loop []
    (let [task (get-task task-name)]
      (if task
        (do
          (println (str "Running task: " task-name))
          (when (:condition task)
            (if ((:condition task) @task-registry)
              ((:on-complete task))))
          ;; Wait based on task interval before repeating
          (<! (async/timeout (interval->ms (:interval task)))))
        (println "Task not found, stopping loop.")))
    (recur)))

;; ---- Example Usage ----

(defn start []
  ;; Define Task A with an interval of 5 minutes
  (hot-load-task
   {:name "Task A"
    :interval "5m"
    :on-complete (fn [] (println "Task A complete!"))
    :condition (fn [_] true)  ;; Always run
    })

  ;; Define Task B which runs every minute
  (hot-load-task
   {:name "Task B"
    :interval "1m"
    :on-complete (fn [] (println "Task B complete!"))
    :condition (fn [_] true)  ;; Always run
    })

  ;; Start the tasks in separate loops
  (task-loop "Task A")
  (task-loop "Task B"))

;; Start the system
(start)
