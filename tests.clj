;; Writing rigorous unit tests for concurrency bugs in Clojure, particularly 
;; for a system using STM (Software Transactional Memory) and core.async, 
;; requires us to focus on ensuring that:
;;
;; - Shared state is updated correctly without race conditions.
;; - Multiple tasks interacting with shared state don’t interfere with each other.
;; - Atomicity of updates is guaranteed by STM.

;; In Clojure, concurrency bugs are hard to detect through unit tests alone because 
;; the errors may only appear under certain timing or execution conditions. However, 
;; we can use tests to simulate concurrent task execution and verify that the 
;; shared state (counter in our case) is updated as expected.

(ns task-scheduler.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async :refer [<! >! go-loop timeout]]
            [task-scheduler.core :refer :all]))

;; Test setup
(defn reset-counter []
  (dosync
    (ref-set counter 0)))

(deftest test-concurrent-counter-increments
  ;; Reset the shared counter before starting the test
  (reset-counter)

  ;; Define how many increments we expect from each task
  (let [iterations 10   ;; Number of times each task will run
        expected-total (* 2 iterations)  ;; Two tasks, each running 'iterations' times
        task-a {:name "Task A"
                :interval "10ms"
                :on-complete (fn [] (increment-counter))
                :condition (fn [_] true)}
        task-b {:name "Task B"
                :interval "10ms"
                :on-complete (fn [] (increment-counter))
                :condition (fn [_] true)}]

    ;; Register the tasks
    (hot-load-task task-a)
    (hot-load-task task-b)

    ;; Start task loops in separate threads
    (task-loop "Task A")
    (task-loop "Task B")

    ;; Wait for all increments to happen
    (Thread/sleep (* iterations 10 2))  ;; Give enough time for the tasks to execute

    ;; Check that the counter value matches the expected number of increments
    (is (= @counter expected-total)
        (str "Expected the counter to be incremented " expected-total " times, but found " @counter))))

(deftest test-stm-atomicity
  ;; Reset the shared counter
  (reset-counter)

  ;; Define two concurrent tasks that both increment the counter
  (let [task-a {:name "Task A"
                :interval "10ms"
                :on-complete (fn [] (increment-counter))
                :condition (fn [_] true)}
        task-b {:name "Task B"
                :interval "10ms"
                :on-complete (fn [] (increment-counter))
                :condition (fn [_] true)}]

    ;; Register both tasks
    (hot-load-task task-a)
    (hot-load-task task-b)

    ;; Start both tasks running concurrently
    (task-loop "Task A")
    (task-loop "Task B")

    ;; Stress test for a while to ensure atomicity is maintained
    (Thread/sleep 100)

    ;; Atomicity test: the counter should always reflect an even number
    ;; since both tasks increment the counter atomically
    (is (even? @counter) "The counter should be even after concurrent updates.")))

(deftest test-hot-swapping
  ;; Reset the counter before starting
  (reset-counter)

  ;; Define an initial task
  (let [task-a {:name "Task A"
                :interval "10ms"
                :on-complete (fn [] (increment-counter))
                :condition (fn [_] true)}]

    ;; Load Task A
    (hot-load-task task-a)

    ;; Start the task
    (task-loop "Task A")

    ;; Allow the task to increment the counter a few times
    (Thread/sleep 50)

    ;; Now, swap out Task A with a new definition that increments twice
    (hot-load-task {:name "Task A"
                    :interval "10ms"
                    :on-complete (fn [] (dosync (alter counter + 2)))
                    :condition (fn [_] true)})

    ;; Wait for the hot-swapped task to run
    (Thread/sleep 50)

    ;; Ensure the counter reflects the combination of both task versions
    ;; It should have some increments of 1 from the original task and increments of 2 after swapping
    (is (>= @counter 10) "The counter should be incremented by at least 10 (combination of old and new task).")))
