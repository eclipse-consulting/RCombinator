# RCombinator

* Run multiple tasks concurrently using Clojure's concurrency primitives (e.g., agents, atoms, core.async)! 
* Define task scheduling rules via a simple DSL embedded in Clojure! 
* Process and execute tasks based on these user-defined rules, and ensure tasks run in parallel!

*Note*: Not for production use.

## Benefits of RCombinator
Clojure's software transactional memory (STM) system and immutable data structures make it highly suited to building concurrent applications without the typical complexities of locking and synchronization. Its agents, refs, and atoms offer tools for managing state in multithreaded environments, making it ideal for concurrent data processing tasks, like building scalable web services or real-time data processing systems.

Clojure's Lisp heritage makes it an excellent choice for building DSLs (Domain-Specific Languages). The language’s homoiconicity (the program is represented in the same structure as the data) makes it easy to write code that manipulates other code. This is useful in creating languages for rule engines, query builders, or configuration management tools.

Clojure's core.async library provides Go-like channels for managing asynchronous communication, which makes it highly suited for writing distributed systems that require message-passing or asynchronous tasks. Clojure's simplicity and high-level abstractions allow for efficient modeling of complex distributed workflows.

Thanks to macros, Clojure allows for powerful metaprogramming, making it possible to generate code at compile time. This is useful for tasks like template generation, test case generation, or simplifying repetitive code patterns in large systems.

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

### Performing a Network Request Every 10 Minutes

```clojure
(require '[clj-http.client :as http])

(hot-load-task
 {:name "API Fetch Task"
  :interval "10m"
  :on-complete (fn []
                 (let [response (http/get "https://api.example.com/data")]
                   (println "Fetched data:" (:body response))))
  :condition (fn [_] true)})
```

### Sending an Email Every Hour
```clojure
(require '[postal.core :as postal])

(hot-load-task
 {:name "Email Task"
  :interval "1h"
  :on-complete (fn []
                 (postal/send-message {:from "you@example.com"
                                       :to "recipient@example.com"
                                       :subject "Task Complete"
                                       :body "Your scheduled task has completed successfully."})
                 (println "Email sent"))
  :condition (fn [_] true)})
```

### Deleting Temp Files Every 30 Minutes

```clojure
(require '[clojure.java.io :as io])

(hot-load-task
 {:name "Cleanup Task"
  :interval "30m"
  :on-complete (fn []
                 (doseq [file (filter #(.startsWith (.getName %) "temp")
                                      (file-seq (io/file "/tmp")))]
                   (io/delete-file file))
                 (println "Temporary files cleaned up"))
  :condition (fn [_] true)})
```



