# RCombinator
Clojure Concurrent Task Scheduler with DSL for Rule Definition

Clojure's software transactional memory (STM) system and immutable data structures make it highly suited to building concurrent applications without the typical complexities of locking and synchronization. Its agents, refs, and atoms offer tools for managing state in multithreaded environments, making it ideal for concurrent data processing tasks, like building scalable web services or real-time data processing systems.

## DSL Example
```clojure
(defn start []
  ;; Define and hot-load Task A that fetches data from an API every 5 minutes
  (hot-load-task
   {:name "API Fetch Task"
    :interval "5m"
    :on-complete (fn []
                   (let [response (http/get "https://api.example.com/data")]
                     (println "Fetched data:" (:body response))))
    :condition (fn [_] true)})

  ;; Define and hot-load Task B that processes some data every minute
  (hot-load-task
   {:name "Data Processing Task"
    :interval "1m"
    :on-complete (fn []
                   (let [data [1 2 3 4 5 6 7 8 9 10]
                         result (reduce + data)]
                     (println "Processed data, sum is:" result)))
    :condition (fn [_] true)})

  ;; Start both tasks in separate loops
  (task-loop "API Fetch Task")
  (task-loop "Data Processing Task"))

```
