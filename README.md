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

### Concurrent Tasks Updating Shared Data 
```clojure
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
```
## What is an R-Combinator Anyway?
In mathematics, an **R-comb** refers to a specific concept in topology, particularly in the study of certain pathological spaces. The **R-comb** (or sometimes called the **rational comb**) is a well-known example of a topological space that illustrates peculiar or counterintuitive properties in point-set topology.

### Construction of the R-comb:
- It consists of the union of two sets in the Euclidean plane:
  1. The vertical line segment from \( (0, 0) \) to \( (0, 1) \).
  2. For each rational number \( r \in [0, 1] \), the vertical line segment from \( (r, 0) \) to \( (r, 1) \).

Thus, you have the vertical line at \( x = 0 \) and a series of "teeth" corresponding to the vertical lines at rational points \( r \in [0, 1] \). The set is dense in the interval \( [0, 1] \) on the x-axis because the rational numbers are dense in \( [0, 1] \).

### Topological Properties:
- The R-comb is often used as an example to illustrate strange behaviors in limit points, closure, and connectedness.
- For instance, the space is not connected because it consists of disjoint vertical segments at rational points and isolated points in between.
- It also demonstrates interesting behavior with respect to convergence, compactness, and closure of sets.

In summary, the R-comb is an important example in topology, used to demonstrate pathological or non-intuitive phenomena in the behavior of point sets.

The **R-comb**, also known as the **rational comb** or sometimes the **rational brush**, is a concept that arose in the field of **point-set topology** (also known as general topology). While the precise historical origin of the R-comb is not widely documented with an attributed mathematician or a specific date, its usage can be understood as part of a broader development in the exploration of pathological examples in topology. 

Here’s a breakdown of the history and context of the **R-comb** within mathematics:

### 1. **Emergence of General Topology (Late 19th to Early 20th Century)**
- The R-comb arose from the work in **general topology**, which investigates properties of spaces that are invariant under continuous transformations. General topology (also known as **point-set topology**) became formalized in the early 20th century with foundational contributions from mathematicians such as **Georg Cantor**, **Felix Hausdorff**, and **Maurice Fréchet**.
- Early topologists began to study and categorize various types of spaces and behaviors of sets, including strange and counterintuitive examples that revealed the complexity of limit points, connectedness, compactness, and closure.

### 2. **Pathological Examples in Topology (Early 20th Century)**
- As topology developed, topologists began constructing **pathological examples** of topological spaces to explore the limits of definitions and theorems. These examples were often used to test the boundaries of concepts like connectedness, compactness, and continuity.
- The **R-comb** is one such pathological space, designed to highlight certain strange properties. For instance, it’s used to show how spaces with dense subsets (such as the rationals being dense in the real numbers) can behave counterintuitively with respect to closure and connectedness.

### 3. **R-comb in Topology Textbooks**
- By the mid-20th century, the R-comb became a standard example in topology courses and textbooks to illustrate how **limit points** and **dense sets** behave. The comb demonstrates that while the set of rational points in a space is dense, the behavior of limits and connectedness in such spaces can lead to non-obvious results.
- The comb’s construction, with a dense set of “teeth” at rational points, became a typical example to show the importance of irrational points in the closure and structure of sets.

### 4. **Uses in Education and Research**
- In educational settings, the R-comb is often introduced to students learning about the subtleties of topological concepts like **connectedness**, **compactness**, and **closures**. The R-comb is frequently cited in examples involving:
  - **Connectedness**: The comb is not connected, despite having dense vertical segments.
  - **Closure**: The closure of the set contains all irrational points in the interval \([0, 1]\) on the x-axis, even though the initial set only contains rational segments.
- In research, the R-comb is one of many examples that show the need for rigor when dealing with concepts of limits and dense sets in topology.

### Importance in Topology
- The R-comb is not unique in topology but represents one of a family of examples that highlight how seemingly simple constructions can lead to complex or unexpected topological behavior. 
- It fits within a tradition of topological examples, such as the **Koch snowflake**, the **Cantor set**, and other fractal-like or dense objects that demonstrate deeper properties about convergence, space, and structure.

### Conclusion
The R-comb arose as part of a broader effort by mathematicians in the 20th century to explore and formalize topological properties, especially by constructing spaces with peculiar or counterintuitive behavior. While the exact origin of the R-comb as a specific example is not clearly attributed to any one mathematician, its role in topology is well-established as a teaching tool and a case study in dense sets, connectedness, and closure properties. It continues to be an important example in point-set topology, often appearing in textbooks and educational settings.
