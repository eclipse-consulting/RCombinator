;; using STM (software transactional memory) for shared state between concurrent tasks

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

;; ---- STM Shared State ----

;; Define a shared counter that multiple tasks will increment.
(def counter (ref 0))

;; Function: increment-counter
;; Increments the shared counter inside a dosync block (STM transaction).
(defn increment-counter []
  (dosync
    (alter counter inc)
    (println "Counter incremented, current value:" @counter)))

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
          ;; Run the task's on-complete function if the condition is true
          (when (:condition task)
            (if ((:condition task) @task-registry)
              ((:on-complete task))))
          ;; Wait based on the task's interval before executing the task again
          (<! (async/timeout (interval->ms (:interval task)))))
        (println "Task not found, stopping loop.")))
    (recur)))

;; ---- Example Usage ----

(defn start []
  ;; Define Task A that increments the counter every 2 seconds
  (hot-load-task
   {:name "Task A"
    :interval "2s"
    :on-complete (fn [] (increment-counter))
    :condition (fn [_] true)})

  ;; Define Task B that also increments the counter every 3 seconds
  (hot-load-task
   {:name "Task B"
    :interval "3s"
    :on-complete (fn [] (increment-counter))
    :condition (fn [_] true)})

  ;; Start both tasks
  (task-loop "Task A")
  (task-loop "Task B"))

;; Start the system by calling the `start` function.
;; This will schedule the tasks and start the execution loops.
(start)
