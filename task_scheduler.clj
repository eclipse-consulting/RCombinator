;; using core.async

(ns task-scheduler.core
  (:require [clojure.core.async :as async :refer [<! >! go-loop]]
            [clojure.edn :as edn]))

;; ---- Task Registry ----

;; The `task-registry` is an atom that stores a map of all registered tasks.
;; Each task is identified by its name as the key and its task definition as the value.
(defonce task-registry (atom {}))

;; Function: register-task
;; Registers a new task in the task registry by associating the task name with the task definition.
;; If the task name already exists, it will be overwritten.
;; Arguments:
;; - task: A map that contains the task definition, including its name, interval, on-complete action, and condition.
(defn register-task [task]
  (swap! task-registry assoc (:name task) task)
  (println (str "Registered task: " (:name task))))

;; Function: deregister-task
;; Removes a task from the task registry by its name.
;; Arguments:
;; - task-name: A string representing the name of the task to remove.
(defn deregister-task [task-name]
  (swap! task-registry dissoc task-name)
  (println (str "Deregistered task: " task-name)))

;; Function: get-task
;; Retrieves the task definition from the registry using the task name.
;; Arguments:
;; - task-name: The name of the task to retrieve.
;; Returns:
;; - The task map if the task is found, otherwise nil.
(defn get-task [task-name]
  (@task-registry task-name))

;; Function: update-task
;; Updates an existing task in the registry with a new definition.
;; If the task does not exist, it will be added to the registry.
;; Arguments:
;; - task-name: The name of the task to update.
;; - new-task: The new task map to replace the existing one.
(defn update-task [task-name new-task]
  (swap! task-registry assoc task-name new-task)
  (println (str "Updated task: " task-name)))

;; ---- DSL Parser and Hot Loading ----

;; Function: schedule-task
;; Registers or updates a task in the task registry using the provided task DSL (domain-specific language).
;; Arguments:
;; - task-dsl: A map containing the task definition in a DSL-like format, including the task name, interval, condition, and on-complete action.
(defn schedule-task [task-dsl]
  (let [task-name (:name task-dsl)]
    (if (get-task task-name)
      (update-task task-name task-dsl)
      (register-task task-dsl))
    (println (str "Task " task-name " scheduled."))))

;; Function: hot-load-task
;; A higher-level function that schedules or updates tasks using the DSL at runtime.
;; This is the function that allows "hot swapping" of tasks, where tasks can be modified or loaded dynamically.
;; Arguments:
;; - task-dsl: The DSL map defining the task details.
(defn hot-load-task [task-dsl]
  (schedule-task task-dsl))

;; ---- Task Execution ----

;; Function: interval->ms
;; Converts a human-readable interval string (like "5m" or "1h") to milliseconds.
;; Supported units are:
;; - "s" for seconds
;; - "m" for minutes
;; - "h" for hours
;; Arguments:
;; - interval-str: A string representing the time interval (e.g., "5m" for 5 minutes).
;; Returns:
;; - The interval in milliseconds as an integer.
(defn interval->ms [interval-str]
  (let [[_ num unit] (re-matches #"(\d+)([smh])" interval-str)
        multiplier (case unit
                     "s" 1000
                     "m" (* 60 1000)
                     "h" (* 60 60 1000))]
    (* (Integer/parseInt num) multiplier)))

;; Function: task-loop
;; The main execution loop for a task. This function continuously runs the task based on its interval and checks for updates
;; to the task definition in the task registry. It allows for hot swapping by always retrieving the latest task definition.
;; Arguments:
;; - task-name: The name of the task to run in a loop.
(defn task-loop [task-name]
  (go-loop []
    (let [task (get-task task-name)]
      (if task
        (do
          ;; Print the task's name when it starts running.
          (println (str "Running task: " task-name))
          
          ;; Check the condition, if it's met, run the on-complete action.
          (when (:condition task)
            (if ((:condition task) @task-registry)
              ((:on-complete task))))
          
          ;; Wait based on the task's interval before executing the task again.
          (<! (async/timeout (interval->ms (:interval task)))))
        
        ;; If the task is not found in the registry, stop the loop.
        (println "Task not found, stopping loop.")))
    
    ;; Recur to keep the task running in a loop.
    (recur)))

;; ---- Example Usage ----

;; Function: start
;; This function demonstrates the scheduling of two tasks: Task A and Task B.
;; Both tasks will be registered and started in their respective loops.
;; Task A runs every 5 minutes, while Task B runs every 1 minute.
(defn start []
  ;; Define and hot-load Task A, which runs every 5 minutes and prints a message when complete.
  (hot-load-task
   {:name "Task A"
    :interval "5m"
    :on-complete (fn [] (println "Task A complete!"))
    :condition (fn [_] true)  ;; This condition always evaluates to true, meaning the task will always run.
    })

  ;; Define and hot-load Task B, which runs every 1 minute and prints a message when complete.
  (hot-load-task
   {:name "Task B"
    :interval "1m"
    :on-complete (fn [] (println "Task B complete!"))
    :condition (fn [_] true)  ;; This condition also always evaluates to true.
    })

  ;; Start both tasks in separate loops to run concurrently.
  (task-loop "Task A")
  (task-loop "Task B"))

;; Start the system by calling the `start` function.
;; This will schedule the tasks and start the execution loops.
(start)
